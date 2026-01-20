package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ICandleDataSet

class CandleData : BarLineScatterCandleBubbleData<ICandleDataSet, Float> {
    constructor() : super()

    constructor(vararg dataSets: ICandleDataSet) : super(*dataSets)
}
