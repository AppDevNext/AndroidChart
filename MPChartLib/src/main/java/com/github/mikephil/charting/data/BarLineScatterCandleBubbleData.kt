package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 */
abstract class BarLineScatterCandleBubbleData<T : IBarLineScatterCandleBubbleDataSet<out Entry>> : ChartData<T> {
    constructor() : super()

    constructor(vararg sets: T) : super(*sets)

    constructor(sets: MutableList<T>) : super(sets)
}
