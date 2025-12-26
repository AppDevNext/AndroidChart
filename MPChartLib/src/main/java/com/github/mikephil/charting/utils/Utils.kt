package com.github.mikephil.charting.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.FSize.Companion.recycleInstance
import com.github.mikephil.charting.utils.MPPointF.Companion.instance
import java.lang.Double
import java.lang.Float
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.Suppress
import kotlin.intArrayOf
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utilities class that has some helper methods. Needs to be initialized by
 * calling Utils.init(...) before usage. Inside the Chart.init() method, this is
 * done, if the Utils are used before that, Utils.init(...) needs to be called
 * manually.
 *
 * @author Philipp Jahoda
 */
object Utils {
    var minimumFlingVelocity: Int = 50
    var maximumFlingVelocity: Int = 8000
    val DEG2RAD: kotlin.Double = (Math.PI / 180.0)
    val FDEG2RAD: kotlin.Float = (Math.PI.toFloat() / 180f)

    @Suppress("unused")
    val DOUBLE_EPSILON: kotlin.Double = Double.longBitsToDouble(1)

    @Suppress("unused")
    val FLOAT_EPSILON: kotlin.Float = Float.intBitsToFloat(1)

    /**
     * initialize method, called inside the Chart.init() method.
     */
    @Suppress("deprecation")
    fun init(context: Context) {
        val viewConfiguration = ViewConfiguration.get(context)
        minimumFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
        maximumFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
    }

    /**
     * calculates the approximate width of a text, depending on a demo text
     * avoid repeated calls (e.g. inside drawing methods)
     */
    fun calcTextWidth(paint: Paint, demoText: String?): Int {
        return paint.measureText(demoText).toInt()
    }

    private val mCalcTextHeightRect = Rect()

    /**
     * calculates the approximate height of a text, depending on a demo text
     * avoid repeated calls (e.g. inside drawing methods)
     */
    fun calcTextHeight(paint: Paint, demoText: String): Int {
        val r = mCalcTextHeightRect
        r.set(0, 0, 0, 0)
        paint.getTextBounds(demoText, 0, demoText.length, r)
        return r.height()
    }

    private val mFontMetrics = Paint.FontMetrics()

    fun getLineHeight(paint: Paint): kotlin.Float {
        return getLineHeight(paint, mFontMetrics)
    }

    fun getLineHeight(paint: Paint, fontMetrics: Paint.FontMetrics): kotlin.Float {
        paint.getFontMetrics(fontMetrics)
        return fontMetrics.descent - fontMetrics.ascent
    }

    fun getLineSpacing(paint: Paint): kotlin.Float {
        return getLineSpacing(paint, mFontMetrics)
    }

    fun getLineSpacing(paint: Paint, fontMetrics: Paint.FontMetrics): kotlin.Float {
        paint.getFontMetrics(fontMetrics)
        return fontMetrics.ascent - fontMetrics.top + fontMetrics.bottom
    }

    /**
     * Returns a recyclable FSize instance.
     * calculates the approximate size of a text, depending on a demo text
     * avoid repeated calls (e.g. inside drawing methods)
     *
     * @param paint
     * @param demoText
     * @return A Recyclable FSize instance
     */
    fun calcTextSize(paint: Paint, demoText: String): FSize {
        val result = FSize.getInstance(0f, 0f)
        calcTextSize(paint, demoText, result)
        return result
    }

    private val mCalcTextSizeRect = Rect()

    /**
     * calculates the approximate size of a text, depending on a demo text
     * avoid repeated calls (e.g. inside drawing methods)
     *
     * @param paint
     * @param demoText
     * @param outputFSize An output variable, modified by the function.
     */
    fun calcTextSize(paint: Paint, demoText: String, outputFSize: FSize) {
        val r = mCalcTextSizeRect
        r.set(0, 0, 0, 0)
        paint.getTextBounds(demoText, 0, demoText.length, r)
        outputFSize.width = r.width().toFloat()
        outputFSize.height = r.height().toFloat()
    }

    /**
     * Math.pow(...) is very expensive, so avoid calling it and create it
     * yourself.
     */
    val POW_10: IntArray = intArrayOf(
        1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
    )

    /** - returns: The default value formatter used for all chart components that needs a default */
    val defaultValueFormatter: IValueFormatter = generateDefaultValueFormatter()

    private fun generateDefaultValueFormatter(): IValueFormatter {
        return DefaultValueFormatter(1)
    }

    /**
     * Returns a recyclable MPPointF instance.
     * Calculates the position around a center point, depending on the distance
     * from the center, and the angle of the position around the center.
     *
     * @param center
     * @param dist
     * @param angle  in degrees, converted to radians internally
     * @return
     */
    fun getPosition(center: MPPointF, dist: kotlin.Float, angle: kotlin.Float): MPPointF {
        val p = MPPointF.getInstance(0f, 0f)
        getPosition(center, dist, angle, p)
        return p
    }

    fun getPosition(center: MPPointF, dist: kotlin.Float, angle: kotlin.Float, outputPoint: MPPointF) {
        outputPoint.x = (center.x + dist * cos(Math.toRadians(angle.toDouble()))).toFloat()
        outputPoint.y = (center.y + dist * sin(Math.toRadians(angle.toDouble()))).toFloat()
    }

    fun velocityTrackerPointerUpCleanUpIfNecessary(ev: MotionEvent, tracker: VelocityTracker) {
        // Check the dot product of current velocities.
        // If the pointer that left was opposing another velocity vector, clear.

        tracker.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
        val upIndex = ev.actionIndex
        val id1 = ev.getPointerId(upIndex)
        val x1 = tracker.getXVelocity(id1)
        val y1 = tracker.getYVelocity(id1)
        var i = 0
        val count = ev.pointerCount
        while (i < count) {
            if (i == upIndex) {
                i++
                continue
            }

            val id2 = ev.getPointerId(i)
            val x = x1 * tracker.getXVelocity(id2)
            val y = y1 * tracker.getYVelocity(id2)

            val dot = x + y
            if (dot < 0) {
                tracker.clear()
                break
            }
            i++
        }
    }

    /**
     * returns an angle between 0.f < 360.f (not less than zero, less than 360)
     */
    fun getNormalizedAngle(angle: kotlin.Float): kotlin.Float {
        var angle = angle
        while (angle < 0f) {
            angle += 360f
        }

        return angle % 360f
    }

    private val mDrawableBoundsCache = Rect()

    fun drawImage(canvas: Canvas, drawable: Drawable, x: Int, y: Int) {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight

        val drawOffset = instance
        drawOffset.x = x - (width / 2).toFloat()
        drawOffset.y = y - (height / 2).toFloat()

        drawable.copyBounds(mDrawableBoundsCache)
        drawable.setBounds(
            mDrawableBoundsCache.left,
            mDrawableBoundsCache.top,
            mDrawableBoundsCache.left + width,
            mDrawableBoundsCache.top + width
        )

        val saveId = canvas.save()
        // translate to the correct position and draw
        canvas.translate(drawOffset.x, drawOffset.y)
        drawable.draw(canvas)
        canvas.restoreToCount(saveId)
    }

    private val mDrawTextRectBuffer = Rect()
    private val mFontMetricsBuffer = Paint.FontMetrics()

    fun drawXAxisValue(
        canvas: Canvas, text: String, x: kotlin.Float, y: kotlin.Float,
        paint: Paint,
        anchor: MPPointF, angleDegrees: kotlin.Float
    ) {
        var drawOffsetX = 0f
        var drawOffsetY = 0f

        val lineHeight = paint.getFontMetrics(mFontMetricsBuffer)
        paint.getTextBounds(text, 0, text.length, mDrawTextRectBuffer)

        // Android sometimes has pre-padding
        drawOffsetX -= mDrawTextRectBuffer.left.toFloat()

        // Android does not snap the bounds to line boundaries,
        //  and draws from bottom to top.
        // And we want to normalize it.
        drawOffsetY -= mFontMetricsBuffer.ascent

        // To have a consistent point of reference, we always draw left-aligned
        val originalTextAlign = paint.textAlign
        paint.textAlign = Align.LEFT

        if (angleDegrees != 0f) {
            // Move the text drawing rect in a way that it always rotates around its center

            drawOffsetX -= mDrawTextRectBuffer.width() * 0.5f
            drawOffsetY -= lineHeight * 0.5f

            var translateX = x
            var translateY = y

            // Move the "outer" rect relative to the anchor, assuming its centered
            if (anchor.x != 0.5f || anchor.y != 0.5f) {
                val rotatedSize = getSizeOfRotatedRectangleByDegrees(
                    mDrawTextRectBuffer.width().toFloat(),
                    lineHeight,
                    angleDegrees
                )

                translateX -= rotatedSize.width * (anchor.x - 0.5f)
                translateY -= rotatedSize.height * (anchor.y - 0.5f)
                recycleInstance(rotatedSize)
            }

            canvas.save()
            canvas.translate(translateX, translateY)
            canvas.rotate(angleDegrees)

            canvas.drawText(text, drawOffsetX, drawOffsetY, paint)

            canvas.restore()
        } else {
            if (anchor.x != 0f || anchor.y != 0f) {
                drawOffsetX -= mDrawTextRectBuffer.width() * anchor.x
                drawOffsetY -= lineHeight * anchor.y
            }

            drawOffsetX += x
            drawOffsetY += y

            canvas.drawText(text, drawOffsetX, drawOffsetY, paint)
        }

        paint.textAlign = originalTextAlign
    }

    /**
     * Returns a recyclable FSize instance.
     * Represents size of a rotated rectangle by degrees.
     *
     * @param rectangleWidth
     * @param rectangleHeight
     * @param degrees
     * @return A Recyclable FSize instance
     */
    fun getSizeOfRotatedRectangleByDegrees(rectangleWidth: kotlin.Float, rectangleHeight: kotlin.Float, degrees: kotlin.Float): FSize {
        val radians = degrees * FDEG2RAD
        return getSizeOfRotatedRectangleByRadians(rectangleWidth, rectangleHeight, radians)
    }

    /**
     * Returns a recyclable FSize instance.
     * Represents size of a rotated rectangle by radians.
     *
     * @param rectangleWidth
     * @param rectangleHeight
     * @param radians
     * @return A Recyclable FSize instance
     */
    fun getSizeOfRotatedRectangleByRadians(rectangleWidth: kotlin.Float, rectangleHeight: kotlin.Float, radians: kotlin.Float): FSize {
        return FSize.getInstance(
            abs(rectangleWidth * cos(radians.toDouble()).toFloat()) + abs(rectangleHeight * sin(radians.toDouble()).toFloat()),
            abs(rectangleWidth * sin(radians.toDouble()).toFloat()) + abs(rectangleHeight * cos(radians.toDouble()).toFloat())
        )
    }
}
