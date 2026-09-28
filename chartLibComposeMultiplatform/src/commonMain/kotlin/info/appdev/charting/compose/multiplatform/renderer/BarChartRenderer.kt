package info.appdev.charting.compose.multiplatform.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.TransformerCore

/**
 * Proof-of-concept Compose Multiplatform data renderer (Step A.7): draws a single, non-stacked
 * [IBarDataSet] using only portable `chartLibCore` types, rendering through Compose's
 * [DrawScope] instead of `android.graphics.Canvas`/`BarBuffer`.
 *
 * Mirrors the non-stacked-bar branch of `chartLib`'s `buffer.BarBuffer.feed`: for every entry,
 * a `[left, top, right, bottom]` value-space rect is built from the entry's `x`/`y` and the
 * dataset's bar width, then mapped to pixel space via [TransformerCore.pointValuesToPixel].
 * Stacked bars, animation phases, and bar borders/shadows are intentionally out of scope for
 * this proof-of-concept slice (same scoping choice as the earlier grid-line renderer).
 */

/**
 * Computes the pixel-space bar rects (four floats per entry: left, top, right, bottom) for
 * every non-stacked entry of [dataSet]. Extracted as a pure function, separate from the
 * [DrawScope] drawing itself, so the coordinate math can be unit-tested without needing a
 * Compose UI test harness.
 */
fun barPixelRects(dataSet: IBarDataSet, barWidth: Float, transformer: TransformerCore): FloatArray {
    val barWidthHalf = barWidth / 2f
    val positions = FloatArray(dataSet.entryCount * 4)

    for (i in 0 until dataSet.entryCount) {
        val entry = dataSet.getEntryForIndex(i) ?: continue
        val x = entry.x
        val y = entry.y

        val top = if (y >= 0) y else 0f
        val bottom = if (y <= 0) y else 0f

        val offset = i * 4
        positions[offset] = x - barWidthHalf
        positions[offset + 1] = top
        positions[offset + 2] = x + barWidthHalf
        positions[offset + 3] = bottom
    }

    // pointValuesToPixel maps (x, y) pairs, so each [left, top, right, bottom] rect is
    // transformed as its two corner points: (left, top) and (right, bottom).
    transformer.pointValuesToPixel(positions)
    return positions
}

/**
 * Draws every non-stacked entry of [dataSet] as a filled rectangle, cycling through the
 * dataset's colors the same way `chartLib`'s `BarChartRenderer.drawDataSet` does via
 * `dataSet.getColorByIndex(index)`.
 */
fun DrawScope.drawBarChartDataSet(
    dataSet: IBarDataSet,
    barWidth: Float,
    transformer: TransformerCore
) {
    val positions = barPixelRects(dataSet, barWidth, transformer)

    var entryIndex = 0
    var i = 0
    while (i < positions.size) {
        val left = positions[i]
        val top = positions[i + 1]
        val right = positions[i + 2]
        val bottom = positions[i + 3]

        val color = Color(dataSet.getColorByIndex(entryIndex))
        drawRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top)
        )

        i += 4
        entryIndex++
    }
}
