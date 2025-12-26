package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ICandleDataSet

class CandleData : BarLineScatterCandleBubbleData<ICandleDataSet> {
    constructor() : super()

    constructor(dataSets: MutableList<ICandleDataSet>) : super(dataSets)

    constructor(vararg dataSets: ICandleDataSet) : super(*dataSets)
}
