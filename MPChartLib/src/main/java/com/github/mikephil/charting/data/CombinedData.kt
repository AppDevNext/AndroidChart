package com.github.mikephil.charting.data

import android.util.Log
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import java.lang.Float
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.Int

/**
 * Data object that allows the combination of Line-, Bar-, Scatter-, Bubble- and
 * CandleData. Used in the CombinedChart class.
 *
 * @author Philipp Jahoda
 */
class CombinedData : BarLineScatterCandleBubbleData<Entry, IBarLineScatterCandleBubbleDataSet<Entry>>() {
    var lineData: LineData? = null
        private set
    var barData: BarData? = null
        private set
    var scatterData: ScatterData? = null
        private set
    var candleData: CandleData? = null
        private set
    var bubbleData: BubbleData? = null
        private set

    fun setData(data: LineData?) {
        this.lineData = data
        notifyDataChanged()
    }

    fun setData(data: BarData?) {
        this.barData = data
        notifyDataChanged()
    }

    fun setData(data: ScatterData?) {
        this.scatterData = data
        notifyDataChanged()
    }

    fun setData(data: CandleData?) {
        this.candleData = data
        notifyDataChanged()
    }

    fun setData(data: BubbleData?) {
        this.bubbleData = data
        notifyDataChanged()
    }

    override fun calcMinMax() {
        dataSets.clear()

        yMax = -Float.MAX_VALUE
        yMin = Float.MAX_VALUE
        xMax = -Float.MAX_VALUE
        xMin = Float.MAX_VALUE

        mLeftAxisMax = -Float.MAX_VALUE
        mLeftAxisMin = Float.MAX_VALUE
        mRightAxisMax = -Float.MAX_VALUE
        mRightAxisMin = Float.MAX_VALUE

        val allData = this.allData

        for (data in allData) {
            data.calcMinMax()

            @Suppress("UNCHECKED_CAST")
            dataSets.addAll(data.dataSets as Collection<IBarLineScatterCandleBubbleDataSet<Entry>>)

            if (data.yMax > yMax) yMax = data.yMax

            if (data.yMin < yMin) yMin = data.yMin

            if (data.xMax > xMax) xMax = data.xMax

            if (data.xMin < xMin) xMin = data.xMin

            for (dataset in dataSets) {
                if (dataset.axisDependency == AxisDependency.LEFT) {
                    if (dataset.yMax > mLeftAxisMax) {
                        mLeftAxisMax = dataset.yMax
                    }

                    if (dataset.yMin < mLeftAxisMin) {
                        mLeftAxisMin = dataset.yMin
                    }
                } else {
                    if (dataset.yMax > mRightAxisMax) {
                        mRightAxisMax = dataset.yMax
                    }

                    if (dataset.yMin < mRightAxisMin) {
                        mRightAxisMin = dataset.yMin
                    }
                }
            }
        }
    }

    val allData
        /**
         * Returns all data objects in row: line-bar-scatter-candle-bubble if not null.
         *
         * @return
         */
        get() = listOfNotNull(
            this.lineData,
            this.barData,
            this.scatterData,
            this.candleData,
            this.bubbleData,
        )

    fun getDataByIndex(index: Int) = this.allData[index]

    override fun notifyDataChanged() {
        lineData?.notifyDataChanged()
        barData?.notifyDataChanged()
        candleData?.notifyDataChanged()
        scatterData?.notifyDataChanged()
        bubbleData?.notifyDataChanged()

        calcMinMax() // recalculate everything
    }

    /**
     * Get the Entry for a corresponding highlight object
     *
     * @param highlight
     * @return the entry that is highlighted
     */
    override fun getEntryForHighlight(highlight: Highlight): Entry? {
        if (highlight.dataIndex >= this.allData.size) return null

        val data: ChartData<*, *> = getDataByIndex(highlight.dataIndex)

        if (highlight.dataSetIndex >= data.dataSetCount) return null

        // The value of the highlighted entry could be NaN -
        //   if we are not interested in highlighting a specific value.
        val entries = data.getDataSetByIndex(highlight.dataSetIndex).getEntriesForXValue(highlight.x)
        for (entry in entries) if (entry.y == highlight.y ||
            Float.isNaN(highlight.y)
        ) return entry

        return null
    }

    /**
     * Get dataset for highlight
     *
     * @param highlight current highlight
     * @return dataset related to highlight
     */
    fun getDataSetByHighlight(highlight: Highlight): IBarLineScatterCandleBubbleDataSet<out Entry>? {
        if (highlight.dataIndex >= this.allData.size) return null

        val data = getDataByIndex(highlight.dataIndex)

        if (highlight.dataSetIndex >= data.dataSetCount) return null

        return data.dataSets[highlight.dataSetIndex]
    }

    fun getDataIndex(data: BarLineScatterCandleBubbleData<Entry, IBarLineScatterCandleBubbleDataSet<Entry>>): Int {
        return this.allData.indexOf(data)
    }

    override fun removeDataSet(d: IDataSet<Entry>?): Boolean {
        val datas = this.allData

        var success = false

        for (data in datas) {
            success = data.removeDataSet(d)

            if (success) {
                break
            }
        }

        return success
    }

    @Deprecated("")
    override fun removeDataSet(index: Int): Boolean {
        Log.e("MPAndroidChart", "removeDataSet(int index) not supported for CombinedData")
        return false
    }

    @Deprecated("")
    override fun removeEntry(e: Entry?, dataSetIndex: Int): Boolean {
        Log.e("MPAndroidChart", "removeEntry(...) not supported for CombinedData")
        return false
    }

    @Deprecated("")
    override fun removeEntry(xValue: kotlin.Float, dataSetIndex: Int): Boolean {
        Log.e("MPAndroidChart", "removeEntry(...) not supported for CombinedData")
        return false
    }
}
