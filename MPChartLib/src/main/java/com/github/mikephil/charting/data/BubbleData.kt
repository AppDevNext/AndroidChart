package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet

class BubbleData : BarLineScatterCandleBubbleData<BubbleEntry, IBubbleDataSet> {
    constructor() : super()

    constructor(vararg dataSets: IBubbleDataSet) : super(*dataSets)

    constructor(dataSets: MutableList<IBubbleDataSet>) : super(dataSets)


    /**
     * Sets the width of the circle that surrounds the bubble when highlighted
     * for all DataSet objects this data object contains, in dp.
     *
     * @param width
     */
    fun setHighlightCircleWidth(width: Float) {
        for (set in dataSets) {
            set?.highlightCircleWidth = width
        }
    }
}
