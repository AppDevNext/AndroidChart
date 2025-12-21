package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.CandleData

interface CandleDataProvider : BarLineScatterCandleBubbleDataProvider {
    val candleData: CandleData?
}
