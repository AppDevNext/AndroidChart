package com.github.mikephil.charting.charts

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import info.appdev.charting.charts.LineChart

@Deprecated("Use same class from package info.appdev.charting.charts instead")
open class LineChart : LineChart {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()
        Log.e("LineChart", "This class is deprecated. Please use info.appdev.charting.charts.LineChart instead.")
    }
}
