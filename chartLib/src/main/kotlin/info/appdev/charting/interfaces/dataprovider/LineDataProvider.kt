package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.LineData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface LineDataProvider : BarLineScatterCandleBubbleDataProvider {
    val lineData: LineData?
}
