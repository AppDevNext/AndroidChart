package info.appdev.charting.data

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import info.appdev.charting.formatter.DefaultFillFormatter
import info.appdev.charting.formatter.IFillFormatter
import info.appdev.charting.interfaces.datasets.ILineDataSet
import java.util.WeakHashMap

/**
 * Android-only side channels for [ILineDataSet]/[LineDataSet], mirroring the pattern already
 * established for `ILineRadarDataSet.fillDrawable` in `LineRadarDataSetAndroid.kt`.
 *
 * [IFillFormatter] depends on `LineDataProvider` (permanently Android-only, see Step A.4), so it
 * has no portable equivalent and is kept as a `WeakHashMap`-backed extension property here instead
 * of a stored property on the common class.
 */
private val fillFormatters = WeakHashMap<ILineDataSet<*>, IFillFormatter>()

/**
 * Sets a custom IFillFormatter to the chart that handles the position of the
 * filled-line for each DataSet. Set this to null to use the default logic.
 */
var ILineDataSet<*>.fillFormatter: IFillFormatter?
    get() = fillFormatters.getOrPut(this) { DefaultFillFormatter() }
    set(value) {
        fillFormatters[this] = value ?: DefaultFillFormatter()
    }

/**
 * Sets the colors that should be used for the circles of this DataSet.
 * Colors are reused as soon as the number of Entries the DataSet represents
 * is higher than the size of the colors array. You can use
 * "new String[] { R.color.red, R.color.green, ... }" to provide colors for
 * this method. Internally, the colors are resolved using
 * getResources().getColor(...)
 */
fun <T : BaseEntry<Float>> LineDataSet<T>.setCircleColors(@ColorInt colors: IntArray, context: Context) {
    val clrs = this.circleColors
    clrs.clear()

    for (color in colors) {
        clrs.add(ContextCompat.getColor(context, color))
    }

    this.circleColors = clrs
}
