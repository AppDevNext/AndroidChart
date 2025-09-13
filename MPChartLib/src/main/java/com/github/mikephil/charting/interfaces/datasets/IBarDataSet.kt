package com.github.mikephil.charting.interfaces.datasets

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.Fill

/**
 * Created by philipp on 21/10/15.
 */
interface IBarDataSet : IBarLineScatterCandleBubbleDataSet<BarEntry> {
    val fills: MutableList<Fill?>?

    fun getFill(index: Int): Fill?

    /**
     * Returns true if this DataSet is stacked (stacksize > 1) or not.
     *
     * @return
     */
    val isStacked: Boolean

    /**
     * Returns the maximum number of bars that can be stacked upon another in
     * this DataSet. This should return 1 for non stacked bars, and > 1 for stacked bars.
     *
     * @return
     */
    val stackSize: Int

    /**
     * Returns the color used for drawing the bar-shadows. The bar shadows is a
     * surface behind the bar that indicates the maximum value.
     *
     * @return
     */
    val barShadowColor: Int

    /**
     * Returns the width used for drawing borders around the bars.
     * If borderWidth == 0, no border will be drawn.
     *
     * @return
     */
    val barBorderWidth: Float
        @JvmField
        set

    /**
     * Returns the color drawing borders around the bars.
     *
     * @return
     */
    val barBorderColor: Int

    /**
     * Returns the alpha value (transparency) that is used for drawing the
     * highlight indicator.
     *
     * @return
     */
    val highLightAlpha: Int


    /**
     * Returns the labels used for the different value-stacks in the legend.
     * This is only relevant for stacked bar entries.
     *
     * @return
     */
    val stackLabels: Array<String?>?
}
