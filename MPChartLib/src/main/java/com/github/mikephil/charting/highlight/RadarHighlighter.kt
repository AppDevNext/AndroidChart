package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import kotlin.math.abs

open class RadarHighlighter(chart: RadarChart) : PieRadarHighlighter<RadarChart>(chart) {
    override fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight? {
        val highlights = getHighlightsAtIndex(index)

        val distanceToCenter = chart!!.distanceToCenter(x, y) / chart!!.getFactor()

        var closest: Highlight? = null
        var distance = Float.MAX_VALUE

        for (i in highlights.indices) {
            val high = highlights[i]

            val cDistance = abs(high.y - distanceToCenter)
            if (cDistance < distance) {
                closest = high
                distance = cDistance
            }
        }

        return closest
    }

    /**
     * Returns an array of Highlight objects for the given index. The Highlight
     * objects give information about the value at the selected index and the
     * DataSet it belongs to. INFORMATION: This method does calculations at
     * runtime. Do not over-use in performance critical situations.
     */
    protected fun getHighlightsAtIndex(index: Int): MutableList<Highlight> {
        mHighlightBuffer.clear()

        val phaseX = chart!!.animator.phaseX
        val phaseY = chart!!.animator.phaseY
        val sliceAngle = chart!!.sliceAngle
        val factor = chart!!.getFactor()

        val pOut = MPPointF.getInstance(0f, 0f)
        for (i in 0..<chart!!.data!!.getDataSetCount()) {
            val dataSet: IDataSet<*> = chart!!.data!!.getDataSetByIndex(i)

            val entry: Entry? = dataSet.getEntryForIndex(index)

            val y = (entry!!.y - chart!!.yChartMin)

            Utils.getPosition(
                chart!!.centerOffsets, y * factor * phaseY,
                sliceAngle * index * phaseX + chart!!.rotationAngle, pOut
            )

            mHighlightBuffer.add(Highlight(index.toFloat(), entry.y, pOut.x, pOut.y, i, dataSet.axisDependency))
        }

        return mHighlightBuffer
    }
}
