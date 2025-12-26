package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.CandleData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface CandleDataProvider : BarLineScatterCandleBubbleDataProvider {
    val candleData: CandleData?
}
