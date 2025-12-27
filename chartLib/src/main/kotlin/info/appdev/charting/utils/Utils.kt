package info.appdev.charting.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import info.appdev.charting.formatter.DefaultValueFormatter
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.utils.MPPointF.Companion.instance
import kotlin.Int
import kotlin.IntArray
import kotlin.Suppress
import kotlin.intArrayOf
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utilities class that has some helper methods. Needs to be initialized by
 * calling Utils.init(...) before usage. Inside the Chart.init() method, this is
 * done, if the Utils are used before that, Utils.init(...) needs to be called manually.
 */
object Utils {
    var minimumFlingVelocity: Int = 50
    var maximumFlingVelocity: Int = 8000
    const val DEG2RAD: Double = (Math.PI / 180.0)
    const val FDEG2RAD: Float = (Math.PI.toFloat() / 180f)

    val FLOAT_EPSILON: Float = java.lang.Float.intBitsToFloat(1)

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
    fun getPosition(center: MPPointF, dist: Float, angle: Float): MPPointF {
        val p = MPPointF.getInstance(0f, 0f)
        getPosition(center, dist, angle, p)
        return p
    }

    fun getPosition(center: MPPointF, dist: Float, angle: Float, outputPoint: MPPointF) {
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
    fun getNormalizedAngle(angle: Float): Float {
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

    /**
     * Returns a recyclable FSize instance.
     * Represents size of a rotated rectangle by degrees.
     *
     * @param rectangleWidth
     * @param rectangleHeight
     * @param degrees
     * @return A Recyclable FSize instance
     */
    fun getSizeOfRotatedRectangleByDegrees(rectangleWidth: Float, rectangleHeight: Float, degrees: Float): FSize {
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
    fun getSizeOfRotatedRectangleByRadians(rectangleWidth: Float, rectangleHeight: Float, radians: Float): FSize {
        return FSize.getInstance(
            abs(rectangleWidth * cos(radians.toDouble()).toFloat()) + abs(rectangleHeight * sin(radians.toDouble()).toFloat()),
            abs(rectangleWidth * sin(radians.toDouble()).toFloat()) + abs(rectangleHeight * cos(radians.toDouble()).toFloat())
        )
    }
}
