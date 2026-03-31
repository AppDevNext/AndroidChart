package info.appdev.chartexample.formatter

import info.appdev.charting.data.BarEntryFloat
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.utils.ViewPortHandler
import java.text.SimpleDateFormat
import java.util.Locale

class UnixTimeValueFormatter(val format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'") : IValueFormatter {

    val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())

    // For bar/line values
    override fun getFormattedValue(value: Float, entryFloat: EntryFloat?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return if (entryFloat is BarEntryFloat) {
            simpleDateFormat.format(entryFloat.yVals?.get(0)!!.toLong()) +
                    " - " +
                    simpleDateFormat.format(entryFloat.yVals?.get(entryFloat.yVals?.size!! - 1)!!.toLong())
        }
        else
            simpleDateFormat.format(value.toLong())
    }
}
