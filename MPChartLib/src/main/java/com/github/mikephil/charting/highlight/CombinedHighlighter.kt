package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.DataSet.Rounding
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.dataprovider.CombinedDataProvider

/**
 * Created by Philipp Jahoda on 12/09/15.
 */
open class CombinedHighlighter(chart: CombinedDataProvider?, barChart: BarDataProvider) : ChartHighlighter<CombinedDataProvider?>(chart), IHighlighter {
    /**
     * bar highlighter for supporting stacked highlighting
     */
    // if there is BarData, create a BarHighlighter
    protected var barHighlighter = if (barChart.barData == null) null else BarHighlighter(barChart)

    override fun getHighlightsAtXValue(xVal: Float, x: Float, y: Float): MutableList<Highlight> {
        mHighlightBuffer.clear()

        val dataObjects = mChart?.combinedData?.allData ?: return mutableListOf()

        for (i in dataObjects.indices) {
            val dataObject = dataObjects[i]

            // in case of BarData, let the BarHighlighter take over
            if (barHighlighter != null && dataObject is BarData) {
                val high = barHighlighter?.getHighlight(x, y)

                if (high != null) {
                    high.dataIndex = i
                    mHighlightBuffer.add(high)
                }
            } else {
                var j = 0
                val dataSetCount = dataObject.dataSetCount
                while (j < dataSetCount) {
                    val dataSet = dataObjects[i].getDataSetByIndex(j)

                    // don't include datasets that cannot be highlighted
                    if (!dataSet.isHighlightEnabled) {
                        j++
                        continue
                    }

                    val highs = buildHighlights(dataSet, j, xVal, Rounding.CLOSEST)
                    for (high in highs) {
                        high.dataIndex = i
                        mHighlightBuffer.add(high)
                    }
                    j++
                }
            }
        }

        return mHighlightBuffer
    } //    protected Highlight getClosest(float x, float y, Highlight... highs) {
    //
    //        Highlight closest = null;
    //        float minDistance = Float.MAX_VALUE;
    //
    //        for (Highlight high : highs) {
    //
    //            if (high == null)
    //                continue;
    //
    //            float tempDistance = getDistance(x, y, high.getXPx(), high.getYPx());
    //
    //            if (tempDistance < minDistance) {
    //                minDistance = tempDistance;
    //                closest = high;
    //            }
    //        }
    //
    //        return closest;
    //    }
}
