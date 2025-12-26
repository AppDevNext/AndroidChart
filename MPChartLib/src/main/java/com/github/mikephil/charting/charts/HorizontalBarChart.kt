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
import com.github.mikephil.charting.utils.MPPointF.Companion.getInstance
import com.github.mikephil.charting.utils.TransformerHorizontalBarChart
import com.github.mikephil.charting.utils.convertDpToPixel
import kotlin.math.max
import kotlin.math.min

/**
 * BarChart with horizontal bar orientation. In this implementation, x- and y-axis are switched, meaning the YAxis class
 * represents the horizontal values and the XAxis class represents the vertical values.
 */
open class HorizontalBarChart : BarChart {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        viewPortHandler = HorizontalViewPortHandler()

        mLeftAxisTransformer = TransformerHorizontalBarChart(viewPortHandler)
        mRightAxisTransformer = TransformerHorizontalBarChart(viewPortHandler)

        mRenderer = HorizontalBarChartRenderer(this, mAnimator, viewPortHandler)
        setHighlighter(HorizontalBarHighlighter(this))

        mAxisRendererLeft = YAxisRendererHorizontalBarChart(viewPortHandler, mAxisLeft, mLeftAxisTransformer)
        mAxisRendererRight = YAxisRendererHorizontalBarChart(viewPortHandler, mAxisRight, mRightAxisTransformer)
        mXAxisRenderer = XAxisRendererHorizontalBarChart(viewPortHandler, mXAxis, mLeftAxisTransformer)
    }

    private val mOffsetsBuffer = RectF()

    override fun calculateLegendOffsets(offsets: RectF) {
        offsets.left = 0f
        offsets.right = 0f
        offsets.top = 0f
        offsets.bottom = 0f

        if (legend == null || !legend!!.isEnabled || legend!!.isDrawInsideEnabled) {
            return
        }

        legend?.let { legend ->
            when (legend.orientation) {
                LegendOrientation.VERTICAL -> when (legend.horizontalAlignment) {
                    LegendHorizontalAlignment.LEFT -> offsets.left += min(
                        legend.mNeededWidth,
                        viewPortHandler.chartWidth * legend.maxSizePercent
                    ) + legend.xOffset

                    LegendHorizontalAlignment.RIGHT -> offsets.right += min(
                        legend.mNeededWidth,
                        viewPortHandler.chartWidth * legend.maxSizePercent
                    ) + legend.xOffset

                    LegendHorizontalAlignment.CENTER -> when (legend.verticalAlignment) {
                        LegendVerticalAlignment.TOP -> offsets.top += min(
                            legend.mNeededHeight,
                            viewPortHandler.chartHeight * legend.maxSizePercent
                        ) + legend.yOffset

                        LegendVerticalAlignment.BOTTOM -> offsets.bottom += min(
                            legend.mNeededHeight,
                            viewPortHandler.chartHeight * legend.maxSizePercent
                        ) + legend.yOffset

                        else -> {}
                    }
                }

                LegendOrientation.HORIZONTAL -> when (legend.verticalAlignment) {
                    LegendVerticalAlignment.TOP -> {
                        offsets.top += min(
                            legend.mNeededHeight,
                            viewPortHandler.chartHeight * legend.maxSizePercent
                        ) + legend.yOffset

                        if (mAxisLeft.isEnabled && mAxisLeft.isDrawLabelsEnabled) {
                            offsets.top += mAxisLeft.getRequiredHeightSpace(
                                mAxisRendererLeft.paintAxisLabels
                            )
                        }
                    }

                    LegendVerticalAlignment.BOTTOM -> {
                        offsets.bottom += min(
                            legend.mNeededHeight,
                            viewPortHandler.chartHeight * legend.maxSizePercent
                        ) + legend.yOffset

                        if (mAxisRight.isEnabled && mAxisRight.isDrawLabelsEnabled) {
                            offsets.bottom += mAxisRight.getRequiredHeightSpace(
                                mAxisRendererRight.paintAxisLabels
                            )
                        }
                    }

                    else -> {}
                }
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
        if (mAxisLeft.needsOffset()) {
            offsetTop += mAxisLeft.getRequiredHeightSpace(mAxisRendererLeft.paintAxisLabels)
        }

        if (mAxisRight.needsOffset()) {
            offsetBottom += mAxisRight.getRequiredHeightSpace(mAxisRendererRight.paintAxisLabels)
        }

        val xLabelWidth = mXAxis.mLabelWidth.toFloat()

        if (mXAxis.isEnabled) {
            // offsets for x-labels

            when (mXAxis.position) {
                XAxisPosition.BOTTOM -> {
                    offsetLeft += xLabelWidth
                }
                XAxisPosition.TOP -> {
                    offsetRight += xLabelWidth
                }
                XAxisPosition.BOTH_SIDED -> {
                    offsetLeft += xLabelWidth
                    offsetRight += xLabelWidth
                }

                XAxisPosition.TOP_INSIDE -> TODO()
                XAxisPosition.BOTTOM_INSIDE -> TODO()
                null -> Log.w(LOG_TAG, "XAxisPosition is null")
            }
        }

        offsetTop += extraTopOffset
        offsetRight += extraRightOffset
        offsetBottom += extraBottomOffset
        offsetLeft += extraLeftOffset

        val minOffset = minOffset.convertDpToPixel()

        viewPortHandler.restrainViewPort(
            max(minOffset, offsetLeft),
            max(minOffset, offsetTop),
            max(minOffset, offsetRight),
            max(minOffset, offsetBottom)
        )

        if (isLogEnabled) {
            Log.i(
                LOG_TAG, "offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop + ", offsetRight: " +
                        offsetRight + ", offsetBottom: " + offsetBottom
            )
            Log.i(LOG_TAG, "Content: " + viewPortHandler.contentRect)
        }

        prepareOffsetMatrix()
        prepareValuePxMatrix()
    }

    override fun prepareValuePxMatrix() {
        mRightAxisTransformer.prepareMatrixValuePx(
            mAxisRight.mAxisMinimum, mAxisRight.mAxisRange, mXAxis.mAxisRange,
            mXAxis.mAxisMinimum
        )
        mLeftAxisTransformer.prepareMatrixValuePx(
            mAxisLeft.mAxisMinimum, mAxisLeft.mAxisRange, mXAxis.mAxisRange,
            mXAxis.mAxisMinimum
        )
    }

    override fun getMarkerPosition(high: Highlight): FloatArray {
        return floatArrayOf(high.drawY, high.drawX)
    }

    override fun getBarBounds(barEntry: BarEntry, outputRect: RectF) {
        mData?.let { data ->
            val set = data.getDataSetForEntry(barEntry)

            val y = barEntry.y
            val x = barEntry.x

            val barWidth = data.barWidth

            val top = x - barWidth / 2f
            val bottom = x + barWidth / 2f
            val left = if (y >= 0) y else 0f
            val right = if (y <= 0) y else 0f

            outputRect.set(left, top, right, bottom)

            getTransformer(set!!.axisDependency).rectValueToPixel(outputRect)
        }
    }

    protected var getPositionBuffer: FloatArray = FloatArray(2)

    /**
     * Returns a recyclable MPPointF instance.
     */
    override fun getPosition(e: Entry?, axis: AxisDependency?): MPPointF? {
        if (e == null) {
            return null
        }

        val vals = getPositionBuffer
        vals[0] = e.y
        vals[1] = e.x

        getTransformer(axis).pointValuesToPixel(vals)

        return getInstance(vals[0], vals[1])
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch point
     * inside the BarChart.
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            if (isLogEnabled) {
                Log.e(LOG_TAG, "Can't select by touch. No data set.")
            }
            return null
        } else {
            return highlighter?.getHighlight(y, x) // switch x and y
        }
    }

    override val lowestVisibleX: Float
        get() {
            getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), posForGetLowestVisibleX
            )
            return max(mXAxis.mAxisMinimum.toDouble(), posForGetLowestVisibleX.y).toFloat()
        }

    override val highestVisibleX: Float
        get() {
            getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentTop(), posForGetHighestVisibleX
            )
            return min(mXAxis.mAxisMaximum.toDouble(), posForGetHighestVisibleX.y).toFloat()
        }

    /**
     * ###### VIEWPORT METHODS BELOW THIS ######
     */
    override fun setVisibleXRangeMaximum(maxXRange: Float) {
        val xScale = mXAxis.mAxisRange / (maxXRange)
        viewPortHandler.setMinimumScaleY(xScale)
    }

    override fun setVisibleXRangeMinimum(minXRange: Float) {
        val xScale = mXAxis.mAxisRange / (minXRange)
        viewPortHandler.setMaximumScaleY(xScale)
    }

    override fun setVisibleXRange(minXRange: Float, maxXRange: Float) {
        val minScale = mXAxis.mAxisRange / minXRange
        val maxScale = mXAxis.mAxisRange / maxXRange
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
