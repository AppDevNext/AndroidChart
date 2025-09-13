package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.data.CombinedData

/**
 * Created by philipp on 11/06/16.
 */
interface CombinedDataProvider : LineDataProvider, BarDataProvider, BubbleDataProvider, CandleDataProvider, ScatterDataProvider {
    val combinedData: CombinedData?
}
