package com.github.mikephil.charting.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import java.lang.Double
import java.lang.Float
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.intArrayOf
import kotlin.let
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin

/**
 * Math.pow(...) is very expensive, so avoid calling it and create it yourself.
 */
val POW_10 = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000)

/**
 * Utilities class that has some helper methods. Needs to be initialized by
 * calling Utils.init(...) before usage. Inside the Chart.init() method, this is
 * done, if the Utils are used before that, Utils.init(...) needs to be called
 * manually.
 */
object Utils {
    var metrics: DisplayMetrics? = null
    var minimumFlingVelocity: Int = 50
        private set
    var maximumFlingVelocity: Int = 8000
        private set
    val DEG2RAD: kotlin.Double = (Math.PI / 180.0)
    val FDEG2RAD: kotlin.Float = (Math.PI.toFloat() / 180f)

    @Suppress("unused")
    val DOUBLE_EPSILON: kotlin.Double = Double.longBitsToDouble(1)

    @Suppress("unused")
    val FLOAT_EPSILON: kotlin.Float = Float.intBitsToFloat(1)

    /**
     * initialize method, called inside the Chart.init() method.
     */
    @JvmStatic
    @Suppress("deprecation")
    fun init(context: Context) {
        val viewConfiguration = ViewConfiguration.get(context)
        minimumFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
        maximumFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity

        val res = context.resources
        metrics = res.displayMetrics
    }

    /**
     * initialize method, called inside the Chart.init() method. backwards
     * compatibility - to not break existing code
     *
     * @param res
     */
    @Deprecated("")
    fun init(res: Resources) {
        metrics = res.displayMetrics

        // noinspection deprecation
        minimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity()
        // noinspection deprecation
        maximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity()
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device
     * density. NEEDS UTILS TO BE INITIALIZED BEFORE USAGE.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need
     * to convert into pixels
     * @return A float value to represent px equivalent to dp depending on
     * device density
     */
    @JvmStatic
    fun convertDpToPixel(dp: kotlin.Float): kotlin.Float {
        if (metrics == null) {
            Log.e(
                "chartLib-Utils",
                "Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before" +
                        " calling Utils.convertDpToPixel(...). Otherwise conversion does not " +
                        "take place."
            )
            return dp
        }

        return dp * metrics!!.density
    }

    /**
     * calculates the approximate width of a text, depending on a demo text
     * avoid repeated calls (e.g. inside drawing methods)
     *
     * @param paint
     * @param demoText
     * @return
     */
    @JvmStatic
    fun calcTextWidth(paint: Paint, demoText: String?): Int {
        return paint.measureText(demoText).toInt()
    }

    private val calcTextHeightRect = Rect()

    /**
     * calculates the approximate height of a text, depending on a demo text
     * avoid repeated calls (e.g. inside drawing methods)
     *
     * @param paint
     * @param demoText
     * @return
     */
    @JvmStatic
    fun calcTextHeight(paint: Paint, demoText: String): Int {
        val r = calcTextHeightRect
        r.set(0, 0, 0, 0)
        paint.getTextBounds(demoText, 0, demoText.length, r)
        return r.height()
    }

    private val fontMetrics = Paint.FontMetrics()

    @JvmStatic
    fun getLineHeight(paint: Paint): kotlin.Float {
        return getLineHeight(paint, fontMetrics)
    }

    fun getLineHeight(paint: Paint, fontMetrics: Paint.FontMetrics): kotlin.Float {
        paint.getFontMetrics(fontMetrics)
        return fontMetrics.descent - fontMetrics.ascent
    }

    @JvmStatic
    fun getLineSpacing(paint: Paint): kotlin.Float {
        return getLineSpacing(paint, fontMetrics)
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
    @JvmStatic
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

    /** - returns: The default value formatter used for all chart components that needs a default */
    val defaultValueFormatter: IValueFormatter = generateDefaultValueFormatter()

    private fun generateDefaultValueFormatter(): IValueFormatter {
        return DefaultValueFormatter(1)
    }

    /**
     * rounds the given number to the next significant number
     *
     * @param number
     * @return
     */
    fun roundToNextSignificant(number: kotlin.Double): kotlin.Float {
        if (Double.isInfinite(number) ||
            Double.isNaN(number) || number == 0.0
        ) {
            return 0f
        }

        val d = ceil(log10(if (number < 0) -number else number).toFloat().toDouble()).toFloat()
        val pw = 1 - d.toInt()
        val magnitude = 10.0.pow(pw.toDouble()).toFloat()
        val shifted = Math.round(number * magnitude)
        return shifted / magnitude
    }

    /**
     * Returns the appropriate number of decimals to be used for the provided
     * number.
     *
     * @param number
     * @return
     */
    @JvmStatic
    fun getDecimals(number: kotlin.Float): Int {
        val i = roundToNextSignificant(number.toDouble())

        if (Float.isInfinite(i)) {
            return 0
        }

        return ceil(-log10(i.toDouble())).toInt() + 2
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

    @JvmStatic
    fun getPosition(center: MPPointF, dist: kotlin.Float, angle: kotlin.Float, outputPoint: MPPointF) {
        outputPoint.x = (center.x + dist * cos(Math.toRadians(angle.toDouble()))).toFloat()
        outputPoint.y = (center.y + dist * sin(Math.toRadians(angle.toDouble()))).toFloat()
    }

    fun velocityTrackerPointerUpCleanUpIfNecessary(
        ev: MotionEvent,
        tracker: VelocityTracker
    ) {
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
     * Original method view.postInvalidateOnAnimation() only supportd in API >=
     * 16, This is a replica of the code from ViewCompat.
     *
     * @param view
     */
    @JvmStatic
    @SuppressLint("NewApi")
    fun postInvalidateOnAnimation(view: View?) {
        view?.postInvalidateOnAnimation()
    }

    /**
     * returns an angle between 0.f < 360.f (not less than zero, less than 360)
     */
    @JvmStatic
    fun getNormalizedAngle(angle: kotlin.Float): kotlin.Float {
        var angle = angle
        while (angle < 0f) {
            angle += 360f
        }

        return angle % 360f
    }

    private val mDrawableBoundsCache = Rect()

    fun drawImage(
        canvas: Canvas,
        drawable: Drawable,
        x: Int, y: Int,
    ) {
        val width: Int = drawable.intrinsicWidth
        val height: Int = drawable.intrinsicHeight
        val drawOffset = MPPointF.getInstance()
        drawOffset.x = x - (width.toFloat() / 2)
        drawOffset.y = y - (height.toFloat() / 2)

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
        canvas: Canvas, text: String?, x: kotlin.Float, y: kotlin.Float,
        paint: Paint,
        anchor: MPPointF, angleDegrees: kotlin.Float
    ) {
        var drawOffsetX = 0f
        var drawOffsetY = 0f

        val lineHeight = paint.getFontMetrics(mFontMetricsBuffer)
        text?.let { paint.getTextBounds(text, 0, it.length, mDrawTextRectBuffer) }

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
                FSize.recycleInstance(rotatedSize)
            }

            canvas.save()
            canvas.translate(translateX, translateY)
            canvas.rotate(angleDegrees)

            text?.let { canvas.drawText(it, drawOffsetX, drawOffsetY, paint) }

            canvas.restore()
        } else {
            if (anchor.x != 0f || anchor.y != 0f) {
                drawOffsetX -= mDrawTextRectBuffer.width() * anchor.x
                drawOffsetY -= lineHeight * anchor.y
            }

            drawOffsetX += x
            drawOffsetY += y

            text?.let { canvas.drawText(it, drawOffsetX, drawOffsetY, paint) }
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

