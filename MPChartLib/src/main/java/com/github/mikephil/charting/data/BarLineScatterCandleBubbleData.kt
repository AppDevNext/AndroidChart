package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 *
 * @author Philipp Jahoda
 */
abstract class BarLineScatterCandleBubbleData<E: Entry, T : IBarLineScatterCandleBubbleDataSet<E>>
    : ChartData<E, T> {
    constructor() : super()

    constructor(vararg sets: T) : super(*sets)

    constructor(sets: MutableList<T>) : super(sets)
}
