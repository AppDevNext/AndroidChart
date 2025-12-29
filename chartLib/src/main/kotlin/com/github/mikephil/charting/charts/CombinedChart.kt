package com.github.mikephil.charting.charts

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import info.appdev.charting.charts.CombinedChart

@Deprecated("Use same class from package info.appdev.charting.charts instead")
open class CombinedChart : CombinedChart {
    @Deprecated("Use same class from package info.appdev.charting.charts instead")
    constructor(context: Context?) : super(context)

    @Deprecated("Use same class from package info.appdev.charting.charts instead")
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    @Deprecated("Use same class from package info.appdev.charting.charts instead")
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()
        Log.e("CombinedChart", "This class is deprecated. Please use info.appdev.charting.charts.CombinedChart instead.")
    }
}
