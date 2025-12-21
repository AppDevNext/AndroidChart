package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.CombinedData

interface CombinedDataProvider : LineDataProvider, BarDataProvider, BubbleDataProvider, CandleDataProvider, ScatterDataProvider {
    val combinedData: CombinedData?
}
