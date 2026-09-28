package info.appdev.charting.utils

/**
 * Platform-independent placeholder for a font/typeface handle used to draw value labels.
 *
 * On Android this is a transparent type alias for [android.graphics.Typeface], so existing
 * Android code that reads/writes `Typeface` values via the data model keeps working unchanged.
 */
expect open class ChartTypeface
