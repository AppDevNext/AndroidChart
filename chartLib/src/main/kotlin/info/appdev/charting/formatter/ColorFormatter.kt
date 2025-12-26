package info.appdev.charting.formatter

import info.appdev.charting.data.Entry
import info.appdev.charting.interfaces.datasets.IDataSet

/**
 * Interface that can be used to return a customized color instead of setting colors via the setColor(...) method of the DataSet.
 */
interface ColorFormatter {
    /**
     * Returns the color to be used for the given Entry at the given index (in the entries array)
     *
     * @param index index in the entries array
     * @param entry the entry to color
     * @param set   the DataSet the entry belongs to
     */
    fun getColor(index: Int, entry: Entry?, set: IDataSet<*>?): Int
}
