package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Path
import info.appdev.charting.charts.RadarChart
import info.appdev.charting.components.YAxis
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.getPosition
import info.appdev.charting.utils.roundToNextSignificant
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.nextUp
import kotlin.math.pow

class YAxisRendererRadarChart(viewPortHandler: ViewPortHandler, yAxis: YAxis, private val chart: RadarChart) : YAxisRenderer(viewPortHandler, yAxis, null) {

    private val renderLimitLinesPathBuffer = Path()

    override fun computeAxisValues(min: Float, max: Float) {

        val labelCount = axis.labelCount
        val range = abs((max - min).toDouble())

        if (labelCount == 0 || range <= 0 || java.lang.Double.isInfinite(range)) {
            axis.entries = floatArrayOf()
            axis.centeredEntries = floatArrayOf()
            axis.entryCount = 0
            return
        }

        // Find out how much spacing (in y value space) between axis values
        val rawInterval = range / labelCount
        var interval = rawInterval.roundToNextSignificant().toDouble()

        // If granularity is enabled, then do not allow the interval to go below specified granularity.
        // This is used to avoid repeated values when rounding values for display.
        if (axis.isGranularityEnabled) interval = if (interval < axis.granularity) axis.granularity.toDouble() else interval

        // Normalize interval
        val intervalMagnitude = 10.0.pow(log10(interval).toInt().toDouble()).roundToNextSignificant().toDouble()
        val intervalSigDigit = (interval / intervalMagnitude).toInt()
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            // if it's 0.0 after floor(), we use the old value
            interval = if (floor(10.0 * intervalMagnitude) == 0.0)
                interval
            else floor(10.0 * intervalMagnitude)
        }

        val centeringEnabled = axis.isCenterAxisLabelsEnabled
        var n = if (centeringEnabled) 1 else 0

        // force label count
        if (axis.isForceLabelsEnabled) {
            val step = range.toFloat() / (labelCount - 1).toFloat()
            axis.entryCount = labelCount

            if (axis.entries.size < labelCount) {
                // Ensure stops contains at least numStops elements.
                axis.entries = FloatArray(labelCount)
            }

            var v = min

            for (i in 0..<labelCount) {
                axis.entries[i] = v
                v += step
            }

            n = labelCount

            // no forced count
        } else {
            var first = if (interval == 0.0) 0.0 else ceil(min / interval) * interval
            if (centeringEnabled) {
                first -= interval
            }

            val last = if (interval == 0.0) 0.0 else (floor(max / interval) * interval).nextUp()

            var f: Double

            if (interval != 0.0) {
                f = first
                while (f <= last) {
                    ++n
                    f += interval
                }
            }

            n++

            axis.entryCount = n

            if (axis.entries.size < n) {
                // Ensure stops contains at least numStops elements.
                axis.entries = FloatArray(n)
            }

            f = first
            var i = 0
            while (i < n) {
                if (f == 0.0)  // Fix for negative zero case (Where value == -0.0, and 0.0 == -0.0)
                    f = 0.0

                axis.entries[i] = f.toFloat()
                f += interval
                ++i
            }
        }

        // set decimals
        if (interval < 1) {
            axis.mDecimals = ceil(-log10(interval)).toInt()
        } else {
            axis.mDecimals = 0
        }

        if (centeringEnabled) {
            if (axis.centeredEntries.size < n) {
                axis.centeredEntries = FloatArray(n)
            }

            val offset = (axis.entries[1] - axis.entries[0]) / 2f

            for (i in 0..<n) {
                axis.centeredEntries[i] = axis.entries[i] + offset
            }
        }

        axis.mAxisMinimum = axis.entries[0]
        axis.mAxisMaximum = axis.entries[n - 1]
        axis.mAxisRange = abs((axis.mAxisMaximum - axis.mAxisMinimum).toDouble()).toFloat()
    }

    override fun renderAxisLabels(canvas: Canvas) {
        if (!yAxis.isEnabled || !yAxis.isDrawLabelsEnabled)
            return

        paintAxisLabels.typeface = yAxis.typeface
        paintAxisLabels.textSize = yAxis.textSize
        paintAxisLabels.color = yAxis.textColor

        val center = chart.centerOffsets
        var pOut = PointF.getInstance(0f, 0f)
        val factor = chart.factor

        val from = if (yAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
        val to = if (yAxis.isDrawTopYLabelEntryEnabled)
            yAxis.entryCount
        else
            (yAxis.entryCount - 1)

        val xOffset = yAxis.labelXOffset

        for (j in from..<to) {
            val r = (yAxis.entries[j] - yAxis.mAxisMinimum) * factor

            pOut = center.getPosition(r, chart.rotationAngle)

            val label = yAxis.getFormattedLabel(j)

            label?.let { canvas.drawText(it, pOut.x + xOffset, pOut.y, paintAxisLabels) }
        }
        PointF.recycleInstance(center)
        PointF.recycleInstance(pOut)
    }

    override fun renderLimitLines(canvas: Canvas) {
        val limitLines = yAxis.limitLines

        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor

        val center = chart.centerOffsets
        var pOut = PointF.getInstance(0f, 0f)
        for (i in limitLines.indices) {
            val limitLine = limitLines[i]

            if (!limitLine.isEnabled) continue

            limitLinePaint.color = limitLine.lineColor
            limitLinePaint.pathEffect = limitLine.dashPathEffect
            limitLinePaint.strokeWidth = limitLine.lineWidth

            val r = (limitLine.limit - chart.yChartMin) * factor

            val limitPath = renderLimitLinesPathBuffer
            limitPath.reset()

            chart.getData()!!.maxEntryCountSet?.let { maxEntryCountSet ->
                for (j in 0..<maxEntryCountSet.entryCount) {
                    pOut = center.getPosition(r, sliceAngle * j + chart.rotationAngle)

                    if (j == 0)
                        limitPath.moveTo(pOut.x, pOut.y)
                    else
                        limitPath.lineTo(pOut.x, pOut.y)
                }
            }
            limitPath.close()

            canvas.drawPath(limitPath, limitLinePaint)
        }
        PointF.recycleInstance(center)
        PointF.recycleInstance(pOut)
    }
}
