package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.BarData
import info.appdev.charting.data.BubbleData
import info.appdev.charting.data.CandleData
import info.appdev.charting.data.CombinedData
import info.appdev.charting.data.LineData
import info.appdev.charting.data.ScatterData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface CombinedDataProvider : BarLineScatterCandleBubbleDataProvider<CombinedData> {
    val combinedData: CombinedData?
    val lineData: LineData?
    val barData: BarData?
    val bubbleData: BubbleData?
    val candleData: CandleData?
    val scatterData: ScatterData?
}
