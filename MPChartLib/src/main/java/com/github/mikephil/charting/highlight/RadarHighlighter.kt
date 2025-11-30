package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import kotlin.math.abs

/**
 * Created by philipp on 12/06/16.
 */
open class RadarHighlighter(chart: RadarChart) : PieRadarHighlighter<RadarChart>(chart) {
    override fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight? {
        val chart = mChart ?: return null
        val highlights = getHighlightsAtIndex(index)

        val distanceToCenter = chart.distanceToCenter(x, y) / chart.factor

        var closest: Highlight? = null
        var distance = Float.Companion.MAX_VALUE

        for (i in highlights.indices) {
            val high = highlights[i]

            val cdistance = abs(high.y - distanceToCenter)
            if (cdistance < distance) {
                closest = high
                distance = cdistance
            }
        }

        return closest
    }

    /**
     * Returns an array of Highlight objects for the given index. The Highlight
     * objects give information about the value at the selected index and the
     * DataSet it belongs to. INFORMATION: This method does calculations at
     * runtime. Do not over-use in performance critical situations.
     *
     * @param index
     * @return
     */
    protected fun getHighlightsAtIndex(index: Int): MutableList<Highlight> {
        mHighlightBuffer.clear()

        val chart = mChart ?: return mutableListOf()

        val phaseX = chart.animator.phaseX
        val phaseY = chart.animator.phaseY
        val sliceangle = chart.sliceAngle
        val factor = chart.factor

        val pOut: MPPointF = MPPointF.Companion.getInstance(0f, 0f)
        val chartData = chart.data ?: return mutableListOf()
        for (i in 0..<chartData.dataSetCount) {
            val dataSet = chartData.getDataSetByIndex(i)

            val entry = dataSet.getEntryForIndex(index)

            val y = (entry.y - chart.yChartMin)

            Utils.getPosition(
                chart.centerOffsets, y * factor * phaseY,
                sliceangle * index * phaseX + chart.rotationAngle, pOut
            )

            mHighlightBuffer.add(Highlight(index.toFloat(), entry.y, pOut.x, pOut.y, i, dataSet.axisDependency))
        }

        return mHighlightBuffer
    }
}
