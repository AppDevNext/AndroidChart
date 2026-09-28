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
- [x] **CI fix — `chartLibCore` KMP module breaks the Linux "Check" CI job.**
      The `com.android.kotlin.multiplatform.library` plugin doesn't enable JVM-based
      unit tests for the `android` target by default, and Gradle errors out (rather
      than just warning) when it hits *disabled* Kotlin/Native targets — like the
      iOS targets on a non-macOS CI runner — unless explicitly told those are okay
      to skip. Fixed both:
      - Added `withHostTest {}` to `chartLibCore/build.gradle.kts`'s `android { }`
        block, which enables a `testAndroidHostTest` task that runs `commonTest`
        (currently just `ObjectPoolTest`) on the JVM, no emulator/device needed —
        this is what makes `chartLibCore`'s tests actually participate in `./gradlew
        test` at all.
      - Added `kotlin.native.ignoreDisabledTargets=true` to the root
        `gradle.properties`, so Gradle treats disabled iOS Kotlin/Native targets
        (unbuildable on Linux CI runners, which lack Xcode) as a no-op instead of a
        hard failure. iOS targets still build/test normally on macOS (verified
        locally on this machine, which has Xcode).
      - Verified locally: `./gradlew test`, `:chartLibCore:build`,
        `:chartLibCore:allTests`, `:chartLibCore:testAndroidHostTest`,
        `:chartLibCompose:assembleDebug`, `:app:assembleDebug` all still pass on
        macOS with Xcode present (all iOS/android/desktop targets still compile and
        test normally; nothing is being silently skipped here — the flag only
        matters on hosts that can't build iOS at all).
- [x] **Step A.3 (remainder, partial) — DataSet-family groundwork: common
      enums/abstractions landed; full `IDataSet`/`BaseDataSet`/`DataSet`/`ChartData`
      move is BLOCKED, see below.**
      - **Landed in `chartLibCore` (new, reusable common types):**
        - `components/AxisDependency.kt` (top-level enum: `LEFT`, `RIGHT`) —
          extracted from `YAxis`'s nested enum. `YAxis` keeps a nested
          `typealias AxisDependency = info.appdev.charting.components.AxisDependency`
          so `YAxis.AxisDependency.LEFT` and all existing imports/call sites keep
          working unchanged (nested type aliases are a real, if lesser-known,
          Kotlin feature — see kotlinlang.org's type-alias docs).
        - `components/LegendForm.kt` (top-level enum: `NONE`, `EMPTY`, `DEFAULT`,
          `SQUARE`, `CIRCLE`, `LINE`) — same nested-typealias treatment applied to
          `Legend.LegendForm`.
        - `utils/PaintStyle.kt` (top-level enum: `FILL`, `STROKE`, `FILL_AND_STROKE`)
          — platform-independent replacement for `android.graphics.Paint.Style`,
          used by `ICandleDataSet`/`CandleDataSet`. A new Android-only converter
          `PaintStyle?.toAndroidPaintStyle()` (in `chartLib`'s
          `utils/PaintStyleAndroid.kt`) bridges it back to `Paint.Style` at the two
          `CandleStickChartRenderer` draw call sites; the demo app's
          `CandleStickChartActivity` was updated to set `PaintStyle.FILL`/`STROKE`
          instead of `Paint.Style.FILL`/`STROKE`.
        - `utils/ChartTypeface.kt` (+ android/iOS/desktop actuals) — same
          `expect`/`actual` pattern as `ChartIcon`. `expect open class ChartTypeface`
          (must be `open`, not `abstract`, to match `android.graphics.Typeface`'s
          modality) with `actual typealias ChartTypeface = android.graphics.Typeface`
          on Android (zero-cost) and placeholder `actual open class ChartTypeface` on
          iOS/desktop. Applied to `IDataSet.valueTypeface`, `BaseDataSet.mValueTypeface`,
          and `ChartData.setValueTypeface(tf: ChartTypeface?)` — these three files
          **stay in `chartLib`** for now (see blocker below) but their public API
          surface is now common-type-ready.
        - `utils/DashEffect.kt` — a common `class DashEffect(intervals: FloatArray,
          phase: Float)` (manual `equals`/`hashCode` via `contentEquals`, since
          Kotlin data classes don't do array content-equality automatically) designed
          to eventually replace `android.graphics.DashPathEffect` in `IDataSet`/
          `BaseDataSet`/`ILineDataSet`/etc. **Not yet wired up** — the DashPathEffect
          usages in `BaseDataSet.formLineDashEffect`, `LineDataSet.dashPathEffect`,
          and `LineScatterCandleRadarDataSet.dashPathEffectHighlight` are consumed
          directly as `android.graphics.DashPathEffect` at several renderer call
          sites (`LegendRenderer`, `LineChartRenderer`, `CandleStickChartRenderer`,
          etc.); swapping the property types requires updating every one of those
          render call sites with an Android-side conversion helper (mirroring what
          was just done for `PaintStyle`). Left as a distinct, separately-scoped
          follow-up sub-slice ("A.3c") rather than folding it into an already-large
          change.
        - `utils/DisplayMetrics.kt` — a common `var chartDensity: Float = 1f` +
          `Float.convertDpToPixel()` extension, replacing the old Android-only
          `Float.convertDpToPixel()` in `chartLib`'s `NumberUtils.kt` (which read a
          nullable `android.util.DisplayMetrics` global and logged a Timber warning
          if uninitialized). `chartLib`'s `Context.initUtils()` now sets
          `chartDensity = this.resources.displayMetrics.density` directly. This is a
          **minor documented behavior change**: the old "Utils NOT INITIALIZED"
          Timber warning is gone — `chartDensity` simply defaults to `1f` (no
          scaling) until a platform sets it, rather than warning and passing the
          value through unscaled.
        - `utils/PointF.kt` moved from `chartLib` to `chartLibCore` commonMain,
          dropping `android.os.Parcelable`/`Parcel` support (same rationale/breaking-
          change disclosure as `EntryFloat` in the previous slice — no internal usage
          of `PointF.CREATOR` found repo-wide) and replacing the JVM-only
          `Math.toRadians(...)` call in `changePosition(...)` with a manual
          `degrees * PI / 180.0` conversion using `kotlin.math.PI`.
        - `ColorTemplate.argb(r, g, b)` made `public` (was `private`) and two new
          public constants `ColorTemplate.WHITE`/`ColorTemplate.BLACK` added, as
          reusable platform-independent replacements for
          `android.graphics.Color.rgb(...)`/`Color.WHITE`/`Color.BLACK` — not yet
          applied inside `LineDataSet`/`RadarDataSet`/`BarLineScatterCandleBubbleDataSet`
          (deferred along with the rest of those classes, see blocker below).
      - **Key blocker discovered (documents a correction to the migration order in
        "Migration order rationale" below):** `IDataSet.valueFormatter` is typed
        `IValueFormatter`, and `IValueFormatter.getFormattedValue(...)` takes a
        `ViewPortHandler?` parameter; `ILineDataSet.fillFormatter` is typed
        `IFillFormatter`, whose `getFillLinePosition(...)` takes a
        `LineDataProvider` (a chart-view-facing `interfaces/dataprovider` type).
        `ViewPortHandler` is a 595-line `Matrix`/`RectF`/`View`-coupled geometry
        engine (planned Step A.6), and `LineDataProvider` is transitively coupled to
        the whole View-based rendering pipeline (planned to move alongside/after
        Compose Multiplatform rendering work, not before). Because `IDataSet` and
        `ILineDataSet` reference these types directly in their member signatures,
        **`IDataSet`/`BaseDataSet`/`DataSet`/`ChartData` and every concrete
        `*DataSet` subclass cannot be moved to `chartLibCore` until Step A.6
        (`ViewPortHandler` abstraction) lands**, and a common formatter/dataprovider
        story is designed. This revises the previously-assumed ordering (A.3 fully
        before A.4/A.6) — **A.6 must now at least partially precede full A.3
        completion.** `interfaces/dataprovider/*` was also confirmed to stay
        Android-only in general (it exposes `RectF`, `Transformer`, and other
        View-pipeline types) rather than move to `chartLibCore`.
      - Also confirmed `interfaces/datasets/IBarDataSet` (depends on `utils/Fill.kt`,
        a `Shader`/`LinearGradient`/`Drawable`-heavy gradient-fill abstraction) and
        `IScatterDataSet` (depends on `renderer/scatter/IShapeRenderer`, which takes
        `Canvas`/`Paint` directly) have their own separate Android-only blockers
        beyond the `IValueFormatter`/`IFillFormatter` one — `BarDataSet` and
        `ScatterDataSet` will need dedicated `Fill`/`IShapeRenderer` abstraction
        work even after Step A.6 unblocks the rest of the family.
      - Verified: `chartLibCore` builds/tests green on Android/iOS×3/desktop-JVM;
        `chartLib:compileDebugKotlin`, full `./gradlew test`,
        `chartLibCompose:assembleDebug`, and `app:assembleDebug` all pass with the
        `YAxis`/`Legend`/`CandleDataSet`/`ICandleDataSet`/`IDataSet`/`BaseDataSet`/
        `ChartData`/`CandleStickChartRenderer`/`CandleStickChartActivity` changes
        described above.
- [x] **Step A.3 (completion) — `IDataSet`/`BaseDataSet`/`DataSet`/`ChartData`
      fully migrated to `chartLibCore` commonMain, unblocked by Step A.6.**
      - **Moved to `chartLibCore` commonMain** (same package/class names, so
        zero import churn for any downstream consumer):
        `interfaces/datasets/IDataSet.kt`, `data/BaseDataSet.kt`, `data/DataSet.kt`
        (including its nested `enum class Rounding { UP, DOWN, CLOSEST }`, which
        simply moved with the file — no separate top-level extraction needed since
        the whole class relocated), `data/ChartData.kt`.
      - **`DashEffect` finally wired up**: `IDataSet.formLineDashEffect`,
        `BaseDataSet.mFormLineDashEffect`/`formLineDashEffect`,
        `components/Legend.formLineDashEffect`, and
        `components/LegendEntry.formLineDashEffect` all switched from
        `android.graphics.DashPathEffect?` to the common `utils/DashEffect.kt`
        (landed unwired in the previous slice). `Legend`/`LegendEntry` themselves
        **stay in `chartLib`** (still blocked by `android.graphics.Paint` usage
        elsewhere in `Legend`), so a new Android-only
        `chartLib/utils/DashEffectAndroid.kt` (`DashEffect.toAndroidDashPathEffect()`)
        converts at the one real Canvas boundary,
        `LegendRenderer.kt`'s `formPaint.pathEffect = ...` assignment. Two demo-app
        call sites (`DataTools.kt`, `SpecificPositionsLineChartActivity.kt`) that
        constructed `DashPathEffect(...)` for `formLineDashEffect` were updated to
        construct `DashEffect(...)` instead — a small source-level (not behavioral)
        breaking change for any external code doing the same.
      - **`IDataSet.kt`'s nested-typealias imports replaced**: now imports the
        top-level common `components/AxisDependency`/`components/LegendForm`
        directly instead of importing `components/YAxis`/`components/Legend` just
        to reach their nested type aliases (same pattern used for `Highlight` in
        Step A.4).
      - **`BaseDataSet.kt` Android-coupling removed**:
        - `@ColorInt`/`@Transient` annotations dropped (lint/serialization hints
          with no multiplatform equivalent; `@Transient` was already meaningless
          once `Serializable` was dropped from `DataSet`).
        - `android.graphics.Color.rgb/argb/red/green/blue(...)` calls replaced with
          new platform-independent equivalents added to `chartLibCore`'s
          `utils/ColorTemplate.kt`: `argb(alpha, r, g, b)` (4-arg overload, the
          existing 3-arg `argb(r, g, b)` stayed for the fully-opaque case),
          `alpha(color)`, `red(color)`, `green(color)`, `blue(color)`.
        - The one `Context`-taking overload, `setColors(colors: IntArray, context:
          Context)` (resolves Android color resources via `ContextCompat`), was
          extracted out of the class body into a new Android-only extension
          function in `chartLib/data/BaseDataSetAndroid.kt`, implemented purely in
          terms of `BaseDataSet`'s existing public `resetColors()`/`addColor(...)`
          API (no `protected` member access needed). Call-site syntax
          (`dataSet.setColors(colors, context)`) is unchanged since Kotlin extension
          functions resolve identically to member functions at call sites; a
          repo-wide grep found zero existing callers of this overload.
        - `Utils.defaultValueFormatter` (an Android-only `object Utils` singleton
          that can't be referenced from commonMain, since dependencies only flow
          `chartLibCore` → `chartLib`, never the reverse) replaced with a private
          `companion object { private val defaultValueFormatter: IValueFormatter by
          lazy { DefaultValueFormatter(1) } }` directly on `BaseDataSet`.
      - **`DataSet.kt`/`ChartData.kt` cleanup**: dropped `@SuppressLint(...)`
        (meaningless outside `chartLib`, which is the only module with the custom
        `:lint` checks applied via `lintChecks(project(":lint"))`), dropped
        `java.io.Serializable` (same disclosed-breaking-change rationale as
        `EntryFloat`/`PointF`/`Highlight` — no internal usage of `DataSet`/
        `ChartData` serialization found repo-wide), and replaced the two
        `Timber.e(...)` calls with `println(...)` (same rare-error-log rationale as
        `ViewPortHandler`'s `Timber.i(...)` → `println(...)` in Step A.6).
      - **Formatter cluster partly unblocked and moved as a natural follow-on**,
        since `IDataSet.valueFormatter: IValueFormatter` now lives in
        `chartLibCore`: `formatter/IValueFormatter.kt` (no changes needed) and
        `formatter/DefaultValueFormatter.kt`/`formatter/StackedValueFormatter.kt`
        (both previously used `java.text.DecimalFormat`, which has no multiplatform
        equivalent — rewritten in terms of a new shared common helper,
        `utils/DecimalFormatting.kt`'s `formatGroupedDecimal(value, digits)`,
        implementing the same rounding + thousands-grouping behavior as the
        `"###,###,###,##0.00"`-style `DecimalFormat` patterns using only
        `kotlin.math`). `formatter/ColorFormatter.kt` also moved (only depended on
        `EntryFloat`/`IDataSet`, both already common). Added
        `chartLibCore/commonTest/formatter/DefaultValueFormatterTest.kt` (5 new
        unit tests covering zero/nonzero decimal digits, thousands grouping,
        negative values, and negative-zero rounding) since this rewrite had no
        prior test coverage.
      - **Still blocked / explicitly out of scope for this slice:**
        `IAxisValueFormatter`, `DefaultAxisValueFormatter`,
        `IndexAxisValueFormatter`, `LargeValueFormatter`, `PercentFormatter`
        (blocked by `components/AxisBase`, Step A.5); `IFillFormatter`,
        `DefaultFillFormatter` (blocked by `interfaces/dataprovider/LineDataProvider`
        and `ILineDataSet`); every concrete `*DataSet` subclass
        (`LineDataSet`, `BarDataSet`, `ScatterDataSet`, etc. — most still use
        `android.graphics.Color`/`Paint`/`Shader` extensively, and `IBarDataSet`/
        `IScatterDataSet` have their own separate `Fill`/`IShapeRenderer` blockers
        noted in the prior Step A.3 entry); `LineDataSet.dashPathEffect`/
        `LineScatterCandleRadarDataSet.dashPathEffectHighlight` (a *different*
        dash-effect property from `formLineDashEffect`, tied to `ILineDataSet`,
        left as `android.graphics.DashPathEffect` since `ILineDataSet` itself isn't
        moving yet).
      - Verified: `:chartLibCore:build` (Android host + desktop-JVM + iOS×3
        simulator/device targets all compile and pass tests, including the 5 new
        `DefaultValueFormatterTest` cases), `chartLib:compileDebugKotlin`
        (zero warnings/errors beyond pre-existing unrelated ones), full
        `./gradlew test`, `chartLibCompose:assembleDebug`, and `app:assembleDebug`
        all pass.
      - **Now unblocked:** highlighter classes in Step A.4 that depend on
        `IDataSet` (though most still also depend on `interfaces/dataprovider/*`,
        so remain blocked for that separate reason); `AxisBase`/Step A.5 can now
        freely reference `IDataSet`-family types if needed.
- [ ] **Step A.4 (partial) — `Highlight`/`IHighlighter` moved; the rest of
      `formatter/` and `highlight/` is BLOCKED by the same Step A.6 dependency
      found while investigating Step A.3.**
      - **Landed:** `highlight/Highlight.kt` and `highlight/IHighlighter.kt` moved
        to `chartLibCore` commonMain. `Highlight` dropped `java.io.Serializable`
        (same disclosed-breaking-change rationale as `EntryFloat`/`PointF` — no
        internal usage of Highlight serialization found repo-wide) and now imports
        the top-level common `info.appdev.charting.components.AxisDependency`
        instead of the nested `YAxis.AxisDependency` type alias (which itself still
        resolves to the exact same type — this is purely so `Highlight` doesn't
        need to reference the Android-only `YAxis` class to get at the enum).
        `IHighlighter` needed no changes at all (it only referenced `Highlight`).
      - **Everything else in `formatter/` is blocked:** every one of the 10 files in
        `formatter/` (`IValueFormatter`, `DefaultValueFormatter`,
        `IAxisValueFormatter`, `DefaultAxisValueFormatter`, `IndexAxisValueFormatter`,
        `LargeValueFormatter`, `PercentFormatter`, `StackedValueFormatter`,
        `IFillFormatter`, `DefaultFillFormatter`, `ColorFormatter`) imports at least
        one of: `utils/ViewPortHandler` (Step A.6, not yet done),
        `components/AxisBase` (Step A.5, not yet done),
        `interfaces/datasets/IDataSet`/`ILineDataSet` (blocked per the Step A.3
        finding), or `interfaces/dataprovider/LineDataProvider` (Android-only, tied
        to the View rendering pipeline). None can move yet.
      - **Everything else in `highlight/` is blocked** for the same reason:
        `ChartHighlighter`, `BarHighlighter`, `HorizontalBarHighlighter`,
        `CombinedHighlighter` all import `interfaces/datasets/IDataSet` and/or
        `interfaces/dataprovider/*`; `PieHighlighter`/`RadarHighlighter`/
        `PieRadarHighlighter` additionally import concrete `charts/PieChart`,
        `charts/RadarChart`, `charts/PieRadarChartBase` `View` subclasses directly.
      - **Conclusion:** Step A.4 cannot meaningfully proceed further until Step A.6
        (`ViewPortHandler`/`Transformer` common abstraction) and enough of Step A.5
        (`AxisBase` at minimum) land, exactly as flagged in the Step A.3 changelog
        entry above. Recommend doing **Step A.6 next**, since it's the single
        blocker unblocking the largest amount of remaining work (rest of A.3, all
        of A.4, most of A.5).
      - Verified: `chartLibCore` build/tests, `chartLib:compileDebugKotlin` (zero
        source changes needed), full `./gradlew test`,
        `chartLibCompose:assembleDebug`, `app:assembleDebug` all pass.
- [x] **Step A.6 — Migrate `utils/` geometry & viewport math
      (`Matrix`/`RectF` value types, `ViewPortHandler`, `Transformer`).**
      - **Common `Matrix`/`RectF` foundation** (landed first, verified in isolation):
        `chartLibCore/utils/Matrix.kt` — a full custom 3×3 affine/perspective matrix
        implementation, row-major and index-compatible with `android.graphics.Matrix`
        (`MSCALE_X=0` … `MPERSP_2=8`), with `reset`, `set`, `getValues`/`setValues`,
        `setTranslate`/`setScale` (with pivot), `postTranslate`/`postScale` (with
        pivot), `postConcat`, `mapPoints`, `mapRect`, and `invert` (cofactor/adjugate,
        returns `false` below a `1e-12f` determinant threshold instead of throwing).
        `chartLibCore/utils/RectF.kt` — a common rectangle type with `width`/`height`/
        `centerX`/`centerY`, `set(...)` overloads (including an `operator fun set`
        supporting the `rect[l,t,r] = b` indexed-assignment idiom `ViewPortHandler`
        relies on), and custom `equals`/`hashCode`/`toString`. 11 unit tests in
        `MatrixTest.kt` cover identity/reset, translate/scale (origin and pivot),
        composition order, `getValues`/`setValues` round-trips, `invert` correctness,
        and `mapRect` bounding-box computation — all pass on JVM (Android host),
        desktop, and iOS simulator.
      - **`ViewPortHandler` moved to `chartLibCore` commonMain**, rebuilt on the
        common `Matrix`/`RectF` instead of `android.graphics.Matrix`/`RectF`. Its
        only two Android touches were removed: the `android.view.View?` parameter on
        `refresh(...)`/`centerViewPort(...)` became a platform-agnostic
        `invalidate: (() -> Unit)?` callback lambda, and the single `Timber.i(...)`
        debug log became a plain `println(...)` (this is a rarely-hit path gated
        behind a `logging: Boolean = false` default, not worth a full logging
        facade for one call site).
      - **`Transformer` split into a common base + Android subclass**, since its
        `generateTransformedValues{Line,Bubble,Candle,Scatter}` methods take
        `ILineDataSet`/`IBubbleDataSet`/`ICandleDataSet`/`IScatterDataSet` params
        (still Android-only, blocked per the Step A.3 finding) and its
        `pathValueToPixel`/`pathValuesToPixel` methods use `android.graphics.Path`
        (no multiplatform equivalent yet):
        - `chartLibCore/utils/TransformerCore.kt` (new, common, open class) — holds
          every matrix/rect operation that doesn't depend on the dataset family or
          `Path`: `prepareMatrixValuePx`, `prepareMatrixOffset` (open, overridden by
          `TransformerHorizontalBarChart`), `pointValuesToPixel`, `rectValueToPixel`
          (+ phase/horizontal variants), `pixelsToValue`, `getValuesByTouchPoint`,
          `getPixelForValues`, `valueToPixelMatrix`/`pixelToValueMatrix`. The
          `FloatArray?`/`RectF?` nullable parameter types from the original
          Android-`Matrix`-flavored signatures were tightened to non-null (the
          common `Matrix.mapPoints`/`mapRect` API is non-null, and a repo-wide
          check confirmed no caller ever passes `null` here).
        - `chartLib/utils/Transformer.kt` (rewritten) — now `open class Transformer
          : TransformerCore(viewPortHandler)`, keeping only the
          `generateTransformedValues*` and `pathValueToPixel`/`pathValuesToPixel`
          methods. The two `Path` methods convert the common `Matrix` to
          `android.graphics.Matrix` via a new `toAndroidMatrix()` extension before
          calling `Path.transform(...)`, since `Path` itself can't move.
        - `TransformerHorizontalBarChart` needed no changes — its override of
          `prepareMatrixOffset` only touches matrices/`ViewPortHandler` accessors,
          all already common.
      - **New Android-side conversion helpers** (`chartLib/utils/MatrixAndroid.kt`,
        `RectFAndroid.kt`) bridge the common `Matrix`/`RectF` back to
        `android.graphics.Matrix`/`RectF` at the handful of places that must still
        call native Canvas/Path APIs directly: `toAndroidMatrix()`/`toCommonMatrix()`,
        `toAndroidRectF()`/`toCommonRectF()`, and `copyInto(target)` overloads in
        both directions (for buffer-style rects that get mutated in place, e.g.
        `mGridClippingRect`, `mBarShadowRectBuffer`, `barRect`).
      - **~20 files updated at the Canvas-drawing boundary** to route through these
        converters — the actual blast radius anticipated in the Step A.6 planning
        note: `Chart.kt` (public `contentRect: RectF` property now converts once at
        the getter), `BarChart.kt`/`HorizontalBarChart.kt` (`getBarBounds` converts
        the caller-supplied `outputRect` round-trip), `BarLineChartBase.kt` (all
        `canvas.clipRect`/`c.drawRect(contentRect, ...)` calls, all `viewPortHandler
        .refresh(...)`/`.centerViewPort(...)` calls swapped from `this`/`View` to
        `{ invalidate() }`), `BarLineChartTouchListener.kt` (switched its `Matrix`
        import to the common type; `refresh(...)` calls now wrap `chart` in
        `{ chart.invalidate() }`), the 4 zoom/pan `jobs/*.kt` files (same `View` →
        lambda swap, common `Matrix` import), and 8 renderer files
        (`XAxisRenderer(HorizontalBarChart)`, `YAxisRenderer(HorizontalBarChart)`,
        `BarChartRenderer`, `HorizontalBarChartRenderer`,
        `Rounded(Horizontal)BarChartRenderer`) whose clipping-rect buffers now
        `copyInto`/`toCommonRectF()` at the `viewPortHandler.contentRect`/
        `rectValueToPixel(...)` boundary instead of relying on same-type `.set(...)`.
      - **Not touched / explicitly out of scope for this slice:** `RadarChart.kt`,
        `PieRadarChartBase.kt` read `viewPortHandler.contentRect.width()`/`.height()`/
        `.left` etc. only — already source-compatible with the common `RectF`, no
        changes needed. `CombinedChart.kt`'s `contentRect` overrides delegate to the
        (already-converting) `Chart.contentRect` property, also untouched.
      - Verified: `chartLibCore` builds/tests green (Android host, desktop-JVM,
        iOS simulator — `Matrix`/`RectF`/`ViewPortHandler`/`TransformerCore` all
        compile with zero platform-specific code), `chartLib:compileDebugKotlin`,
        full `./gradlew test`, `chartLibCompose:assembleDebug`, and
        `app:assembleDebug` all pass.
      - **Now unblocked:** the rest of Step A.3 (`IDataSet`/`BaseDataSet`/
        `DataSet`/`ChartData`/concrete `*DataSet` subclasses, since
        `IValueFormatter`/`IFillFormatter` no longer need an Android-only
        `ViewPortHandler`/`LineDataProvider`), most of Step A.4 (`formatter/`
        package, most of `highlight/`), and Step A.5 groundwork.
- [x] **Step A.5 (axes + limit lines) — `components/`
      (`ComponentBase`/`AxisBase`/`XAxis`/`YAxis`/`LimitLine`/`LimitRange`) +
      the `AxisBase`-coupled formatter cluster
      (`IAxisValueFormatter`/`DefaultAxisValueFormatter`/`IndexAxisValueFormatter`/
      `LargeValueFormatter`/`PercentFormatter`) fully migrated to `chartLibCore`
      commonMain. `Legend`/`LegendEntry`/`Description`/`IMarker`/`MarkerImage`/
      `MarkerView` remain in `chartLib` for a follow-up slice.**
      - **Circular-dependency slice:** `AxisBase.valueFormatter` references
        `DefaultAxisValueFormatter` directly, and `IAxisValueFormatter`/
        `DefaultAxisValueFormatter` both take an `axis: AxisBase?` parameter —
        so `AxisBase` + `IAxisValueFormatter` + `DefaultAxisValueFormatter` had to
        move together as one atomic unit (can't be split across separate commits).
      - `ComponentBase.kt`: `Typeface?` → common `ChartTypeface?` (same
        zero-cost-typealias pattern as `IDataSet.valueTypeface` from Step A.3,
        so every existing `paintX.typeface = someComponent.typeface` call site at
        chartLib render boundaries stays source-compatible unchanged);
        `Color.BLACK` → `ColorTemplate.BLACK`; dropped `@ColorInt`.
      - `AxisBase.kt`: `Color.GRAY` → `ColorTemplate.GRAY` (new constant added to
        `chartLibCore/utils/ColorTemplate.kt`, `argb(128, 128, 128)`);
        `axisLineDashPathEffect`/`gridDashPathEffect` switched from
        `android.graphics.DashPathEffect?` to common `DashEffect?` (same pattern as
        Step A.3's `IDataSet.formLineDashEffect`); `Timber.e(...)` → `println(...)`
        in `addLimitLine`/`addLimitRange`; dropped `@ColorInt`. The one
        `Paint`-dependent method, `getLongestLabel(p: Paint?)`, was extracted out of
        the class body into a new Android-only extension function in
        `chartLib/components/AxisBaseAndroid.kt` (`fun AxisBase.getLongestLabel(p:
        Paint?): String`) since `Paint.measureText` has no multiplatform
        equivalent; the no-arg `longestLabel` property (pure string-length
        comparison, no `Paint`) stayed as a member on the common class.
      - `YAxis.kt`: `Color.GRAY` → `ColorTemplate.GRAY`; dropped `@ColorInt`. The
        two `Paint`-dependent methods, `getRequiredWidthSpace(p: Paint)`/
        `getRequiredHeightSpace(p: Paint)`, were extracted the same way into a new
        `chartLib/components/YAxisAndroid.kt` (calling the also-Android-only
        `PaintUtils.kt` `calcTextWidth`/`calcTextHeight` extensions and the new
        `AxisBase.getLongestLabel(p)` extension). `XAxis.kt` needed zero changes
        (already fully portable — only used the already-common
        `convertDpToPixel`).
      - `LimitLine.kt`/`LimitRange.kt`: `Color.rgb(...)` →
        `ColorTemplate.argb(...)`; `Paint.Style?` → the already-existing common
        `PaintStyle?` enum (from the earlier `ICandleDataSet`/`CandleDataSet`
        slice); `DashPathEffect?` → common `DashEffect?`; dropped `@ColorInt`.
      - Renderer call-site updates for the `DashEffect`→`DashPathEffect` and
        `PaintStyle`→`Paint.Style` conversions (via the existing
        `toAndroidDashPathEffect()`/`toAndroidPaintStyle()` converters):
        `XAxisRenderer.kt`, `YAxisRenderer.kt`,
        `XAxisRendererHorizontalBarChart.kt`, `YAxisRendererHorizontalBarChart.kt`,
        `YAxisRendererRadarChart.kt` — all at their
        `paintGrid.pathEffect =`/`paintAxisLine.pathEffect =`/
        `limitLinePaint.pathEffect =`/`limitRangePaint.pathEffect =`/
        `limitLinePaint.style =`/`limitRangePaint.style =` call sites. Also added
        the new `getRequiredWidthSpace`/`getRequiredHeightSpace` extension-function
        imports at their two call sites, `BarLineChartBase.kt` and
        `HorizontalBarChart.kt`.
      - **Formatter cluster** (now unblocked once `AxisBase` is common):
        - `IAxisValueFormatter.kt`/`IndexAxisValueFormatter.kt`: moved unchanged
          (no Android coupling).
        - `DefaultAxisValueFormatter.kt`: dropped the `java.text.DecimalFormat`
          field, rewritten using the existing shared
          `chartLibCore/utils/DecimalFormatting.kt` helper
          `formatGroupedDecimal(value, digits)` (same helper already used by
          `DefaultValueFormatter`/`StackedValueFormatter` in Step A.3).
        - `PercentFormatter.kt`: same `formatGroupedDecimal(value, 1)` rewrite;
          the unused `PercentFormatter(format: DecimalFormat)` constructor overload
          (zero callers repo-wide, confirmed via grep) was replaced with
          `PercentFormatter(decimalDigits: Int)` — a disclosed breaking change,
          consistent with this migration's established pattern of trading
          Java-only APIs (`Serializable`, `Parcelable`, now `DecimalFormat`) for
          KMP-portable equivalents.
        - `LargeValueFormatter.kt`: the most involved rewrite — replaced
          `DecimalFormat("###E00")`-based engineering-notation formatting with a
          manual Kotlin algorithm: compute `floor(log10(|value|))` (self-corrected
          for floating-point imprecision by checking neighbouring powers of ten),
          floor-divide by 3 and re-multiply to get the engineering exponent
          (nearest multiple of 3 ≤ the true exponent), compute the mantissa,
          determine how many fractional digits are still needed to reach exactly
          3 significant digits total, round with carry-over handling (mantissa
          rounding up to ≥1000 bumps the exponent bracket and recomputes), then
          format and strip trailing zeros/the decimal point. Validated against a
          real JDK `DecimalFormat("###E00")` (`/tmp/DFTest.java`) to empirically
          confirm the exact rounding/formatting behavior before writing the
          Kotlin port. **Caught and fixed one bug during verification:** the
          initial port forgot to re-multiply the floor-divided exponent by 3
          (`floorDiv3(exponent)` instead of `floorDiv3(exponent) * 3`), which
          surfaced immediately as a test failure (`1100f` → `"110"` instead of
          `"1.1k"`) and was fixed before landing.
      - **Test migration:** `chartLib/src/test/.../LargeValueFormatterTest.kt`
        (JUnit4, ~25 assertions) moved to
        `chartLibCore/src/commonTest/.../LargeValueFormatterTest.kt` and ported to
        `kotlin.test` (`Test`/`assertEquals`) — now runs on every `chartLibCore`
        target (Android host, desktop JVM, iOS simulator) instead of only the
        Android JVM unit-test target. All ~25 assertions pass unchanged after the
        rewrite (this was the regression check for the manual engineering-notation
        algorithm).
      - Verified: `chartLibCore:allTests` green (Android host + desktop JVM + iOS
        simulator, including the ported `LargeValueFormatterTest`),
        `chartLib:compileDebugKotlin`, full `./gradlew test`,
        `chartLibCompose:assembleDebug`, `app:assembleDebug` all pass.
      - **Now unblocked:** Step A.4's dataprovider-independent formatter cluster
        (`IAxisValueFormatter` family) is fully migrated as part of this slice;
        the `Legend`/`LegendEntry` components (Step A.5 continuation) are now
        migrated too, see below.

- [x] **Step A.5 (continuation) — `Legend`/`LegendEntry`/`Description` fully
      migrated to `chartLibCore` commonMain; `IMarker`/`MarkerImage`/`MarkerView`
      confirmed permanently Android-only (Canvas/Context/Drawable/RelativeLayout
      coupling with no separable portable core) and left in `chartLib`.**
      - `LegendEntry.kt`: trivial move, only dropped `@ColorInt`.
      - `Legend.kt`: dropped `@ColorInt` (n/a — none present besides the
        already-common enums); the bulk of the class (alignment/orientation
        enums, spacing properties, `entries`/`extraEntries`, `setCustom`/
        `setExtra`/`resetCustom`) needed no changes at all. Three methods were
        `Paint`-dependent and were extracted into a new Android-only extension
        file, `chartLib/components/LegendAndroid.kt`:
        `Legend.getMaximumEntryWidth(p: Paint)`, `Legend.getMaximumEntryHeight(p:
        Paint)`, and — the largest and most involved extraction in this
        migration so far — `Legend.calculateDimensions(labelPaint: Paint,
        viewPortHandler: ViewPortHandler)` (the ~110-line horizontal/vertical
        legend-layout algorithm). Unlike the smaller single/double-line
        extractions in Step A.3/A.5 (`getLongestLabel`, `getRequiredWidthSpace`),
        this shows the extension-function pattern scales to large,
        multi-branch business logic as long as every field/property it touches
        is `public` on the common class — required switching the two internal
        offset reads at the end of `calculateDimensions` from the `protected`
        `mXOffset`/`mYOffset` fields to the existing public `xOffset`/`yOffset`
        accessors on `ComponentBase` (extension functions cannot see `protected`
        members from outside the class hierarchy, same constraint documented in
        Step A.3's `BaseDataSetAndroid.kt`).
      - `Description.kt`: `textAlign: android.graphics.Paint.Align?` replaced
        with a new common `chartLibCore/utils/TextAlign.kt` enum
        (`LEFT`/`CENTER`/`RIGHT`) plus a new `chartLib/utils/TextAlignAndroid.kt`
        converter (`TextAlign?.toAndroidAlign(default)`), following the same
        common-enum-plus-Android-converter pattern already established for
        `PaintStyle`/`DashEffect`. Only one render call site needed updating,
        `Chart.kt`'s `drawDescription()` (`mDescPaint.textAlign =
        description.textAlign.toAndroidAlign()`); grepped for other
        `description.textAlign`/`Description(...)` usages repo-wide and found
        none that set a custom alignment (only the no-op default).
      - `LegendRenderer.kt` updated to import the new
        `info.appdev.charting.components.calculateDimensions` extension
        function at its one call site (member-call syntax unchanged, resolves
        identically as an extension call).
      - Verified: `chartLibCore:allTests` green, `chartLib:compileDebugKotlin`,
        full `./gradlew test`, `chartLibCompose:assembleDebug`,
        `app:assembleDebug` all pass.
      - **Step A.5 is now considered fully complete**: all of `components/`
        that has a genuinely portable core (axes, limit lines, legend,
        description) lives in `chartLibCore`; the remainder
        (`IMarker`/`MarkerImage`/`MarkerView`) is permanently Android-only by
        design, matching the same category of decision already made for
        `highlight/`'s dataprovider-coupled classes in Step A.4.

- [ ] **Step A.7 — New `chartLibComposeMultiplatform` module**: Compose
      Multiplatform renderers built on `chartLibCore` using `DrawScope`.
      Given the plan doc already flags this as "the single largest remaining
      chunk of work (~32 renderer files)", it is being sub-sliced:
    - [x] **Step A.7 (module scaffold)**: created the new
          `chartLibComposeMultiplatform` module and verified the whole Gradle/
          Kotlin/Compose Multiplatform toolchain resolves and builds across
          every target *before* investing in any renderer port.
      - `chartLibComposeMultiplatform/build.gradle.kts` closely mirrors
        `chartLibCore/build.gradle.kts` (`com.android.kotlin.multiplatform.library`
        + `org.jetbrains.kotlin.multiplatform`), adding two more plugins:
        `org.jetbrains.compose` (the Compose Multiplatform Gradle plugin, which
        supplies the `compose.runtime`/`compose.foundation`/`compose.ui`
        dependency aliases) and `org.jetbrains.kotlin.plugin.compose` (the
        Compose compiler, version-pinned to the project's Kotlin `2.4.10`, same
        as `chartLibCompose`). `commonMain` depends on `chartLibCore` (`api`)
        plus `compose.runtime`/`compose.foundation`/`compose.ui`.
      - No Gradle version catalog (`libs.versions.toml`) exists in this repo;
        plugin/dependency versions are declared inline per-module, so the new
        Compose Multiplatform plugin version is pinned directly in this
        module's `build.gradle.kts`.
      - **Compose Multiplatform plugin version required raising this module's
        `compileSdk` to 37** (kept at `36` everywhere else in the repo):
        Compose Multiplatform `1.12.1`'s Android artifacts
        (`androidx.compose.ui:ui-android`, `foundation-android`,
        `runtime-saveable-android`, etc.) declare an AAR metadata minimum of
        API 37, which fails `checkAndroidMainAarMetadata` at `compileSdk=36`.
        An older plugin version (`1.8.2`) avoids the compileSdk bump but is
        incompatible with AGP 9.2.1's newer
        `KotlinMultiplatformAndroidComponentsExtension` API
        (`NoSuchMethodError` on `onVariant`) — confirmed by testing both.
        `compileSdk = 37` is therefore scoped to only this one module for now;
        the rest of the repo (`chartLib`, `chartLibCore`, `chartLibCompose`,
        `app`) intentionally stays on `36` until there's a reason to bump
        everything at once.
      - **Dropped `iosX64` for this module only** (unlike `chartLibCore`,
        which still targets it): Compose Multiplatform `1.12.x` no longer
        publishes `compose.ui`/`compose.foundation`/`compose.runtime`
        artifacts for the Intel iOS simulator target, so `iosX64` dependency
        resolution fails outright. Only `iosArm64()`/`iosSimulatorArm64()` are
        configured, matching upstream Compose Multiplatform's own supported
        iOS target set.
      - Added a placeholder `chartLibComposeMultiplatform/.../Placeholder.kt`
        (a single documented constant) purely to validate the module compiles
        end-to-end on every target — no real renderer code yet.
      - `settings.gradle.kts` updated with
        `include(":chartLibComposeMultiplatform")`.
      - Verified: `:chartLibComposeMultiplatform:build` succeeds (Android AAR,
        desktop jar, and both iOS arm64 klibs/frameworks all compile/link),
        plus the full existing chain (`chartLibCore:allTests`,
        `chartLib:compileDebugKotlin`, full `./gradlew test`,
        `chartLibCompose:assembleDebug`, `app:assembleDebug`) still green.
      - **Not yet done** (remaining sub-slices of Step A.7): the actual
        `DrawScope` port of the ~32 renderer files, and deciding/implementing
        a slice order for them (proof-of-concept with one simple renderer
        first, per the plan, before the rest).
    - [x] **Step A.7 (grid line proof-of-concept)**: ported the vertical/
          horizontal axis grid line drawing (`XAxisRenderer.renderGridLines`/
          `drawGridLine` and `YAxisRenderer.renderGridLines`/`linePath`/
          `transformedPositions`) to Compose's `DrawScope`, as the first real
          renderer slice, chosen because it needs no concrete chart-type
          `DataSet` (`LineDataSet`/`BarDataSet`/etc. are still Android-coupled
          and haven't moved to `chartLibCore` yet — only the axis/viewport/
          transform types from Step A.5/A.6 are needed), making it fully
          self-contained.
      - New file `chartLibComposeMultiplatform/.../renderer/AxisGridRenderer.kt`
        with `DrawScope.drawXAxisGridLines(xAxis, viewPortHandler, transformer)`
        and `DrawScope.drawYAxisGridLines(yAxis, viewPortHandler, transformer)`,
        operating purely on `chartLibCore` types (`XAxis`/`YAxis`,
        `ViewPortHandler`, `TransformerCore`) — no dependency on chartLib's
        `Renderer`/`DataRenderer`/`AxisRenderer` Android base-class hierarchy,
        confirming the intended Step A.8 direction: Compose renderers are
        fresh idiomatic functions operating on the portable model, not a
        line-for-line port of the View-based OOP renderer classes.
      - The axis-value-to-pixel math (`xAxisGridPixelPositions`/
        `yAxisGridPixelPositions`) is deliberately factored out as plain
        functions returning a `FloatArray`, separate from the actual
        `drawLine`/`clipRect` calls, specifically so it can be unit-tested
        without a Compose UI test harness.
      - New test `chartLibComposeMultiplatform/commonTest/.../
        AxisGridRendererTest.kt`: sets up a `ViewPortHandler`/`TransformerCore`
        for a 100x100 content rect mapping axis values `[0, 10]`, and asserts
        axis value `0`/`10` map to the expected content-rect edges (left/right
        for X, bottom/top for Y, confirming the Y-pixel-axis inversion).
      - Removed the earlier scaffold placeholder file now that real renderer
        code exists in the module.
      - Verified: this is the **first slice where `chartLibComposeMultiplatform`
        tests actually ran and passed on three targets** —
        `testAndroidHostTest`, `desktopTest`, and (this session's sandbox
        happened to have a usable iOS toolchain) `iosSimulatorArm64Test` — all
        2/2 tests green on each. Full existing chain
        (`chartLibCore:allTests`, `chartLib:compileDebugKotlin`,
        `./gradlew test`, `chartLibCompose:assembleDebug`,
        `app:assembleDebug`) also still green.
      - **Not yet done**: the remaining ~30 renderer files (data renderers for
        each chart type, legend renderer, limit line renderer, marker
        rendering, etc.), most of which *do* need the concrete `DataSet`
        classes ported to `chartLibCore` first (a prerequisite not yet
        started) before they can be tackled the same way.
- [x] **Step A.3 (continuation, part 2) — concrete `DataSet` family (Bar/Line/
      Scatter/Candle/Bubble/Radar/Pie) prerequisite for Step A.7's renderer
      port.** The Step A.7 grid-line slice above surfaced that most of the
      remaining ~30 renderer files need concrete chart-type `DataSet` classes
      (`LineDataSet`/`BarDataSet`/etc.), which hadn't moved to `chartLibCore`
      yet even though the abstract `DataSet`/`BaseDataSet`/`IDataSet` base
      already had (Step A.3 completion). This slice migrates every concrete
      `DataSet` class that has *no* remaining Android-only blocker, and
      leaves the ones that genuinely do (documented below) as Android-only
      leaves extending the now-common parent classes — same pattern as
      `IMarker`/highlighters in Steps A.4/A.5.
      - **Moved to `chartLibCore` commonMain** (same package/class names):
        `data/BarLineScatterCandleBubbleDataSet.kt`,
        `data/LineScatterCandleRadarDataSet.kt`, `data/LineRadarDataSet.kt`,
        `data/BubbleDataSet.kt`, `data/CandleDataSet.kt`, `data/PieDataSet.kt`,
        `data/RadarDataSet.kt`, and their interfaces
        `interfaces/datasets/IBarLineScatterCandleBubbleDataSet.kt`,
        `ILineScatterCandleRadarDataSet.kt`, `ILineRadarDataSet.kt`,
        `IBubbleDataSet.kt`, `ICandleDataSet.kt`, `IPieDataSet.kt`,
        `IRadarDataSet.kt`. `BubbleDataSet`/`PieDataSet`/`CandleDataSet`/
        `RadarDataSet` are now fully portable leaf classes; the abstract
        parents unblock any future common data renderer for those chart
        types.
      - Trivial Android cleanup applied throughout: `@ColorInt` annotations
        dropped (non-functional at runtime); `android.graphics.Color.rgb(...)`/
        `Color.WHITE` literals replaced with the existing common
        `ColorTemplate.argb(r, g, b)` helper (already used elsewhere in
        `chartLibCore`); the `convertDpToPixel()` calls in these files already
        resolved to the common `Float.convertDpToPixel()` extension
        established in Step A.5 (backed by the global `chartDensity`
        set via `Context.initUtils()`), so no changes were needed there.
      - `ILineScatterCandleRadarDataSet.dashPathEffectHighlight`/
        `LineScatterCandleRadarDataSet.enableDashedHighlightLine(...)`:
        `android.graphics.DashPathEffect` replaced with the common `DashEffect`
        type (Step A.5's `DashEffect`/`toAndroidDashPathEffect()` pattern,
        reused as-is). One render call site updated,
        `LineScatterCandleRadarRenderer.drawHighlightLines(...)`
        (`paintHighlight.pathEffect = set.dashPathEffectHighlight` →
        `...?.toAndroidDashPathEffect()`).
      - **`fillDrawable` (on `ILineRadarDataSet`/`LineRadarDataSet`) removed
        from common code entirely** and replaced with a new Android-only
        side-channel: `chartLib/data/LineRadarDataSetAndroid.kt` defines
        `var ILineRadarDataSet<*>.fillDrawable: Drawable?` as an extension
        property backed by a `WeakHashMap<ILineRadarDataSet<*>, Drawable?>`
        keyed by dataset identity. `android.graphics.drawable.Drawable` has no
        portable equivalent (unlike `DashPathEffect`/`Paint.Style`, which
        already had common replacements), so — matching the established
        extension-property pattern from Step A.3's `BaseDataSet.setColors` —
        this keeps `dataSet.fillDrawable = ...`/`dataSet.fillDrawable`
        call-site syntax completely unchanged for both `LineDataSet` (still
        Android-only, stays in `chartLib`) and `RadarDataSet` (now common),
        at the cost of adding an explicit
        `import info.appdev.charting.data.fillDrawable` at each call site
        (`LineChartRenderer.kt`, `RadarChartRenderer.kt`, and three `app`
        example activities that set a custom fill drawable).
      - **Two small disclosed behavior changes**, documented in
        `LineRadarDataSet`'s class doc, both around the removed `fillDrawable`
        stored property: setting `fillColor` no longer implicitly clears a
        previously set `fillDrawable` (previously an automatic side effect of
        the old stored-property setter), and `.copy()` no longer propagates
        `fillDrawable` to the copy. Callers relying on either behavior should
        set `fillDrawable` explicitly afterward — same category of
        already-disclosed minor breaking change as `PercentFormatter`'s
        constructor change in Step A.5.
      - **Left as Android-only leaves** (extending the now-common abstract
        parents, same as `IMarker`/highlighters in Steps A.4/A.5): `BarDataSet`
        (needs the heavily Canvas/Paint/Drawable/LinearGradient-coupled `Fill`
        class, which itself would need the same common-type-plus-Android-
        renderer split as `DashEffect`/`PaintStyle` — out of scope for this
        slice), `LineDataSet` (needs `Fill`, `Context`/`ContextCompat`-based
        drawable-resource fill loading, and `IFillFormatter`, which in turn
        depends on the permanently-Android-only `LineDataProvider` from Step
        A.4), and `ScatterDataSet`/`IScatterDataSet` (needs the
        Canvas/Paint-coupled `IShapeRenderer` family in
        `renderer/scatter/`).
      - Verified: `chartLibCore:compileKotlinDesktop` (chartLibCore's own
        compile), full `chartLibCore:allTests`, `chartLib:compileDebugKotlin`,
        full `./gradlew test`, `chartLibCompose:assembleDebug`,
        `app:assembleDebug`, and `chartLibComposeMultiplatform:build` (all
        targets, since it depends on `chartLibCore`) all pass.
      - **Not yet done**: `Fill` splitting (common data + Android drawing
        extension) to unblock `BarDataSet`/`LineDataSet`; `IFillFormatter`/
        `LineDataProvider` decoupling; `IShapeRenderer`/`ScatterDataSet`. Any
        of these would be reasonable next slices before resuming the Step A.7
        renderer port for Bar/Line/Scatter charts (Candle/Radar/Bubble/Pie
        renderers are unblocked already on the data-model side).
- [x] **Step A.3 (continuation, part 3) — split the `Fill` class, migrate
      `BarDataSet`.** Tackles the first of the three blockers flagged above.
      `Fill` (`utils/Fill.kt`) mixed a small amount of portable state
      (`type`/`color`/`alpha`/`gradientColors`/`gradientPositions`/the derived
      `finalColor`) with Canvas/Paint/Drawable/LinearGradient-based drawing
      logic (`fillRect(...)`/`fillPath(...)`) and a `Drawable` payload for
      `Type.DRAWABLE` — none of which have a portable KMP equivalent.
      - **`chartLibCore/utils/Fill.kt`** now holds only the portable state:
        `Type`/`Direction` enums, `type`, `color`, `alpha`,
        `gradientColors`/`gradientPositions`, the `startColor,endColor`
        gradient constructor, and `setGradientColors(...)`. The previously
        `private var mFinalColor` is exposed as a new public read-only
        `val finalColor: Int?` property (was only accessible internally to
        `fillRect`/`fillPath` before) so the Android-only drawing extensions
        can read it. `@ColorInt` dropped (non-functional at runtime, same as
        prior slices).
      - **New `chartLib/utils/FillAndroid.kt`** holds everything
        Android-only: `fun Fill.fillRect(...)`/`fun Fill.fillPath(...)` as
        extension functions (ported verbatim from the old member functions,
        just reading `finalColor` instead of the private `mFinalColor`), plus
        `var Fill.drawable: Drawable?` as a `WeakHashMap<Fill, Drawable?>`-backed
        extension property for the `Type.DRAWABLE` payload — same side-channel
        pattern as `ILineRadarDataSet.fillDrawable` from continuation part 2.
        (Note: `Type.DRAWABLE`/`drawable` were already dead code with no
        public setter anywhere in the codebase before this slice; the
        side-channel exists for API completeness/future use, not because
        anything currently exercises it.)
      - Two call sites (`BarChartRenderer.kt`, `HorizontalBarChartRenderer.kt`,
        both calling `dataSet.getFill(pos)?.fillRect(...)`) needed only an
        added `import info.appdev.charting.utils.fillRect` — no call-site
        syntax changes, since Kotlin resolves member-call syntax
        (`fill.fillRect(...)`) identically whether `fillRect` is a member or
        an extension function.
      - **Moved to `chartLibCore` commonMain**: `data/BarDataSet.kt`,
        `interfaces/datasets/IBarDataSet.kt` — `IBarDataSet` needed no changes
        (it was already portable, referencing only `Fill`/`BarEntryFloat`);
        `BarDataSet` had `android.graphics.Color.rgb(...)`/`Color.BLACK`
        replaced with `ColorTemplate.argb(...)`/`ColorTemplate.BLACK` (the
        latter a pre-existing common constant) and `@ColorInt` dropped.
        `BarDataSet` is now a fully portable leaf class — first of the three
        remaining Android-only `DataSet` leaves to move.
      - **`LineDataSet`/`ScatterDataSet` remain Android-only** — `LineDataSet`
        still needs `IFillFormatter`/`LineDataProvider` decoupling and
        `Context`/`ContextCompat`-based drawable-resource fill loading;
        `ScatterDataSet` still needs the `IShapeRenderer` family split.
      - No behavior changes disclosed for this slice — `finalColor` was
        already computed identically before (just via a private property),
        and the dead `Type.DRAWABLE` path behaves the same (returns early
        when no drawable is set, same as before when `drawable` was `null`).
      - Verified: `chartLibCore:compileKotlinDesktop`,
        `chartLib:compileDebugKotlin`, full `chartLibCore:allTests`,
        `chartLibCompose:assembleDebug`, `app:assembleDebug`, full
        `./gradlew test`, and `chartLibComposeMultiplatform:build` (all
        targets) all pass.
      - **Not yet done**: `IFillFormatter`/`LineDataProvider` decoupling and
        `IShapeRenderer`/`ScatterDataSet` splitting remain as candidate next
        slices to fully unblock `LineDataSet`/`ScatterDataSet`; resuming the
        Step A.7 renderer port itself (e.g. a Bar/Candle/Radar/Bubble/Pie data
        renderer proof-of-concept) is also now viable given the expanded set
        of portable `DataSet` leaves.
- [x] **Step A.3 (continuation, part 4) — decouple `IFillFormatter`, migrate
      `LineDataSet`.** Tackles the second blocker flagged above. `LineDataSet`
      had three remaining Android couplings: `android.graphics.DashPathEffect`,
      the `IFillFormatter?` stored property (whose interface method takes a
      `LineDataProvider`, permanently Android-only per Step A.4), and a
      `Context`/`ContextCompat`-based `setCircleColors(colors, context)`
      overload.
      - `DashPathEffect` → common `DashEffect` (Step A.5's type, already reused
        in continuation part 2): `dashPathEffect` property, private backing
        field, and `enableDashedLine(...)`'s construction all now use
        `DashEffect(floatArrayOf(lineLength, spaceLength), phase)`. One
        renderer call site updated, `LineChartRenderer.drawCubicFill`-adjacent
        line-paint setup (`paintRender.pathEffect = dataSet.dashPathEffect` →
        `...?.toAndroidDashPathEffect()`, reusing the existing conversion
        extension).
      - **`fillFormatter` (on `ILineDataSet`/`LineDataSet`) removed from
        common code entirely**, same side-channel pattern as `fillDrawable`
        from continuation part 2: new
        `chartLib/data/LineDataSetAndroid.kt` defines
        `var ILineDataSet<*>.fillFormatter: IFillFormatter?` backed by a
        `WeakHashMap<ILineDataSet<*>, IFillFormatter>`, with the getter
        lazily materializing a `DefaultFillFormatter()` default (matching the
        old stored-property's non-null default) and the setter mapping
        `null` back to a fresh `DefaultFillFormatter()` (matching the old
        setter's behavior). Six call sites updated with an added
        `import info.appdev.charting.data.fillFormatter`:
        `LineChartRenderer.kt` (2 reads) and five `app` example activities
        that set a custom `fillFormatter`.
      - **`setCircleColors(colors: IntArray, context: Context)`** (the
        `ContextCompat.getColor(...)`-resolving overload) moved to the same
        `LineDataSetAndroid.kt` as a generic extension function
        (`fun <T : BaseEntry<Float>> LineDataSet<T>.setCircleColors(...)`);
        call-site syntax is unchanged (no existing call sites in this repo
        use this overload, but the public API shape is preserved for
        external consumers).
      - Trivial cleanup: `@ColorInt` dropped; `Color.WHITE`/
        `Color.rgb(140, 234, 255)` → `ColorTemplate.WHITE`/
        `ColorTemplate.argb(140, 234, 255)`; two `Timber.e(...)` validation
        warnings (circle/circle-hole radius) → `println(...)`, matching the
        `println`-for-warnings pattern already used elsewhere in
        `chartLibCore` (`AxisBase`, `ViewPortHandler`, `DataSet`,
        `ChartData`); the `@SuppressLint("RawTypeDataSet")` annotation on the
        internal `copy(lineDataSet: LineDataSet<*>)` helper was dropped
        (Android-only annotation type, and the custom `RawTypeDataSet` lint
        check only runs against the `chartLib` module, not `chartLibCore`).
      - **Moved to `chartLibCore` commonMain**: `data/LineDataSet.kt`,
        `interfaces/datasets/ILineDataSet.kt`. `LineDataSet` is now a fully
        portable leaf class (generic over `BaseEntry<Float>`, same as before).
      - **Disclosed behavior change**: `.copy()` no longer propagates a
        custom `fillFormatter` to the copy (previously copied via the old
        `copy(LineDataSet)` helper's `mFillFormatter` assignment) — same
        category as the `fillDrawable` copy-non-propagation disclosed in
        continuation part 2. Documented in `LineDataSet`'s class doc.
      - **`ScatterDataSet`/`IScatterDataSet` still Android-only** — needs the
        `IShapeRenderer` family split (`renderer/scatter/*`, Canvas/Paint-
        coupled) plus extracting the `ScatterShape` enum (currently nested
        inside the Android-only `ScatterChart`) to a portable location.
      - Verified: `chartLibCore:compileKotlinDesktop`,
        `chartLib:compileDebugKotlin`, full `chartLibCore:allTests`,
        `chartLibCompose:assembleDebug`, `app:assembleDebug`, full
        `./gradlew test`, and `chartLibComposeMultiplatform:build` (all
        targets) all pass.
      - **Not yet done**: `IShapeRenderer`/`ScatterDataSet` splitting (the
        last remaining Android-only `DataSet` leaf); resuming the Step A.7
        renderer port itself is now viable for every chart type except
        Scatter.
- [x] **Step A.3 (continuation, part 5) — split `IShapeRenderer`, migrate
      `ScatterDataSet`.** Tackles the last of the three blockers flagged
      above, completing the concrete `DataSet` family migration: every chart
      type's `DataSet` is now portable.
      - **`ScatterShape` extracted out of the Android-only `ScatterChart`
        class** into a new top-level, fully portable
        `chartLibCore/charts/ScatterShape.kt` (same package,
        `info.appdev.charting.charts`, so `chartLib`'s `ScatterChart.kt`
        needed no import change — just deletion of the nested `enum class
        ScatterShape { ... }` block). This is a source-breaking change for
        callers: `ScatterChart.ScatterShape.SQUARE` → `ScatterShape.SQUARE`
        (5 call sites updated: `ScatterDataSetAndroid.kt`,
        `ScatterChartActivity.kt`, `SimpleFragment.kt`,
        `ChartExamples.kt`, `ChartState.kt`).
      - **`shapeRenderer` (on `IScatterDataSet`/`ScatterDataSet`) removed
        from common code entirely**, same side-channel pattern as
        `fillDrawable`/`fillFormatter`: new
        `chartLib/data/ScatterDataSetAndroid.kt` defines
        `var IScatterDataSet.shapeRenderer: IShapeRenderer?` backed by a
        `WeakHashMap<IScatterDataSet, IShapeRenderer?>`. Unlike
        `fillFormatter`'s "null means reset to default" semantics,
        `shapeRenderer` can be legitimately `null` (`ScatterChartRenderer`
        treats a `null` renderer as "skip drawing"), so the getter uses
        `containsKey` rather than `getOrPut` to distinguish "never set"
        (lazily materializes the default `SquareShapeRenderer()`) from
        "explicitly set to null" — `getOrPut` cannot tell those apart when
        the map's value type is itself nullable.
      - **`setScatterShape(shape)`** moved to the same file as an extension
        function on `ScatterDataSet`; **`getRendererForShape(shape)`** moved
        from `ScatterDataSet`'s companion object to a top-level function in
        `chartLib` (source-breaking: `ScatterDataSet.getRendererForShape(...)`
        → `getRendererForShape(...)`, unused elsewhere in this repo).
      - The `renderer/scatter/*` shape-drawing classes themselves
        (`IShapeRenderer` + 7 concrete renderers) stay exactly where they
        are, unchanged — they're Canvas/Paint-coupled drawing logic, same
        permanently-Android-only category as the `Renderer`/`DataRenderer`
        hierarchy from Step A.4, not something to "split".
      - **Moved to `chartLibCore` commonMain**: `data/ScatterDataSet.kt`,
        `interfaces/datasets/IScatterDataSet.kt`. `ScatterDataSet` is now a
        fully portable leaf class — the last of the seven concrete `DataSet`
        classes (Bar/Line/Scatter/Candle/Bubble/Radar/Pie) to move. **The
        concrete `DataSet` family migration that started as a Step A.7
        prerequisite is now complete.**
      - **Disclosed behavior change**: `.copy()` no longer propagates a
        custom `shapeRenderer` to the copy — same category as the
        `fillDrawable`/`fillFormatter` copy-non-propagation disclosed in
        continuation parts 2 and 4. Documented in `ScatterDataSet`'s class
        doc.
      - Verified: `chartLibCore:compileKotlinDesktop`,
        `chartLib:compileDebugKotlin`, full `chartLibCore:allTests`,
        `chartLibCompose:assembleDebug`, `app:assembleDebug`, full
        `./gradlew test`, and `chartLibComposeMultiplatform:build` (all
        targets) all pass.
      - **Next up**: resuming the Step A.7 Compose Multiplatform renderer
        port itself is now viable for every chart type's data model
        (Bar/Line/Scatter/Candle/Bubble/Radar/Pie all have portable
        `DataSet`s); alternatively Step A.8 (gesture handling), A.9
        (`demoKmp`), A.10 (CI), or A.11 (Publishing).
- [x] **Step A.7 (continuation) — first data renderer: non-stacked
      `IBarDataSet` proof-of-concept.** Resumes the Compose Multiplatform
      renderer port now that the concrete `DataSet` family is portable, using
      the same "pure pixel-math function + thin `DrawScope` extension"
      pattern established by the grid-line renderer, applied to a chart
      *data* renderer for the first time.
      - New `chartLibComposeMultiplatform/commonMain/.../renderer/
        BarChartRenderer.kt`: `barPixelRects(dataSet, barWidth, transformer)`
        computes one `[left, top, right, bottom]` value-space rect per
        non-stacked entry (mirroring the non-stacked branch of `chartLib`'s
        `buffer.BarBuffer.feed`) and maps it to pixel space via
        `TransformerCore.pointValuesToPixel` (reusing the point-pair transform
        already used by the grid-line renderer, since a rect's two corners
        are just two (x, y) points); `DrawScope.drawBarChartDataSet(...)`
        draws each rect with `drawRect`, cycling through the dataset's colors
        via `getColorByIndex(index)` the same way
        `BarChartRenderer.drawDataSet` does.
      - **Deliberately out of scope for this slice** (same scoping choice as
        the grid-line proof-of-concept): stacked bars, animation phases
        (`phaseX`/`phaseY`), inverted axis, bar borders/shadows, rounded
        bars, and the `BarData`/`ChartData` container classes (still
        Android-only in `chartLib`) — this renderer operates directly on a
        single `IBarDataSet` plus a caller-supplied `barWidth`, matching how
        `buffer.BarBuffer` itself is parameterized, without requiring
        `BarData` to be ported first.
      - New test `chartLibComposeMultiplatform/commonTest/.../
        BarChartRendererTest.kt`: same 100x100 content-rect setup as
        `AxisGridRendererTest`; asserts a positive-value entry's rect maps to
        the expected content-rect pixel positions, and that a negative-value
        entry's rect keeps pixel-space `top < bottom` (i.e. the Y-axis
        inversion is handled correctly regardless of value sign) — both
        without a Compose UI test harness.
      - Verified: `chartLibComposeMultiplatform:testAndroidHostTest`/
        `desktopTest` first (new tests green: 2/2 on each), then the full
        chain — `chartLibComposeMultiplatform:build` (all targets, including
        `iosSimulatorArm64Test`), `chartLibCore:allTests`,
        `chartLib:compileDebugKotlin`, `chartLibCompose:assembleDebug`,
        `app:assembleDebug`, full `./gradlew test` — all pass.
      - **Not yet done**: stacked/grouped bars and the remaining ~29 renderer
        files for every other chart type (Line/Scatter/Candle/Bubble/Radar/
        Pie data renderers, legend renderer, limit lines, markers, combined
        chart compositing).
- [x] **Step A.8 — Gesture handling proof-of-concept** with
      `pointerInput`/`detectTransformGestures` replacing `ChartTouchListener`/`MotionEvent`.
      Turned out to need very little new common code: `ViewPortHandler`'s
      `zoom`/`translate`/`refresh`/`limitTransAndScale` matrix math (bounds-clamped pan/zoom)
      already lived in `chartLibCore` from earlier steps, so this slice only needed to combine
      Compose's per-frame pan+zoom+centroid gesture callback into a single matrix update.
      - New `chartLibComposeMultiplatform/commonMain/.../gesture/GestureHandling.kt`:
        `chartGestureMatrix(viewPortHandler, pan, zoom, centroid, scaleXEnabled, scaleYEnabled,
        dragXEnabled, dragYEnabled)` copies the current `matrixTouch`, applies
        `postTranslate(pan)` then `postScale(zoom, zoom, centroid)` (mirrors `chartLib`'s
        `BarLineChartTouchListener.performDrag`/`performZoom` combined into one step, since
        Compose reports incremental deltas per frame rather than cumulative-since-gesture-start
        deltas like `MotionEvent`); `Modifier.chartTransformGestures(viewPortHandler, ...,
        onGesture)` wires `detectTransformGestures` to it and calls
        `ViewPortHandler.refresh(matrix, onGesture, true)` (which also applies the existing
        `limitTransAndScale` bounds clamping).
      - **Deliberately out of scope for this proof-of-concept** (same scoping choice as the
        earlier axis/data renderer slices): rotation gestures, independent X/Y-only zoom modes
        (`X_ZOOM`/`Y_ZOOM`), highlight-on-drag, double-tap zoom, fling/deceleration, and
        `OnChartGestureListener` callbacks — `chartLib`'s `BarLineChartTouchListener` handles
        all of these but they're not needed to prove the Compose gesture wiring works.
      - New test `chartLibComposeMultiplatform/commonTest/.../GestureHandlingTest.kt`: verifies
        `chartGestureMatrix`'s pure matrix math directly (pan-only, zoom-about-centroid-only,
        and disabled-axis cases), reading back `MTRANS_X`/`MTRANS_Y`/`MSCALE_X`/`MSCALE_Y` via
        `Matrix.getValues` — no `ViewPortHandler.refresh`/clamping involved, since the clamp
        bounds depend on chart state in ways that would make the pure-math assertions less
        direct (same "test the pure function before the stateful wiring" approach as the
        renderer tests).
      - Verified: `chartLibComposeMultiplatform:testAndroidHostTest`/`desktopTest` first (new
        tests green: 3/3 on each), then the full chain —
        `chartLibComposeMultiplatform:build` (all targets, including `iosSimulatorArm64Test`),
        `chartLibCore:allTests`, `chartLib:compileDebugKotlin`, `chartLibCompose:assembleDebug`,
        `app:assembleDebug`, full `./gradlew test` — all pass.
      - **Not yet done**: wiring `chartTransformGestures` into an actual composable chart
        (there is no interactive Compose Multiplatform chart screen yet, since Step A.9's
        `demoKmp` app doesn't exist), and the out-of-scope gesture features listed above.
- [x] **Step A.9 — `demoKmp` Compose Multiplatform demo app** (Android, iOS, Desktop).
      Ties together every Compose Multiplatform proof-of-concept from Steps A.7/A.8 into one
      actually-runnable `@Composable` shown on all three platforms: axis grid lines
      (`drawXAxisGridLines`/`drawYAxisGridLines`), a bar chart data renderer
      (`drawBarChartDataSet`), and pan/pinch-zoom gestures (`chartTransformGestures`) — all from
      `chartLibComposeMultiplatform`, none from the Android-only `chartLib`/`chartLibCompose`.
      - **Two Gradle modules instead of one**, discovered while implementing this slice: AGP
        9's new KMP DSL forbids combining `com.android.application` with
        `org.jetbrains.kotlin.multiplatform`'s `androidTarget()` in the same project (a hard
        error, not just a warning), so the plan's original single-module sketch needed
        splitting:
        - **`demoKmp`** (new, `com.android.kotlin.multiplatform.library` — same shape as
          `chartLibComposeMultiplatform`): `commonMain/DemoKmpApp.kt` holds the shared
          `@Composable` (a `BarDataSet` of 5 entries, rendered via the Step A.7 grid-line +
          bar-chart renderers inside a `Canvas`, with `Modifier.chartTransformGestures(...)`
          from Step A.8 attached for interactivity); `desktopMain/Main.kt` (`fun main() =
          application { Window(...) { DemoKmpApp() } }`, also exposed as `:demoKmp:run`);
          `iosMain/MainViewController.kt` (`ComposeUIViewController { DemoKmpApp() }`, for a
          future thin Xcode wrapper to embed — see "not yet done" below).
        - **`demoKmpAndroid`** (new, plain `com.android.application`, *not* multiplatform):
          just `MainActivity`/`AndroidManifest.xml`/`applicationId`/launcher-activity
          boilerplate, depending on `project(":demoKmp")` for `DemoKmpApp()` and the shared
          renderer/gesture code.
      - Both modules registered in `settings.gradle.kts`
        (`include(":demoKmp")`/`include(":demoKmpAndroid")`).
      - `demoKmp`'s `compileSdk` had to be `37` (not `36`, matching
        `chartLibComposeMultiplatform`): Compose Multiplatform 1.12.1's Android artifacts
        require it; `demoKmpAndroid` needed the same bump plus its own distinct
        `namespace` (`info.appdev.charting.demokmp.androidapp`, vs. `demoKmp`'s
        `info.appdev.charting.demokmp`) since AGP's manifest merger rejects two modules in the
        same dependency graph sharing one namespace.
      - Verified: `:demoKmpAndroid:assembleDebug` (Android APK), `:demoKmp:compileKotlinDesktop`
        and a manual `:demoKmp:run` smoke-test (desktop app launches without exceptions),
        `:demoKmp:compileKotlinIosSimulatorArm64`, and the full `:demoKmp:build` (all targets,
        including linking both debug and release `iosArm64`/`iosSimulatorArm64` frameworks) all
        pass. Full existing chain (`chartLibComposeMultiplatform:build`, `chartLibCore:allTests`,
        `chartLib:compileDebugKotlin`, `chartLibCompose:assembleDebug`, `app:assembleDebug`,
        `./gradlew test`) also still green.
      - **Not yet done**: an actual Xcode wrapper project (`.xcodeproj`) embedding
        `MainViewController()` on a real iOS device/simulator screen (only the Gradle-buildable
        `iosArm64`/`iosSimulatorArm64` framework artifact exists so far); a `wasmJsMain` target;
        richer demo content (multiple chart types/screens, matching `chartLibCompose`'s
        `examples/ChartExamples.kt` breadth) — intentionally deferred until more renderers exist
        per Step A.7's remaining ~29-file scope.
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

> **Correction found while executing step 1 (see the Status changelog above):**
> `interfaces/datasets/IDataSet.valueFormatter` is typed `IValueFormatter`, whose
> `getFormattedValue(...)` takes a `ViewPortHandler?`, and
> `ILineDataSet.fillFormatter` is typed `IFillFormatter`, whose
> `getFillLinePosition(...)` takes a `LineDataProvider` (`interfaces/dataprovider`).
> This means step 1 (`data/`+`interfaces/`) **cannot fully complete before** step 5
> (`ViewPortHandler`/`Transformer`) at least partially lands, and before a
> common-friendly design exists for `interfaces/dataprovider` (which today exposes
> `RectF`/`Transformer` and is tightly coupled to the concrete `View`-based chart
> classes). The enums/typedefs that *don't* transitively depend on these
> (`AxisDependency`, `LegendForm`, `PaintStyle`, `ChartTypeface`, `DashEffect`,
> `PointF`, `chartDensity`/`convertDpToPixel`) were still landed in `chartLibCore`
> as groundwork, and applied to their Android-only call sites in `chartLib` where
> that was safe to do without moving the whole class. `IBarDataSet`/`BarDataSet`
> (via `utils/Fill.kt`) and `IScatterDataSet`/`ScatterDataSet` (via
> `renderer/scatter/IShapeRenderer`) have their own independent blockers on top of
> this and will need dedicated abstraction work regardless of when steps 1/5
> otherwise complete.

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

- Two Gradle modules (split forced by AGP 9's new KMP DSL, which forbids
  `com.android.application` + `androidTarget()` in the same project):
  - `demoKmp` (`com.android.kotlin.multiplatform.library`, same shape as
    `chartLibComposeMultiplatform`): `commonMain` (shared `DemoKmpApp()` composable
    using the Step A.7/A.8 renderers/gestures), `desktopMain` (`main()` launching a
    `ComposeWindow`, also runnable via `:demoKmp:run`), `iosMain`
    (`MainViewController()` via `ComposeUIViewController`).
  - `demoKmpAndroid` (plain `com.android.application`): `MainActivity` +
    `AndroidManifest.xml` + `applicationId`, depending on `project(":demoKmp")`.
- Registered in `settings.gradle.kts` as `include(":demoKmp")` and
  `include(":demoKmpAndroid")`.
- Not yet done: an actual Xcode wrapper project embedding `MainViewController()`,
  a `wasmJsMain` target, and richer demo content (multiple chart types/screens).

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
