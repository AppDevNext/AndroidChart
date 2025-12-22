package com.github.mikephil.charting.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable

private val mDrawableBoundsCache = Rect()

private val calcTextSizeRect = Rect()

/**
 * calculates the approximate size of a text, depending on a demo text
 * avoid repeated calls (e.g. inside drawing methods)
 *
 * @param outputFSize An output variable, modified by the function.
 */
fun Paint.calcTextSize(demoText: String, outputFSize: FSize) {
    val r = calcTextSizeRect
    r.set(0, 0, 0, 0)
    this.getTextBounds(demoText, 0, demoText.length, r)
    outputFSize.width = r.width().toFloat()
    outputFSize.height = r.height().toFloat()
}

/**
 * Returns a recyclable FSize instance.
 * calculates the approximate size of a text, depending on a demo text
 * avoid repeated calls (e.g. inside drawing methods)
 * @return A Recyclable FSize instance
 */
fun Paint.calcTextSize(demoText: String): FSize {
    val result = FSize.getInstance(0f, 0f)
    this.calcTextSize(demoText, result)
    return result
}

/**
 * calculates the approximate height of a text, depending on a demo text
 * avoid repeated calls (e.g. inside drawing methods)
 */
fun Paint.calcTextHeight(demoText: String): Int {
    val rect = Rect()
    rect.set(0, 0, 0, 0)
    this.getTextBounds(demoText, 0, demoText.length, rect)
    return rect.height()
}

/**
 * Utilities class that has some helper methods. Needs to be initialized by
 * calling Utils.init(...) before usage. Inside the Chart.init() method, this is
 * done, if the Utils are used before that, Utils.init(...) needs to be called
 * manually.
 */
fun Canvas.drawImage(
    drawable: Drawable,
    x: Int, y: Int,
) {
    val width: Int = drawable.intrinsicWidth
    val height: Int = drawable.intrinsicHeight
    val drawOffset = MPPointF.getInstance(width.toFloat(), height.toFloat())
    drawOffset.x = x - (width.toFloat() / 2)
    drawOffset.y = y - (height.toFloat() / 2)

    drawable.copyBounds(mDrawableBoundsCache)
    drawable.setBounds(
        mDrawableBoundsCache.left,
        mDrawableBoundsCache.top,
        mDrawableBoundsCache.left + width,
        mDrawableBoundsCache.top + width
    )

    val saveId = this.save()
    // translate to the correct position and draw
    this.translate(drawOffset.x, drawOffset.y)
    drawable.draw(this)
    this.restoreToCount(saveId)
}
