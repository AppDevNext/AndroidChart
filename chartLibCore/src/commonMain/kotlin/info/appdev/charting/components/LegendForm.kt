package info.appdev.charting.components

/**
 * Describes how a DataSet's legend entry ("form") should be drawn.
 *
 * Platform-independent home for this enum; [info.appdev.charting.components.Legend]
 * (Android-only for now) exposes it as a nested `Legend.LegendForm` type alias
 * to preserve the existing public API.
 */
enum class LegendForm {
    /**
     * Avoid drawing a form
     */
    NONE,

    /**
     * Do not draw the form, but leave space for it
     */
    EMPTY,

    /**
     * Use default (default dataset's form to the legend's form)
     */
    DEFAULT,

    /**
     * Draw a square
     */
    SQUARE,

    /**
     * Draw a circle
     */
    CIRCLE,

    /**
     * Draw a horizontal line
     */
    LINE
}
