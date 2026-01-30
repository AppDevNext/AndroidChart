package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ILineDataSet

/**
 * Data object that encapsulates all data associated with a LineChart.
 */
class LineDataDouble : BarLineScatterCandleBubbleDataDouble<ILineDataSet<out BaseEntry<Double>, Double>> {
    constructor() : super()

    constructor(vararg dataSets: ILineDataSet<out BaseEntry<Double>, Double>) : super(*dataSets)

    constructor(dataSets: MutableList<ILineDataSet<out BaseEntry<Double>, Double>>) : super(dataSets)
}
