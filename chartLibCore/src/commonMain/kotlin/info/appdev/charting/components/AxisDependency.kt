package info.appdev.charting.components

/**
 * Specifies which y-axis (LEFT or RIGHT) a DataSet should be plotted against.
 *
 * Platform-independent home for this enum; [info.appdev.charting.components.YAxis]
 * (Android-only for now) exposes it as a nested `YAxis.AxisDependency` type alias
 * to preserve the existing public API.
 */
enum class AxisDependency {
    LEFT,
    RIGHT
}
