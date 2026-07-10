# GitHub Copilot Instructions

## Project overview

AndroidChart is a 100% Kotlin chart library for Android, available in two flavors:
- `chartLib` — traditional Android View-based charts
- `chartLibCompose` — Jetpack Compose charts

Package name: `info.appdev.charting`

### Modules
| Module | Purpose |
|---|---|
| `chartLib` | View-based chart library (main library) |
| `chartLibCompose` | Jetpack Compose chart library |
| `app` | Demo/example app |
| `lint` | Custom lint rules enforcing library conventions |

## Tech stack

- **Language**: 100% Kotlin — no Java source files
- **Android**: minSdk 23, compileSdk 35
- **JVM target**: 17
- **Kotlin**: 2.4.0
- **Build**: Gradle with Kotlin DSL (`.gradle.kts` files only)
- **AGP**: 9.2.1

## Naming conventions

These are breaking changes from the original MPAndroidChart — always use the new names:

| Old (wrong) | New (correct) |
|---|---|
| `Entry` | `EntryFloat` or `EntryDouble` |
| `BarEntry` | `BarEntryFloat` or `BarEntryDouble` |
| `BubbleEntry` | `BubbleEntryFloat` |
| `PieEntry` | `PieEntryFloat` |
| `RadarEntry` | `RadarEntryFloat` |
| `CandleEntry` | `CandleEntryFloat` |
| `setSomethingEnabled(true)` | `isSomething = true` (Kotlin property) |
| `MPPointF` | `PointF` |
| `MPPointD` | `PointD` |

## Chart types

`BarChart`, `LineChart`, `PieChart`, `RadarChart`, `BubbleChart`, `CandleStickChart`, `ScatterChart`, `CombinedChart`, `HorizontalBarChart`, `GanttChart`

## Code guidelines

### Kotlin idioms
- Use Kotlin properties, not Java-style getters/setters
- Prefer `?.let`, `?:`, `?: return`, `?: continue` for null handling
- Use `for (x in collection)` loops; use `while` only when index manipulation is required

### Null safety and Java interop
- `Array<LegendEntry>` and similar arrays can contain `null` at runtime due to Java interop even when typed as non-nullable. Always guard with `if (entry == null) continue` in loops over such arrays.
- When iterating indexed `while` loops that skip nulls, always increment `i` before `continue`: `if (e == null) { i++; continue }`

### Bitmap
- Always use `Bitmap.Config.ARGB_8888` — `ARGB_4444` is deprecated
- Use `createBitmap(width, height, Bitmap.Config.ARGB_8888)` directly; no need for a local `conf` variable

### Deprecated Android APIs
- `Bitmap.Config.ARGB_4444` → use `ARGB_8888`
- `CompressFormat.WEBP` is deprecated (API 30+: `WEBP_LOSSLESS` / `WEBP_LOSSY`). Since minSdk is 23, suppress the deprecation with `@Suppress("DEPRECATION")` on the enclosing `when` statement rather than referencing the API-30-only replacements directly

### Custom lint rules
The `lint` module enforces two rules — do not work around them:
- `RawTypeDataSetDetector` — raw generic `DataSet` types are forbidden
- `EntryUsageDetector` — old `Entry`/`BarEntry`/etc. class names are forbidden; use the typed variants above

## Testing

- **Unit tests**: JUnit 4 + Mockito, located in `chartLib/src/test/kotlin/`
  - Run with: `./gradlew :chartLib:test`
- **Instrumentation tests**: Espresso, located in `app/src/androidTest/kotlin/`
  - Run with: `./gradlew cAT` (requires emulator, API 28)
- CI runs instrumentation tests on an API 28 x86_64 emulator via GitHub Actions

## Publishing

Distributed via:
- **JitPack**: `com.github.AppDevNext.AndroidChart:chartLib:VERSION`
- **Maven Central (snapshot)**: `info.mxtracks:chart:VERSION-SNAPSHOT`

Publishing is handled by `com.vanniktech.maven.publish`. Version is derived from git tags via `getVersionText()` from `buildSrc`.

## Project status

Maintenance mode — issues are not actively addressed, but pull requests are reviewed. Keep changes minimal and focused.
