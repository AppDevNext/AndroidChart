package info.appdev.charting.formatter

import info.appdev.charting.data.EntryFloat
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.formatGroupedDecimal

/**
 * Default formatter used for formatting values inside the chart. Formats values with a
 * pre-calculated number of decimal digits and groups the integer part with thousands
 * separators, platform-independent replacement for the former `java.text.DecimalFormat`-based
 * implementation (e.g. pattern "###,###,###,##0.00").
 */
open class DefaultValueFormatter(digits: Int) : IValueFormatter {
    /**
     * Returns the number of decimal digits this formatter uses.
     */
    var decimalDigits = 0
        protected set

    /**
     * Constructor that specifies to how many digits the value should be formatted.
     */
    init {
        setup(digits)
    }

    /**
     * Sets up the formatter with a given number of decimal digits.
     *
     * @param digits
     */
    fun setup(digits: Int) {
        decimalDigits = digits
    }

    override fun getFormattedValue(value: Float, entryFloat: EntryFloat?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        // put more logic here ...
        // avoid memory allocations here (for performance reasons)
        return formatGroupedDecimal(value.toDouble(), decimalDigits)
    }
}
