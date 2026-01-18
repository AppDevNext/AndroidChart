package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ILineDataSet

/**
 * Data object that encapsulates all data associated with a LineChart.
 */
class LineData : BarLineScatterCandleBubbleData<ILineDataSet<out BaseEntry<Float>, Float>> {
    constructor() : super()

    constructor(vararg dataSets: ILineDataSet<out BaseEntry<Float>, Float>) : super(*dataSets)

    constructor(dataSets: MutableList<ILineDataSet<out BaseEntry<Float>, Float>>) : super(dataSets)
}
