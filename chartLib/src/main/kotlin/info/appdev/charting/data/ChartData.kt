package info.appdev.charting.data

import android.graphics.Typeface
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IDataSet
import timber.log.Timber
import java.io.Serializable

/**
 * Class that holds all relevant data that represents the chart. That involves at least one (or more) DataSets, and an array of x-values.
 */
@Suppress("unused")
abstract class ChartData<T> : Serializable where T : IDataSet<out BaseEntry<Float>, Float> {
    /**
     * maximum y-value in the value array across all axes
     */
    var yMax: Float = -Float.MAX_VALUE
        protected set

    /**
     * the minimum y-value in the value array across all axes
     */
    var yMin: Float = Float.MAX_VALUE
        protected set

    /**
     * maximum x-value in the value array
     */
    var xMax: Float = -Float.MAX_VALUE
        protected set

    /**
     * minimum x-value in the value array
     */
    var xMin: Float = Float.MAX_VALUE
        protected set

    protected var mLeftAxisMax: Float = -Float.MAX_VALUE

    protected var mLeftAxisMin: Float = Float.MAX_VALUE

    protected var mRightAxisMax: Float = -Float.MAX_VALUE

    protected var mRightAxisMin: Float = Float.MAX_VALUE

    /**
     * all DataSets the ChartData object represents
     */
    open var dataSets: MutableList<T> = mutableListOf()
        protected set

    constructor() {
        this.dataSets = ArrayList<T>()
    }

    constructor(vararg dataSets: T) {
        this.dataSets = dataSets.toMutableList()
        notifyDataChanged()
    }

    constructor(sets: MutableList<T>) {
        this.dataSets = sets
        notifyDataChanged()
    }

    /**
     * Call this method to let the ChartData know that the underlying data has
     * changed. Calling this performs all necessary recalculations needed when
     * the contained data has changed.
     */
    open fun notifyDataChanged() {
        calcMinMax()
    }

    /**
     * Calc minimum and maximum y-values over all DataSets.
     * Tell DataSets to recalculate their min and max y-values, this is only needed for autoScaleMinMax.
     *
     * @param fromX the x-value to start the calculation from
     * @param toX   the x-value to which the calculation should be performed
     */
    fun calcMinMaxY(fromX: Float, toX: Float) {
        for (set in this.dataSets) {
            set.calcMinMaxY(fromX, toX)
        }

        // apply the new data
        calcMinMax()
    }

    /**
     * Calc minimum and maximum values (both x and y) over all DataSets.
     */
    open fun calcMinMax() {
        this.yMax = -Float.MAX_VALUE
        this.yMin = Float.MAX_VALUE
        this.xMax = -Float.MAX_VALUE
        this.xMin = Float.MAX_VALUE

        for (set in this.dataSets) {
            calcMinMax(set)
        }

        mLeftAxisMax = -Float.MAX_VALUE
        mLeftAxisMin = Float.MAX_VALUE
        mRightAxisMax = -Float.MAX_VALUE
        mRightAxisMin = Float.MAX_VALUE

        // left axis
        val firstLeft = getFirstLeft(this.dataSets)

        if (firstLeft != null) {
            mLeftAxisMax = firstLeft.yMax
            mLeftAxisMin = firstLeft.yMin

            for (dataSet in this.dataSets) {
                if (dataSet.axisDependency == AxisDependency.LEFT) {
                    if (dataSet.yMin < mLeftAxisMin) {
                        mLeftAxisMin = dataSet.yMin
                    }

                    if (dataSet.yMax > mLeftAxisMax) {
                        mLeftAxisMax = dataSet.yMax
                    }
                }
            }
        }

        // right axis
        val firstRight = getFirstRight(this.dataSets)

        if (firstRight != null) {
            mRightAxisMax = firstRight.yMax
            mRightAxisMin = firstRight.yMin

            for (dataSet in this.dataSets) {
                if (dataSet.axisDependency == AxisDependency.RIGHT) {
                    if (dataSet.yMin < mRightAxisMin) {
                        mRightAxisMin = dataSet.yMin
                    }

                    if (dataSet.yMax > mRightAxisMax) {
                        mRightAxisMax = dataSet.yMax
                    }
                }
            }
        }
    }

    /**
     * The number of LineDataSets this object contains
     */
    val dataSetCount: Int
        get() {
            return dataSets.size
        }

    /**
     * Returns the minimum y-value for the specified axis.
     */
    fun getYMin(axis: AxisDependency?): Float {
        return if (axis == AxisDependency.LEFT) {
            if (mLeftAxisMin == Float.MAX_VALUE) {
                mRightAxisMin
            } else {
                mLeftAxisMin
            }
        } else {
            if (mRightAxisMin == Float.MAX_VALUE) {
                mLeftAxisMin
            } else {
                mRightAxisMin
            }
        }
    }

    /**
     * Returns the maximum y-value for the specified axis.
     */
    fun getYMax(axis: AxisDependency?): Float {
        return if (axis == AxisDependency.LEFT) {
            if (mLeftAxisMax == -Float.MAX_VALUE) {
                mRightAxisMax
            } else {
                mLeftAxisMax
            }
        } else {
            if (mRightAxisMax == -Float.MAX_VALUE) {
                mLeftAxisMax
            } else {
                mRightAxisMax
            }
        }
    }

    /**
     * Retrieve the index of a DataSet with a specific label from the ChartData.
     * Search can be case sensitive or not. IMPORTANT: This method does
     * calculations at runtime, do not over-use in performance critical
     * situations.
     *
     * @param dataSets   the DataSet array to search
     * @param ignoreCase if true, the search is not case-sensitive
     */
    protected fun getDataSetIndexByLabel(
        dataSets: MutableList<@UnsafeVariance T>, label: String,
        ignoreCase: Boolean
    ): Int {
        if (ignoreCase) {
            for (i in dataSets.indices) {
                if (label.equals(dataSets[i].label, ignoreCase = true)) {
                    return i
                }
            }
        } else {
            for (i in dataSets.indices) {
                if (label == dataSets[i].label) {
                    return i
                }
            }
        }

        return -1
    }

    /**
     * The labels of all DataSets this ChartData object contains.
     */
    val dataSetLabels: Array<String?>
        get() {
            val types = arrayOfNulls<String>(dataSets.size)

            for (i in dataSets.indices) {
                types[i] = dataSets[i].label
            }

            return types
        }

    /**
     * Get the Entry for a corresponding highlight object
     * @return the entry that is highlighted
     */
    open fun getEntryForHighlight(highlight: Highlight): BaseEntry<Float>? {
        return if (highlight.dataSetIndex >= dataSets.size) {
            null
        } else {
            dataSets[highlight.dataSetIndex].getEntryForXValue(highlight.x, highlight.y)
        }
    }

    /**
     * Returns the DataSet object with the given label. Search can be case
     * sensitive or not. IMPORTANT: This method does calculations at runtime.
     * Use with care in performance critical situations.
     */
    open fun getDataSetByLabel(label: String, ignoreCase: Boolean): T? {
        val index = getDataSetIndexByLabel(this.dataSets, label, ignoreCase)

        return if (index < 0 || index >= dataSets.size) {
            null
        } else {
            dataSets[index]
        }
    }

    open fun getDataSetByIndex(index: Int): T? {
        if (index < 0 || index >= dataSets.size) {
            return null
        }

        return dataSets[index]
    }

    /**
     * Adds a DataSet dynamically.
     */
    fun addDataSet(d: @UnsafeVariance T?) {
        if (d == null) {
            return
        }

        calcMinMax(d)

        dataSets.add(d)
    }

    /**
     * Removes the given DataSet from this data object. Also recalculates all
     * minimum and maximum values. Returns true if a DataSet was removed, false
     * if no DataSet could be removed.
     */
    open fun removeDataSet(d: @UnsafeVariance T?): Boolean {
        if (d == null) {
            return false
        }

        val removed = dataSets.remove(d)

        // if a DataSet was removed
        if (removed) {
            notifyDataChanged()
        }

        return removed
    }

    /**
     * Removes the DataSet at the given index in the DataSet array from the data
     * object. Also recalculates all minimum and maximum values. Returns true if
     * a DataSet was removed, false if no DataSet could be removed.
     */
    open fun removeDataSet(index: Int): Boolean {
        if (index >= dataSets.size || index < 0) {
            return false
        }

        val set = dataSets[index]
        return removeDataSet(set)
    }

    /**
     * Adds an Entry to the DataSet at the specified index.
     * Entries are added to the end of the list.
     */
    @Suppress("UNCHECKED_CAST")
    fun addEntry(entry: Entry, dataSetIndex: Int) {
        if (dataSets.size > dataSetIndex && dataSetIndex >= 0) {
            val set: T = dataSets[dataSetIndex]
            // add the entry to the dataset
            // We need to cast here because T is covariant (out) but addEntry needs to consume T
            val dataSet = set as IDataSet<Entry, Float>
            if (!dataSet.addEntry(entry)) {
                return
            }

            calcMinMax(entry, set.axisDependency)
        } else {
            Timber.e("Cannot add Entry because dataSetIndex too high or too low.")
        }
    }

    /**
     * Adjusts the current minimum and maximum values based on the provided Entry object.
     */
    protected fun calcMinMax(e: Entry, axis: AxisDependency?) {
        if (this.yMax < e.y) {
            this.yMax = e.y
        }
        if (this.yMin > e.y) {
            this.yMin = e.y
        }

        if (this.xMax < e.x) {
            this.xMax = e.x
        }
        if (this.xMin > e.x) {
            this.xMin = e.x
        }

        if (axis == AxisDependency.LEFT) {
            if (mLeftAxisMax < e.y) {
                mLeftAxisMax = e.y
            }
            if (mLeftAxisMin > e.y) {
                mLeftAxisMin = e.y
            }
        } else {
            if (mRightAxisMax < e.y) {
                mRightAxisMax = e.y
            }
            if (mRightAxisMin > e.y) {
                mRightAxisMin = e.y
            }
        }
    }

    /**
     * Adjusts the minimum and maximum values based on the given DataSet.
     */
    protected fun calcMinMax(d: @UnsafeVariance T?) {
        if (this.yMax < d!!.yMax) {
            this.yMax = d.yMax
        }
        if (this.yMin > d.yMin) {
            this.yMin = d.yMin
        }

        if (this.xMax < d.xMax) {
            this.xMax = d.xMax
        }
        if (this.xMin > d.xMin) {
            this.xMin = d.xMin
        }

        if (d.axisDependency == AxisDependency.LEFT) {
            if (mLeftAxisMax < d.yMax) {
                mLeftAxisMax = d.yMax
            }
            if (mLeftAxisMin > d.yMin) {
                mLeftAxisMin = d.yMin
            }
        } else {
            if (mRightAxisMax < d.yMax) {
                mRightAxisMax = d.yMax
            }
            if (mRightAxisMin > d.yMin) {
                mRightAxisMin = d.yMin
            }
        }
    }

    /**
     * Removes the given Entry object from the DataSet at the specified index.
     */
    @Suppress("UNCHECKED_CAST")
    open fun removeEntry(entry: BaseEntry<Float>, dataSetIndex: Int): Boolean {
        // entry null, out of bounds
        if (dataSetIndex >= dataSets.size) {
            return false
        }

        val set: T = dataSets[dataSetIndex]

        // remove the entry from the dataset
        val dataSet = set as IDataSet<BaseEntry<Float>, Float>
        val removed: Boolean = dataSet.removeEntry(entry)

        if (removed) {
            notifyDataChanged()
        }

        return removed
    }

    /**
     * Removes the Entry object closest to the given DataSet at the
     * specified index. Returns true if an Entry was removed, false if no Entry
     * was found that meets the specified requirements.
     */
    open fun removeEntry(xValue: Float, dataSetIndex: Int): Boolean {
        if (dataSetIndex >= dataSets.size) {
            return false
        }

        val dataSet: IDataSet<*, *> = dataSets[dataSetIndex]
        val entry = dataSet.getEntryForXValue(xValue, Float.NaN) as? Entry ?: return false

        return removeEntry(entry, dataSetIndex)
    }

    /**
     * Returns the DataSet that contains the provided Entry, or null, if no DataSet contains this Entry.
     */
    fun getDataSetForEntry(e: Entry): T? {
        for (i in dataSets.indices) {
            val set = dataSets[i]

            for (j in 0..<set.entryCount) {
                if (e.equalTo(set.getEntryForXValue(e.x, e.y) as? Entry)) {
                    return set
                }
            }
        }

        return null
    }

    /**
     * All colors used across all DataSet objects this object represents.
     */
    val colors: IntArray
        get() {
            var clrCount = 0

            for (i in dataSets.indices) {
                clrCount += dataSets[i].colors.size
            }

            val colorArray = IntArray(clrCount)
            var cnt = 0

            for (i in dataSets.indices) {
                val clrs = dataSets[i].colors

                for (clr in clrs) {
                    colorArray[cnt] = clr
                    cnt++
                }
            }
            return colorArray
        }

    /**
     * Returns the index of the provided DataSet in the DataSet array of this data object, or -1 if it does not exist.
     */
    fun getIndexOfDataSet(dataSet: @UnsafeVariance T?): Int {
        return dataSets.indexOf(dataSet)
    }

    /**
     * Returns the first DataSet from the datasets-array that has it's dependency on the left axis.
     * Returns null if no DataSet with left dependency could be found.
     */
    protected fun getFirstLeft(sets: MutableList<@UnsafeVariance T>): T? {
        for (dataSet in sets) {
            if (dataSet.axisDependency == AxisDependency.LEFT) {
                return dataSet
            }
        }
        return null
    }

    /**
     * Returns the first DataSet from the datasets-array that has it's dependency on the right axis.
     * Returns null if no DataSet with right dependency could be found.
     */
    fun getFirstRight(sets: MutableList<@UnsafeVariance T>): T? {
        for (dataSet in sets) {
            if (dataSet.axisDependency == AxisDependency.RIGHT) {
                return dataSet
            }
        }
        return null
    }

    /**
     * Sets a custom IValueFormatter for all DataSets this data object contains.
     */
    fun setValueFormatter(f: IValueFormatter) {
        for (set in this.dataSets) {
            set.valueFormatter = f
        }
    }

    /**
     * Sets the color of the value-text (color in which the value-labels are
     * drawn) for all DataSets this data object contains.
     */
    fun setValueTextColor(color: Int) {
        for (set in this.dataSets) {
            set.valueTextColor = color
        }
    }

    /**
     * Sets the same list of value-colors for all DataSets this
     * data object contains.
     */
    fun setValueTextColors(colors: MutableList<Int>) {
        for (set in this.dataSets) {
            set.valueTextColors = colors
        }
    }

    /**
     * Sets the Typeface for all value-labels for all DataSets this data object contains.
     */
    fun setValueTypeface(tf: Typeface?) {
        for (set in this.dataSets) {
            set.valueTypeface = tf
        }
    }

    /**
     * Sets the size (in dp) of the value-text for all DataSets this data object contains.
     */
    fun setValueTextSize(size: Float) {
        for (set in this.dataSets) {
            set.valueTextSize = size
        }
    }

    /**
     * Enables / disables drawing values (value-text) for all DataSets this data object contains.
     */
    fun setDrawValues(enabled: Boolean) {
        for (set in this.dataSets) {
            set.isDrawValues = enabled
        }
    }

    /**
     * Enables / disables highlighting values for all DataSets this data object
     * contains. If set to true, this means that values can be highlighted programmatically or by touch gesture.
     */
    var isHighlight: Boolean
        get() {
            for (set in this.dataSets) {
                if (!set.isHighlight) {
                    return false
                }
            }
            return true
        }
        set(value) {
            for (set in this.dataSets) {
                set.isHighlight = value
            }
        }

    /**
     * Clears this data object from all DataSets and removes all Entries.
     * Don't forget to invalidate the chart after this.
     */
    fun clearValues() {
        dataSets.clear()
        notifyDataChanged()
    }

    /**
     * Checks if this data object contains the specified DataSet. Returns true
     * if so, false if not.
     */
    fun contains(dataSet: @UnsafeVariance T?): Boolean {
        for (set in this.dataSets) {
            if (set == dataSet) {
                return true
            }
        }

        return false
    }

    /**
     * The total entry count across all DataSet objects this data object contains.
     */
    val entryCount: Int
        get() {
            var count = 0

            for (set in this.dataSets) {
                count += set.entryCount
            }

            return count
        }

    /**
     * The DataSet object with the maximum number of entries or null if there are no DataSets.
     */
    val maxEntryCountSet: T?
        get() {
            if (dataSets.isEmpty()) {
                return null
            }

            var max = dataSets[0]

            for (set in this.dataSets) {
                if (set.entryCount > max.entryCount) {
                    max = set
                }
            }

            return max
        }
}
