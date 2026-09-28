package info.appdev.charting.components

import android.graphics.Paint

/**
 * Returns the longest formatted label (in terms of px), this axis
 * contains. If paint is null, then returns the longest formatted label
 * (in terms of characters), this axis contains.
 *
 * Android-only extension since text width measurement requires
 * `android.graphics.Paint`.
 */
fun AxisBase.getLongestLabel(p: Paint?): String {
    if (p == null) {
        return this.longestLabel
    }
    var longest: String? = ""
    val max = 0f

    for (i in entries.indices) {
        val text = getFormattedLabel(i)
        if (text != null) {
            val width = p.measureText(text)
            if (max < width) {
                longest = text
            }
        }
    }

    return longest!!
}
