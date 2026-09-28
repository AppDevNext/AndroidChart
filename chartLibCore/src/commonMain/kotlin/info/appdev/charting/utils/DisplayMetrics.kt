package info.appdev.charting.utils

/**
 * Current display density scale factor (pixels per dp), used by [convertDpToPixel] to convert
 * dp values to pixels. Must be set once per platform before dp-based sizes are used
 * (e.g. via `Context.initUtils()` on Android). Defaults to 1f (no scaling) so that non-Android
 * targets still produce a sane (if unscaled) result before an explicit density is provided.
 */
var chartDensity: Float = 1f

/**
 * Converts a dp (density-independent pixels) value to pixels, depending on [chartDensity].
 */
fun Float.convertDpToPixel(): Float = this * chartDensity
