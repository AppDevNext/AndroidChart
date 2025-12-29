package info.appdev.charting.highlight

import info.appdev.charting.data.BarData
import info.appdev.charting.data.ChartData
import info.appdev.charting.data.DataSet
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.dataprovider.CombinedDataProvider

open class CombinedHighlighter(combinedDataProvider: CombinedDataProvider, barDataProvider: BarDataProvider) : ChartHighlighter<CombinedDataProvider>(combinedDataProvider), IHighlighter {
    /**
     * bar highlighter for supporting stacked highlighting
     */
    protected var barHighlighter: BarHighlighter?

    init {
        // if there is BarData, create a BarHighlighter
        barDataProvider.barData
        barHighlighter = BarHighlighter(barDataProvider)
    }

    override fun getHighlightsAtXValue(xVal: Float, x: Float, y: Float): MutableList<Highlight>? {
        highlightBuffer.clear()

        val dataObjects = provider.combinedData!!.allData

        for (i in dataObjects.indices) {
            val dataObject: ChartData<*> = dataObjects[i]

            // in case of BarData, let the BarHighlighter take over
            if (barHighlighter != null && dataObject is BarData) {
                val high = barHighlighter!!.getHighlight(x, y)

                if (high != null) {
                    high.dataIndex = i
                    highlightBuffer.add(high)
                }
            } else {
                var j = 0
                val dataSetCount = dataObject.dataSetCount
                while (j < dataSetCount) {
                    val dataSet = dataObjects[i].getDataSetByIndex(j)

                    dataSet?.let {
                        // don't include datasets that cannot be highlighted
                        if (!it.isHighlightEnabled) {
                            j++
                            continue
                        }

                        val highs = buildHighlights(it, j, xVal, DataSet.Rounding.CLOSEST)
                        for (high in highs) {
                            high.dataIndex = i
                            highlightBuffer.add(high)
                        }
                    }
                    j++
                }
            }
        }

        return highlightBuffer
    }
}
