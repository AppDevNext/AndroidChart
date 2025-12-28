package info.appdev.charting.utils

import android.graphics.Paint
import android.graphics.Rect

/**
 * calculates the approximate width of a text, depending on a demo text
 * avoid repeated calls (e.g. inside drawing methods)
 */
fun Paint.calcTextWidth(demoText: String?): Int {
    return measureText(demoText).toInt()
}

private val mCalcTextHeightRect = Rect()

/**
 * calculates the approximate height of a text, depending on a demo text
 * avoid repeated calls (e.g. inside drawing methods)
 */
fun Paint.calcTextHeight(demoText: String): Int {
    val r = mCalcTextHeightRect
    r.set(0, 0, 0, 0)
    this.getTextBounds(demoText, 0, demoText.length, r)
    return r.height()
}

private val mFontMetrics = Paint.FontMetrics()

fun Paint.getLineHeight(): Float {
    return this.getLineHeight(mFontMetrics)
}

fun Paint.getLineHeight(fontMetrics: Paint.FontMetrics): Float {
    this.getFontMetrics(fontMetrics)
    return fontMetrics.descent - fontMetrics.ascent
}

fun Paint.getLineSpacing(): Float {
    return this.getLineSpacing(mFontMetrics)
}

fun Paint.getLineSpacing(fontMetrics: Paint.FontMetrics): Float {
    this.getFontMetrics(fontMetrics)
    return fontMetrics.ascent - fontMetrics.top + fontMetrics.bottom
}

/**
 * Returns a recyclable FSize instance.
 * calculates the approximate size of a text, depending on a demo text
 * avoid repeated calls (e.g. inside drawing methods)
 *
 * @return A Recyclable FSize instance
 */
fun Paint.calcTextSize(demoText: String): FSize {
    val result = FSize.getInstance(0f, 0f)
    calcTextSize(demoText, result)
    return result
}

private val mCalcTextSizeRect = Rect()

/**
 * calculates the approximate size of a text, depending on a demo text
 * avoid repeated calls (e.g. inside drawing methods)
 *
 * @param outputFSize An output variable, modified by the function.
 */
fun Paint.calcTextSize(demoText: String, outputFSize: FSize) {
    val r = mCalcTextSizeRect
    r.set(0, 0, 0, 0)
    this.getTextBounds(demoText, 0, demoText.length, r)
    outputFSize.width = r.width().toFloat()
    outputFSize.height = r.height().toFloat()
}
