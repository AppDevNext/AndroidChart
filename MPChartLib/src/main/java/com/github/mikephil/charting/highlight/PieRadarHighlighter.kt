package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.PieRadarChartBase

/**
 * Created by philipp on 12/06/16.
 */
abstract class PieRadarHighlighter<T : PieRadarChartBase<*, *, *>?>(protected var mChart: T?) : IHighlighter {
    /**
     * buffer for storing previously highlighted values
     */
    protected var mHighlightBuffer: MutableList<Highlight> = ArrayList()

    override fun getHighlight(x: Float, y: Float): Highlight? {
        val chart = mChart ?: return null

        val touchDistanceToCenter = chart.distanceToCenter(x, y)

        // check if a slice was touched
        if (touchDistanceToCenter > chart.radius) {
            // if no slice was touched, highlight nothing

            return null
        } else {
            var angle = chart.getAngleForPoint(x, y)

            if (mChart is PieChart) {
                angle /= chart.animator.phaseY
            }

            val index = chart.getIndexForAngle(angle)

            // check if the index could be found
            return if (index < 0 || chart.data?.maxEntryCountSet?.entryCount?.let { it >= index } == true) {
                null
            } else {
                getClosestHighlight(index, x, y)
            }
        }
    }

    /**
     * Returns the closest Highlight object of the given objects based on the touch position inside the chart.
     *
     * @param index
     * @param x
     * @param y
     * @return
     */
    protected abstract fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight?
}
