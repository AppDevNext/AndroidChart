package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.BaseEntry

interface IBarLineScatterCandleBubbleDataSet<T, N_XAxis> : IDataSet<T, N_XAxis> where T : BaseEntry<N_XAxis>, N_XAxis : Number, N_XAxis : Comparable<N_XAxis> {

    val highLightColor: Int
}
