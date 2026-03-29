package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.EntryFloat

interface IBarLineScatterCandleBubbleDataSet<T : EntryFloat> : IDataSet<T> {

    val highLightColor: Int
}
