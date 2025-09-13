package com.github.mikephil.charting.data

import java.io.Serializable
import kotlin.math.abs

/**
 * The DataSet class represents one group or type of entries (Entry) in the
 * Chart that belong together. It is designed to logically separate different
 * groups of values inside the Chart (e.g. the values for a specific line in the
 * LineChart, or the values of a specific group of bars in the BarChart).
 *
 * @author Philipp Jahoda
 */
abstract class DataSet<T : Entry>(
    /**
     * the entries that this DataSet represents / holds together
     */
    protected var mEntries: MutableList<T>, label: String
) : BaseDataSet<T>(label), Serializable {
    /**
     * maximum y-value in the value array
     */
    protected var mYMax: Float = -Float.MAX_VALUE

    /**
     * minimum y-value in the value array
     */
    protected var mYMin: Float = Float.MAX_VALUE

    /**
     * maximum x-value in the value array
     */
    protected var mXMax: Float = -Float.Companion.MAX_VALUE

    /**
     * minimum x-value in the value array
     */
    protected var mXMin: Float = Float.Companion.MAX_VALUE


    /**
     * Creates a new DataSet object with the given values (entries) it represents. Also, a
     * label that describes the DataSet can be specified. The label can also be
     * used to retrieve the DataSet from a ChartData object.
     *
     * @param mEntries
     * @param label
     */
    init {
        calcMinMax()
    }

    override fun calcMinMax() {
        mYMax = -Float.Companion.MAX_VALUE
        mYMin = Float.Companion.MAX_VALUE
        mXMax = -Float.Companion.MAX_VALUE
        mXMin = Float.Companion.MAX_VALUE

        if (mEntries.isEmpty()) return

        for (e in mEntries) {
            calcMinMax(e)
        }
    }

    override fun calcMinMaxY(fromX: Float, toX: Float) {
        mYMax = -Float.Companion.MAX_VALUE
        mYMin = Float.Companion.MAX_VALUE

        if (mEntries.isEmpty()) return

        val indexFrom = getEntryIndex(fromX, Float.Companion.NaN, Rounding.DOWN)
        val indexTo = getEntryIndex(toX, Float.Companion.NaN, Rounding.UP)

        if (indexTo < indexFrom) return

        for (i in indexFrom..indexTo) {
            // only recalculate y

            calcMinMaxY(mEntries[i])
        }
    }

    /**
     * Updates the min and max x and y value of this DataSet based on the given Entry.
     *
     * @param e
     */
    protected open fun calcMinMax(e: T) {
        calcMinMaxX(e)

        calcMinMaxY(e)
    }

    protected fun calcMinMaxX(e: T) {
        if (e.x < mXMin) mXMin = e.x

        if (e.x > mXMax) mXMax = e.x
    }

    protected open fun calcMinMaxY(e: T) {
        if (e.y < mYMin) mYMin = e.y

        if (e.y > mYMax) mYMax = e.y
    }

    override val entryCount: Int
        get() = mEntries.size

    @get:Deprecated("")
    @set:Deprecated("")
    var values: MutableList<T>
        /**
         * This method is deprecated.
         * Use getEntries() instead.
         *
         * @return
         */
        get() = mEntries
        /**
         * This method is deprecated.
         * Use setEntries(...) instead.
         *
         * @param values
         */
        set(values) {
            this.entries = values
        }

    var entries: MutableList<T>
        /**
         * Returns the array of entries that this DataSet represents.
         *
         * @return
         */
        get() = mEntries
        /**
         * Sets the array of entries that this DataSet represents, and calls notifyDataSetChanged()
         *
         * @return
         */
        set(entries) {
            mEntries = entries
            notifyDataSetChanged()
        }

    /**
     * Provides an exact copy of the DataSet this method is used on.
     *
     * @return
     */
    abstract fun copy(): DataSet<T>

    /**
     * @param dataSet
     */
    protected fun copy(dataSet: DataSet<*>) {
        super.copy(dataSet)
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        buffer.append(toSimpleString())
        for (i in mEntries.indices) {
            buffer.append(mEntries[i].toString() + " ")
        }
        return buffer.toString()
    }

    /**
     * Returns a simple string representation of the DataSet with the type and
     * the number of Entries.
     *
     * @return
     */
    fun toSimpleString(): String {
        val buffer = StringBuffer()
        buffer.append(
            "DataSet, label: " + (label) + ", entries: " + mEntries.size +
                    "\n"
        )
        return buffer.toString()
    }

    override val yMin: Float
        get() = mYMin

    override val yMax: Float
        get() = mYMax

    override val xMin: Float
        get() = mXMin

    override val xMax: Float
        get() = mXMax

    override fun addEntryOrdered(e: T) {
        calcMinMax(e)

        if (mEntries.isNotEmpty() && mEntries[mEntries.size - 1].x > e.x) {
            val closestIndex = getEntryIndex(e.x, e.y, Rounding.UP)
            mEntries.add(closestIndex, e)
        } else {
            mEntries.add(e)
        }
    }

    override fun clear() {
        mEntries.clear()
        notifyDataSetChanged()
    }

    override fun addEntry(e: T): Boolean {

        val values = this.entries

        calcMinMax(e)

        // add the entry
        return values.add(e)
    }

    override fun removeEntry(e: T?): Boolean {
        if (e == null) return false

        // remove the entry
        val removed = mEntries.remove(e)

        if (removed) {
            calcMinMax()
        }

        return removed
    }

    override fun getEntryIndex(e: Entry): Int {
        return mEntries.indexOf(e)
    }

    override fun getEntryForXValue(xValue: Float, closestToY: Float, rounding: Rounding?): T? {
        val index = getEntryIndex(xValue, closestToY, rounding)
        if (index > -1) return mEntries[index]
        return null
    }

    override fun getEntryForXValue(xValue: Float, closestToY: Float): T? {
        return getEntryForXValue(xValue, closestToY, Rounding.CLOSEST)
    }

    override fun getEntryForIndex(index: Int): T {
        if (index < 0) throw ArrayIndexOutOfBoundsException(index)
        if (index >= mEntries.size) throw ArrayIndexOutOfBoundsException(index)
        return mEntries[index]
    }

    override fun getEntryIndex(xValue: Float, closestToY: Float, rounding: Rounding?): Int {
        if (mEntries.isEmpty()) return -1

        var low = 0
        var high = mEntries.size - 1
        var closest = high

        while (low < high) {
            val m = low + (high - low) / 2

            val currentEntry: Entry? = mEntries[m]

            if (currentEntry != null) {
                val nextEntry: Entry? = mEntries[m + 1]

                if (nextEntry != null) {
                    val d1 = currentEntry.x - xValue
                    val d2 = nextEntry.x - xValue
                    val ad1 = abs(d1)
                    val ad2 = abs(d2)

                    if (ad2 < ad1) {
                        // [m + 1] is closer to xValue
                        // Search in an higher place
                        low = m + 1
                    } else if (ad1 < ad2) {
                        // [m] is closer to xValue
                        // Search in a lower place
                        high = m
                    } else {
                        // We have multiple sequential x-value with same distance

                        if (d1 >= 0.0) {
                            // Search in a lower place
                            high = m
                        } else if (d1 < 0.0) {
                            // Search in an higher place
                            low = m + 1
                        }
                    }

                    closest = high
                }
            }
        }

        if (closest != -1) {
            val closestEntry: Entry? = mEntries[closest]
            if (closestEntry != null) {
                val closestXValue = closestEntry.x
                if (rounding == Rounding.UP) {
                    // If rounding up, and found x-value is lower than specified x, and we can go upper...
                    if (closestXValue < xValue && closest < mEntries.size - 1) {
                        ++closest
                    }
                } else if (rounding == Rounding.DOWN) {
                    // If rounding down, and found x-value is upper than specified x, and we can go lower...
                    if (closestXValue > xValue && closest > 0) {
                        --closest
                    }
                }

                // Search by closest to y-value
                if (!closestToY.isNaN()) {
                    while (closest > 0 && mEntries[closest - 1].x == closestXValue) closest -= 1

                    var closestYValue = closestEntry.y
                    var closestYIndex = closest

                    while (true) {
                        closest += 1
                        if (closest >= mEntries.size) break

                        val value: Entry? = mEntries[closest]

                        if (value == null) {
                            continue
                        }

                        if (value.x != closestXValue) break

                        if (abs(value.y - closestToY) <= abs(closestYValue - closestToY)) {
                            closestYValue = closestToY
                            closestYIndex = closest
                        }
                    }

                    closest = closestYIndex
                }
            }
        }
        return closest
    }

    override fun getEntriesForXValue(xValue: Float): MutableList<T> {
        val entries: MutableList<T> = ArrayList()

        var low = 0
        var high = mEntries.size - 1

        while (low <= high) {
            var m = (high + low) / 2
            var entry = mEntries[m]

            // if we have a match
            if (xValue == entry.x) {
                while (m > 0 && mEntries[m - 1].x == xValue) m--

                high = mEntries.size

                // loop over all "equal" entries
                while (m < high) {
                    entry = mEntries[m]
                    if (entry.x == xValue) {
                        entries.add(entry)
                    } else {
                        break
                    }
                    m++
                }

                break
            } else {
                if (xValue > entry.x) low = m + 1
                else high = m - 1
            }
        }

        return entries
    }

    /**
     * Determines how to round DataSet index values for
     * [DataSet.getEntryIndex] DataSet.getEntryIndex()}
     * when an exact x-index is not found.
     */
    enum class Rounding {
        UP,
        DOWN,
        CLOSEST,
    }
}