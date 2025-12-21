package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface CandleDataProvider : BarLineScatterCandleBubbleDataProvider {
    val candleData: CandleData?
}
