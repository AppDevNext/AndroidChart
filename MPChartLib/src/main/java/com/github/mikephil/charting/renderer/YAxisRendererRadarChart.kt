package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Path
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

open class YAxisRendererRadarChart(viewPortHandler: ViewPortHandler, yAxis: YAxis, private val mChart: RadarChart) : YAxisRenderer(viewPortHandler, yAxis, null) {
    override fun computeAxisValues(min: Float, max: Float) {
        val yMin = min
        val yMax = max

        val labelCount = mAxis.labelCount
        val range = abs((yMax - yMin).toDouble())

        if (labelCount == 0 || range <= 0 || java.lang.Double.isInfinite(range)) {
            mAxis.mEntries = floatArrayOf()
            mAxis.mCenteredEntries = floatArrayOf()
            mAxis.mEntryCount = 0
            return
        }

        // Find out how much spacing (in y value space) between axis values
        val rawInterval = range / labelCount
        var interval = Utils.roundToNextSignificant(rawInterval).toDouble()

        // If granularity is enabled, then do not allow the interval to go below specified granularity.
        // This is used to avoid repeated values when rounding values for display.
        if (mAxis.isGranularityEnabled) interval = if (interval < mAxis.granularity) mAxis.granularity.toDouble() else interval

        // Normalize interval
        val intervalMagnitude = Utils.roundToNextSignificant(10.0.pow(log10(interval).toInt().toDouble())).toDouble()
        val intervalSigDigit = (interval / intervalMagnitude).toInt()
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            // if it's 0.0 after floor(), we use the old value
            interval = if (floor(10.0 * intervalMagnitude) == 0.0)
                interval
            else floor(10.0 * intervalMagnitude)
        }

        val centeringEnabled = mAxis.isCenterAxisLabelsEnabled
        var n = if (centeringEnabled) 1 else 0

        // force label count
        if (mAxis.isForceLabelsEnabled) {
            val step = range.toFloat() / (labelCount - 1).toFloat()
            mAxis.mEntryCount = labelCount

            if (mAxis.mEntries.size < labelCount) {
                // Ensure stops contains at least numStops elements.
                mAxis.mEntries = FloatArray(labelCount)
            }

            var v = min

            for (i in 0..<labelCount) {
                mAxis.mEntries[i] = v
                v += step
            }

            n = labelCount

            // no forced count
        } else {
            var first = if (interval == 0.0) 0.0 else ceil(yMin / interval) * interval
            if (centeringEnabled) {
                first -= interval
            }

            val last = if (interval == 0.0) 0.0 else Utils.nextUp(floor(yMax / interval) * interval)

            var f: Double

            if (interval != 0.0) {
                f = first
                while (f <= last) {
                    ++n
                    f += interval
                }
            }

            n++

            mAxis.mEntryCount = n

            if (mAxis.mEntries.size < n) {
                // Ensure stops contains at least numStops elements.
                mAxis.mEntries = FloatArray(n)
            }

            f = first
            var i = 0
            while (i < n) {
                if (f == 0.0)  // Fix for negative zero case (Where value == -0.0, and 0.0 == -0.0)
                    f = 0.0

                mAxis.mEntries[i] = f.toFloat()
                f += interval
                ++i
            }
        }

        // set decimals
        if (interval < 1) {
            mAxis.mDecimals = ceil(-log10(interval)).toInt()
        } else {
            mAxis.mDecimals = 0
        }

        if (centeringEnabled) {
            if (mAxis.mCenteredEntries.size < n) {
                mAxis.mCenteredEntries = FloatArray(n)
            }

            val offset = (mAxis.mEntries[1] - mAxis.mEntries[0]) / 2f

            for (i in 0..<n) {
                mAxis.mCenteredEntries[i] = mAxis.mEntries[i] + offset
            }
        }

        mAxis.mAxisMinimum = mAxis.mEntries[0]
        mAxis.mAxisMaximum = mAxis.mEntries[n - 1]
        mAxis.mAxisRange = abs((mAxis.mAxisMaximum - mAxis.mAxisMinimum).toDouble()).toFloat()
    }

    override fun renderAxisLabels(c: Canvas) {
        if (!mYAxis.isEnabled || !mYAxis.isDrawLabelsEnabled) return

        paintAxisLabels!!.setTypeface(mYAxis.typeface)
        paintAxisLabels!!.textSize = mYAxis.textSize
        paintAxisLabels!!.color = mYAxis.textColor

        val center = mChart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)
        val factor = mChart.factor

        val from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
        val to = if (mYAxis.isDrawTopYLabelEntryEnabled)
            mYAxis.mEntryCount
        else
            (mYAxis.mEntryCount - 1)

        val xOffset = mYAxis.labelXOffset

        for (j in from..<to) {
            val r = (mYAxis.mEntries[j] - mYAxis.mAxisMinimum) * factor

            Utils.getPosition(center, r, mChart.rotationAngle, pOut)

            val label = mYAxis.getFormattedLabel(j)

            c.drawText(label, pOut.x + xOffset, pOut.y, paintAxisLabels!!)
        }
        MPPointF.recycleInstance(center)
        MPPointF.recycleInstance(pOut)
    }

    private val mRenderLimitLinesPathBuffer = Path()

    override fun renderLimitLines(c: Canvas) {
        val limitLines = mYAxis.limitLines ?: return

        val sliceangle = mChart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = mChart.factor

        val center = mChart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)
        for (i in limitLines.indices) {
            val l = limitLines[i]

            if (!l.isEnabled) continue

            mLimitLinePaint!!.color = l.lineColor
            mLimitLinePaint!!.setPathEffect(l.dashPathEffect)
            mLimitLinePaint!!.strokeWidth = l.lineWidth

            val r = (l.limit - mChart.yChartMin) * factor

            val limitPath = mRenderLimitLinesPathBuffer
            limitPath.reset()


            for (j in 0..<mChart.data!!.maxEntryCountSet.entryCount) {
                Utils.getPosition(center, r, sliceangle * j + mChart.rotationAngle, pOut)

                if (j == 0) limitPath.moveTo(pOut.x, pOut.y)
                else limitPath.lineTo(pOut.x, pOut.y)
            }
            limitPath.close()

            c.drawPath(limitPath, mLimitLinePaint!!)
        }
        MPPointF.recycleInstance(center)
        MPPointF.recycleInstance(pOut)
    }
}
