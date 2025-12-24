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
 *
 * @author Philipp Jahoda
 */
class HorizontalBarChart : BarChart {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        mViewPortHandler = HorizontalViewPortHandler()

        super.init()

        mLeftAxisTransformer = TransformerHorizontalBarChart(mViewPortHandler)
        mRightAxisTransformer = TransformerHorizontalBarChart(mViewPortHandler)

        mRenderer = HorizontalBarChartRenderer(this, mAnimator, mViewPortHandler)
        setHighlighter(HorizontalBarHighlighter(this))

        mAxisRendererLeft = YAxisRendererHorizontalBarChart(mViewPortHandler, mAxisLeft, mLeftAxisTransformer)
        mAxisRendererRight = YAxisRendererHorizontalBarChart(mViewPortHandler, mAxisRight, mRightAxisTransformer)
        mXAxisRenderer = XAxisRendererHorizontalBarChart(mViewPortHandler, mXAxis, mLeftAxisTransformer)
    }

    private val mOffsetsBuffer = RectF()

    override fun calculateLegendOffsets(offsets: RectF) {
        offsets.left = 0f
        offsets.right = 0f
        offsets.top = 0f
        offsets.bottom = 0f

        if (mLegend == null || !mLegend.isEnabled || mLegend.isDrawInsideEnabled) {
            return
        }

        when (mLegend.orientation) {
            LegendOrientation.VERTICAL -> when (mLegend.horizontalAlignment) {
                LegendHorizontalAlignment.LEFT -> offsets.left += min(
                    mLegend.mNeededWidth,
                    mViewPortHandler.chartWidth * mLegend.maxSizePercent
                ) + mLegend.xOffset

                LegendHorizontalAlignment.RIGHT -> offsets.right += min(
                    mLegend.mNeededWidth,
                    mViewPortHandler.chartWidth * mLegend.maxSizePercent
                ) + mLegend.xOffset

                LegendHorizontalAlignment.CENTER -> when (mLegend.verticalAlignment) {
                    LegendVerticalAlignment.TOP -> offsets.top += min(
                        mLegend.mNeededHeight,
                        mViewPortHandler.chartHeight * mLegend.maxSizePercent
                    ) + mLegend.yOffset

                    LegendVerticalAlignment.BOTTOM -> offsets.bottom += min(
                        mLegend.mNeededHeight,
                        mViewPortHandler.chartHeight * mLegend.maxSizePercent
                    ) + mLegend.yOffset

                    else -> {}
                }
            }

            LegendOrientation.HORIZONTAL -> when (mLegend.verticalAlignment) {
                LegendVerticalAlignment.TOP -> {
                    offsets.top += min(
                        mLegend.mNeededHeight,
                        mViewPortHandler.chartHeight * mLegend.maxSizePercent
                    ) + mLegend.yOffset

                    if (mAxisLeft.isEnabled && mAxisLeft.isDrawLabelsEnabled) {
                        offsets.top += mAxisLeft.getRequiredHeightSpace(
                            mAxisRendererLeft.paintAxisLabels
                        )
                    }
                }

                LegendVerticalAlignment.BOTTOM -> {
                    offsets.bottom += min(
                        mLegend.mNeededHeight,
                        mViewPortHandler.chartHeight * mLegend.maxSizePercent
                    ) + mLegend.yOffset

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

            if (mXAxis.position == XAxisPosition.BOTTOM) {
                offsetLeft += xLabelWidth
            } else if (mXAxis.position == XAxisPosition.TOP) {
                offsetRight += xLabelWidth
            } else if (mXAxis.position == XAxisPosition.BOTH_SIDED) {
                offsetLeft += xLabelWidth
                offsetRight += xLabelWidth
            }
        }

        offsetTop += extraTopOffset
        offsetRight += extraRightOffset
        offsetBottom += extraBottomOffset
        offsetLeft += extraLeftOffset

        val minOffset = mMinOffset.convertDpToPixel()

        mViewPortHandler.restrainViewPort(
            max(minOffset, offsetLeft),
            max(minOffset, offsetTop),
            max(minOffset, offsetRight),
            max(minOffset, offsetBottom)
        )

        if (mLogEnabled) {
            Log.i(
                LOG_TAG, "offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop + ", offsetRight: " +
                        offsetRight + ", offsetBottom: " + offsetBottom
            )
            Log.i(LOG_TAG, "Content: " + mViewPortHandler.contentRect)
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
        val bounds = outputRect
        val set = mData.getDataSetForEntry(barEntry)

        val y = barEntry.y
        val x = barEntry.x

        val barWidth = mData.barWidth

        val top = x - barWidth / 2f
        val bottom = x + barWidth / 2f
        val left = if (y >= 0) y else 0f
        val right = if (y <= 0) y else 0f

        bounds.set(left, top, right, bottom)

        getTransformer(set!!.axisDependency)!!.rectValueToPixel(bounds)
    }

    protected var mGetPositionBuffer: FloatArray = FloatArray(2)

    /**
     * Returns a recyclable MPPointF instance.
     */
    override fun getPosition(e: Entry?, axis: AxisDependency?): MPPointF? {
        if (e == null) {
            return null
        }

        val vals = mGetPositionBuffer
        vals[0] = e.y
        vals[1] = e.x

        getTransformer(axis)!!.pointValuesToPixel(vals)

        return getInstance(vals[0], vals[1])
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch point
     * inside the BarChart.
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            if (mLogEnabled) {
                Log.e(LOG_TAG, "Can't select by touch. No data set.")
            }
            return null
        } else {
            return highlighter.getHighlight(y, x) // switch x and y
        }
    }

    override val lowestVisibleX: Float
        get() {
            getTransformer(AxisDependency.LEFT)!!.getValuesByTouchPoint(
                mViewPortHandler.contentLeft(),
                mViewPortHandler.contentBottom(), posForGetLowestVisibleX
            )
            return max(mXAxis.mAxisMinimum.toDouble(), posForGetLowestVisibleX.y).toFloat()
        }

    override val highestVisibleX: Float
        get() {
            getTransformer(AxisDependency.LEFT)!!.getValuesByTouchPoint(
                mViewPortHandler.contentLeft(),
                mViewPortHandler.contentTop(), posForGetHighestVisibleX
            )
            return min(mXAxis.mAxisMaximum.toDouble(), posForGetHighestVisibleX.y).toFloat()
        }

    /**
     * ###### VIEWPORT METHODS BELOW THIS ######
     */
    override fun setVisibleXRangeMaximum(maxXRange: Float) {
        val xScale = mXAxis.mAxisRange / (maxXRange)
        mViewPortHandler.setMinimumScaleY(xScale)
    }

    override fun setVisibleXRangeMinimum(minXRange: Float) {
        val xScale = mXAxis.mAxisRange / (minXRange)
        mViewPortHandler.setMaximumScaleY(xScale)
    }

    override fun setVisibleXRange(minXRange: Float, maxXRange: Float) {
        val minScale = mXAxis.mAxisRange / minXRange
        val maxScale = mXAxis.mAxisRange / maxXRange
        mViewPortHandler.setMinMaxScaleY(minScale, maxScale)
    }

    override fun setVisibleYRangeMaximum(maxYRange: Float, axis: AxisDependency?) {
        val yScale = getAxisRange(axis) / maxYRange
        mViewPortHandler.setMinimumScaleX(yScale)
    }

    override fun setVisibleYRangeMinimum(minYRange: Float, axis: AxisDependency?) {
        val yScale = getAxisRange(axis) / minYRange
        mViewPortHandler.setMaximumScaleX(yScale)
    }

    override fun setVisibleYRange(minYRange: Float, maxYRange: Float, axis: AxisDependency?) {
        val minScale = getAxisRange(axis) / minYRange
        val maxScale = getAxisRange(axis) / maxYRange
        mViewPortHandler.setMinMaxScaleX(minScale, maxScale)
    }
}
