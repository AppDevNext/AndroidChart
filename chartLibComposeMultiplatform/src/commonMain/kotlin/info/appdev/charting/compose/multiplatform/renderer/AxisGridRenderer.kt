package info.appdev.charting.compose.multiplatform.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.utils.TransformerCore
import info.appdev.charting.utils.ViewPortHandler

/**
 * Proof-of-concept Compose Multiplatform renderer (Step A.7): draws the X-axis vertical
 * grid lines using only portable `chartLibCore` types ([XAxis], [ViewPortHandler],
 * [TransformerCore]), rendering through Compose's [DrawScope] instead of
 * `android.graphics.Canvas`.
 *
 * Mirrors the pixel math of `chartLib`'s `XAxisRenderer.renderGridLines`/`drawGridLine`:
 * one vertical line per axis entry (or specific position), transformed from axis-value
 * space to pixel space and clipped to the viewport's content rect.
 */
/**
 * Computes the pixel-space vertical grid line x-positions for [xAxis] via [transformer]
 * (axis-value space -> pixel space). Extracted as a pure function, separate from the
 * [DrawScope] drawing itself, so the coordinate math can be unit-tested without needing
 * a Compose UI test harness.
 */
fun xAxisGridPixelPositions(xAxis: XAxis, transformer: TransformerCore): FloatArray {
    val values = if (xAxis.isShowSpecificPositions) xAxis.specificPositions else xAxis.entries
    val positions = FloatArray(values.size * 2)
    for (i in values.indices) {
        positions[i * 2] = values[i]
        positions[i * 2 + 1] = values[i]
    }
    transformer.pointValuesToPixel(positions)
    return positions
}

fun DrawScope.drawXAxisGridLines(
    xAxis: XAxis,
    viewPortHandler: ViewPortHandler,
    transformer: TransformerCore
) {
    if (!xAxis.isDrawGridLines || !xAxis.isEnabled) return

    val positions = xAxisGridPixelPositions(xAxis, transformer)

    val clip = viewPortHandler.contentRect
    val color = Color(xAxis.gridColor)
    clipRect(clip.left, clip.top, clip.right, clip.bottom) {
        var i = 0
        while (i < positions.size) {
            val x = positions[i]
            drawLine(
                color = color,
                start = Offset(x, viewPortHandler.contentBottom()),
                end = Offset(x, viewPortHandler.contentTop()),
                strokeWidth = xAxis.gridLineWidth
            )
            i += 2
        }
    }
}

/**
 * Computes the pixel-space horizontal grid line y-positions for [yAxis] via [transformer]
 * (axis-value space -> pixel space). Extracted as a pure function for the same testability
 * reason as [xAxisGridPixelPositions].
 */
fun yAxisGridPixelPositions(yAxis: YAxis, transformer: TransformerCore): FloatArray {
    val values = if (yAxis.isShowSpecificPositions) yAxis.specificPositions else yAxis.entries
    val positions = FloatArray(values.size * 2)
    for (i in values.indices) {
        // only fill y values, x values are not needed for y grid lines
        positions[i * 2 + 1] = values[i]
    }
    transformer.pointValuesToPixel(positions)
    return positions
}

/**
 * Draws the Y-axis horizontal grid lines, mirroring `chartLib`'s
 * `YAxisRenderer.renderGridLines`/`linePath`/`transformedPositions`: one horizontal line
 * per axis entry (or specific position), transformed from axis-value space to pixel space
 * and clipped to the viewport's content rect.
 */
fun DrawScope.drawYAxisGridLines(
    yAxis: YAxis,
    viewPortHandler: ViewPortHandler,
    transformer: TransformerCore
) {
    if (!yAxis.isEnabled || !yAxis.isDrawGridLines) return

    val positions = yAxisGridPixelPositions(yAxis, transformer)

    val clip = viewPortHandler.contentRect
    val color = Color(yAxis.gridColor)
    clipRect(clip.left, clip.top, clip.right, clip.bottom) {
        var i = 0
        while (i < positions.size) {
            val y = positions[i + 1]
            drawLine(
                color = color,
                start = Offset(viewPortHandler.offsetLeft(), y),
                end = Offset(viewPortHandler.contentRight(), y),
                strokeWidth = yAxis.gridLineWidth
            )
            i += 2
        }
    }
}
