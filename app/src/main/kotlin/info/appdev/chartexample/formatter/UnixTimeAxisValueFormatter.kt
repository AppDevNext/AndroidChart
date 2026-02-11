package info.appdev.chartexample.formatter

import info.appdev.charting.components.AxisBase
import info.appdev.charting.formatter.IAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class UnixTimeAxisValueFormatter(val format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'") : IAxisValueFormatter {

    val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return simpleDateFormat.format(value.toLong())
    }

}
