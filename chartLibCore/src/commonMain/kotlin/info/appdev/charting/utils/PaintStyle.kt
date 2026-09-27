package info.appdev.charting.utils

/**
 * Platform-independent replacement for `android.graphics.Paint.Style`.
 * Used by the data model (e.g. [info.appdev.charting.interfaces.datasets.ICandleDataSet])
 * to describe how a shape should be painted, without depending on `android.graphics.Paint`.
 */
enum class PaintStyle {
    FILL,
    STROKE,
    FILL_AND_STROKE
}
