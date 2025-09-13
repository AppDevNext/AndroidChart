package com.github.mikephil.charting.data

import android.graphics.Typeface
import android.util.Log
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import java.io.Serializable

/**
 * Class that holds all relevant data that represents the chart. That involves
 * at least one (or more) DataSets, and an array of x-values.
 *
 * @author Philipp Jahoda
 */
abstract class ChartData<E: Entry, T : IDataSet<E>>(
    /**
     * Returns all DataSet objects this ChartData object holds.
     *
     * @return
     */
    /**
     * array that holds all DataSets the ChartData object represents
     */
    open val dataSets: MutableList<T> = arrayListOf(),
) : Serializable {
    /**
     * Returns the greatest y-value the data object contains.
     *
     * @return
     */
    /**
     * maximum y-value in the value array across all axes
     */
    var yMax: Float = -Float.Companion.MAX_VALUE
        protected set

    /**
     * Returns the smallest y-value the data object contains.
     *
     * @return
     */
    /**
     * the minimum y-value in the value array across all axes
     */
    var yMin: Float = Float.Companion.MAX_VALUE
        protected set

    /**
     * Returns the maximum x-value this data object contains.
     *
     * @return
     */
    /**
     * maximum x-value in the value array
     */
    var xMax: Float = -Float.Companion.MAX_VALUE
        protected set

    /**
     * Returns the minimum x-value this data object contains.
     *
     * @return
     */
    /**
     * minimum x-value in the value array
     */
    var xMin: Float = Float.Companion.MAX_VALUE
        protected set


    protected var mLeftAxisMax: Float = -Float.Companion.MAX_VALUE

    protected var mLeftAxisMin: Float = Float.Companion.MAX_VALUE

    protected var mRightAxisMax: Float = -Float.Companion.MAX_VALUE

    protected var mRightAxisMin: Float = Float.Companion.MAX_VALUE

    /**
     * Constructor taking single or multiple DataSet objects.
     *
     * @param dataSets
     */
    constructor(vararg dataSets: T): this(dataSets = dataSets.toMutableList())

    init {
        if (dataSets.isNotEmpty()) {
            notifyDataChanged()
        }
    }

    /**
     * Created because Arrays.asList(...) does not support modification.
     *
     * @param array
     * @return
     */
    private fun arrayToList(array: Array<T?>): MutableList<T?> {
        val list: MutableList<T?> = ArrayList()

        for (set in array) {
            list.add(set)
        }

        return list
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
            set!!.calcMinMaxY(fromX, toX)
        }

        // apply the new data
        calcMinMax()
    }

    /**
     * Calc minimum and maximum values (both x and y) over all DataSets.
     */
    open fun calcMinMax() {
        this.yMax = -Float.Companion.MAX_VALUE
        this.yMin = Float.Companion.MAX_VALUE
        this.xMax = -Float.Companion.MAX_VALUE
        this.xMin = Float.Companion.MAX_VALUE

        for (set in this.dataSets) {
            calcMinMax(set)
        }

        mLeftAxisMax = -Float.Companion.MAX_VALUE
        mLeftAxisMin = Float.Companion.MAX_VALUE
        mRightAxisMax = -Float.Companion.MAX_VALUE
        mRightAxisMin = Float.Companion.MAX_VALUE

        // left axis
        val firstLeft = getFirstLeft(this.dataSets)

        if (firstLeft != null) {
            mLeftAxisMax = firstLeft.yMax
            mLeftAxisMin = firstLeft.yMin

            for (dataSet in this.dataSets) {
                if (dataSet!!.axisDependency == AxisDependency.LEFT) {
                    if (dataSet.yMin < mLeftAxisMin) mLeftAxisMin = dataSet.yMin

                    if (dataSet.yMax > mLeftAxisMax) mLeftAxisMax = dataSet.yMax
                }
            }
        }

        // right axis
        val firstRight = getFirstRight(this.dataSets)

        if (firstRight != null) {
            mRightAxisMax = firstRight.yMax
            mRightAxisMin = firstRight.yMin

            for (dataSet in this.dataSets) {
                if (dataSet!!.axisDependency == AxisDependency.RIGHT) {
                    if (dataSet.yMin < mRightAxisMin) mRightAxisMin = dataSet.yMin

                    if (dataSet.yMax > mRightAxisMax) mRightAxisMax = dataSet.yMax
                }
            }
        }
    }

    /** ONLY GETTERS AND SETTERS BELOW THIS  */
    val dataSetCount: Int
        /**
         * returns the number of LineDataSets this object contains
         *
         * @return
         */
        get() {
            return dataSets.size
        }

    /**
     * Returns the minimum y-value for the specified axis.
     *
     * @param axis
     * @return
     */
    fun getYMin(axis: AxisDependency?): Float {
        return if (axis == AxisDependency.LEFT) {
            if (mLeftAxisMin == Float.Companion.MAX_VALUE) {
                mRightAxisMin
            } else mLeftAxisMin
        } else {
            if (mRightAxisMin == Float.Companion.MAX_VALUE) {
                mLeftAxisMin
            } else mRightAxisMin
        }
    }

    /**
     * Returns the maximum y-value for the specified axis.
     *
     * @param axis
     * @return
     */
    fun getYMax(axis: AxisDependency?): Float {
        if (axis == AxisDependency.LEFT) {
            if (mLeftAxisMax == -Float.Companion.MAX_VALUE) {
                return mRightAxisMax
            } else return mLeftAxisMax
        } else {
            if (mRightAxisMax == -Float.Companion.MAX_VALUE) {
                return mLeftAxisMax
            } else return mRightAxisMax
        }
    }

    /**
     * Retrieve the index of a DataSet with a specific label from the ChartData.
     * Search can be case sensitive or not. IMPORTANT: This method does
     * calculations at runtime, do not over-use in performance critical
     * situations.
     *
     * @param dataSets   the DataSet array to search
     * @param label
     * @param ignorecase if true, the search is not case-sensitive
     * @return
     */
    protected fun getDataSetIndexByLabel(
        dataSets: MutableList<out T?>, label: String,
        ignorecase: Boolean
    ): Int {
        if (ignorecase) {
            for (i in dataSets.indices) if (label.equals(dataSets[i]!!.label, ignoreCase = true)) return i
        } else {
            for (i in dataSets.indices) if (label == dataSets[i]!!.label) return i
        }

        return -1
    }

    val dataSetLabels: Array<String?>
        /**
         * Returns the labels of all DataSets as a string array.
         *
         * @return
         */
        get() {
            val types = arrayOfNulls<String>(dataSets.size)

            for (i in dataSets.indices) {
                types[i] = dataSets[i]!!.label
            }

            return types
        }

    /**
     * Get the Entry for a corresponding highlight object
     *
     * @param highlight
     * @return the entry that is highlighted
     */
    open fun getEntryForHighlight(highlight: Highlight): E? {
        return if (highlight.dataSetIndex >= dataSets.size) null
        else {
            dataSets[highlight.dataSetIndex]!!.getEntryForXValue(highlight.x, highlight.y)
        }
    }

    /**
     * Returns the DataSet object with the given label. Search can be case
     * sensitive or not. IMPORTANT: This method does calculations at runtime.
     * Use with care in performance critical situations.
     *
     * @param label
     * @param ignorecase
     * @return
     */
    open fun getDataSetByLabel(label: String, ignorecase: Boolean): T? {
        val index = getDataSetIndexByLabel(this.dataSets, label, ignorecase)

        return if (index < 0 || index >= dataSets.size) null
        else dataSets[index]
    }

    open fun getDataSetByIndex(index: Int): T {
        if (index < 0 || index >= dataSets.size) throw ArrayIndexOutOfBoundsException(index)

        return dataSets[index]
    }

    /**
     * Adds a DataSet dynamically.
     *
     * @param d
     */
    fun addDataSet(d: T?) {
        if (d == null) return

        calcMinMax(d)

        dataSets.add(d)
    }

    /**
     * Removes the given DataSet from this data object. Also recalculates all
     * minimum and maximum values. Returns true if a DataSet was removed, false
     * if no DataSet could be removed.
     *
     * @param d
     */
    open fun removeDataSet(d: IDataSet<out Entry>?): Boolean {
        if (d == null) return false

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
     *
     * @param index
     */
    open fun removeDataSet(index: Int): Boolean {
        if (index >= dataSets.size || index < 0) return false

        val set = dataSets[index]
        return removeDataSet(set)
    }

    /**
     * Adds an Entry to the DataSet at the specified index.
     * Entries are added to the end of the list.
     *
     * @param e
     * @param dataSetIndex
     */
    fun addEntry(e: E, dataSetIndex: Int) {
        if (dataSets.size > dataSetIndex && dataSetIndex >= 0) {
            val set = dataSets[dataSetIndex]
            // add the entry to the dataset
            if (!set.addEntry(e)) return

            calcMinMax(e, set.axisDependency)
        } else {
            Log.e("addEntry", "Cannot add Entry because dataSetIndex too high or too low.")
        }
    }

    /**
     * Adjusts the current minimum and maximum values based on the provided Entry object.
     *
     * @param e
     * @param axis
     */
    protected fun calcMinMax(e: E, axis: AxisDependency?) {
        if (this.yMax < e.y) this.yMax = e.y
        if (this.yMin > e.y) this.yMin = e.y

        if (this.xMax < e.x) this.xMax = e.x
        if (this.xMin > e.x) this.xMin = e.x

        if (axis == AxisDependency.LEFT) {
            if (mLeftAxisMax < e.y) mLeftAxisMax = e.y
            if (mLeftAxisMin > e.y) mLeftAxisMin = e.y
        } else {
            if (mRightAxisMax < e.y) mRightAxisMax = e.y
            if (mRightAxisMin > e.y) mRightAxisMin = e.y
        }
    }

    /**
     * Adjusts the minimum and maximum values based on the given DataSet.
     *
     * @param d
     */
    protected fun calcMinMax(d: T?) {
        if (this.yMax < d!!.yMax) this.yMax = d.yMax
        if (this.yMin > d.yMin) this.yMin = d.yMin

        if (this.xMax < d.xMax) this.xMax = d.xMax
        if (this.xMin > d.xMin) this.xMin = d.xMin

        if (d.axisDependency == AxisDependency.LEFT) {
            if (mLeftAxisMax < d.yMax) mLeftAxisMax = d.yMax
            if (mLeftAxisMin > d.yMin) mLeftAxisMin = d.yMin
        } else {
            if (mRightAxisMax < d.yMax) mRightAxisMax = d.yMax
            if (mRightAxisMin > d.yMin) mRightAxisMin = d.yMin
        }
    }

    /**
     * Removes the given Entry object from the DataSet at the specified index.
     *
     * @param e
     * @param dataSetIndex
     */
    open fun removeEntry(e: E?, dataSetIndex: Int): Boolean {
        // entry null, outofbounds

        if (e == null || dataSetIndex >= dataSets.size) return false

        val set = dataSets[dataSetIndex]

        if (set != null) {
            // remove the entry from the dataset
            val removed: Boolean = set.removeEntry(e)

            if (removed) {
                notifyDataChanged()
            }

            return removed
        } else return false
    }

    /**
     * Removes the Entry object closest to the given DataSet at the
     * specified index. Returns true if an Entry was removed, false if no Entry
     * was found that meets the specified requirements.
     *
     * @param xValue
     * @param dataSetIndex
     * @return
     */
    open fun removeEntry(xValue: Float, dataSetIndex: Int): Boolean {
        if (dataSetIndex >= dataSets.size) return false

        val dataSet = dataSets[dataSetIndex]!!
        val e = dataSet.getEntryForXValue(xValue, Float.Companion.NaN)

        if (e == null) return false

        return removeEntry(e, dataSetIndex)
    }

    /**
     * Returns the DataSet that contains the provided Entry, or null, if no
     * DataSet contains this Entry.
     *
     * @param e
     * @return
     */
    fun getDataSetForEntry(e: E?): T? {
        if (e == null) return null

        for (i in dataSets.indices) {
            val set = dataSets[i]

            for (j in 0..<set!!.entryCount) {
                if (e.equalTo(set.getEntryForXValue(e.x, e.y))) return set
            }
        }

        return null
    }

    val colors: IntArray?
        /**
         * Returns all colors used across all DataSet objects this object
         * represents.
         *
         * @return
         */
        get() {
            var clrcnt = 0

            for (i in dataSets.indices) {
                clrcnt += dataSets[i]!!.colors!!.size
            }

            val colors = IntArray(clrcnt)
            var cnt = 0

            for (i in dataSets.indices) {
                val clrs = dataSets[i]!!.colors!!

                for (clr in clrs) {
                    colors[cnt] = clr!!
                    cnt++
                }
            }

            return colors
        }

    /**
     * Returns the index of the provided DataSet in the DataSet array of this data object, or -1 if it does not exist.
     *
     * @param dataSet
     * @return
     */
    fun getIndexOfDataSet(dataSet: T?): Int {
        return dataSets.indexOf(dataSet)
    }

    /**
     * Returns the first DataSet from the datasets-array that has it's dependency on the left axis.
     * Returns null if no DataSet with left dependency could be found.
     *
     * @return
     */
    protected fun getFirstLeft(sets: MutableList<out T?>): T? {
        for (dataSet in sets) {
            if (dataSet!!.axisDependency == AxisDependency.LEFT) return dataSet
        }
        return null
    }

    /**
     * Returns the first DataSet from the datasets-array that has it's dependency on the right axis.
     * Returns null if no DataSet with right dependency could be found.
     *
     * @return
     */
    fun getFirstRight(sets: MutableList<out T?>): T? {
        for (dataSet in sets) {
            if (dataSet!!.axisDependency == AxisDependency.RIGHT) return dataSet
        }
        return null
    }

    /**
     * Sets a custom IValueFormatter for all DataSets this data object contains.
     *
     * @param f
     */
    fun setValueFormatter(f: IValueFormatter?) {
        if (f == null) return
        else {
            for (set in this.dataSets) {
                set!!.valueFormatter = f
            }
        }
    }

    /**
     * Sets the color of the value-text (color in which the value-labels are
     * drawn) for all DataSets this data object contains.
     *
     * @param color
     */
    fun setValueTextColor(color: Int) {
        for (set in this.dataSets) {
            set!!.valueTextColor = color
        }
    }

    /**
     * Sets the same list of value-colors for all DataSets this
     * data object contains.
     *
     * @param colors
     */
    fun setValueTextColors(colors: MutableList<Int>) {
        for (set in this.dataSets) {
            set.setValueTextColors(colors)
        }
    }

    /**
     * Sets the Typeface for all value-labels for all DataSets this data object
     * contains.
     *
     * @param tf
     */
    fun setValueTypeface(tf: Typeface?) {
        for (set in this.dataSets) {
            set.valueTypeface = tf
        }
    }

    /**
     * Sets the size (in dp) of the value-text for all DataSets this data object
     * contains.
     *
     * @param size
     */
    fun setValueTextSize(size: Float) {
        for (set in this.dataSets) {
            set.valueTextSize = size
        }
    }

    /**
     * Enables / disables drawing values (value-text) for all DataSets this data
     * object contains.
     *
     * @param enabled
     */
    fun setDrawValues(enabled: Boolean) {
        for (set in this.dataSets) {
            set.isDrawValuesEnabled = enabled
        }
    }

    var isHighlightEnabled: Boolean
        /**
         * Returns true if highlighting of all underlying values is enabled, false
         * if not.
         *
         * @return
         */
        get() {
            for (set in this.dataSets) {
                if (!set!!.isHighlightEnabled) return false
            }
            return true
        }
        /**
         * Enables / disables highlighting values for all DataSets this data object
         * contains. If set to true, this means that values can
         * be highlighted programmatically or by touch gesture.
         */
        set(enabled) {
            for (set in this.dataSets) {
                set!!.isHighlightEnabled = enabled
            }
        }

    /**
     * Clears this data object from all DataSets and removes all Entries. Don't
     * forget to invalidate the chart after this.
     */
    fun clearValues() {
        dataSets.clear()
        notifyDataChanged()
    }

    /**
     * Checks if this data object contains the specified DataSet. Returns true
     * if so, false if not.
     *
     * @param dataSet
     * @return
     */
    fun contains(dataSet: T?): Boolean {
        for (set in this.dataSets) {
            if (set == dataSet) return true
        }

        return false
    }

    val entryCount: Int
        /**
         * Returns the total entry count across all DataSet objects this data object contains.
         *
         * @return
         */
        get() {
            var count = 0

            for (set in this.dataSets) {
                count += set!!.entryCount
            }

            return count
        }

    val maxEntryCountSet: T?
        /**
         * Returns the DataSet object with the maximum number of entries or null if there are no DataSets.
         *
         * @return
         */
        get() {
            if (dataSets.isEmpty()) return null

            var max = dataSets[0]

            for (set in this.dataSets) {
                if (set!!.entryCount > max!!.entryCount) max = set
            }

            return max
        }
}
