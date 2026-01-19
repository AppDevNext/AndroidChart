package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import info.appdev.charting.interfaces.datasets.IDataSet

/**
 * Baseclass for all Line, Bar, Scatter, Candle and Bubble data.
 */
abstract class BarLineScatterCandleBubbleData<out T> : ChartData<@UnsafeVariance T>
        where T : IDataSet<out BaseEntry<Float>, Float>,
              T : IBarLineScatterCandleBubbleDataSet<out BaseEntry<Float>, Float> {

    constructor() : super()

    @Suppress("UNCHECKED_CAST")
    constructor(vararg sets: @UnsafeVariance T) : super(*sets)

    @Suppress("UNCHECKED_CAST")
    constructor(sets: MutableList<@UnsafeVariance T>) : super(sets)
}
