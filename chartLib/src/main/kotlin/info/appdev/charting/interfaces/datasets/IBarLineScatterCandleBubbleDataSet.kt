package info.appdev.charting.interfaces.datasets

import androidx.annotation.ColorInt
import info.appdev.charting.data.Entry

interface IBarLineScatterCandleBubbleDataSet<T : Entry> : IDataSet<T> {

    val highLightColor: Int
}
