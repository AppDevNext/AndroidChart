package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.BarEntry
import info.appdev.charting.utils.Fill

interface IBarDataSet : IBarLineScatterCandleBubbleDataSet<BarEntry, Float> {
    var fills: MutableList<Fill>

    fun getFill(index: Int): Fill?

    /**
     * Returns true if this DataSet is stacked (stackSize > 1) or not.
     */
    val isStacked: Boolean

    /**
     * Returns the maximum number of bars that can be stacked upon another in
     * this DataSet. This should return 1 for non stacked bars, and > 1 for stacked bars.
     */
    var stackSize: Int

    /**
     * Returns the color used for drawing the bar-shadows. The bar shadows is a
     * surface behind the bar that indicates the maximum value.
     */
    var barShadowColor: Int

    /**
     * Returns the width used for drawing borders around the bars.
     * If borderWidth == 0, no border will be drawn.
     */
    var barBorderWidth: Float

    /**
     * Returns the color drawing borders around the bars.
     */
    var barBorderColor: Int

    /**
     * Returns the alpha value (transparency) that is used for drawing the
     * highlight indicator.
     */
    var highLightAlpha: Int

    /**
     * Returns the labels used for the different value-stacks in the legend.
     * This is only relevant for stacked bar entries.
     */
    var stackLabels: MutableList<String>
}
