package com.github.mikephil.charting.data

import android.util.Log
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet

/**
 * A PieData object can only represent one DataSet. Unlike all other charts, the
 * legend labels of the PieChart are created from the x-values array, and not
 * from the DataSet labels. Each PieData object can only represent one
 * PieDataSet (multiple PieDataSets inside a single PieChart are not possible).
 */
class PieData : ChartData<IPieDataSet?> {
    constructor() : super()

    constructor(dataSet: IPieDataSet?) : super(dataSet)

    var dataSet: IPieDataSet?
        /**
         * Returns the DataSet this PieData object represents. A PieData object can
         * only contain one DataSet.
         */
        get() = mDataSets[0]
        /**
         * Sets the PieDataSet this data object should represent.
         */
        set(dataSet) {
            mDataSets.clear()
            mDataSets.add(dataSet)
            notifyDataChanged()
        }

    override fun getDataSets(): MutableList<IPieDataSet?> {
        val dataSets = super.getDataSets()

        if (dataSets.isEmpty()) {
            Log.e(
                "AndroidChart",
                "Found multiple data sets while pie chart only allows one"
            )
        }

        return dataSets
    }

    /**
     * The PieData object can only have one DataSet. Use getDataSet() method instead.
     */
    override fun getDataSetByIndex(index: Int): IPieDataSet? {
        return if (index == 0) this.dataSet else null
    }

    override fun getDataSetByLabel(label: String, ignorecase: Boolean): IPieDataSet? {
        return if (ignorecase) if (label.equals(mDataSets[0]!!.label, ignoreCase = true))
            mDataSets[0]
        else
            null else if (label == mDataSets[0]!!.label) mDataSets[0] else null
    }

    override fun getEntryForHighlight(highlight: Highlight): Entry? {
        return this.dataSet!!.getEntryForIndex(highlight.x.toInt())
    }

    val yValueSum: Float
        /**
         * Returns the sum of all values in this PieData object.
         */
        get() {
            var sum = 0f

            for (i in 0..<this.dataSet!!.entryCount) sum += this.dataSet!!.getEntryForIndex(i)!!.y


            return sum
        }
}
