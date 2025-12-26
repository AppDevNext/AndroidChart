package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.CombinedData

interface CombinedDataProvider : LineDataProvider, BarDataProvider, BubbleDataProvider, CandleDataProvider, ScatterDataProvider {
    val combinedData: CombinedData?
}
