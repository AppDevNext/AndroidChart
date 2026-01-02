package info.appdev.charting.utils

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import info.appdev.charting.formatter.DefaultValueFormatter
import info.appdev.charting.formatter.IValueFormatter
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

    private val mDrawableBoundsCache = Rect()

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
