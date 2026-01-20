package info.appdev.charting.interfaces.dataprovider.base

import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BarLineScatterCandleBubbleData
import info.appdev.charting.utils.Transformer

interface BarLineScatterCandleBubbleDataProvider<T : BarLineScatterCandleBubbleData<*, *>> : IBaseProvider<T> {
    fun getTransformer(axis: YAxis.AxisDependency?): Transformer?
    fun isInverted(axis: YAxis.AxisDependency?): Boolean
    val lowestVisibleX: Float
    val highestVisibleX: Float
}