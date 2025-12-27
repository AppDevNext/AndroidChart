package info.appdev.charting.listener

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View.OnTouchListener
import info.appdev.charting.charts.Chart
import info.appdev.charting.highlight.Highlight
import kotlin.math.sqrt

abstract class ChartTouchListener<T : Chart<*>>(protected var chart: T) : SimpleOnGestureListener(), OnTouchListener {
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
     * the gesture detector used for detecting taps and long presses, ...
     */
    protected var gestureDetector: GestureDetector? = GestureDetector(chart.context, this)

    /**
     * Calls the OnChartGestureListener to do the start callback
     */
    fun startAction(me: MotionEvent) {
        chart.onChartGestureListener?.onChartGestureStart(me, this.lastGesture)
    }

    /**
     * Calls the OnChartGestureListener to do the end callback
     */
    fun endAction(me: MotionEvent) {
        chart.onChartGestureListener?.onChartGestureEnd(me, this.lastGesture)
    }

    /**
     * Sets the last value that was highlighted via touch.
     */
    fun setLastHighlighted(high: Highlight?) {
        mLastHighlighted = high
    }

    /**
     * Perform a highlight operation.
     */
    protected fun performHighlight(highlight: Highlight?) {
        if (highlight == null || highlight.equalTo(mLastHighlighted)) {
            chart.highlightValue(null, true)
            mLastHighlighted = null
        } else {
            chart.highlightValue(highlight, true)
            mLastHighlighted = highlight
        }
    }

    companion object {
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
        protected fun distance(eventX: Float, startX: Float, eventY: Float, startY: Float): Float {
            val dx = eventX - startX
            val dy = eventY - startY
            return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
    }
}
