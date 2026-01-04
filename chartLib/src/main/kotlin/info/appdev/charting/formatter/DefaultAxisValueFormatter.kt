package info.appdev.charting.formatter

import info.appdev.charting.components.AxisBase
import java.text.DecimalFormat

open class DefaultAxisValueFormatter(digits: Int) : IAxisValueFormatter {
    /**
     * decimal format for formatting
     */
    protected var decimalFormat: DecimalFormat
    /**
     * The number of decimal digits this formatter uses or -1, if unspecified.
     */
    var decimalDigits = 0
        protected set

    init {
        decimalDigits = digits
        val stringBuffer = StringBuffer()
        for (i in 0 until digits) {
            if (i == 0)
                stringBuffer.append(".")
            stringBuffer.append("0")
        }
        decimalFormat = DecimalFormat("###,###,###,##0$stringBuffer")
    }

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        // avoid memory allocations here (for performance)
        return decimalFormat.format(value.toDouble())
    }
}
