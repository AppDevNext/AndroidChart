package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.BarLineScatterCandleBubbleData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface LineDataProvider : BarLineScatterCandleBubbleDataProvider<BarLineScatterCandleBubbleData<*, *>> {
    val lineData: BarLineScatterCandleBubbleData<*, *>?
}
