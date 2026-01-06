package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.BarData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface BarDataProvider : BarLineScatterCandleBubbleDataProvider<BarData> {
    val barData: BarData?
    var isDrawBarShadow: Boolean
    var isDrawValueAboveBar: Boolean
    var isHighlightFullBar: Boolean
}
