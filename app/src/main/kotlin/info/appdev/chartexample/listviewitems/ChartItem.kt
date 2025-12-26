package info.appdev.chartexample.listviewitems

import android.content.Context
import android.view.View
import info.appdev.charting.data.ChartData

/**
 * Base class of the Chart ListView items
 */
@Suppress("unused")
abstract class ChartItem internal constructor(var chartData: ChartData<*>) {
    abstract val itemType: Int

    abstract fun getView(position: Int, convertView: View?, c: Context?): View?

    companion object {
        const val TYPE_BARCHART: Int = 0
        const val TYPE_LINECHART: Int = 1
        const val TYPE_PIECHART: Int = 2
    }
}
