package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.BaseEntry

interface IBarLineScatterCandleBubbleDataSet<T, N> : IDataSet<T, N> where T : BaseEntry<N>, N : Number, N : Comparable<N> {

    val highLightColor: Int
}
