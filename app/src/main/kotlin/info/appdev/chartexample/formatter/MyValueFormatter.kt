package info.appdev.chartexample.formatter

import info.appdev.charting.data.BaseEntry
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.utils.ViewPortHandler
import java.text.DecimalFormat

class MyValueFormatter : IValueFormatter {
    private val decimalFormat = DecimalFormat("###,###,###,##0.0")

    override fun getFormattedValue(value: Float, entry: BaseEntry<Float>?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return decimalFormat.format(value.toDouble()) + " $"
    }
}
