package info.appdev.charting.utils

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Formats [value] rounded to the given number of decimal [digits], grouping the integer part
 * with thousands separators. Platform-independent replacement for the
 * `java.text.DecimalFormat`-based formatting previously used by value formatters
 * (e.g. pattern "###,###,###,##0.00").
 */
fun formatGroupedDecimal(value: Double, digits: Int): String {
    val factor = 10.0.pow(digits)
    val roundedTotal = (abs(value) * factor).roundToLong()
    val intPart = if (digits == 0) roundedTotal else roundedTotal / factor.toLong()
    val fracPart = if (digits == 0) 0L else roundedTotal % factor.toLong()

    val sb = StringBuilder()
    if (value < 0 && roundedTotal != 0L) sb.append("-")
    sb.append(groupThousands(intPart.toString()))
    if (digits > 0) {
        sb.append(".")
        sb.append(fracPart.toString().padStart(digits, '0'))
    }
    return sb.toString()
}

private fun groupThousands(digitsString: String): String {
    val length = digitsString.length
    val sb = StringBuilder()
    for (i in digitsString.indices) {
        if (i > 0 && (length - i) % 3 == 0) sb.append(",")
        sb.append(digitsString[i])
    }
    return sb.toString()
}
