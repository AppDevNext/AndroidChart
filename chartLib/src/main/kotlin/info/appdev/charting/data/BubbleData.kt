package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IBubbleDataSet

class BubbleData : BarLineScatterCandleBubbleData<IBubbleDataSet, Float> {
    constructor() : super()

    constructor(dataSets: MutableList<IBubbleDataSet>) : super(dataSets)

    /**
     * Sets the width of the circle that surrounds the bubble when highlighted
     * for all DataSet objects this data object contains, in dp.
     */
    fun setHighlightCircleWidth(width: Float) {
        for (set in dataSets) {
            set.highlightCircleWidth = width
        }
    }
}
