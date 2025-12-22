package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.PieRadarChartBase

abstract class PieRadarHighlighter<T : PieRadarChartBase<*>>(protected var chartPieRadar: T) : IHighlighter {
    /**
     * buffer for storing previously highlighted values
     */
    protected var mHighlightBuffer: MutableList<Highlight> = ArrayList()

    override fun getHighlight(x: Float, y: Float): Highlight? {
        val touchDistanceToCenter = chartPieRadar.distanceToCenter(x, y)

        // check if a slice was touched
        if (touchDistanceToCenter > chartPieRadar.radius) {
            // if no slice was touched, highlight nothing

            return null
        } else {
            var angle = chartPieRadar.getAngleForPoint(x, y)

            if (chartPieRadar is PieChart) {
                angle /= chartPieRadar.animator.phaseY
            }

            val index = chartPieRadar.getIndexForAngle(angle)

            // check if the index could be found
            return if (index < 0 || index >= chartPieRadar.getData()!!.getMaxEntryCountSet().entryCount) {
                null
            } else {
                getClosestHighlight(index, x, y)
            }
        }
    }

    /**
     * Returns the closest Highlight object of the given objects based on the touch position inside the chart.
     */
    protected abstract fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight?
}
