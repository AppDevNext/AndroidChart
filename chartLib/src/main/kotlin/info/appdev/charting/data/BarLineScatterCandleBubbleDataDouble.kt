package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import info.appdev.charting.interfaces.datasets.IDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data with Double precision.
 */
abstract class BarLineScatterCandleBubbleDataDouble<out T> : ChartData<@UnsafeVariance T, Double>
        where T : IDataSet<out BaseEntry<Double>, Double>,
              T : IBarLineScatterCandleBubbleDataSet<out BaseEntry<Double>, Double> {

    constructor() : super()

    @Suppress("UNCHECKED_CAST")
    constructor(vararg sets: @UnsafeVariance T) : super(*sets)

    @Suppress("UNCHECKED_CAST")
    constructor(sets: MutableList<@UnsafeVariance T>) : super(sets)
}
