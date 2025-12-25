package com.github.mikephil.charting.interfaces.dataprovider.base

import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.utils.Transformer

interface BarLineScatterCandleBubbleDataProvider : IBaseProvider {
    fun getTransformer(axis: YAxis.AxisDependency?): Transformer?
    fun isInverted(axis: YAxis.AxisDependency?): Boolean
    val lowestVisibleX: Float
    val highestVisibleX: Float
//    override fun getData(): BarLineScatterCandleBubbleData<*>
}