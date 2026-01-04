package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.BarData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface BarDataProvider : BarLineScatterCandleBubbleDataProvider<BarData> {
    val barData: BarData?
    var isDrawBarShadowEnabled: Boolean
    var isDrawValueAboveBarEnabled: Boolean
    var isHighlightFullBarEnabled: Boolean
    var isOwnRoundedRendererUsed : Boolean
}
