package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.BarData

interface BarDataProvider : BarLineScatterCandleBubbleDataProvider {
    val barData: BarData?
    val isDrawBarShadowEnabled: Boolean
    val isDrawValueAboveBarEnabled: Boolean
    val isHighlightFullBarEnabled: Boolean
}
