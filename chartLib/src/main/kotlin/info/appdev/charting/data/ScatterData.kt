package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IScatterDataSet

class ScatterData : BarLineScatterCandleBubbleData<IScatterDataSet> {
    constructor() : super()

    constructor(dataSets: MutableList<IScatterDataSet>) : super(dataSets)

    constructor(vararg dataSets: IScatterDataSet) : super(*dataSets)

    val greatestShapeSize: Float
        /**
         * Returns the maximum shape-size across all DataSets.
         */
        get() {
            var max = 0f

            if (dataSets != null) {
                for (set in dataSets) {
                    val size = set.scatterShapeSize

                    if (size > max) max = size
                }
            }

            return max
        }
}
