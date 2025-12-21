package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.dataprovider.CombinedDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet

open class CombinedHighlighter(chart: CombinedDataProvider?, barChart: BarDataProvider) : ChartHighlighter<CombinedDataProvider?>(chart), IHighlighter {
    /**
     * bar highlighter for supporting stacked highlighting
     */
    protected var barHighlighter: BarHighlighter?

    init {
        // if there is BarData, create a BarHighlighter
        barChart.barData
        barHighlighter = BarHighlighter(barChart)
    }

    override fun getHighlightsAtXValue(xVal: Float, x: Float, y: Float): MutableList<Highlight?>? {
        mHighlightBuffer.clear()

        val dataObjects = mChart!!.combinedData!!.getAllData()

        for (i in dataObjects.indices) {
            val dataObject: ChartData<*> = dataObjects[i]

            // in case of BarData, let the BarHighlighter take over
            if (barHighlighter != null && dataObject is BarData) {
                val high = barHighlighter!!.getHighlight(x, y)

                if (high != null) {
                    high.dataIndex = i
                    mHighlightBuffer.add(high)
                }
            } else {
                var j = 0
                val dataSetCount = dataObject.getDataSetCount()
                while (j < dataSetCount) {
                    val dataSet: IDataSet<*> = dataObjects[i].getDataSetByIndex(j)

                    // don't include datasets that cannot be highlighted
                    if (!dataSet.isHighlightEnabled) {
                        j++
                        continue
                    }

                    val highs = buildHighlights(dataSet, j, xVal, DataSet.Rounding.CLOSEST)
                    for (high in highs) {
                        high.dataIndex = i
                        mHighlightBuffer.add(high)
                    }
                    j++
                }
            }
        }

        return mHighlightBuffer
    }
}
