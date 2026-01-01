package info.appdev.charting.highlight

import info.appdev.charting.charts.RadarChart
import info.appdev.charting.data.Entry
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.changePosition
import kotlin.math.abs

open class RadarHighlighter(chart: RadarChart) : PieRadarHighlighter<RadarChart>(chart) {
    override fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight? {
        val highlights = getHighlightsAtIndex(index)

        val distanceToCenter = chartPieRadar.distanceToCenter(x, y) / chartPieRadar.factor

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

        val phaseX = chartPieRadar.animator.phaseX
        val phaseY = chartPieRadar.animator.phaseY
        val sliceAngle = chartPieRadar.sliceAngle
        val factor = chartPieRadar.factor

        val pOut = PointF.getInstance(0f, 0f)
        for (i in 0..<chartPieRadar.data!!.dataSetCount) {
            val dataSet = chartPieRadar.data!!.getDataSetByIndex(i)

            val entry: Entry? = dataSet?.getEntryForIndex(index)

            val y = (entry!!.y - chartPieRadar.yChartMin)

            chartPieRadar.centerOffsets.changePosition(
                y * factor * phaseY,
                sliceAngle * index * phaseX + chartPieRadar.rotationAngle, pOut
            )

            mHighlightBuffer.add(Highlight(index.toFloat(), entry.y, pOut.x, pOut.y, i, dataSet.axisDependency))
        }

        return mHighlightBuffer
    }
}
