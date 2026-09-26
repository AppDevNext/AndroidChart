# Kotlin Multiplatform (KMP) Plan for AndroidChart

This document tracks the plan to add a Kotlin Multiplatform library and demo app to
AndroidChart, alongside the existing `chartLib` (View-based) and `chartLibCompose`
(Android-only Compose wrapper) modules.

## Why this is not a small config change

`chartLib` and `chartLibCompose` are deeply coupled to Android APIs:

- `chartLib` uses `android.graphics.Canvas/Paint/Bitmap/Matrix/RectF/Typeface/DashPathEffect/
  Drawable`, `android.view.View/MotionEvent/ViewGroup`, `android.content.Context`,
  `android.os.Parcelable/Handler` throughout renderers, data classes, and components.
- `chartLibCompose` does **not** draw with Compose `Canvas`/`DrawScope`. Every
  `*Composable.kt` wraps the existing View-based chart via `AndroidView` interop
  (`androidx.compose.ui.viewinterop.AndroidView`). It is Android-only end to end.

A scan of `data/`, `components/`, `utils/`, `interfaces/` shows Android types baked
directly into field/property signatures (not just implementation details), e.g.:

| Android type              | Where used                                   | Occurrences |
|----------------------------|-----------------------------------------------|-------------|
| `android.graphics.drawable.Drawable` | `BaseEntry.icon`, `MarkerImage`         | 17 |
| `android.annotation.SuppressLint`    | misc                                    | 14 |
| `android.graphics.DashPathEffect`    | `IDataSet`, axis/line dash styling       | 6 |
| `android.graphics.Color`             | `ColorTemplate`, components               | 6 |
| `android.os.Build`                   | version checks                            | 4 |
| `android.graphics.Typeface`           | `ComponentBase`, `IDataSet`               | 3 |
| `android.os.Parcelable`/`Parcel`      | `EntryFloat`, `PointF`, etc.               | 4 |
| `android.graphics.Paint`              | `AxisBase`, renderers                     | 2 |
| `android.content.Context`             | `AssetManagerUtils`, `ContextUtils`       | 2 |

Because of this, there is no small "first slice" that avoids introducing common
(platform-agnostic) replacement abstractions — nearly every core class in `data/`,
`components/`, and `utils/` needs at least one Android type removed from its public
API before it can move into a `commonMain` source set.

## Two possible strategies (recap)

**A. Compose Multiplatform rewrite (recommended)** — rewrite `chartLibCompose` (or a
new sibling module) to draw with `androidx.compose.foundation.Canvas` / `DrawScope`
instead of wrapping `AndroidView`, and target Kotlin Multiplatform + JetBrains Compose
Multiplatform (Android, iOS, Desktop, Wasm/JS). This reuses Compose's own drawing,
gesture, and text APIs instead of reinventing Canvas/Paint/Matrix abstractions.

**B. Full KMP for `chartLib` itself** — keep the View-based renderer but introduce
`expect`/`actual` for `Canvas`, `Paint`, `Bitmap`, `Context`, gesture input, etc. Far
larger in scope (touches ~180 files, all 22+ renderers, all touch listeners, save/export
utils) and duplicates what Compose Multiplatform already provides. Not recommended
unless non-Compose consumers on other platforms are a hard requirement.

This plan follows **Strategy A**, built up incrementally by first extracting a
platform-agnostic core data/logic module (`chartLibCore`) that both the existing
Android `chartLib` and a future Compose Multiplatform renderer can share.

## Module layout (target end state)

```
chartLib            Android View-based chart library (unchanged, Android-only)
chartLibCore         NEW: Kotlin Multiplatform pure-Kotlin data model & logic
                      (commonMain + androidMain + iosMain + desktop jvm)
chartLibCompose      Android-only Compose wrapper around chartLib (kept as-is for
                      AndroidView-based consumers, or deprecated once
                      chartLibComposeMultiplatform matures)
chartLibComposeMultiplatform  NEW: Compose Multiplatform renderer built on
                      chartLibCore + Compose Canvas/DrawScope
                      (commonMain + androidMain + iosMain + desktop + wasmJs)
app                  Existing Android demo app (unchanged)
demoKmp              NEW: Compose Multiplatform demo app consuming
                      chartLibComposeMultiplatform on Android/iOS/Desktop/Wasm
lint                 Existing custom lint rules (unchanged)
```

## Status

- [x] **Step 0 — Feasibility scan.** Confirmed Xcode toolchain is available locally;
      AGP 9.2.1 requires the new `com.android.kotlin.multiplatform.library` plugin
      (not `com.android.library`) for KMP modules with an Android target.
- [x] **Step A.1 — Module scaffold + first portable-class extraction.**
      Created `chartLibCore` (KMP: `androidTarget`, `jvm("desktop")`, `iosX64`,
      `iosArm64`, `iosSimulatorArm64`). Moved the 5 classes with zero true Android
      coupling into `commonMain`, preserving package names so no call sites changed:
      - `utils/ObjectPool.kt` (removed JVM-only `@Synchronized`, see note below)
      - `utils/FSize.kt`
      - `utils/PointD.kt`
      - `utils/ColorTemplate.kt` (replaced `android.graphics.Color.rgb(...)` with a
        common `argb()` helper)
      - `highlight/Range.kt`
      - `highlight/RangeDouble.kt`
      - Moved `ObjectPoolTest` into `commonTest`, ported JUnit → `kotlin.test`.
      - `chartLib` now depends on `chartLibCore` via `api(project(":chartLibCore"))`.
      - Verified: `chartLibCore` builds/tests green on Android, desktop-JVM, and all
        3 iOS targets; `chartLib:test`, `chartLibCompose:assembleDebug`, and the full
        `./gradlew test` all still pass unchanged.
      - **Known trade-off:** `ObjectPool` lost `@Synchronized` (JVM-only annotation,
        unavailable in `commonMain`). Revisit with `kotlinx-atomicfu` or a
        platform-specific `expect`/`actual` lock if thread-safety across pool
        instances becomes a real requirement on non-JVM targets.
- [x] **Step A.2 (partial) + first half of A.3 — `ChartIcon` abstraction + full
      Entry family migration.**
      - Added `expect abstract class ChartIcon` in `chartLibCore` commonMain, with
        `actual typealias ChartIcon = android.graphics.drawable.Drawable` on Android
        (zero source/behavior change for Android consumers — `entry.icon` is still
        literally a `Drawable`) and empty placeholder `actual abstract class ChartIcon`
        on iOS/desktop until Compose Multiplatform icon rendering (e.g. `Painter`) is
        designed. Required adding `-Xexpect-actual-classes` to `chartLibCore`'s
        `compilerOptions.freeCompilerArgs` (expect/actual classes are still Beta).
      - Moved the entire `data/` entry hierarchy into `chartLibCore` commonMain,
        unchanged in package/API shape:
        `BaseEntry`, `EntryFloat`, `EntryDouble`, the deprecated `Entry` bridge class,
        and all `Bar/Pie/Radar/Bubble/Candle` `*Entry`/`*EntryFloat`/`*EntryDouble`
        variants (19 files total).
      - Replaced `android.graphics.drawable.Drawable?` fields/params with `ChartIcon?`
        throughout (transparent on Android via the typealias).
      - Removed `android.annotation.SuppressLint`/`@TargetApi` (build-tool-only,
        meaningless outside Android — safe to drop).
      - **Breaking change (flagged, not silently made):** `EntryFloat` (and therefore
        `Entry`) no longer implements `android.os.Parcelable` or `java.io.Serializable`
        — both are JVM/Android-only types with no multiplatform equivalent. A repo-wide
        search found **no internal usage** of `EntryFloat.CREATOR`, `is Parcelable`,
        or entry serialization, so this is believed safe, but it **is** a public API
        removal for any external consumer relying on putting entries into a `Bundle`/
        `Intent` via `Parcelable`/`Serializable`. If needed later, this can be restored
        Android-only via the `expect class` + additional-supertypes-on-`actual` pattern
        (an `actual` declaration is allowed to implement extra platform-specific
        interfaces beyond what `expect` declares).
      - Replaced the JVM-only `Build.VERSION.SDK_INT` branch in `toString()`
        implementations with `this::class.simpleName` (works identically on all
        Kotlin targets).
      - Replaced `Utils.FLOAT_EPSILON`/`Utils.DOUBLE_EPSILON` (which pulled in the
        still-Android-only `Utils` object) with local common constants
        `ENTRY_FLOAT_EPSILON`/`ENTRY_DOUBLE_EPSILON` in `EntryFloat.kt`, built with the
        common-Kotlin-stdlib `Float.fromBits(1)`/`Double.fromBits(1L)`.
      - Removed two `timber.log.Timber.i(...)` informational log calls from
        `PieEntryFloat`'s deprecated `x` accessor (Timber is Android-only; logging
        facade design deferred — see the `timber.log.Timber` row in the abstractions
        table below).
      - `PieEntryDouble.kt` was moved as-is (it was already an empty file pre-existing
        in `chartLib` — a gap in the deprecated-class migration table unrelated to
        this KMP effort; left untouched, out of scope here).
      - Verified: `chartLibCore` builds clean on Android/iOS×3/desktop-JVM;
        `chartLib:compileDebugKotlin` succeeds **with zero source changes needed** in
        `chartLib` itself (proof the package-preserving move is fully transparent to
        existing consumers); full `./gradlew test :chartLibCore:allTests` and
        `chartLibCompose:assembleDebug`/`app:assembleDebug` all pass.
- [ ] **Step A.3 (remainder) — Migrate `BaseDataSet`/`DataSet`/`ChartData` and
      `interfaces/` (`IDataSet` family, `dataprovider` family).**
- [ ] **Step A.4 — Migrate formatters (`formatter/`) and remaining highlighters
      (`highlight/`).**
- [ ] **Step A.5 — Migrate `components/` (axes, legend, limit lines).**
- [ ] **Step A.6 — Migrate `utils/` geometry & viewport math
      (`ViewPortHandler`, `Transformer`, `PointF`).**
- [ ] **Step A.7 — New `chartLibComposeMultiplatform` module**: Compose
      Multiplatform renderers built on `chartLibCore` using `DrawScope`.
- [ ] **Step A.8 — Gesture handling** with `pointerInput`/`detectTransformGestures`
      replacing `ChartTouchListener`/`MotionEvent`.
- [ ] **Step A.9 — `demoKmp` Compose Multiplatform demo app**
      (Android, iOS, Desktop, optionally Wasm/JS).
- [ ] **Step A.10 — CI.** Extend GitHub Actions to build iOS/desktop/wasm targets
      (today only an Android emulator runs instrumentation tests).
- [ ] **Step A.11 — Publishing.** Extend `com.vanniktech.maven.publish` KMP
      publication support with per-target artifact coordinates for `chartLibCore`
      and `chartLibComposeMultiplatform`.

## Step A.2 — Common replacement abstractions needed before further migration

These must exist in `chartLibCore` (or be designed) before the corresponding
Android-coupled classes can move:

| Android type                     | Common replacement                                                        | Consumers to update |
|-----------------------------------|-----------------------------------------------------------------------------|----------------------|
| `android.graphics.Color` (Int)    | Already just an `Int` ARGB value at the API boundary — no wrapper needed, just remove the `android.graphics.Color` import/usages (done for `ColorTemplate`; remaining in `components/`). | `ComponentBase`, `AxisBase` |
| `android.graphics.DashPathEffect` | `data class DashEffect(val intervals: FloatArray, val phase: Float)` in `chartLibCore`; Android renderer maps it to a real `DashPathEffect` at draw time. | `IDataSet`, `LineDataSet`, `BarLineScatterCandleBubbleDataSet`, renderers |
| `android.graphics.Typeface`       | `expect class ChartTypeface` (or drop entirely from the data model and let each renderer own font/typeface as a rendering concern, not a dataset concern). | `ComponentBase`, `IDataSet` |
| `android.graphics.drawable.Drawable` (`icon`) | `expect class ChartIcon` wrapping a platform image handle (`Drawable` on Android, `UIImage`/`Painter`-friendly type on Compose Multiplatform), or make icon rendering a Compose-only concern (`painterResource` at the composable layer, not baked into `EntryFloat`). | `BaseEntry`, `EntryFloat`, `MarkerImage` |
| `android.os.Parcelable`           | Drop from common data classes. Parcelable is an Android IPC/state-restoration detail, not core chart data. If Android call sites rely on `Parcelable` (e.g. `EntryFloat` in a `Bundle`), add an Android-only wrapper/extension in `chartLib`'s `androidMain`. | `EntryFloat`, `PointF` |
| `android.graphics.Matrix` / `RectF` | Pure-Kotlin `Matrix3x2`/`Rect` value classes for the shared viewport math in `ViewPortHandler`/`Transformer`; Android renderer converts to/from `android.graphics.Matrix`/`RectF` only at the drawing boundary. | `ViewPortHandler`, `Transformer`, `TransformerHorizontalBarChart` |
| `android.view.View` (in `ViewPortHandler`) | Remove the `View` reference from shared viewport math; pass width/height as plain values instead. | `ViewPortHandler` |
| `android.animation.TimeInterpolator` (`Easing`) | Plain Kotlin `fun interface Easing { fun getInterpolation(input: Float): Float }`; Android animator wraps it in a `TimeInterpolator` adapter at the call site. | `ChartAnimator`, `jobs/*` |
| `timber.log.Timber` (Android-only logging) | Introduce a tiny common logging `expect`/`actual` facade (`ChartLog.d(...)`), or simply drop debug logging from common code and keep it Android-only via `androidMain`. | `NumberUtils`, `ViewPortHandler`, `AxisBase` |
| `android.annotation.SuppressLint` / `@TargetApi` | Just remove — these are Android lint/build-tool annotations with no multiplatform meaning. | `ChartHighlighter`, `HorizontalBarHighlighter`, others |
| `android.content.Context` (`AssetManagerUtils`, `ContextUtils`) | Keep these Android-only; they stay in `chartLib`'s `androidMain`, not `commonMain`. | `AssetManagerUtils`, `ContextUtils` |

## Migration order rationale (Steps A.3–A.6)

The dependency graph forces a specific order — nothing in `formatter/` or
`highlight/` can move until `data/EntryFloat`, `interfaces/datasets/IDataSet`, and
`utils/ViewPortHandler` are portable, because every formatter/highlighter references
at least one of them:

1. **`data/` + `interfaces/`** — apply the `DashEffect`/`ChartTypeface`/`ChartIcon`/
   dropped-`Parcelable` abstractions from Step A.2 to `BaseEntry`, `EntryFloat`,
   `EntryDouble`, all `*Entry*` subclasses, `BaseDataSet`, `DataSet`, all
   `*DataSet` subclasses, `ChartData` and subclasses, and all `interfaces/datasets/*`
   / `interfaces/dataprovider/*`.
2. **`formatter/`** — zero Android imports already; only blocked by `EntryFloat`,
   `IDataSet`, `ViewPortHandler`, `AxisBase`. Once `data/`+`interfaces/` move, only
   `ViewPortHandler`/`AxisBase` block the formatters that reference them
   (`DefaultValueFormatter`, `IValueFormatter`, `LargeValueFormatter`,
   `PercentFormatter`, `StackedValueFormatter`) — see Step A.6.
3. **`highlight/`** — `Range`/`RangeDouble` already moved. The rest
   (`Highlight`, `IHighlighter`, `BarHighlighter`, `CombinedHighlighter`,
   `PieHighlighter`, `PieRadarHighlighter`, `RadarHighlighter`, `ChartHighlighter`,
   `HorizontalBarHighlighter`) only need `@SuppressLint` removed plus the `data/`
   and `interfaces/` migration from step 1. Note: `PieHighlighter`/`RadarHighlighter`/
   `PieRadarHighlighter` reference `charts/PieChart`, `charts/RadarChart`,
   `charts/PieRadarChartBase` directly — these need to depend on new common
   *provider interfaces* instead of concrete `View`-based chart classes (introduce
   `interfaces/dataprovider` equivalents if not already sufficient), so the
   highlighter logic doesn't require the Android `View` subclasses at all.
4. **`components/`** — `ComponentBase`/`AxisBase` need `Typeface`/`Paint`/`Color`
   removed per Step A.2. `Legend`, `LegendEntry`, `XAxis`, `YAxis`, `LimitLine`,
   `LimitRange`, `Description` mostly extend/compose these, so they follow
   naturally once the base classes are portable. `MarkerView`/`MarkerImage`
   (Android `View` subclasses) stay in `chartLib`'s `androidMain` — they are
   inherently platform UI, not shared logic.
5. **`utils/` geometry** — `ViewPortHandler`, `Transformer`,
   `TransformerHorizontalBarChart` need the `Matrix`/`RectF`/`View` replacement
   from Step A.2. `PointF` needs `Parcelable` dropped from the common variant
   (keep an Android-only `Parcelable` extension if needed for `Bundle` interop).
   `CanvasUtils`, `SaveUtils`, `PaintUtils`, `AssetManagerUtils`, `ContextUtils`
   stay Android-only (`androidMain`) — they are fundamentally platform IO/graphics
   concerns, not shared chart logic.

## Step A.7+ — Compose Multiplatform renderer

Once `chartLibCore` covers data/formatters/highlighters/components/viewport math,
create `chartLibComposeMultiplatform`:

- Apply `org.jetbrains.kotlin.multiplatform` + `org.jetbrains.compose` (JetBrains
  Compose Multiplatform gradle plugin, not just `org.jetbrains.kotlin.plugin.compose`
  which only enables the compiler plugin for Android/JVM-only Compose).
- Reimplement each renderer (`LineChartRenderer`, `BarChartRenderer`,
  `PieChartRenderer`, etc.) as `DrawScope` extension functions using
  `drawLine`/`drawPath`/`drawCircle`/`drawText` instead of `Canvas.drawX(Paint)`.
  This is the single largest remaining chunk of work (~32 renderer files).
- Replace touch/gesture handling (`ChartTouchListener`, `BarLineChartTouchListener`,
  `PieRadarChartTouchListener`) with Compose `pointerInput` +
  `detectTransformGestures`/`detectDragGestures`.
- Target `androidTarget()`, `iosX64/iosArm64/iosSimulatorArm64()`,
  `jvm("desktop")`, and optionally `wasmJs { browser() }`.
- Structure source sets as `commonMain` (chart composables + draw logic),
  `androidMain`, `iosMain`, `desktopMain`, `wasmJsMain` only for platform-specific
  bits (e.g., font loading, bitmap export).

## Step A.9 — `demoKmp` app

- New Compose Multiplatform application module with `commonMain` (shared UI +
  chart usage examples mirroring `chartLibCompose`'s `examples/ChartExamples.kt`),
  `androidMain` (activity entry point), `iosMain` (SwiftUI/Compose entry point via
  `MainViewController`), `desktopMain` (`main()` launching a `ComposeWindow`), and
  optionally `wasmJsMain`.
- Register in `settings.gradle.kts` as `include(":demoKmp")`.

## Step A.10 — CI

- Extend `.github/workflows` to run `./gradlew :chartLibCore:allTests` and
  `:chartLibComposeMultiplatform:allTests` on macOS runners (required for iOS
  targets) in addition to the existing Android emulator instrumentation job.
- Add a desktop smoke-test / screenshot job for `demoKmp` if feasible.

## Step A.11 — Publishing

- Extend `mavenPublishing { }` blocks in `chartLibCore` and
  `chartLibComposeMultiplatform` for KMP publications (each target publishes its
  own artifact suffix, e.g. `chartLibCore-android`, `chartLibCore-iosarm64`,
  `chartLibCore-iossimulatorarm64`, `chartLibCore-iosx64`,
  `chartLibCore-desktop`, plus a `chartLibCore` metadata/common artifact).
- Update `jitpack.yml` / README to document new KMP coordinates once published.

## Notes / open questions for maintainers

- This roughly doubles the surface area to keep in sync with the existing
  View-based `chartLib`, which is explicitly in **maintenance mode**. Confirm
  there's real demand/maintainer bandwidth before committing to Steps A.7–A.11.
- Decide whether `chartLibCompose` (AndroidView-wrapper) stays long-term as a
  lighter-weight Android-only option, or is deprecated once
  `chartLibComposeMultiplatform` reaches parity.
- Decide on `wasmJs`/JS support scope now vs. later — it adds meaningful
  complexity (font rendering, canvas API differences) for comparatively niche
  demand relative to Android/iOS/Desktop.
