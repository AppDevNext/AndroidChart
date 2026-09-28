package info.appdev.charting.formatter

import info.appdev.charting.components.AxisBase
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.formatGroupedDecimal

/**
 * This IValueFormatter is just for convenience and simply puts a "%" sign after each value. (Recommended for PieChart)
 */
open class PercentFormatter : IValueFormatter, IAxisValueFormatter {
    /**
     * The number of decimal digits used when formatting the percentage value.
     */
    protected var decimalDigits: Int = 1

    constructor()

    /**
     * Allow a custom number of decimal digits.
     */
    constructor(decimalDigits: Int) {
        this.decimalDigits = decimalDigits
    }

    // IValueFormatter
    override fun getFormattedValue(value: Float, entryFloat: EntryFloat?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String? {
        return formatGroupedDecimal(value.toDouble(), decimalDigits) + " %"
    }

    // IAxisValueFormatter
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return formatGroupedDecimal(value.toDouble(), decimalDigits) + " %"
    }
}

