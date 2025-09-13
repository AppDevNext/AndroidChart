package com.github.mikephil.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.interfaces.dataprovider.BubbleDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.renderer.BubbleChartRenderer

/**
 * The BubbleChart. Draws bubbles. Bubble chart implementation: Copyright 2015
 * Pierre-Marc Airoldi Licensed under Apache License 2.0. In the BubbleChart, it
 * is the area of the bubble, not the radius or diameter of the bubble that
 * conveys the data.
 *
 * @author Philipp Jahoda
 */
class BubbleChart : BarLineChartBase<BubbleEntry, IBubbleDataSet, BubbleData>, BubbleDataProvider {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mRenderer = BubbleChartRenderer(this, mAnimator, viewPortHandler)
    }

    override val bubbleData: BubbleData?
        get() = mData

    override val accessibilityDescription: String?
        get() = "This is bubble chart"
}
