package com.github.mikephil.charting.charts

import android.content.Context
import android.util.Log

@Deprecated("Use same class from package info.appdev.charting.charts instead")
open class BarChart : info.appdev.charting.charts.BarChart {
    @Deprecated("Use same class from package info.appdev.charting.charts instead")
    constructor(context: Context?) : super(context)

    @Deprecated("Use same class from package info.appdev.charting.charts instead")
    constructor(context: Context?, attrs: android.util.AttributeSet?) : super(context, attrs)

    @Deprecated("Use same class from package info.appdev.charting.charts instead")
    constructor(context: Context?, attrs: android.util.AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    override fun init() {
        super.init()
        Log.e("BarChart", "This class is deprecated. Please use info.appdev.charting.charts.BarChart instead.")
    }
}
