package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ILineDataSet

/**
 * Data object that encapsulates all data associated with a LineChart.
 */
class LineData : BarLineScatterCandleBubbleData<ILineDataSet> {
    constructor() : super()

    constructor(vararg dataSets: ILineDataSet) : super(*dataSets)

    constructor(dataSets: MutableList<ILineDataSet>) : super(dataSets)
}
