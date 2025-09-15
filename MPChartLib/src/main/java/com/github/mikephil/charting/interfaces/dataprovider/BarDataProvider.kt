package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.BarData

interface BarDataProvider : BarLineScatterCandleBubbleDataProvider {
    var barData: BarData?
    var isDrawBarShadowEnabled: Boolean
    var isDrawValueAboveBarEnabled: Boolean
    var isHighlightFullBarEnabled: Boolean
}
