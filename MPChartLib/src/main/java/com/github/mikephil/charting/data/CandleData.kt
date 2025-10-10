package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet

class CandleData : BarLineScatterCandleBubbleData<CandleEntry, ICandleDataSet> {
    constructor() : super()

    constructor(dataSets: MutableList<ICandleDataSet>) : super(dataSets)

    constructor(vararg dataSets: ICandleDataSet) : super(*dataSets)
}
