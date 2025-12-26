package info.appdev.charting.interfaces.dataprovider.base

import info.appdev.charting.components.YAxis
import info.appdev.charting.utils.Transformer

interface BarLineScatterCandleBubbleDataProvider : IBaseProvider {
    fun getTransformer(axis: YAxis.AxisDependency?): Transformer?
    fun isInverted(axis: YAxis.AxisDependency?): Boolean
    val lowestVisibleX: Float
    val highestVisibleX: Float
}