package info.appdev.charting.components

import info.appdev.charting.utils.convertDpToPixel

/**
 * Class representing the x-axis labels settings. Only use the setter methods to
 * modify it. Do not access public variables directly. Be aware that not all
 * features the XLabels class provides are suitable for the RadarChart.
 */
class XAxis : AxisBase() {
    /**
     * width of the x-axis labels in pixels - this is automatically
     * calculated by the computeSize() methods in the renderers
     */
    var labelWidth: Int = 1

    /**
     * height of the x-axis labels in pixels - this is automatically
     * calculated by the computeSize() methods in the renderers
     */
    var labelHeight: Int = 1

    /**
     * This is the angle for drawing the X axis labels (in degrees)
     */
    var labelRotationAngle: Float = 0f

    /**
     * if set to true, the chart will avoid that the first and last label entry
     * in the chart "clip" off the edge of the chart
     */
    var isAvoidFirstLastClipping = false

    /**
     * the position of the x-labels relative to the chart
     */
    var position: XAxisPosition? = XAxisPosition.TOP

    /**
     * enum for the position of the x-labels relative to the chart
     */
    enum class XAxisPosition {
        TOP, BOTTOM, BOTH_SIDED, TOP_INSIDE, BOTTOM_INSIDE
    }

    init {
        mYOffset = 4f.convertDpToPixel() // -3
    }

}
