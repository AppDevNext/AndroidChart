package info.appdev.charting.renderer

import android.graphics.Canvas
import info.appdev.charting.charts.RadarChart
import info.appdev.charting.components.XAxis
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.Utils
import info.appdev.charting.utils.ViewPortHandler

class XAxisRendererRadarChart(viewPortHandler: ViewPortHandler, xAxis: XAxis, private val chart: RadarChart) : XAxisRenderer(viewPortHandler, xAxis, null) {
    override fun renderAxisLabels(canvas: Canvas) {
        if (!xAxis.isEnabled || !xAxis.isDrawLabelsEnabled)
            return

        val labelRotationAngleDegrees = xAxis.labelRotationAngle
        val drawLabelAnchor = PointF.getInstance(0.5f, 0.25f)

        paintAxisLabels.typeface = xAxis.typeface
        paintAxisLabels.textSize = xAxis.textSize
        paintAxisLabels.color = xAxis.textColor

        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor

        val center = chart.centerOffsets
        val pOut = PointF.getInstance(0f, 0f)
        chart.getData()!!.maxEntryCountSet?.let { maxEntryCountSet ->
            for (i in 0..<maxEntryCountSet.entryCount) {
                val label = xAxis.valueFormatter?.getFormattedValue(i.toFloat(), xAxis)

                val angle = (sliceAngle * i + chart.rotationAngle) % 360f

                Utils.getPosition(
                    center, chart.yRange * factor + xAxis.mLabelWidth / 2f, angle, pOut
                )

                drawLabel(
                    canvas, label, pOut.x, pOut.y - xAxis.mLabelHeight / 2f,
                    drawLabelAnchor, labelRotationAngleDegrees
                )
            }
        }
        PointF.recycleInstance(center)
        PointF.recycleInstance(pOut)
        PointF.recycleInstance(drawLabelAnchor)
    }

    /**
     * XAxis LimitLines on RadarChart not yet supported.
     */
    override fun renderLimitLines(canvas: Canvas) = Unit
}
