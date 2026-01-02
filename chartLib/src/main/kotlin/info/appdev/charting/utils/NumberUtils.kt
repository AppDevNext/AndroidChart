package info.appdev.charting.utils

import timber.log.Timber
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Returns the appropriate number of decimals to be used for the provided number.
 */
fun Float.getDecimals(): Int {
    val i = this.toDouble().roundToNextSignificant()

    if (i.isInfinite()) {
        return 0
    }

    return ceil(-log10(i.toDouble())).toInt() + 2
}

/**
 * rounds the given number to the next significant number
 */
fun Double.roundToNextSignificant(): Float {
    if (this.isInfinite() ||
        this.isNaN() || this == 0.0
    ) {
        return 0f
    }

    val d = ceil(log10(if (this < 0) -this else this).toFloat().toDouble()).toFloat()
    val pw = 1 - d.toInt()
    val magnitude = 10.0.pow(pw.toDouble()).toFloat()
    val shifted = (this * magnitude).roundToInt()
    return shifted / magnitude
}

/**
 * This method converts dp unit to equivalent pixels, depending on device
 * density. NEEDS UTILS TO BE INITIALIZED BEFORE USAGE.
 *
 * dp A value in dp (density independent pixels) unit. Which we need
 * to convert into pixels
 * @return A float value to represent px equivalent to dp depending on
 * device density
 */
fun Float.convertDpToPixel(): Float {
    if (metrics == null) {
        Timber.e("Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before calling Utils.convertDpToPixel(...). Otherwise conversion does not take place.")
        return this
    } else
        return this * metrics!!.density
}

fun Float.formatNumber(digitCount: Int, separateThousands: Boolean, separateChar: Char = '.'): String {
    var number = this
    var digitCount = digitCount
    val out = CharArray(35)

    var neg = false
    if (number == 0f) {
        return "0"
    }

    val zero = number < 1 && number > -1

    if (number < 0) {
        neg = true
        number = -number
    }

    if (digitCount > Utils.POW_10.size) {
        digitCount = Utils.POW_10.size - 1
    }

    number *= Utils.POW_10[digitCount].toFloat()
    var lVal = Math.round(number).toLong()
    var ind = out.size - 1
    var charCount = 0
    var decimalPointAdded = false

    while (lVal != 0L || charCount < (digitCount + 1)) {
        val digit = (lVal % 10).toInt()
        lVal /= 10
        out[ind--] = (digit + '0'.code).toChar()
        charCount++

        // add decimal point
        if (charCount == digitCount) {
            out[ind--] = ','
            charCount++
            decimalPointAdded = true

            // add thousand separators
        } else if (separateThousands && lVal != 0L && charCount > digitCount) {
            if (decimalPointAdded) {
                if ((charCount - digitCount) % 4 == 0) {
                    out[ind--] = separateChar
                    charCount++
                }
            } else {
                if ((charCount - digitCount) % 4 == 3) {
                    out[ind--] = separateChar
                    charCount++
                }
            }
        }
    }

    // if number around zero (between 1 and -1)
    if (zero) {
        out[ind--] = '0'
        charCount += 1
    }

    // if the number is negative
    if (neg) {
        out[ind--] = '-'
        charCount += 1
    }

    val start = out.size - charCount

    // use this instead of "new String(...)" because of issue < Android 4.0
    return String(out, start, out.size - start)
}

/**
 * returns an angle between 0.f < 360.f (not less than zero, less than 360)
 */
fun Float.getNormalizedAngle(): Float {
    var angle = this
    while (angle < 0f) {
        angle += 360f
    }

    return angle % 360f
}
