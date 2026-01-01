package info.appdev.charting.charts

import android.content.Context
import android.util.AttributeSet
import info.appdev.charting.data.BubbleData
import info.appdev.charting.interfaces.dataprovider.BubbleDataProvider
import info.appdev.charting.renderer.BubbleChartRenderer

/**
 * The BubbleChart. Draws bubbles. Bubble chart implementation: Copyright 2015
 * Pierre-Marc Airoldi Licensed under Apache License 2.0. In the BubbleChart, it
 * is the area of the bubble, not the radius or diameter of the bubble that
 * conveys the data.
 */
open class BubbleChart : BarLineChartBase<BubbleData>, BubbleDataProvider {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        dataRenderer = BubbleChartRenderer(this, mAnimator, viewPortHandler)
    }

    override val bubbleData: BubbleData?
        get() = mData

    override val accessibilityDescription: String
        get() = "This is bubble chart"
}
