package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface BarDataProvider : BarLineScatterCandleBubbleDataProvider {
    val barData: BarData?
    var isDrawBarShadowEnabled: Boolean
    var isDrawValueAboveBarEnabled: Boolean
    var isHighlightFullBarEnabled: Boolean
}
