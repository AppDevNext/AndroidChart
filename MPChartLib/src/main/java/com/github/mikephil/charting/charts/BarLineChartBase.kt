package com.github.mikephil.charting.charts

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.ChartHighlighter
import com.github.mikephil.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.jobs.AnimatedMoveViewJob.Companion.getInstance
import com.github.mikephil.charting.jobs.AnimatedZoomJob.Companion.getInstance
import com.github.mikephil.charting.jobs.MoveViewJob.Companion.getInstance
import com.github.mikephil.charting.jobs.ZoomJob.Companion.getInstance
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.listener.OnDrawListener
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.utils.MPPointD.Companion.getInstance
import com.github.mikephil.charting.utils.MPPointD.Companion.recycleInstance
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.MPPointF.Companion.getInstance
import com.github.mikephil.charting.utils.MPPointF.Companion.recycleInstance
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.convertDpToPixel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Base-class of LineChart, BarChart, ScatterChart and CandleStickChart.
 */
@Suppress("unused")
@SuppressLint("RtlHardcoded")
abstract class BarLineChartBase<T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>>> : Chart<T>,
    BarLineScatterCandleBubbleDataProvider {
    /**
     * the maximum number of entries to which values will be drawn
     * (entry numbers greater than this value will cause value-labels to disappear)
     */
    override var maxVisibleCount: Int = 100
        protected set

    /**
     * Flag that indicates if auto scaling on the y axis is enabled. This is
     * especially interesting for charts displaying financial data.
     */
    var isAutoScaleMinMaxEnabled: Boolean = false

    /**
     * flag that indicates if pinch-zoom is enabled. if true, both x and y axis
     * can be scaled with 2 fingers, if false, x and y axis can be scaled separately
     */
    var isPinchZoomEnabled: Boolean = false
        protected set

    /**
     * flag that indicates if double tap zoom is enabled or not
     */
    var isDoubleTapToZoomEnabled: Boolean = true

    /**
     * flag that indicates if highlighting per dragging over a fully zoomed out
     * chart is enabled
     */
    var isHighlightPerDragEnabled: Boolean = true

    /**
     * if true, dragging is enabled for the chart
     */
    var isDragXEnabled: Boolean = true

    /**
     * Set this to true to enable dragging on the Y axis
     */
    var isDragYEnabled: Boolean = true

    var isScaleXEnabled: Boolean = true
    var isScaleYEnabled: Boolean = true

    /**
     * if true, fling gesture is enabled for the chart
     */
    var isFlingEnabled: Boolean = false

    /**
     * paint object for the (by default) lightgrey background of the grid
     */
    protected var mGridBackgroundPaint: Paint? = null

    protected var mBorderPaint: Paint? = null

    /**
     * flag indicating if the grid background should be drawn or not
     */
    protected var mDrawGridBackground: Boolean = false

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     */
    var isDrawBordersEnabled: Boolean = false
        protected set

    /**
     * When enabled, the values will be clipped to contentRect,
     * otherwise they can bleed outside the content rect.
     */
    var isClipValuesToContentEnabled: Boolean = false
        protected set

    /**
     * When disabled, the data and/or highlights will not be clipped to contentRect. Disabling this option can
     * be useful, when the data lies fully within the content rect, but is drawn in such a way (such as thick lines)
     * that there is unwanted clipping.
     */
    var isClipDataToContentEnabled: Boolean = true
        protected set

    /**
     * Gets the minimum offset (padding) around the chart, defaults to 15.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 15.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 15
     */
    var minOffset: Float = 15f

    /**
     * flag indicating if the chart should stay at the same position after a rotation. Default is false.
     */
    var isKeepPositionOnRotation: Boolean = false

    /**
     * the listener for user drawing on the chart
     */
    var drawListener: OnDrawListener? = null
        protected set

    /**
     * the object representing the labels on the left y-axis
     */
    protected var mAxisLeft: YAxis = YAxis(AxisDependency.LEFT)

    /**
     * the object representing the labels on the right y-axis
     */
    protected var mAxisRight: YAxis = YAxis(AxisDependency.RIGHT)

    protected var mAxisRendererLeft: YAxisRenderer
    protected var mAxisRendererRight: YAxisRenderer

    protected var mLeftAxisTransformer: Transformer = Transformer(viewPortHandler)
    protected var mRightAxisTransformer: Transformer = Transformer(viewPortHandler)

    protected var mXAxisRenderer: XAxisRenderer

    // /** the approximator object used for data filtering */
    // private Approximator mApproximator;
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)

    init {

        mAxisRendererLeft = YAxisRenderer(viewPortHandler, mAxisLeft, mLeftAxisTransformer)
        mAxisRendererRight = YAxisRenderer(viewPortHandler, mAxisRight, mRightAxisTransformer)

        mXAxisRenderer = XAxisRenderer(viewPortHandler, mXAxis, mLeftAxisTransformer)

        setHighlighter(ChartHighlighter<BarLineChartBase<T>>(this))

        mChartTouchListener = BarLineChartTouchListener(this, viewPortHandler.matrixTouch, 3f)

        mGridBackgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            // setColor(Color.WHITE);
            color = Color.rgb(240, 240, 240) // light
        }

        // grey
        mBorderPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.BLACK
            strokeWidth = 1f.convertDpToPixel()
        }
    }

    // for performance tracking
    private var totalTime: Long = 0
    private var drawCycles: Long = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mData == null) {
            return
        }

        val starttime = System.currentTimeMillis()

        // execute all drawing commands
        drawGridBackground(canvas)

        if (this.isAutoScaleMinMaxEnabled) {
            autoScale()
        }

        if (mAxisLeft.isEnabled) {
            mAxisRendererLeft.computeAxis(mAxisLeft.mAxisMinimum, mAxisLeft.mAxisMaximum, mAxisLeft.isInverted)
        }

        if (mAxisRight.isEnabled) {
            mAxisRendererRight.computeAxis(mAxisRight.mAxisMinimum, mAxisRight.mAxisMaximum, mAxisRight.isInverted)
        }

        if (mXAxis.isEnabled) {
            mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false)
        }

        // Y-axis labels could have changed in size affecting the offsets
        if (this.isAutoScaleMinMaxEnabled) {
            calculateOffsets()
            viewPortHandler.refresh(viewPortHandler.matrixTouch, this, false)
        }

        mXAxisRenderer.renderAxisLine(canvas)
        mAxisRendererLeft.renderAxisLine(canvas)
        mAxisRendererRight.renderAxisLine(canvas)

        if (mXAxis.isDrawGridLinesBehindDataEnabled) {
            mXAxisRenderer.renderGridLines(canvas)
        }

        if (mAxisLeft.isDrawGridLinesBehindDataEnabled) {
            mAxisRendererLeft.renderGridLines(canvas)
        }

        if (mAxisRight.isDrawGridLinesBehindDataEnabled) {
            mAxisRendererRight.renderGridLines(canvas)
        }

        if (mXAxis.isEnabled && mXAxis.isDrawLimitLinesBehindDataEnabled) {
            mXAxisRenderer.renderLimitLines(canvas)
        }

        if (mAxisLeft.isEnabled && mAxisLeft.isDrawLimitLinesBehindDataEnabled) {
            mAxisRendererLeft.renderLimitLines(canvas)
        }

        if (mAxisRight.isEnabled && mAxisRight.isDrawLimitLinesBehindDataEnabled) {
            mAxisRendererRight.renderLimitLines(canvas)
        }

        var clipRestoreCount = canvas.save()

        if (this.isClipDataToContentEnabled) {
            // make sure the data cannot be drawn outside the content-rect
            canvas.clipRect(viewPortHandler.contentRect)
        }

        mRenderer!!.drawData(canvas)

        if (!mXAxis.isDrawGridLinesBehindDataEnabled) {
            mXAxisRenderer.renderGridLines(canvas)
        }

        if (!mAxisLeft.isDrawGridLinesBehindDataEnabled) {
            mAxisRendererLeft.renderGridLines(canvas)
        }

        if (!mAxisRight.isDrawGridLinesBehindDataEnabled) {
            mAxisRendererRight.renderGridLines(canvas)
        }

        // if highlighting is enabled
        if (valuesToHighlight()) {
            mRenderer!!.drawHighlighted(canvas, highlighted!!)
        }

        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount)

        mRenderer!!.drawExtras(canvas)

        if (mXAxis.isEnabled && !mXAxis.isDrawLimitLinesBehindDataEnabled) {
            mXAxisRenderer.renderLimitLines(canvas)
        }

        if (mAxisLeft.isEnabled && !mAxisLeft.isDrawLimitLinesBehindDataEnabled) {
            mAxisRendererLeft.renderLimitLines(canvas)
        }

        if (mAxisRight.isEnabled && !mAxisRight.isDrawLimitLinesBehindDataEnabled) {
            mAxisRendererRight.renderLimitLines(canvas)
        }

        mXAxisRenderer.renderAxisLabels(canvas)
        mAxisRendererLeft.renderAxisLabels(canvas)
        mAxisRendererRight.renderAxisLabels(canvas)

        if (this.isClipValuesToContentEnabled) {
            clipRestoreCount = canvas.save()
            canvas.clipRect(viewPortHandler.contentRect)

            mRenderer!!.drawValues(canvas)

            canvas.restoreToCount(clipRestoreCount)
        } else {
            mRenderer!!.drawValues(canvas)
        }

        legendRenderer!!.renderLegend(canvas)

        drawDescription(canvas)

        drawMarkers(canvas)

        if (isLogEnabled) {
            val drawtime = (System.currentTimeMillis() - starttime)
            totalTime += drawtime
            drawCycles += 1
            val average = totalTime / drawCycles
            Log.i(LOG_TAG, "Drawtime: " + drawtime + " ms, average: " + average + " ms, cycles: " + drawCycles)
        }
    }

    /**
     * RESET PERFORMANCE TRACKING FIELDS
     */
    fun resetTracking() {
        totalTime = 0
        drawCycles = 0
    }

    protected open fun prepareValuePxMatrix() {
        if (isLogEnabled) {
            Log.i(LOG_TAG, "Preparing Value-Px Matrix, xmin: " + mXAxis.mAxisMinimum + ", xmax: " + mXAxis.mAxisMaximum + ", xdelta: " + mXAxis.mAxisRange)
        }

        mRightAxisTransformer.prepareMatrixValuePx(mXAxis.mAxisMinimum, mXAxis.mAxisRange, mAxisRight.mAxisRange, this.mAxisRight.mAxisMinimum)
        mLeftAxisTransformer.prepareMatrixValuePx(mXAxis.mAxisMinimum, mXAxis.mAxisRange, mAxisLeft.mAxisRange, mAxisLeft.mAxisMinimum)
    }

    protected fun prepareOffsetMatrix() {
        mRightAxisTransformer.prepareMatrixOffset(mAxisRight.isInverted)
        mLeftAxisTransformer.prepareMatrixOffset(mAxisLeft.isInverted)
    }

    override fun notifyDataSetChanged() {
        if (mData == null) {
            if (isLogEnabled) {
                Log.i(LOG_TAG, "Preparing... DATA NOT SET.")
            }
            return
        } else {
            if (isLogEnabled) {
                Log.i(LOG_TAG, "Preparing...")
            }
        }

        if (mRenderer != null) {
            mRenderer!!.initBuffers()
        }

        calcMinMax()

        mAxisRendererLeft.computeAxis(mAxisLeft.mAxisMinimum, mAxisLeft.mAxisMaximum, mAxisLeft.isInverted)
        mAxisRendererRight.computeAxis(mAxisRight.mAxisMinimum, mAxisRight.mAxisMaximum, mAxisRight.isInverted)
        mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false)

        if (legend != null) {
            legendRenderer!!.computeLegend(mData!!)
        }

        calculateOffsets()
    }

    /**
     * Performs auto scaling of the axis by recalculating the minimum and maximum y-values based on the entries currently in view.
     */
    protected fun autoScale() {
        val fromX = lowestVisibleX
        val toX = highestVisibleX

        mData!!.calcMinMaxY(fromX, toX)

        calcMinMax()
    }

    override fun calcMinMax() {
        mXAxis.calculate(mData!!.xMin, mData!!.xMax)

        // calculate axis range (min / max) according to provided data
        mAxisLeft.calculate(mData!!.getYMin(AxisDependency.LEFT), mData!!.getYMax(AxisDependency.LEFT))
        mAxisRight.calculate(mData!!.getYMin(AxisDependency.RIGHT), mData!!.getYMax(AxisDependency.RIGHT))
    }

    protected open fun calculateLegendOffsets(offsets: RectF) {
        offsets.left = 0f
        offsets.right = 0f
        offsets.top = 0f
        offsets.bottom = 0f

        if (legend == null || !legend!!.isEnabled || legend!!.isDrawInsideEnabled) {
            return
        }

        when (legend!!.orientation) {
            LegendOrientation.VERTICAL -> when (legend!!.horizontalAlignment) {
                LegendHorizontalAlignment.LEFT -> offsets.left += min(
                    legend!!.mNeededWidth,
                    viewPortHandler.chartWidth * legend!!.maxSizePercent
                ) + legend!!.xOffset

                LegendHorizontalAlignment.RIGHT -> offsets.right += min(
                    legend!!.mNeededWidth,
                    viewPortHandler.chartWidth * legend!!.maxSizePercent
                ) + legend!!.xOffset

                LegendHorizontalAlignment.CENTER -> when (legend!!.verticalAlignment) {
                    LegendVerticalAlignment.TOP -> offsets.top += min(
                        legend!!.mNeededHeight,
                        viewPortHandler.chartHeight * legend!!.maxSizePercent
                    ) + legend!!.yOffset

                    LegendVerticalAlignment.BOTTOM -> offsets.bottom += min(
                        legend!!.mNeededHeight,
                        viewPortHandler.chartHeight * legend!!.maxSizePercent
                    ) + legend!!.yOffset

                    else -> {}
                }
            }

            LegendOrientation.HORIZONTAL -> when (legend!!.verticalAlignment) {
                LegendVerticalAlignment.TOP -> offsets.top += min(
                    legend!!.mNeededHeight,
                    viewPortHandler.chartHeight * legend!!.maxSizePercent
                ) + legend!!.yOffset


                LegendVerticalAlignment.BOTTOM -> offsets.bottom += min(
                    legend!!.mNeededHeight,
                    viewPortHandler.chartHeight * legend!!.maxSizePercent
                ) + legend!!.yOffset


                else -> {}
            }
        }
    }

    private val mOffsetsBuffer = RectF()

    public override fun calculateOffsets() {
        if (!mCustomViewPortEnabled) {
            var offsetLeft = 0f
            var offsetRight = 0f
            var offsetTop = 0f
            var offsetBottom = 0f

            calculateLegendOffsets(mOffsetsBuffer)

            offsetLeft += mOffsetsBuffer.left
            offsetTop += mOffsetsBuffer.top
            offsetRight += mOffsetsBuffer.right
            offsetBottom += mOffsetsBuffer.bottom

            // offsets for y-labels
            if (mAxisLeft.needsOffset()) {
                offsetLeft += mAxisLeft.getRequiredWidthSpace(mAxisRendererLeft.paintAxisLabels)
            }

            if (mAxisRight.needsOffset()) {
                offsetRight += mAxisRight.getRequiredWidthSpace(mAxisRendererRight.paintAxisLabels)
            }

            if (mXAxis.isEnabled && mXAxis.isDrawLabelsEnabled) {
                val xLabelHeight = mXAxis.mLabelHeight + mXAxis.yOffset

                // offsets for x-labels
                if (mXAxis.position == XAxisPosition.BOTTOM) {
                    offsetBottom += xLabelHeight
                } else if (mXAxis.position == XAxisPosition.TOP) {
                    offsetTop += xLabelHeight
                } else if (mXAxis.position == XAxisPosition.BOTH_SIDED) {
                    offsetBottom += xLabelHeight
                    offsetTop += xLabelHeight
                }
            }

            offsetTop += extraTopOffset
            offsetRight += extraRightOffset
            offsetBottom += extraBottomOffset
            offsetLeft += extraLeftOffset

            val minOffset = minOffset.convertDpToPixel()

            viewPortHandler.restrainViewPort(max(minOffset, offsetLeft), max(minOffset, offsetTop), max(minOffset, offsetRight), max(minOffset, offsetBottom))

            if (isLogEnabled) {
                Log.i(LOG_TAG, "offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop + ", offsetRight: " + offsetRight + ", offsetBottom: " + offsetBottom)
                Log.i(LOG_TAG, "Content: " + viewPortHandler.contentRect)
            }
        }

        prepareOffsetMatrix()
        prepareValuePxMatrix()
    }

    /**
     * draws the grid background
     */
    protected fun drawGridBackground(c: Canvas) {
        if (mDrawGridBackground) {
            // draw the grid background

            c.drawRect(viewPortHandler.contentRect, mGridBackgroundPaint!!)
        }

        if (this.isDrawBordersEnabled) {
            c.drawRect(viewPortHandler.contentRect, mBorderPaint!!)
        }
    }

    /**
     * Returns the Transformer class that contains all matrices and is
     * responsible for transforming values into pixels on the screen and
     * backwards.
     */
    override fun getTransformer(axis: AxisDependency?): Transformer {
        if (axis == AxisDependency.LEFT) {
            return mLeftAxisTransformer
        } else {
            return mRightAxisTransformer
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)

        if (mChartTouchListener == null || mData == null) {
            return false
        }

        // check if touch gestures are enabled
        if (!mTouchEnabled) {
            return false
        } else {
            return mChartTouchListener!!.onTouch(this, event)
        }
    }

    override fun computeScroll() {
        if (mChartTouchListener is BarLineChartTouchListener) {
            (mChartTouchListener as BarLineChartTouchListener).computeScroll()
        }
    }

    /**
     * CODE BELOW THIS RELATED TO SCALING AND GESTURES AND MODIFICATION OF THE
     * VIEWPORT
     */
    protected var mZoomMatrixBuffer: Matrix = Matrix()

    /**
     * Zooms in by 1.4f, into the charts center.
     */
    fun zoomIn() {
        val center = viewPortHandler.contentCenter

        viewPortHandler.zoomIn(center.x, -center.y, mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        recycleInstance(center)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms out by 0.7f, from the charts center.
     */
    fun zoomOut() {
        val center = viewPortHandler.contentCenter

        viewPortHandler.zoomOut(center.x, -center.y, mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        recycleInstance(center)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms out to original size.
     */
    fun resetZoom() {
        viewPortHandler.resetZoom(mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms in or out by the given scale factor. x and y are the coordinates
     * (in pixels) of the zoom center.
     *
     * @param scaleX if < 1f --> zoom out, if > 1f --> zoom in
     * @param scaleY if < 1f --> zoom out, if > 1f --> zoom in
     */
    fun zoom(scaleX: Float, scaleY: Float, x: Float, y: Float) {
        viewPortHandler.zoom(scaleX, scaleY, x, -y, mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms in or out by the given scale factor.
     * x and y are the values (NOT PIXELS) of the zoom center..
     *
     * @param axis the axis relative to which the zoom should take place
     */
    fun zoom(scaleX: Float, scaleY: Float, xValue: Float, yValue: Float, axis: AxisDependency?) {
        val job: Runnable = getInstance(viewPortHandler, scaleX, scaleY, xValue, yValue, getTransformer(axis), axis, this)
        addViewportJob(job)
    }

    /**
     * Zooms to the center of the chart with the given scale factor.
     */
    fun zoomToCenter(scaleX: Float, scaleY: Float) {
        val center = centerOffsets

        val save = mZoomMatrixBuffer
        viewPortHandler.zoom(scaleX, scaleY, center.x, -center.y, save)
        viewPortHandler.refresh(save, this, false)
    }

    /**
     * Zooms by the specified scale factor to the specified values on the specified axis.
     */
    fun zoomAndCenterAnimated(scaleX: Float, scaleY: Float, xValue: Float, yValue: Float, axis: AxisDependency?, duration: Long) {
        val origin = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), axis)

        val job: Runnable = getInstance(
            viewPortHandler,
            this,
            getTransformer(axis),
            getAxis(axis),
            mXAxis.mAxisRange,
            scaleX,
            scaleY,
            viewPortHandler.scaleX,
            viewPortHandler.scaleY,
            xValue,
            yValue,
            origin.x.toFloat(),
            origin.y.toFloat(),
            duration
        )
        addViewportJob(job)

        recycleInstance(origin)
    }

    protected var mFitScreenMatrixBuffer: Matrix = Matrix()

    /**
     * Resets all zooming and dragging and makes the chart fit exactly it's
     * bounds.
     */
    fun fitScreen() {
        val save = mFitScreenMatrixBuffer
        viewPortHandler.fitScreen(save)
        viewPortHandler.refresh(save, this, false)

        calculateOffsets()
        postInvalidate()
    }

    /**
     * Sets the minimum scale factor value to which can be zoomed out. 1f =
     * fitScreen
     */
    fun setScaleMinima(scaleX: Float, scaleY: Float) {
        viewPortHandler.setMinimumScaleX(scaleX)
        viewPortHandler.setMinimumScaleY(scaleY)
    }

    /**
     * Sets the size of the area (range on the x-axis) that should be maximum
     * visible at once (no further zooming out allowed). If this is e.g. set to
     * 10, no more than a range of 10 on the x-axis can be viewed at once without
     * scrolling.
     *
     * @param maxXRange The maximum visible range of x-values.
     */
    open fun setVisibleXRangeMaximum(maxXRange: Float) {
        val xScale = mXAxis.mAxisRange / (maxXRange)
        viewPortHandler.setMinimumScaleX(xScale)
    }

    /**
     * Sets the size of the area (range on the x-axis) that should be minimum
     * visible at once (no further zooming in allowed). If this is e.g. set to
     * 10, no less than a range of 10 on the x-axis can be viewed at once without
     * scrolling.
     *
     * @param minXRange The minimum visible range of x-values.
     */
    open fun setVisibleXRangeMinimum(minXRange: Float) {
        val xScale = mXAxis.mAxisRange / (minXRange)
        viewPortHandler.setMaximumScaleX(xScale)
    }

    /**
     * Limits the maximum and minimum x range that can be visible by pinching and zooming. e.g. minRange=10, maxRange=100 the
     * smallest range to be displayed at once is 10, and no more than a range of 100 values can be viewed at once without
     * scrolling
     */
    open fun setVisibleXRange(minXRange: Float, maxXRange: Float) {
        val minScale = mXAxis.mAxisRange / minXRange
        val maxScale = mXAxis.mAxisRange / maxXRange
        viewPortHandler.setMinMaxScaleX(minScale, maxScale)
    }

    /**
     * Sets the size of the area (range on the y-axis) that should be maximum
     * visible at once.
     *
     * @param maxYRange the maximum visible range on the y-axis
     * @param axis      the axis for which this limit should apply
     */
    open fun setVisibleYRangeMaximum(maxYRange: Float, axis: AxisDependency?) {
        val yScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinimumScaleY(yScale)
    }

    /**
     * Sets the size of the area (range on the y-axis) that should be minimum visible at once, no further zooming in possible.
     *
     * @param axis the axis for which this limit should apply
     */
    open fun setVisibleYRangeMinimum(minYRange: Float, axis: AxisDependency?) {
        val yScale = getAxisRange(axis) / minYRange
        viewPortHandler.setMaximumScaleY(yScale)
    }

    /**
     * Limits the maximum and minimum y range that can be visible by pinching and zooming.
     */
    open fun setVisibleYRange(minYRange: Float, maxYRange: Float, axis: AxisDependency?) {
        val minScale = getAxisRange(axis) / minYRange
        val maxScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinMaxScaleY(minScale, maxScale)
    }


    /**
     * Moves the left side of the current viewport to the specified x-position.
     * This also refreshes the chart by calling invalidate().
     */
    fun moveViewToX(xValue: Float) {
        val job: Runnable = getInstance(viewPortHandler, xValue, 0f, getTransformer(AxisDependency.LEFT), this)

        addViewportJob(job)
    }

    /**
     * This will move the left side of the current viewport to the specified
     * x-value on the x-axis, and center the viewport to the specified y value on the y-axis.
     * This also refreshes the chart by calling invalidate().
     *
     * @param axis - which axis should be used as a reference for the y-axis
     */
    fun moveViewTo(xValue: Float, yValue: Float, axis: AxisDependency?) {
        val yInView = getAxisRange(axis) / viewPortHandler.scaleY

        val job: Runnable = getInstance(viewPortHandler, xValue, yValue + yInView / 2f, getTransformer(axis), this)

        addViewportJob(job)
    }

    /**
     * This will move the left side of the current viewport to the specified x-value
     * and center the viewport to the y value animated.
     * This also refreshes the chart by calling invalidate().
     *
     * @param duration the duration of the animation in milliseconds
     */
    fun moveViewToAnimated(xValue: Float, yValue: Float, axis: AxisDependency?, duration: Long) {
        val bounds = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), axis)

        val yInView = getAxisRange(axis) / viewPortHandler.scaleY

        val job: Runnable =
            getInstance(viewPortHandler, xValue, yValue + yInView / 2f, getTransformer(axis), this, bounds.x.toFloat(), bounds.y.toFloat(), duration)

        addViewportJob(job)

        recycleInstance(bounds)
    }

    /**
     * Centers the viewport to the specified y value on the y-axis.
     * This also refreshes the chart by calling invalidate().
     *
     * @param axis - which axis should be used as a reference for the y-axis
     */
    fun centerViewToY(yValue: Float, axis: AxisDependency?) {
        val valsInView = getAxisRange(axis) / viewPortHandler.scaleY

        val job: Runnable = getInstance(viewPortHandler, 0f, yValue + valsInView / 2f, getTransformer(axis), this)

        addViewportJob(job)
    }

    /**
     * This will move the center of the current viewport to the specified
     * x and y value.
     * This also refreshes the chart by calling invalidate().
     *
     * @param axis - which axis should be used as a reference for the y axis
     */
    fun centerViewTo(xValue: Float, yValue: Float, axis: AxisDependency?) {
        val yInView = getAxisRange(axis) / viewPortHandler.scaleY
        val xInView = xAxis.mAxisRange / viewPortHandler.scaleX

        val job: Runnable = getInstance(viewPortHandler, xValue - xInView / 2f, yValue + yInView / 2f, getTransformer(axis), this)

        addViewportJob(job)
    }

    /**
     * This will move the center of the current viewport to the specified
     * x and y value animated.
     *
     * @param duration the duration of the animation in milliseconds
     */
    fun centerViewToAnimated(xValue: Float, yValue: Float, axis: AxisDependency?, duration: Long) {
        val bounds = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), axis)

        val yInView = getAxisRange(axis) / viewPortHandler.scaleY
        val xInView = xAxis.mAxisRange / viewPortHandler.scaleX

        val job: Runnable = getInstance(
            viewPortHandler,
            xValue - xInView / 2f,
            yValue + yInView / 2f,
            getTransformer(axis),
            this,
            bounds.x.toFloat(),
            bounds.y.toFloat(),
            duration
        )

        addViewportJob(job)

        recycleInstance(bounds)
    }

    /**
     * flag that indicates if a custom viewport offset has been set
     */
    private var mCustomViewPortEnabled = false

    /**
     * Sets custom offsets for the current ViewPort (the offsets on the sides of
     * the actual chart window). Setting this will prevent the chart from
     * automatically calculating it's offsets. Use resetViewPortOffsets() to
     * undo this. ONLY USE THIS WHEN YOU KNOW WHAT YOU ARE DOING, else use
     * setExtraOffsets(...).
     */
    fun setViewPortOffsets(left: Float, top: Float, right: Float, bottom: Float) {
        mCustomViewPortEnabled = true
        post(object : Runnable {
            override fun run() {
                viewPortHandler.restrainViewPort(left, top, right, bottom)
                prepareOffsetMatrix()
                prepareValuePxMatrix()
            }
        })
    }

    /**
     * Resets all custom offsets set via setViewPortOffsets(...) method. Allows
     * the chart to again calculate all offsets automatically.
     */
    fun resetViewPortOffsets() {
        mCustomViewPortEnabled = false
        calculateOffsets()
    }

    /**
     * Returns the range of the specified axis.
     */
    protected fun getAxisRange(axis: AxisDependency?): Float {
        if (axis == AxisDependency.LEFT) {
            return mAxisLeft.mAxisRange
        } else {
            return mAxisRight.mAxisRange
        }
    }

    /**
     * Sets the OnDrawListener
     */
    fun setOnDrawListener(drawListener: OnDrawListener?) {
        this.drawListener = drawListener
    }

    protected var mGetPositionBuffer: FloatArray = FloatArray(2)

    /**
     * Returns a recyclable MPPointF instance.
     * Returns the position (in pixels) the provided Entry has inside the chart
     * view or null, if the provided Entry is null.
     */
    open fun getPosition(e: Entry?, axis: AxisDependency?): MPPointF? {
        if (e == null) {
            return null
        }

        mGetPositionBuffer[0] = e.x
        mGetPositionBuffer[1] = e.y

        getTransformer(axis).pointValuesToPixel(mGetPositionBuffer)

        return getInstance(mGetPositionBuffer[0], mGetPositionBuffer[1])
    }

    /**
     * sets the number of maximum visible drawn values on the chart only active
     * when setDrawValues() is enabled
     */
    fun setMaxVisibleValueCount(count: Int) {
        this.maxVisibleCount = count
    }

    /**
     * Sets the color for the background of the chart-drawing area (everything
     * behind the grid lines).
     */
    fun setGridBackgroundColor(color: Int) {
        mGridBackgroundPaint!!.color = color
    }

    var isDragEnabled: Boolean
        /**
         * Returns true if dragging is enabled for the chart, false if not.
         */
        get() = this.isDragXEnabled || this.isDragYEnabled
        /**
         * Set this to true to enable dragging (moving the chart with the finger)
         * for the chart (this does not effect scaling).
         */
        set(enabled) {
            this.isDragXEnabled = enabled
            this.isDragYEnabled = enabled
        }

    /**
     * Set this to true to enable scaling (zooming in and out by gesture) for
     * the chart (this does not effect dragging) on both X- and Y-Axis.
     */
    fun setScaleEnabled(enabled: Boolean) {
        this.isScaleXEnabled = enabled
        this.isScaleYEnabled = enabled
    }

    /**
     * set this to true to draw the grid background, false if not
     */
    fun setDrawGridBackground(enabled: Boolean) {
        mDrawGridBackground = enabled
    }

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     */
    fun setDrawBorders(enabled: Boolean) {
        this.isDrawBordersEnabled = enabled
    }

    /**
     * When enabled, the values will be clipped to contentRect,
     * otherwise they can bleed outside the content rect.
     */
    fun setClipValuesToContent(enabled: Boolean) {
        this.isClipValuesToContentEnabled = enabled
    }

    /**
     * When disabled, the data and/or highlights will not be clipped to contentRect. Disabling this option can
     * be useful, when the data lies fully within the content rect, but is drawn in such a way (such as thick lines)
     * that there is unwanted clipping.
     */
    fun setClipDataToContent(enabled: Boolean) {
        this.isClipDataToContentEnabled = enabled
    }

    /**
     * Sets the width of the border lines in dp.
     */
    fun setBorderWidth(width: Float) {
        mBorderPaint!!.strokeWidth = width.convertDpToPixel()
    }

    /**
     * Sets the color of the chart border lines.
     */
    fun setBorderColor(color: Int) {
        mBorderPaint!!.color = color
    }

    /**
     * Returns a recyclable MPPointD instance
     * Returns the x and y values in the chart at the given touch point
     * (encapsulated in a MPPointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelForValues(...).
     */
    fun getValuesByTouchPoint(x: Float, y: Float, axis: AxisDependency?): MPPointD {
        val result = getInstance(0.0, 0.0)
        getValuesByTouchPoint(x, y, axis, result)
        return result
    }

    fun getValuesByTouchPoint(x: Float, y: Float, axis: AxisDependency?, outputPoint: MPPointD) {
        getTransformer(axis).getValuesByTouchPoint(x, y, outputPoint)
    }

    /**
     * Returns a recyclable MPPointD instance
     * Transforms the given chart values into pixels. This is the opposite
     * method to getValuesByTouchPoint(...).
     */
    fun getPixelForValues(x: Float, y: Float, axis: AxisDependency?): MPPointD {
        return getTransformer(axis).getPixelForValues(x, y)
    }

    /**
     * returns the Entry object displayed at the touched position of the chart
     */
    fun getEntryByTouchPoint(x: Float, y: Float): Entry? {
        val h = getHighlightByTouchPoint(x, y)
        if (h != null) {
            return mData!!.getEntryForHighlight(h)
        }
        return null
    }

    /**
     * returns the DataSet object displayed at the touched position of the chart
     */
    fun getDataSetByTouchPoint(x: Float, y: Float): IBarLineScatterCandleBubbleDataSet<*>? {
        val h = getHighlightByTouchPoint(x, y)
        if (h != null) {
            return mData!!.getDataSetByIndex(h.dataSetIndex)
        }
        return null
    }

    /**
     * buffer for storing lowest visible x point
     */
    protected var posForGetLowestVisibleX: MPPointD = getInstance(0.0, 0.0)

    override val lowestVisibleX: Float
        /**
         * Returns the lowest x-index (value on the x-axis) that is still visible on the chart.
         */
        get() {
            getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentBottom(), posForGetLowestVisibleX)
            return max(mXAxis.mAxisMinimum.toDouble(), posForGetLowestVisibleX.x).toFloat()
        }

    /**
     * buffer for storing highest visible x point
     */
    protected var posForGetHighestVisibleX: MPPointD = getInstance(0.0, 0.0)

    override val highestVisibleX: Float
        /**
         * Returns the highest x-index (value on the x-axis) that is still visible
         * on the chart.
         */
        get() {
            getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(
                viewPortHandler.contentRight(),
                viewPortHandler.contentBottom(),
                posForGetHighestVisibleX
            )
            return min(mXAxis.mAxisMaximum.toDouble(), posForGetHighestVisibleX.x).toFloat()
        }

    val visibleXRange: Float
        /**
         * Returns the range visible on the x-axis.
         */
        get() = abs(highestVisibleX - lowestVisibleX)

    /**
     * returns the current x-scale factor
     */
    override fun getScaleX(): Float {
        return viewPortHandler.scaleX
    }

    /**
     * returns the current y-scale factor
     */
    override fun getScaleY(): Float {
        return viewPortHandler.scaleY
    }

    val isFullyZoomedOut: Boolean
        /**
         * if the chart is fully zoomed out, return true
         */
        get() = viewPortHandler.isFullyZoomedOut

    val axisLeft: YAxis
        /**
         * Returns the left y-axis object. In the horizontal bar-chart, this is the
         * top axis.
         */
        get() = mAxisLeft

    val axisRight: YAxis
        /**
         * Returns the right y-axis object. In the horizontal bar-chart, this is the
         * bottom axis.
         */
        get() = mAxisRight

    /**
     * Returns the y-axis object to the corresponding AxisDependency. In the
     * horizontal bar-chart, LEFT == top, RIGHT == BOTTOM
     */
    fun getAxis(axis: AxisDependency?): YAxis {
        if (axis == AxisDependency.LEFT) {
            return mAxisLeft
        } else {
            return mAxisRight
        }
    }

    override fun isInverted(axis: AxisDependency?): Boolean {
        return getAxis(axis).isInverted
    }

    /**
     * If set to true, both x and y axis can be scaled simultaneously with 2 fingers, if false,
     * x and y axis can be scaled separately. default: false
     */
    fun setPinchZoom(enabled: Boolean) {
        this.isPinchZoomEnabled = enabled
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the x-axis.
     */
    fun setDragOffsetX(offset: Float) {
        viewPortHandler.setDragOffsetX(offset)
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the y-axis.
     */
    fun setDragOffsetY(offset: Float) {
        viewPortHandler.setDragOffsetY(offset)
    }

    /**
     * Returns true if both drag offsets (x and y) are zero or smaller.
     */
    fun hasNoDragOffset(): Boolean {
        return viewPortHandler.hasNoDragOffset()
    }

    val rendererXAxis: XAxisRenderer
        get() = mXAxisRenderer

    /**
     * Sets a custom XAxisRenderer and overrides the existing (default) one.
     */
    fun setXAxisRenderer(xAxisRenderer: XAxisRenderer) {
        mXAxisRenderer = xAxisRenderer
    }

    var rendererLeftYAxis: YAxisRenderer
        get() = mAxisRendererLeft
        /**
         * Sets a custom axis renderer for the left axis and overwrites the existing one.
         */
        set(rendererLeftYAxis) {
            mAxisRendererLeft = rendererLeftYAxis
        }

    var rendererRightYAxis: YAxisRenderer
        get() = mAxisRendererRight
        /**
         * Sets a custom axis renderer for the right acis and overwrites the existing one.
         */
        set(rendererRightYAxis) {
            mAxisRendererRight = rendererRightYAxis
        }

    override val yChartMax: Float
        get() = max(mAxisLeft.mAxisMaximum, mAxisRight.mAxisMaximum)

    override val yChartMin: Float
        get() = min(mAxisLeft.mAxisMinimum, mAxisRight.mAxisMinimum)

    val isAnyAxisInverted: Boolean
        /**
         * Returns true if either the left or the right or both axes are inverted.
         */
        get() {
            if (mAxisLeft.isInverted) {
                return true
            }
            return mAxisRight.isInverted
        }

    override fun setPaint(p: Paint, which: Int) {
        super.setPaint(p, which)

        if (which == PAINT_GRID_BACKGROUND) {
            mGridBackgroundPaint = p
        }
    }

    override fun getPaint(which: Int): Paint? {
        val p = super.getPaint(which)
        if (p != null) {
            return p
        }

        if (which == PAINT_GRID_BACKGROUND) {
            return mGridBackgroundPaint
        }

        return null
    }

    protected var mOnSizeChangedBuffer: FloatArray = FloatArray(2)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Saving current position of chart.

        mOnSizeChangedBuffer[1] = 0f
        mOnSizeChangedBuffer[0] = mOnSizeChangedBuffer[1]

        if (this.isKeepPositionOnRotation) {
            mOnSizeChangedBuffer[0] = viewPortHandler.contentLeft()
            mOnSizeChangedBuffer[1] = viewPortHandler.contentTop()
            getTransformer(AxisDependency.LEFT).pixelsToValue(mOnSizeChangedBuffer)
        }

        //Superclass transforms chart.
        super.onSizeChanged(w, h, oldw, oldh)

        if (this.isKeepPositionOnRotation) {
            //Restoring old position of chart.

            getTransformer(AxisDependency.LEFT).pointValuesToPixel(mOnSizeChangedBuffer)
            viewPortHandler.centerViewPort(mOnSizeChangedBuffer, this)
        } else {
            viewPortHandler.refresh(viewPortHandler.matrixTouch, this, true)
        }
    }

    /**
     * Sets the text color to use for the labels. Make sure to use
     * getResources().getColor(...) when using a color from the resources.
     */
    fun setTextColor(color: Int) {
        mAxisRendererLeft.setTextColor(color)
        mAxisRendererRight.setTextColor(color)
        mXAxisRenderer.setTextColor(color)
    }
}
