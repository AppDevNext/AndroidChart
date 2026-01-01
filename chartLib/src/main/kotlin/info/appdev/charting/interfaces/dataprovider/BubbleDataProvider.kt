package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.BubbleData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface BubbleDataProvider : BarLineScatterCandleBubbleDataProvider<BubbleData> {
    val bubbleData: BubbleData?
}
