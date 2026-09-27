package info.appdev.charting.formatter

import info.appdev.charting.components.AxisBase
import info.appdev.charting.utils.formatGroupedDecimal

open class DefaultAxisValueFormatter(digits: Int) : IAxisValueFormatter {
    /**
     * The number of decimal digits this formatter uses or -1, if unspecified.
     */
    var decimalDigits = digits
        protected set

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        // avoid memory allocations here (for performance)
        return formatGroupedDecimal(value.toDouble(), decimalDigits)
    }
}

