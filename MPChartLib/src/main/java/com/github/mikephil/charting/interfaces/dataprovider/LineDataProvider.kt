package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface LineDataProvider : BarLineScatterCandleBubbleDataProvider {
    val lineData: LineData?
}