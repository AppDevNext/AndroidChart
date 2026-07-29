package info.appdev.charting.highlight

import android.view.View
import info.appdev.charting.charts.PieRadarChartBase
import kotlin.math.hypot

abstract class PieRadarHighlighter<T : PieRadarChartBase<*>>(protected var chartPieRadar: T) : IHighlighter {

    /**
     * Buffer for storing previously highlighted values.
     */
    protected var highlightBuffer: MutableList<Highlight> = ArrayList()

    override fun getHighlight(x: Float, y: Float): Highlight? {
        val touchDistanceToCenter = chartPieRadar.distanceToCenter(x, y)

        // ─── SAFE PROPORTIONAL BOUNDARY FOR PIE CHARTS ───
        val extendedRadius = chartPieRadar.radius * 1.25f

        // Check if a slice was touched within the expanded boundary
        if (touchDistanceToCenter > extendedRadius) {
            return null
        }

        var angle = chartPieRadar.getAngleForPoint(x, y)

        // ─── TYPE-SAFE PHASE TRANSFORMATION FALLBACK ───
        // Only modify angle calculations via animation phase adjustments if target is explicitly a PieChart
        val isPieChart = chartPieRadar.javaClass.name.contains("PieChart", ignoreCase = true)
        if (isPieChart) {
            angle /= chartPieRadar.animator.phaseY
        }

        val index = chartPieRadar.getIndexForAngle(angle)

        val localData = chartPieRadar.data
        val maxCount = localData?.maxEntryCountSet?.entryCount ?: 0

        if (index !in 0..<maxCount) {
            return null
        }

        val hint = getClosestHighlight(index, x, y)

        // ─── NEW RADAR CHART PROXIMITY FILTER ───
        // If it's a Radar Chart, enforce a comfortable 40dp finger-sized touch boundary
        val isRadarChart = chartPieRadar.javaClass.name.contains("RadarChart", ignoreCase = true)
        if (isRadarChart && hint != null) {
            val density = (chartPieRadar as View).context.resources.displayMetrics.density
            val maxSelectionDistance = 40f * density

            // Calculate absolute distance between your touch point and the actual drawn vertex
            val distanceToVertex = hypot((x - hint.xPx).toDouble(), (y - hint.yPx).toDouble()).toFloat()

            // If your finger is further than 40dp from the actual point, reject the touch registration
            if (distanceToVertex > maxSelectionDistance) {
                return null
            }
        }

        return hint
    }

    /**
     * Returns the closest Highlight object of the given objects based on the touch position inside the chart.
     */
    protected abstract fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight?

    override fun getHighlightsAtXValue(xVal: Float, x: Float, y: Float): MutableList<Highlight>? {
        return highlightBuffer
    }
}