package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.ScatterData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface ScatterDataProvider : BarLineScatterCandleBubbleDataProvider<ScatterData> {
    val scatterData: ScatterData?
}
