package info.appdev.charting.utils

/**
 * Converts the platform-independent [RectF] to an `android.graphics.RectF`, for the call sites
 * that must hand a rect to native Android Canvas/Path APIs.
 */
fun RectF.toAndroidRectF(): android.graphics.RectF = android.graphics.RectF(left, top, right, bottom)

/**
 * Copies the values of this platform-independent [RectF] into an existing `android.graphics.RectF`.
 */
fun RectF.copyInto(target: android.graphics.RectF) {
    target.set(left, top, right, bottom)
}

/**
 * Converts an `android.graphics.RectF` to the platform-independent [RectF].
 */
fun android.graphics.RectF.toCommonRectF(): RectF = RectF(left, top, right, bottom)

/**
 * Copies the values of this `android.graphics.RectF` into an existing platform-independent [RectF].
 */
fun android.graphics.RectF.copyInto(target: RectF) {
    target.set(left, top, right, bottom)
}
