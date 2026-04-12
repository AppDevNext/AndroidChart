package info.appdev.chartexample.formatter

import info.appdev.charting.data.BarEntryDouble
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
        return when (entryFloat) {
            is BarEntryDouble if entryFloat.yValsDouble != null -> {
                // High-precision double path
                val vals = entryFloat.yValsDouble!!
                val start = simpleDateFormat.format(vals.first().toLong())
                val end   = simpleDateFormat.format(vals.last().toLong())
                "$start - $end"
            }

            is BarEntryFloat if entryFloat.yVals != null -> {
                // Float path
                val vals = entryFloat.yVals!!
                val start = simpleDateFormat.format(vals.first().toLong())
                val end   = simpleDateFormat.format(vals.last().toLong())
                "$start - $end"
            }

            else -> simpleDateFormat.format(value.toLong())
        }
    }
}
