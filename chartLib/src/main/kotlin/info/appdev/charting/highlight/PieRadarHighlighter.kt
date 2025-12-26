package info.appdev.charting.highlight

import info.appdev.charting.charts.PieRadarChartBase

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

            angle /= chartPieRadar.animator.phaseY

            val index = chartPieRadar.getIndexForAngle(angle)

            val localData = chartPieRadar.getData()
            val maxCount = localData?.maxEntryCountSet?.entryCount ?: 0
            return if (index !in 0..<maxCount) {
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
