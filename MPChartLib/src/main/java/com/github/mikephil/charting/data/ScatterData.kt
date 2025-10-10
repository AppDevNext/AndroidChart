package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet

class ScatterData : BarLineScatterCandleBubbleData<Entry, IScatterDataSet> {
    constructor() : super()

    constructor(dataSets: MutableList<IScatterDataSet>) : super(dataSets)

    constructor(vararg dataSets: IScatterDataSet) : super(*dataSets)

    val greatestShapeSize: Float
        /**
         * Returns the maximum shape-size across all DataSets.
         *
         * @return
         */
        get() {
            var max = 0f

            for (set in dataSets) {
                val size = set.scatterShapeSize

                if (size > max) max = size
            }

            return max
        }
}
