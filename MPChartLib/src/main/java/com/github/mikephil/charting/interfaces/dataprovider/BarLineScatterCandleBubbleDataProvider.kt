package com.github.mikephil.charting.interfaces.dataprovider

import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.utils.Transformer

interface BarLineScatterCandleBubbleDataProvider : ChartInterface {
    fun getTransformer(axis: AxisDependency?): Transformer
    fun isInverted(axis: AxisDependency?): Boolean
    val lowestVisibleX: Float
    val highestVisibleX: Float

    override val data: ChartData<*, *>?
}