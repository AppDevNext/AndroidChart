package com.github.mikephil.charting.listener

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.AnimationUtils
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.convertDpToPixel
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * TouchListener for Bar-, Line-, Scatter- and CandleStickChart with handles all
 * touch interaction. Longpress == Zoom out. Double-Tap == Zoom in.
 */
@Suppress("MemberVisibilityCanBePrivate")
class BarLineChartTouchListener(
    chart: BarLineChartBase<out BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>>,
    touchMatrix: Matrix,
    dragTriggerDistance: Float
) :
    ChartTouchListener<BarLineChartBase<out BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>?>?>(chart) {
    /**
     * the original touch-matrix from the chart
     */
    var matrix: Matrix = Matrix()
        private set

    /**
     * matrix for saving the original matrix state
     */
    private val savedMatrix = Matrix()

    /**
     * point where the touch action started
     */
    private val touchStartPoint: MPPointF = MPPointF.getInstance(0f, 0f)

    /**
     * center between two pointers (fingers on the display)
     */
    private val touchPointCenter: MPPointF = MPPointF.getInstance(0f, 0f)

    private var savedXDist = 1f
    private var savedYDist = 1f
    private var savedDist = 1f

    private var closestDataSetToTouch: IDataSet<*>? = null

    /**
     * used for tracking velocity of dragging
     */
    private var velocityTracker: VelocityTracker? = null

    private var decelerationLastTime: Long = 0
    private val decelerationCurrentPoint: MPPointF = MPPointF.getInstance(0f, 0f)
    private val decelerationVelocity: MPPointF = MPPointF.getInstance(0f, 0f)

    /**
     * the distance of movement that will be counted as a drag
     */
    private var dragTriggerDist: Float

    /**
     * the minimum distance between the pointers that will trigger a zoom gesture
     */
    private val minScalePointerDistance: Float


    private val matrixBuffer = FloatArray(9)
    private val tempMatrix = Matrix()

    /**
     * Constructor with initialization parameters.
     *
     * @param chart               instance of the chart
     * @param touchMatrix         the touch-matrix of the chart
     * @param dragTriggerDistance the minimum movement distance that will be interpreted as a "drag" gesture in dp (3dp equals
     * to about 9 pixels on a 5.5" FHD screen)
     */
    init {
        this.matrix = touchMatrix

        this.dragTriggerDist = dragTriggerDistance.convertDpToPixel()

        this.minScalePointerDistance = 3.5f.convertDpToPixel()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(event)

        if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
            if (velocityTracker != null) {
                velocityTracker!!.recycle()
                velocityTracker = null
            }
        }

        if (touchMode == NONE || chart!!.isFlingEnabled) {
            gestureDetector?.onTouchEvent(event)
        }

        if (!chart!!.isDragEnabled && (!chart!!.isScaleXEnabled && !chart!!.isScaleYEnabled)) return true

        // Handle touch events here...
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                startAction(event)

                stopDeceleration()

                saveTouchStart(event)
            }

            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount >= 2) {
                chart!!.disableScroll()

                saveTouchStart(event)

                // get the distance between the pointers on the x-axis
                savedXDist = getXDist(event)

                // get the distance between the pointers on the y-axis
                savedYDist = getYDist(event)

                // get the total distance between the pointers
                savedDist = spacing(event)

                if (savedDist > 10f) {
                    touchMode = if (chart!!.isPinchZoomEnabled) {
                        PINCH_ZOOM
                    } else {
                        if (chart!!.isScaleXEnabled != chart!!.isScaleYEnabled) {
                            if (chart!!.isScaleXEnabled) X_ZOOM else Y_ZOOM
                        } else {
                            if (savedXDist > savedYDist) X_ZOOM else Y_ZOOM
                        }
                    }
                }

                // determine the touch-pointer center
                midPoint(touchPointCenter, event)
            }

            MotionEvent.ACTION_MOVE -> if (touchMode == DRAG) {
                chart!!.disableScroll()

                val x = if (chart!!.isDragXEnabled) event.x - touchStartPoint.x else 0f
                val y = if (chart!!.isDragYEnabled) event.y - touchStartPoint.y else 0f

                performDrag(event, x, y)
            } else if (touchMode == X_ZOOM || touchMode == Y_ZOOM || touchMode == PINCH_ZOOM) {
                chart!!.disableScroll()

                if (chart!!.isScaleXEnabled || chart!!.isScaleYEnabled) performZoom(event)
            } else if (touchMode == NONE
                && abs(
                    distance(
                        event.x, touchStartPoint.x, event.y,
                        touchStartPoint.y
                    ).toDouble()
                ) > dragTriggerDist
            ) {
                if (chart!!.isDragEnabled) {
                    val shouldPan = !chart!!.isFullyZoomedOut ||
                            !chart!!.hasNoDragOffset()

                    if (shouldPan) {
                        val distanceX = abs((event.x - touchStartPoint.x).toDouble()).toFloat()
                        val distanceY = abs((event.y - touchStartPoint.y).toDouble()).toFloat()

                        // Disable dragging in a direction that's disallowed
                        if ((chart!!.isDragXEnabled || distanceY >= distanceX) &&
                            (chart!!.isDragYEnabled || distanceY <= distanceX)
                        ) {
                            lastGesture = ChartGesture.DRAG
                            touchMode = DRAG
                        }
                    } else {
                        if (chart!!.isHighlightPerDragEnabled) {
                            lastGesture = ChartGesture.DRAG

                            if (chart!!.isHighlightPerDragEnabled) performHighlightDrag(event)
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                velocityTracker?.let {
                    val pointerId = event.getPointerId(0)
                    it.computeCurrentVelocity(1000, Utils.maximumFlingVelocity.toFloat())
                    val velocityY = it.getYVelocity(pointerId)
                    val velocityX = it.getXVelocity(pointerId)

                    if (abs(velocityX.toDouble()) > Utils.minimumFlingVelocity ||
                        abs(velocityY.toDouble()) > Utils.minimumFlingVelocity
                    ) {
                        if (touchMode == DRAG && chart!!.isDragDecelerationEnabled) {
                            stopDeceleration()

                            decelerationLastTime = AnimationUtils.currentAnimationTimeMillis()

                            decelerationCurrentPoint.x = event.x
                            decelerationCurrentPoint.y = event.y

                            decelerationVelocity.x = velocityX
                            decelerationVelocity.y = velocityY

                            // This causes computeScroll to fire, recommended for this by Google
                            chart?.postInvalidateOnAnimation()
                        }
                    }

                    if (touchMode == X_ZOOM || touchMode == Y_ZOOM || touchMode == PINCH_ZOOM || touchMode == POST_ZOOM) {
                        // Range might have changed, which means that Y-axis labels
                        // could have changed in size, affecting Y-axis size.
                        // So we need to recalculate offsets.

                        chart!!.calculateOffsets()
                        chart!!.postInvalidate()
                    }

                    touchMode = NONE
                    chart!!.enableScroll()

                    it.recycle()
                    velocityTracker = null

                    endAction(event)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                velocityTracker?.let { Utils.velocityTrackerPointerUpCleanUpIfNecessary(event, it) }

                touchMode = POST_ZOOM
            }

            MotionEvent.ACTION_CANCEL -> {
                touchMode = NONE
                endAction(event)
            }
        }

        // perform the transformation, update the chart
        matrix = chart!!.viewPortHandler.refresh(matrix, chart!!, true)

        return true // indicate event was handled
    }

    /** BELOW CODE PERFORMS THE ACTUAL TOUCH ACTIONS  */
    /**
     * Saves the current Matrix state and the touch-start point.
     *
     * @param event
     */
    private fun saveTouchStart(event: MotionEvent) {
        savedMatrix.set(matrix)
        touchStartPoint.x = event.x
        touchStartPoint.y = event.y

        closestDataSetToTouch = chart!!.getDataSetByTouchPoint(event.x, event.y)
    }

    /**
     * Performs all necessary operations needed for dragging.
     *
     * @param event
     */
    private fun performDrag(event: MotionEvent, distanceX: Float, distanceY: Float) {
        var distanceXLocal = distanceX
        var distanceYLocal = distanceY
        lastGesture = ChartGesture.DRAG

        matrix.set(savedMatrix)

        val l = chart!!.onChartGestureListener

        // check if axis is inverted
        if (inverted()) {
            // if there is an inverted horizontalbarchart

            if (chart is HorizontalBarChart) {
                distanceXLocal = -distanceXLocal
            } else {
                distanceYLocal = -distanceYLocal
            }
        }

        matrix.postTranslate(distanceXLocal, distanceYLocal)

        l?.onChartTranslate(event, distanceXLocal, distanceYLocal)
    }

    /**
     * Performs the all operations necessary for pinch and axis zoom.
     *
     * @param event
     */
    private fun performZoom(event: MotionEvent) {
        if (event.pointerCount >= 2) { // two finger zoom

            val l = chart!!.onChartGestureListener

            // get the distance between the pointers of the touch event
            val totalDist = spacing(event)

            if (totalDist > minScalePointerDistance) {
                // get the translation

                val t = getTrans(touchPointCenter.x, touchPointCenter.y)
                val h = chart!!.viewPortHandler

                // take actions depending on the activated touch mode
                if (touchMode == PINCH_ZOOM) {
                    lastGesture = ChartGesture.PINCH_ZOOM

                    val scale = totalDist / savedDist // total scale

                    val isZoomingOut = (scale < 1)

                    val canZoomMoreX = if (isZoomingOut) h.canZoomOutMoreX() else h.canZoomInMoreX()

                    val canZoomMoreY = if (isZoomingOut) h.canZoomOutMoreY() else h.canZoomInMoreY()

                    val scaleX = if (chart!!.isScaleXEnabled) scale else 1f
                    val scaleY = if (chart!!.isScaleYEnabled) scale else 1f

                    if (canZoomMoreY || canZoomMoreX) {
                        matrix.set(savedMatrix)
                        matrix.postScale(getLimitedScaleX(scaleX, t), getLimitedScaleY(scaleY, t), t.x, t.y)

                        l?.onChartScale(event, scaleX, scaleY)
                    }
                } else if (touchMode == X_ZOOM && chart!!.isScaleXEnabled) {
                    lastGesture = ChartGesture.X_ZOOM

                    val xDist = getXDist(event)
                    val scaleX = xDist / savedXDist // x-axis scale

                    val isZoomingOut = (scaleX < 1)
                    val canZoomMoreX = if (isZoomingOut) h.canZoomOutMoreX() else h.canZoomInMoreX()

                    if (canZoomMoreX) {
                        matrix.set(savedMatrix)
                        matrix.postScale(getLimitedScaleX(scaleX, t), 1f, t.x, t.y)

                        l?.onChartScale(event, scaleX, 1f)
                    }
                } else if (touchMode == Y_ZOOM && chart!!.isScaleYEnabled) {
                    lastGesture = ChartGesture.Y_ZOOM

                    val yDist = getYDist(event)
                    val scaleY = yDist / savedYDist // y-axis scale

                    val isZoomingOut = (scaleY < 1)
                    val canZoomMoreY = if (isZoomingOut) h.canZoomOutMoreY() else h.canZoomInMoreY()

                    if (canZoomMoreY) {
                        matrix.set(savedMatrix)
                        matrix.postScale(1f, getLimitedScaleY(scaleY, t), t.x, t.y)

                        l?.onChartScale(event, 1f, scaleY)
                    }
                }

                MPPointF.recycleInstance(t)
            }
        }
    }

    /**
     * limit scaleX range
     */
    private fun getLimitedScaleX(scaleX: Float, t: MPPointF): Float {
        val h = chart!!.viewPortHandler
        tempMatrix.set(savedMatrix)
        tempMatrix.postScale(scaleX, 1f, t.x, t.y)

        savedMatrix.getValues(matrixBuffer)
        val lastScaleX = matrixBuffer[Matrix.MSCALE_X]

        tempMatrix.getValues(matrixBuffer)
        val calScaleX = matrixBuffer[Matrix.MSCALE_X]

        var resultScaleX = scaleX

        if (calScaleX < h.minScaleX) {
            resultScaleX = h.minScaleX / lastScaleX
        } else if (calScaleX > h.maxScaleX) {
            resultScaleX = h.maxScaleX / lastScaleX
        }
        return resultScaleX
    }

    /**
     * limit scaleY range
     */
    private fun getLimitedScaleY(scaleY: Float, t: MPPointF): Float {
        val h = chart!!.viewPortHandler
        tempMatrix.set(savedMatrix)
        tempMatrix.postScale(1f, scaleY, t.x, t.y)

        savedMatrix.getValues(matrixBuffer)
        val lastScaleY = matrixBuffer[Matrix.MSCALE_Y]

        tempMatrix.getValues(matrixBuffer)
        val calScaleY = matrixBuffer[Matrix.MSCALE_Y]

        var resultScaleY = scaleY

        if (calScaleY < h.minScaleY) {
            resultScaleY = h.minScaleY / lastScaleY
        } else if (calScaleY > h.maxScaleY) {
            resultScaleY = h.maxScaleY / lastScaleY
        }
        return resultScaleY
    }

    /**
     * Highlights upon dragging, generates callbacks for the selection-listener.
     *
     * @param motionEvent
     */
    private fun performHighlightDrag(motionEvent: MotionEvent) {
        val highlight = chart!!.getHighlightByTouchPoint(motionEvent.x, motionEvent.y)

        if (highlight != null && !highlight.equalTo(mLastHighlighted)) {
            mLastHighlighted = highlight
            chart!!.highlightValue(highlight, true)
        }
    }


    /**
     * Returns a recyclable MPPointF instance.
     * returns the correct translation depending on the provided x and y touch
     * points
     */
    fun getTrans(x: Float, y: Float): MPPointF {
        val vph = chart!!.viewPortHandler

        val xTrans = x - vph.offsetLeft()

        // check if axis is inverted
        val yTrans: Float = if (inverted()) {
            -(y - vph.offsetTop())
        } else {
            -(chart!!.measuredHeight - y - vph.offsetBottom())
        }

        return MPPointF.getInstance(xTrans, yTrans)
    }

    /**
     * Returns true if the current touch situation should be interpreted as inverted, false if not.
     */
    private fun inverted(): Boolean {
        return (closestDataSetToTouch == null && chart!!.isAnyAxisInverted) || (closestDataSetToTouch != null
                && chart!!.isInverted(closestDataSetToTouch!!.axisDependency))
    }

    /**
     * Sets the minimum distance that will be interpreted as a "drag" by the chart in dp.
     * Default: 3dp
     *
     * @param dragTriggerDistance
     */
    fun setDragTriggerDist(dragTriggerDistance: Float) {
        this.dragTriggerDist = dragTriggerDistance.convertDpToPixel()
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        lastGesture = ChartGesture.DOUBLE_TAP

        val onChartGestureListener = chart!!.onChartGestureListener

        onChartGestureListener?.onChartDoubleTapped(e)

        // check if double-tap zooming is enabled
        if (chart!!.isDoubleTapToZoomEnabled && chart!!.data!!.entryCount > 0) {
            val trans = getTrans(e.x, e.y)

            val scaleX = if (chart!!.isScaleXEnabled) 1.4f else 1f
            val scaleY = if (chart!!.isScaleYEnabled) 1.4f else 1f

            chart!!.zoom(scaleX, scaleY, trans.x, trans.y)

            if (chart!!.isLogEnabled) Log.i(
                "BarlineChartTouch", ("Double-Tap, Zooming In, x: " + trans.x + ", y: "
                        + trans.y)
            )

            onChartGestureListener?.onChartScale(e, scaleX, scaleY)

            MPPointF.recycleInstance(trans)
        }

        return super.onDoubleTap(e)
    }

    override fun onLongPress(e: MotionEvent) {
        lastGesture = ChartGesture.LONG_PRESS

        val l = chart!!.onChartGestureListener

        l?.onChartLongPressed(e)
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        lastGesture = ChartGesture.SINGLE_TAP

        val l = chart!!.onChartGestureListener

        l?.onChartSingleTapped(e)

        if (!chart!!.isHighlightPerTapEnabled) {
            return false
        }

        val h = chart!!.getHighlightByTouchPoint(e.x, e.y)
        performHighlight(h, e)

        return super.onSingleTapUp(e)
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        lastGesture = ChartGesture.FLING

        val chartGestureListener = chart!!.onChartGestureListener

        chartGestureListener?.onChartFling(e1, e2, velocityX, velocityY)

        return super.onFling(e1, e2, velocityX, velocityY)
    }

    fun stopDeceleration() {
        decelerationVelocity.x = 0f
        decelerationVelocity.y = 0f
    }

    fun computeScroll() {
        if (decelerationVelocity.x == 0f && decelerationVelocity.y == 0f) return  // There's no deceleration in progress


        val currentTime = AnimationUtils.currentAnimationTimeMillis()

        decelerationVelocity.x *= chart!!.dragDecelerationFrictionCoef
        decelerationVelocity.y *= chart!!.dragDecelerationFrictionCoef

        val timeInterval = (currentTime - decelerationLastTime).toFloat() / 1000f

        val distanceX = decelerationVelocity.x * timeInterval
        val distanceY = decelerationVelocity.y * timeInterval

        decelerationCurrentPoint.x += distanceX
        decelerationCurrentPoint.y += distanceY

        val event = MotionEvent.obtain(
            currentTime, currentTime, MotionEvent.ACTION_MOVE, decelerationCurrentPoint.x,
            decelerationCurrentPoint.y, 0
        )

        val dragDistanceX = if (chart!!.isDragXEnabled) decelerationCurrentPoint.x - touchStartPoint.x else 0f
        val dragDistanceY = if (chart!!.isDragYEnabled) decelerationCurrentPoint.y - touchStartPoint.y else 0f

        performDrag(event, dragDistanceX, dragDistanceY)

        event.recycle()
        matrix = chart!!.viewPortHandler.refresh(matrix, chart!!, false)

        decelerationLastTime = currentTime

        if (abs(decelerationVelocity.x.toDouble()) >= 0.01 || abs(decelerationVelocity.y.toDouble()) >= 0.01)
            chart?.postInvalidateOnAnimation() // This causes computeScroll to fire, recommended for this by Google
        else {
            // Range might have changed, which means that Y-axis labels
            // could have changed in size, affecting Y-axis size.
            // So we need to recalculate offsets.
            chart?.calculateOffsets()
            chart?.postInvalidate()

            stopDeceleration()
        }
    }

    companion object {
        /**
         * ################ ################ ################ ################
         */
        /** DOING THE MATH BELOW ;-)  */
        /**
         * Determines the center point between two pointer touch points.
         *
         * @param point
         * @param event
         */
        private fun midPoint(point: MPPointF, event: MotionEvent) {
            val x = event.getX(0) + event.getX(1)
            val y = event.getY(0) + event.getY(1)
            point.x = (x / 2f)
            point.y = (y / 2f)
        }

        /**
         * returns the distance between two pointer touch points
         */
        private fun spacing(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return sqrt((x * x + y * y).toDouble()).toFloat()
        }

        /**
         * calculates the distance on the x-axis between two pointers (fingers on
         * the display)
         */
        private fun getXDist(e: MotionEvent): Float {
            val x = abs((e.getX(0) - e.getX(1)).toDouble()).toFloat()
            return x
        }

        /**
         * calculates the distance on the y-axis between two pointers (fingers on
         * the display)
         */
        private fun getYDist(e: MotionEvent): Float {
            val y = abs((e.getY(0) - e.getY(1)).toDouble()).toFloat()
            return y
        }
    }
}
