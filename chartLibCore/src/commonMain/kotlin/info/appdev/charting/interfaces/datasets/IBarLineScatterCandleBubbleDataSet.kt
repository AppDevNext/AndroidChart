package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.BaseEntry

interface IBarLineScatterCandleBubbleDataSet<T : BaseEntry<Float>> : IDataSet<T> {

    val highLightColor: Int
}
