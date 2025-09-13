package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

/**
 * Data object that encapsulates all data associated with a LineChart.
 *
 * @author Philipp Jahoda
 */
class LineData : BarLineScatterCandleBubbleData<Entry, ILineDataSet> {
    constructor() : super()

    constructor(vararg dataSets: ILineDataSet) : super(*dataSets)

    constructor(dataSets: MutableList<ILineDataSet>) : super(dataSets)
}
