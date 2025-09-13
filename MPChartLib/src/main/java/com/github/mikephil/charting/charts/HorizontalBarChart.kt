package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.HorizontalBarHighlighter
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer
import com.github.mikephil.charting.renderer.XAxisRendererHorizontalBarChart
import com.github.mikephil.charting.renderer.YAxisRendererHorizontalBarChart
import com.github.mikephil.charting.utils.HorizontalViewPortHandler
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.TransformerHorizontalBarChart
import com.github.mikephil.charting.utils.Utils
import kotlin.math.max
import kotlin.math.min

/**
 * BarChart with horizontal bar orientation. In this implementation, x- and y-axis are switched, meaning the YAxis class
 * represents the horizontal values and the XAxis class represents the vertical values.
 *
 * @author Philipp Jahoda
 */
class HorizontalBarChart : BarChart {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        viewPortHandler = HorizontalViewPortHandler()

        super.init()

        mLeftAxisTransformer = TransformerHorizontalBarChart(viewPortHandler)
        mRightAxisTransformer = TransformerHorizontalBarChart(viewPortHandler)

        mRenderer = HorizontalBarChartRenderer(this, mAnimator, viewPortHandler)
        setHighlighter(HorizontalBarHighlighter(this))

        mAxisRendererLeft = YAxisRendererHorizontalBarChart(viewPortHandler, mAxisLeft!!, mLeftAxisTransformer)
        mAxisRendererRight = YAxisRendererHorizontalBarChart(viewPortHandler, mAxisRight!!, mRightAxisTransformer)
        mXAxisRenderer = XAxisRendererHorizontalBarChart(viewPortHandler, mXAxis!!, mLeftAxisTransformer)
    }

    private val mOffsetsBuffer = RectF()

    override fun calculateLegendOffsets(offsets: RectF) {
        offsets.left = 0f
        offsets.right = 0f
        offsets.top = 0f
        offsets.bottom = 0f

        if (legend == null || !legend!!.isEnabled || legend!!.isDrawInsideEnabled) return

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

                else -> {}
            }

            LegendOrientation.HORIZONTAL -> when (legend!!.verticalAlignment) {
                LegendVerticalAlignment.TOP -> {
                    offsets.top += min(
                        legend!!.mNeededHeight,
                        viewPortHandler.chartHeight * legend!!.maxSizePercent
                    ) + legend!!.yOffset

                    if (mAxisLeft!!.isEnabled && mAxisLeft!!.isDrawLabelsEnabled) offsets.top += mAxisLeft!!.getRequiredHeightSpace(
                        mAxisRendererLeft!!.paintAxisLabels
                    )
                }

                LegendVerticalAlignment.BOTTOM -> {
                    offsets.bottom += min(
                        legend!!.mNeededHeight,
                        viewPortHandler.chartHeight * legend!!.maxSizePercent
                    ) + legend!!.yOffset

                    if (mAxisRight!!.isEnabled && mAxisRight!!.isDrawLabelsEnabled) offsets.bottom += mAxisRight!!.getRequiredHeightSpace(
                        mAxisRendererRight!!.paintAxisLabels
                    )
                }

                else -> {}
            }
        }
    }

    override fun calculateOffsets() {
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
        if (mAxisLeft!!.needsOffset()) {
            offsetTop += mAxisLeft!!.getRequiredHeightSpace(mAxisRendererLeft!!.paintAxisLabels)
        }

        if (mAxisRight!!.needsOffset()) {
            offsetBottom += mAxisRight!!.getRequiredHeightSpace(mAxisRendererRight!!.paintAxisLabels)
        }

        val xLabelWidth = mXAxis!!.mLabelWidth.toFloat()

        if (mXAxis!!.isEnabled) {
            // offsets for x-labels

            if (mXAxis!!.position == XAxisPosition.BOTTOM) {
                offsetLeft += xLabelWidth
            } else if (mXAxis!!.position == XAxisPosition.TOP) {
                offsetRight += xLabelWidth
            } else if (mXAxis!!.position == XAxisPosition.BOTH_SIDED) {
                offsetLeft += xLabelWidth
                offsetRight += xLabelWidth
            }
        }

        offsetTop += extraTopOffset
        offsetRight += extraRightOffset
        offsetBottom += extraBottomOffset
        offsetLeft += extraLeftOffset

        val minOffset = Utils.convertDpToPixel(minOffset)

        viewPortHandler.restrainViewPort(
            max(minOffset, offsetLeft),
            max(minOffset, offsetTop),
            max(minOffset, offsetRight),
            max(minOffset, offsetBottom)
        )

        if (isLogEnabled) {
            Log.i(
                LOG_TAG, ("offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop + ", offsetRight: " +
                        offsetRight + ", offsetBottom: "
                        + offsetBottom)
            )
            Log.i(LOG_TAG, "Content: " + viewPortHandler.contentRect.toString())
        }

        prepareOffsetMatrix()
        prepareValuePxMatrix()
    }

    override fun prepareValuePxMatrix() {
        mRightAxisTransformer!!.prepareMatrixValuePx(
            mAxisRight!!.mAxisMinimum, mAxisRight!!.mAxisRange, mXAxis!!.mAxisRange,
            mXAxis!!.mAxisMinimum
        )
        mLeftAxisTransformer!!.prepareMatrixValuePx(
            mAxisLeft!!.mAxisMinimum, mAxisLeft!!.mAxisRange, mXAxis!!.mAxisRange,
            mXAxis!!.mAxisMinimum
        )
    }

    override fun getMarkerPosition(high: Highlight): FloatArray {
        return floatArrayOf(high.drawY, high.drawX)
    }

    override fun getBarBounds(barEntry: BarEntry, outputRect: RectF) {
        val bounds = outputRect
        val set = mData!!.getDataSetForEntry(barEntry)

        if (set == null) {
            outputRect.set(Float.Companion.MIN_VALUE, Float.Companion.MIN_VALUE, Float.Companion.MIN_VALUE, Float.Companion.MIN_VALUE)
            return
        }

        val y = barEntry.y
        val x = barEntry.x

        val barWidth = mData!!.barWidth

        val top = x - barWidth / 2f
        val bottom = x + barWidth / 2f
        val left = if (y >= 0) y else 0f
        val right = if (y <= 0) y else 0f

        bounds.set(left, top, right, bottom)

        getTransformer(set.axisDependency).rectValueToPixel(bounds)
    }

    /**
     * Returns a recyclable MPPointF instance.
     *
     * @param e
     * @param axis
     * @return
     */
    override fun getPosition(e: Entry, axis: AxisDependency?): MPPointF? {
        val vals = mGetPositionBuffer
        vals[0] = e.y
        vals[1] = e.x

        getTransformer(axis).pointValuesToPixel(vals)

        return MPPointF.Companion.getInstance(vals[0], vals[1])
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch point
     * inside the BarChart.
     *
     * @param x
     * @param y
     * @return
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            if (isLogEnabled) Log.e(LOG_TAG, "Can't select by touch. No data set.")
            return null
        } else return highlighter!!.getHighlight(y, x) // switch x and y
    }

    override val lowestVisibleX: Float
        get() {
            getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), posForGetLowestVisibleX
            )
            val result = max(mXAxis!!.mAxisMinimum.toDouble(), posForGetLowestVisibleX.y).toFloat()
            return result
        }

    override val highestVisibleX: Float
        get() {
            getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentTop(), posForGetHighestVisibleX
            )
            val result = min(mXAxis!!.mAxisMaximum.toDouble(), posForGetHighestVisibleX.y).toFloat()
            return result
        }

    /**
     * ###### VIEWPORT METHODS BELOW THIS ######
     */
    override fun setVisibleXRangeMaximum(maxXRange: Float) {
        val xScale = mXAxis!!.mAxisRange / (maxXRange)
        viewPortHandler.setMinimumScaleY(xScale)
    }

    override fun setVisibleXRangeMinimum(minXRange: Float) {
        val xScale = mXAxis!!.mAxisRange / (minXRange)
        viewPortHandler.setMaximumScaleY(xScale)
    }

    override fun setVisibleXRange(minXRange: Float, maxXRange: Float) {
        val minScale = mXAxis!!.mAxisRange / minXRange
        val maxScale = mXAxis!!.mAxisRange / maxXRange
        viewPortHandler.setMinMaxScaleY(minScale, maxScale)
    }

    override fun setVisibleYRangeMaximum(maxYRange: Float, axis: AxisDependency?) {
        val yScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinimumScaleX(yScale)
    }

    override fun setVisibleYRangeMinimum(minYRange: Float, axis: AxisDependency?) {
        val yScale = getAxisRange(axis) / minYRange
        viewPortHandler.setMaximumScaleX(yScale)
    }

    override fun setVisibleYRange(minYRange: Float, maxYRange: Float, axis: AxisDependency?) {
        val minScale = getAxisRange(axis) / minYRange
        val maxScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinMaxScaleX(minScale, maxScale)
    }
}
