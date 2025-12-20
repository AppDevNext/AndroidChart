package com.github.mikephil.charting.listener

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.highlight.Highlight
import kotlin.math.sqrt

abstract class ChartTouchListener<T : Chart<*>?>(
    @JvmField protected var chart: T?) : SimpleOnGestureListener(), OnTouchListener {
    enum class ChartGesture {
        NONE, DRAG, X_ZOOM, Y_ZOOM, PINCH_ZOOM, ROTATE, SINGLE_TAP, DOUBLE_TAP, LONG_PRESS, FLING
    }

    /**
     * the last touch gesture that has been performed
     */
    var lastGesture: ChartGesture = ChartGesture.NONE
        protected set

    /**
     * returns the touch mode the listener is currently in
     */
    var touchMode: Int = NONE
        protected set

    /**
     * the last highlighted object (via touch)
     */
    protected var mLastHighlighted: Highlight? = null

    /**
     * the gesturedetector used for detecting taps and longpresses, ...
     */
    @JvmField
    protected var gestureDetector: GestureDetector? = GestureDetector(chart!!.getContext(), this)

    /**
     * Calls the OnChartGestureListener to do the start callback
     *
     * @param me
     */
    fun startAction(me: MotionEvent) {
        val l = chart!!.getOnChartGestureListener()

        if (l != null) l.onChartGestureStart(me, this.lastGesture)
    }

    /**
     * Calls the OnChartGestureListener to do the end callback
     *
     * @param me
     */
    fun endAction(me: MotionEvent) {
        val l = chart!!.getOnChartGestureListener()

        if (l != null) l.onChartGestureEnd(me, this.lastGesture)
    }

    /**
     * Sets the last value that was highlighted via touch.
     *
     * @param high
     */
    fun setLastHighlighted(high: Highlight?) {
        mLastHighlighted = high
    }

    /**
     * Perform a highlight operation.
     *
     * @param motionEvent
     */
    protected fun performHighlight(highlight: Highlight?, motionEvent: MotionEvent?) {
        if (highlight == null || highlight.equalTo(mLastHighlighted)) {
            chart!!.highlightValue(null, true)
            mLastHighlighted = null
        } else {
            chart!!.highlightValue(highlight, true)
            mLastHighlighted = highlight
        }
    }

    companion object {
        // states
        protected const val NONE: Int = 0
        protected const val DRAG: Int = 1
        protected const val X_ZOOM: Int = 2
        protected const val Y_ZOOM: Int = 3
        protected const val PINCH_ZOOM: Int = 4
        protected const val POST_ZOOM: Int = 5
        protected const val ROTATE: Int = 6

        /**
         * returns the distance between two points
         */
        @JvmStatic
        protected fun distance(eventX: Float, startX: Float, eventY: Float, startY: Float): Float {
            val dx = eventX - startX
            val dy = eventY - startY
            return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
    }
}
