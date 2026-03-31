package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ILineDataSet

/**
 * Data object that encapsulates all data associated with a LineChart.
 */
class LineData : BarLineScatterCandleBubbleData<ILineDataSet<EntryFloat>> {
    constructor() : super()

    constructor(vararg dataSets: ILineDataSet<EntryFloat>) : super(*dataSets)

    constructor(dataSets: MutableList<ILineDataSet<EntryFloat>>) : super(dataSets)
}
