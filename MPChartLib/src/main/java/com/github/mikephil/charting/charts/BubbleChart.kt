package com.github.mikephil.charting.charts

import android.content.Context
import android.util.AttributeSet
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.interfaces.dataprovider.BubbleDataProvider
import com.github.mikephil.charting.renderer.BubbleChartRenderer

/**
 * The BubbleChart. Draws bubbles. Bubble chart implementation: Copyright 2015
 * Pierre-Marc Airoldi Licensed under Apache License 2.0. In the BubbleChart, it
 * is the area of the bubble, not the radius or diameter of the bubble that
 * conveys the data.
 */
class BubbleChart : BarLineChartBase<BubbleData?>, BubbleDataProvider {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    protected override fun init() {
        super.init()

        mRenderer = BubbleChartRenderer(this, mAnimator, mViewPortHandler)
    }

    override val bubbleData: BubbleData?
        get() = mData

    override fun getAccessibilityDescription(): String {
        return "This is bubble chart"
    }

    override fun setData(data: BubbleData?) {
        super.setData(data)
    }
}
