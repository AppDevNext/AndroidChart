package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.BarData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface BarDataProvider : BarLineScatterCandleBubbleDataProvider {
    val barData: BarData?
    var isDrawBarShadowEnabled: Boolean
    var isDrawValueAboveBarEnabled: Boolean
    var isHighlightFullBarEnabled: Boolean
}
