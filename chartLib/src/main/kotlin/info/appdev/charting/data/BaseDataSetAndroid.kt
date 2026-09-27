package info.appdev.charting.data

import android.content.Context
import androidx.core.content.ContextCompat

/**
 * Sets the colors that should be used for this DataSet. Colors are reused
 * as soon as the number of Entries the DataSet represents is higher than
 * the size of the colors array. You can use
 * "new int[] { R.color.red, R.color.green, ... }" to provide colors for
 * this method. Internally, the colors are resolved using
 * ContextCompat.getColor(context,...)
 *
 * Android-only extension since it needs a `Context` to resolve color resources.
 */
fun BaseDataSet<*>.setColors(colors: IntArray, context: Context) {
    resetColors()
    for (color in colors) {
        addColor(ContextCompat.getColor(context, color))
    }
}
