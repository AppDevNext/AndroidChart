package info.appdev.chartexample.formatter

import info.appdev.charting.components.AxisBase
import info.appdev.charting.formatter.IAxisValueFormatter
import java.text.DecimalFormat

class MyAxisValueFormatter : IAxisValueFormatter {
    private val decimalFormat = DecimalFormat("###,###,###,##0.0")

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return decimalFormat.format(value.toDouble()) + " $"
    }
}
