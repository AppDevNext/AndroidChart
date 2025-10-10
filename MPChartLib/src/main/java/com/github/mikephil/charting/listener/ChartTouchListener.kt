package com.github.mikephil.charting.listener

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.highlight.Highlight
import kotlin.math.sqrt

/**
 * Created by philipp on 12/06/15.
 */
abstract class ChartTouchListener<T : Chart<*, *, *>>(
    /**
     * the chart the listener represents
     */
    protected var mChart: T,
) : SimpleOnGestureListener(), OnTouchListener {
    enum class ChartGesture {
        NONE, DRAG, X_ZOOM, Y_ZOOM, PINCH_ZOOM, ROTATE, SINGLE_TAP, DOUBLE_TAP, LONG_PRESS, FLING
    }

    /**
     * Returns the last gesture that has been performed on the chart.
     *
     * @return
     */
    /**
     * the last touch gesture that has been performed
     */
    var lastGesture: ChartGesture = ChartGesture.NONE
        protected set

    /**
     * returns the touch mode the listener is currently in
     *
     * @return
     */
    /**
     * integer field that holds the current touch-state
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
    protected val mGestureDetector: GestureDetector? = GestureDetector(mChart.context, this)

    /**
     * Calls the OnChartGestureListener to do the start callback
     *
     * @param me
     */
    fun startAction(me: MotionEvent?) {
        val l = mChart.onChartGestureListener

        l?.onChartGestureStart(me, this.lastGesture)
    }

    /**
     * Calls the OnChartGestureListener to do the end callback
     *
     * @param me
     */
    fun endAction(me: MotionEvent?) {
        val l = mChart.onChartGestureListener

        l?.onChartGestureEnd(me, this.lastGesture)
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
     * @param e
     */
    protected fun performHighlight(h: Highlight?, e: MotionEvent?) {
        if (h == null || h.equalTo(mLastHighlighted)) {
            mChart.highlightValue(null, true)
            mLastHighlighted = null
        } else {
            mChart.highlightValue(h, true)
            mLastHighlighted = h
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
         *
         * @param eventX
         * @param startX
         * @param eventY
         * @param startY
         * @return
         */
        protected fun distance(eventX: Float, startX: Float, eventY: Float, startY: Float): Float {
            val dx = eventX - startX
            val dy = eventY - startY
            return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
    }
}
