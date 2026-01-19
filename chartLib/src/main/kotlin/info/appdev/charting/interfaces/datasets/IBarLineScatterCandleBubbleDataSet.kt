package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.Entry

interface IBarLineScatterCandleBubbleDataSet<T : Entry> : IDataSet<T> {

    val highLightColor: Int
}
