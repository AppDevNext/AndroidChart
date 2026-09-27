package info.appdev.charting.components

import android.graphics.Paint
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.calcTextWidth
import info.appdev.charting.utils.convertDpToPixel
import kotlin.math.max
import kotlin.math.min

/**
 * This is for normal (not horizontal) charts horizontal spacing.
 *
 * Android-only extension since text measurement requires `android.graphics.Paint`.
 */
fun YAxis.getRequiredWidthSpace(p: Paint): Float {
    p.textSize = textSize

    val label = getLongestLabel(p)
    var width = p.calcTextWidth(label).toFloat() + xOffset * 2f

    var minWidth = this.minWidth
    var maxWidth = this.maxWidth

    if (minWidth > 0f) minWidth = minWidth.convertDpToPixel()

    if (maxWidth > 0f && maxWidth != Float.POSITIVE_INFINITY) maxWidth = maxWidth.convertDpToPixel()

    width = max(minWidth, min(width, if (maxWidth > 0.0) maxWidth else width))

    return width
}

/**
 * This is for HorizontalBarChart vertical spacing.
 *
 * Android-only extension since text measurement requires `android.graphics.Paint`.
 */
fun YAxis.getRequiredHeightSpace(p: Paint): Float {
    p.textSize = textSize

    val label = getLongestLabel(p)
    return p.calcTextHeight(label).toFloat() + yOffset * 2f
}
