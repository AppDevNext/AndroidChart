package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IScatterDataSet

class ScatterData : BarLineScatterCandleBubbleData<IScatterDataSet> {
    constructor() : super()

    constructor(dataSets: MutableList<IScatterDataSet>) : super(dataSets)

    constructor(vararg dataSets: IScatterDataSet) : super(*dataSets)

    /**
     * Returns the maximum shape-size across all DataSets.
     */
    val greatestShapeSize: Float
        get() {
            var max = 0f

            for (set in dataSets) {
                val size = set.scatterShapeSize

                if (size > max) max = size
            }

            return max
        }
}
