package info.appdev.charting.data

import timber.log.Timber
import java.io.Serializable
import kotlin.math.abs

/**
 * The DataSet class represents one group or type of entries (Entry) in the
 * Chart that belong together. It is designed to logically separate different
 * groups of values inside the Chart (e.g. the values for a specific line in the
 * LineChart, or the values of a specific group of bars in the BarChart).
 */
abstract class DataSet<T : Entry>(
    protected var mEntries: MutableList<T>,
    label: String = ""
) : BaseDataSet<T>(label), Serializable {
    /**
     * maximum y-value in the value array
     */
    override var yMax: Float = -Float.MAX_VALUE
        protected set

    /**
     * minimum y-value in the value array
     */
    override var yMin: Float = Float.MAX_VALUE
        protected set

    /**
     * maximum x-value in the value array
     */
    override var xMax: Float = -Float.MAX_VALUE
        protected set

    /**
     * minimum x-value in the value array
     */
    override var xMin: Float = Float.MAX_VALUE
        protected set

    /**
     * Creates a new DataSet object with the given values (entries) it represents. Also, a
     * label that describes the DataSet can be specified. The label can also be
     * used to retrieve the DataSet from a ChartData object.
     */
    init {
        calcMinMax()
    }

    override fun calcMinMax() {
        this.yMax = -Float.MAX_VALUE
        this.yMin = Float.MAX_VALUE
        this.xMax = -Float.MAX_VALUE
        this.xMin = Float.MAX_VALUE

        if (mEntries.isEmpty()) {
            return
        }

        for (e in mEntries) {
            calcMinMax(e)
        }
    }

    override fun calcMinMaxY(fromX: Float, toX: Float) {
        this.yMax = -Float.MAX_VALUE
        this.yMin = Float.MAX_VALUE

        if (mEntries.isEmpty()) {
            return
        }

        val indexFrom = getEntryIndex(fromX, Float.NaN, Rounding.DOWN)
        val indexTo = getEntryIndex(toX, Float.NaN, Rounding.UP)

        if (indexTo < indexFrom) {
            return
        }

        for (i in indexFrom..indexTo) {
            // only recalculate y

            calcMinMaxY(mEntries[i])
        }
    }

    /**
     * Updates the min and max x and y value of this DataSet based on the given Entry.
     */
    protected open fun calcMinMax(entry: T) {
        calcMinMaxX(entry)
        calcMinMaxY(entry)
    }

    protected fun calcMinMaxX(entry: T) {
        if (entry.x < this.xMin) {
            this.xMin = entry.x
        }

        if (entry.x > this.xMax) {
            this.xMax = entry.x
        }
    }

    protected open fun calcMinMaxY(entry: T) {
        if (entry.y < this.yMin) {
            this.yMin = entry.y
        }

        if (entry.y > this.yMax) {
            this.yMax = entry.y
        }
    }

    override val entryCount: Int
        get() = mEntries.size

    /**
     * Returns the array of entries that this DataSet represents.
     */
    var entries: MutableList<T>
        get() = mEntries
        set(entries) {
            mEntries = entries
            notifyDataChanged()
        }

    /**
     * Provides an exact copy of the DataSet this method is used on.
     */
    abstract fun copy(): DataSet<T>?

    protected fun copy(dataSet: DataSet<*>) {
        super.copy(dataSet)
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append(toSimpleString())
        for (i in mEntries.indices) {
            buffer.append(mEntries[i].toString()).append(" ")
        }
        return buffer.toString()
    }

    /**
     * Returns a simple string representation of the DataSet with the type and the number of Entries.
     */
    fun toSimpleString() = "DataSet, label: $label, entries: ${mEntries.size}"

    override fun addEntryOrdered(entry: T) {
        calcMinMax(entry)

        if (!mEntries.isEmpty() && mEntries[mEntries.size - 1].x > entry.x) {
            val closestIndex = getEntryIndex(entry.x, entry.y, Rounding.UP)
            mEntries.add(closestIndex, entry)
        } else {
            mEntries.add(entry)
        }
    }

    override fun clear() {
        mEntries.clear()
        notifyDataChanged()
    }

    override fun addEntry(entry: T): Boolean {
        calcMinMax(entry)

        return entries.add(entry)
    }

    override fun removeEntry(entry: T): Boolean {
        val removed = mEntries.remove(entry)

        if (removed) {
            calcMinMax()
        }
        return removed
    }

    override fun getEntryIndex(entry: T): Int {
        return mEntries.indexOf(entry)
    }


    override fun getEntryForXValue(xValue: Float, closestToY: Float, rounding: Rounding?): T? {
        val index = getEntryIndex(xValue, closestToY, rounding)
        if (index > -1) {
            return mEntries[index]
        }
        return null
    }

    override fun getEntryForXValue(xValue: Float, closestToY: Float): T? {
        return getEntryForXValue(xValue, closestToY, Rounding.CLOSEST)
    }

    override fun getEntryForIndex(index: Int): T? {
        if (index < 0) {
            Timber.e("index $index is < 0 for getEntryForIndex")
            return null
        } else if (index >= mEntries.size) {
            Timber.e("index $index / ${mEntries.size} is out of range for getEntryForIndex")
            return null
        }
        return mEntries[index]
    }

    override fun getEntryIndex(xValue: Float, closestToY: Float, rounding: Rounding?): Int {
        if (mEntries.isEmpty()) {
            return -1
        }

        var low = 0
        var high = mEntries.size - 1
        var closest = high

        while (low < high) {
            val m = low + (high - low) / 2

            val currentEntry: Entry = mEntries[m]

            val nextEntry: Entry = mEntries[m + 1]

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

        val closestEntry: Entry = mEntries[closest]
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
            while (closest > 0 && mEntries[closest - 1].x == closestXValue) {
                closest -= 1
            }

            var closestYValue = closestEntry.y
            var closestYIndex = closest

            while (true) {
                closest += 1
                if (closest >= mEntries.size) {
                    break
                }

                val value: T = mEntries[closest]

                if (value.x != closestXValue) {
                    break
                }

                if (abs(value.y - closestToY) <= abs(closestYValue - closestToY)) {
                    closestYValue = closestToY
                    closestYIndex = closest
                }
            }

            closest = closestYIndex
        }
        return closest
    }

    override fun getEntriesForXValue(xValue: Float): MutableList<T> {
        val entries: MutableList<T> = mutableListOf()

        var low = 0
        var high = mEntries.size - 1

        while (low <= high) {
            var m = (high + low) / 2
            var entry = mEntries[m]

            // if we have a match
            if (xValue == entry.x) {
                while (m > 0 && mEntries[m - 1].x == xValue) {
                    m--
                }

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
                if (xValue > entry.x) {
                    low = m + 1
                } else {
                    high = m - 1
                }
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