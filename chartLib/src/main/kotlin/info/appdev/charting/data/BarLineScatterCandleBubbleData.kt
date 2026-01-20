package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import info.appdev.charting.interfaces.datasets.IDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 */
abstract class BarLineScatterCandleBubbleData<out T, N> : ChartData<@UnsafeVariance T, N>
        where T : IDataSet<out BaseEntry<N>, N>,
              T : IBarLineScatterCandleBubbleDataSet<out BaseEntry<N>, N>,
              N : Number,
              N : Comparable<N> {

    constructor() : super()

    @Suppress("UNCHECKED_CAST")
    constructor(vararg sets: @UnsafeVariance T) : super(*sets)

    @Suppress("UNCHECKED_CAST")
    constructor(sets: MutableList<@UnsafeVariance T>) : super(sets)
}
