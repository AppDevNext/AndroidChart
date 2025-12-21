package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface ScatterDataProvider : BarLineScatterCandleBubbleDataProvider {
    val scatterData: ScatterData?
}
