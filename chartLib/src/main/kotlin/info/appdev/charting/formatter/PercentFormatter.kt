package info.appdev.charting.formatter

import info.appdev.charting.components.AxisBase
import info.appdev.charting.data.BaseEntry
import info.appdev.charting.utils.ViewPortHandler
import java.text.DecimalFormat

/**
 * This IValueFormatter is just for convenience and simply puts a "%" sign after each value. (Recommended for PieChart)
 */
open class PercentFormatter : IValueFormatter, IAxisValueFormatter {
    protected var decimalFormat: DecimalFormat

    constructor() {
        decimalFormat = DecimalFormat("###,###,##0.0")
    }

    /**
     * Allow a custom decimal format
     */
    constructor(format: DecimalFormat) {
        decimalFormat = format
    }

    // IValueFormatter
    override fun getFormattedValue(value: Float, entry: BaseEntry<Float>?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String? {
        return decimalFormat.format(value.toDouble()) + " %"
    }

    // IAxisValueFormatter
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return decimalFormat.format(value.toDouble()) + " %"
    }

    val decimalDigits: Int
        get() = 1
}
