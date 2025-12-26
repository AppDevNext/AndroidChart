package info.appdev.charting.interfaces.dataprovider

import info.appdev.charting.data.ScatterData
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider

interface ScatterDataProvider : BarLineScatterCandleBubbleDataProvider {
    val scatterData: ScatterData?
}
