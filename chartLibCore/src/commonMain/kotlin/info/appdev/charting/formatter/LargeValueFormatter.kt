package info.appdev.charting.formatter

import info.appdev.charting.components.AxisBase
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Predefined value-formatter that formats large numbers in a pretty way.
 * Outputs: 856 = 856; 1000 = 1k; 5821 = 5.8k; 10500 = 10k; 101800 = 102k;
 * 2000000 = 2m; 7800000 = 7.8m; 92150000 = 92m; 123200000 = 123m; 9999999 =
 * 10m; 1000000000 = 1b;
 * Special thanks to Roman Gromov
 * (https://github.com/romangromov) for this piece of code.
 */
open class LargeValueFormatter() : IValueFormatter, IAxisValueFormatter {

    private var suffix = arrayOf(
        "", "k", "m", "b", "t"
    )
    private var maxLength = 5
    private var text = ""

    /**
     * Creates a formatter that appends a specified text to the result string
     *
     * @param appendix a text that will be appended
     */
    constructor(appendix: String) : this() {
        text = appendix
    }

    // IValueFormatter
    override fun getFormattedValue(value: Float, entryFloat: EntryFloat?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
        return makePretty(value.toDouble()) + text
    }

    // IAxisValueFormatter
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return makePretty(value.toDouble()) + text
    }

    /**
     * Set an appendix text to be added at the end of the formatted value.
     */
    fun setAppendix(appendix: String) {
        text = appendix
    }

    /**
     * Set custom suffix to be appended after the values.
     * Default suffix: ["", "k", "m", "b", "t"]
     *
     * @param suffixArray new suffix
     */
    fun setSuffix(suffixArray: Array<String>) {
        suffix = suffixArray
    }

    fun setMaxLength(max: Int) {
        maxLength = max
    }

    /**
     * Formats the number using engineering notation (exponent forced to a multiple of 3) with
     * the mantissa rounded to 3 significant digits, then replaces the exponent with the
     * corresponding suffix (k, m, b, t, ...). Platform-independent replacement of the previous
     * `java.text.DecimalFormat("###E00")`-based implementation.
     */
    private fun makePretty(number: Double): String {
        if (number == 0.0) return "0"

        val negative = number < 0
        val absNumber = abs(number)

        // floor(log10(absNumber)), self-corrected for floating point imprecision
        var exponent = floor(log10(absNumber)).toInt()
        if (10.0.pow(exponent) > absNumber) exponent--
        if (10.0.pow(exponent + 1) <= absNumber) exponent++

        // round down to the nearest (lower or equal) multiple of 3, engineering-notation style
        var engineeringExponent = floorDiv3(exponent) * 3
        var mantissa = absNumber / 10.0.pow(engineeringExponent)

        // number of integer digits in the mantissa (1..3), used to know how many fractional
        // digits are still needed to reach 3 significant digits in total
        var integerDigits = digitCount(mantissa)
        var fractionDigits = (3 - integerDigits).coerceAtLeast(0)
        mantissa = roundTo(mantissa, fractionDigits)

        // rounding may have pushed the mantissa to 1000 or beyond (e.g. 999.999 -> 1000.0),
        // in which case the exponent bracket needs to shift up by one step
        if (mantissa >= 1000.0) {
            engineeringExponent += 3
            mantissa /= 1000.0
            integerDigits = digitCount(mantissa)
            fractionDigits = (3 - integerDigits).coerceAtLeast(0)
            mantissa = roundTo(mantissa, fractionDigits)
        }

        val suffixIndex = engineeringExponent / 3
        val suffixText = if (suffixIndex in suffix.indices) suffix[suffixIndex] else ""

        val mantissaString = stripTrailingZeros(formatFixed(mantissa, fractionDigits))

        return (if (negative) "-" else "") + mantissaString + suffixText
    }

    private fun floorDiv3(value: Int): Int {
        val quotient = value / 3
        return if (value % 3 != 0 && value < 0) quotient - 1 else quotient
    }

    private fun digitCount(mantissa: Double): Int {
        var digits = 1
        var threshold = 10.0
        while (mantissa >= threshold) {
            digits++
            threshold *= 10.0
        }
        return digits
    }

    private fun roundTo(value: Double, digits: Int): Double {
        val factor = 10.0.pow(digits)
        return (value * factor).roundToLong() / factor
    }

    private fun formatFixed(value: Double, digits: Int): String {
        if (digits <= 0) return value.roundToLong().toString()

        val factor = 10.0.pow(digits)
        val scaled = (value * factor).roundToLong()
        val intPart = scaled / factor.toLong()
        val fracPart = scaled % factor.toLong()
        return "$intPart." + fracPart.toString().padStart(digits, '0')
    }

    private fun stripTrailingZeros(value: String): String {
        if (!value.contains('.')) return value
        var result = value.trimEnd('0')
        if (result.endsWith('.')) result = result.dropLast(1)
        return result
    }
}
