package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.highlight.BarHighlighter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import java.util.Locale

/**
 * Chart that draws bars.
 *
 * @author Philipp Jahoda
 */
open class BarChart : BarLineChartBase<BarEntry, IBarDataSet, BarData>, BarDataProvider {
    /**
     * flag that indicates whether the highlight should be full-bar oriented, or single-value?
     */
    protected var mHighlightFullBarEnabled: Boolean = false

    /**
     * if set to true, all values are drawn above their bars, instead of below their top
     */
    private var mDrawValueAboveBar = true

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the maximum value
     */
    private var mDrawBarShadow = false

    /**
     * if set to true, the bar chart's bars would be round on all corners instead of rectangular
     */
    private var mDrawRoundedBars = false

    /**
     * the radius of the rounded bar chart bars
     */
    private var mRoundedBarRadius = 0f

    private var mFitBars = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        mRenderer = BarChartRenderer(this, mAnimator, viewPortHandler, mDrawRoundedBars, mRoundedBarRadius)

        setHighlighter(BarHighlighter(this))

        xAxis.spaceMin = 0.5f
        xAxis.spaceMax = 0.5f
    }

    override fun calcMinMax() {
        mData?.let { data ->
            if (mFitBars) {
                mXAxis.calculate(data.xMin - data.barWidth / 2f, data.xMax + data.barWidth / 2f)
            } else {
                mXAxis.calculate(data.xMin, data.xMax)
            }

            // calculate axis range (min / max) according to provided data
            mAxisLeft.calculate(data.getYMin(AxisDependency.LEFT), data.getYMax(AxisDependency.LEFT))
            mAxisRight.calculate(data.getYMin(AxisDependency.RIGHT), data.getYMax(AxisDependency.RIGHT))
        }
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
     * point
     * inside the BarChart.
     *
     * @param x
     * @param y
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.")
            return null
        } else {
            val h = highlighter?.getHighlight(x, y)
            if (h == null || !isHighlightFullBarEnabled) return h

            // For isHighlightFullBarEnabled, remove stackIndex
            return Highlight(
                h.x, h.y,
                h.xPx, h.yPx,
                h.dataSetIndex, -1, h.axis
            )
        }
    }

    /**
     * Returns the bounding box of the specified Entry in the specified DataSet. Returns null if the Entry could not be
     * found in the charts data.  Performance-intensive code should use void getBarBounds(BarEntry, RectF) instead.
     *
     * @param barEntry
     */
    fun getBarBounds(barEntry: BarEntry): RectF {
        val bounds = RectF()
        getBarBounds(barEntry, bounds)

        return bounds
    }

    /**
     * The passed outputRect will be assigned the values of the bounding box of the specified Entry in the specified DataSet.
     * The rect will be assigned Float.MIN_VALUE in all locations if the Entry could not be found in the charts data.
     *
     * @param barEntry
     */
    open fun getBarBounds(barEntry: BarEntry, outputRect: RectF) {
        val set = mData?.getDataSetForEntry(barEntry)

        if (set == null) {
            outputRect.set(Float.Companion.MIN_VALUE, Float.Companion.MIN_VALUE, Float.Companion.MIN_VALUE, Float.Companion.MIN_VALUE)
            return
        }

        val y = barEntry.y
        val x = barEntry.x

        val barWidth = mData?.barWidth ?: return

        val left = x - barWidth / 2f
        val right = x + barWidth / 2f
        val top = if (y >= 0) y else 0f
        val bottom = if (y <= 0) y else 0f

        outputRect.set(left, top, right, bottom)

        getTransformer(set.axisDependency).rectValueToPixel(outputRect)
    }

    /**
     * If set to true, all values are drawn above their bars, instead of below their top.
     *
     */
    fun setDrawValueAboveBar(enabled: Boolean) {
        mDrawValueAboveBar = enabled
    }

    /**
     * returns true if drawing values above bars is enabled, false if not
     *
     */
    override val isDrawValueAboveBarEnabled: Boolean
        get() = mDrawValueAboveBar

    /**
     * If set to true, a grey area is drawn behind each bar that indicates the maximum value. Enabling his will reduce
     * performance by about 50%.
     *
     */
    fun setDrawBarShadow(enabled: Boolean) {
        mDrawBarShadow = enabled
    }

    /**
     * returns true if drawing shadows (maxvalue) for each bar is enabled, false if not
     *
     */
    override val isDrawBarShadowEnabled: Boolean
        get() = mDrawBarShadow

    /**
     * Set this to true to make the highlight operation full-bar oriented, false to make it highlight single values (relevant
     * only for stacked). If enabled, highlighting operations will highlight the whole bar, even if only a single stack entry
     * was tapped.
     * Default: false
     *
     */
    fun setHighlightFullBarEnabled(enabled: Boolean) {
        mHighlightFullBarEnabled = enabled
    }

    /**
     * @return true the highlight operation is be full-bar oriented, false if single-value
     */
    override val isHighlightFullBarEnabled: Boolean
        get() = mHighlightFullBarEnabled

    /**
     * Highlights the value at the given x-value in the given DataSet. Provide
     * -1 as the dataSetIndex to undo all highlighting.
     *
     * @param x
     * @param dataSetIndex
     * @param dataIndex   the index inside the stack - only relevant for stacked entries
     */
    override fun highlightValue(x: Float, y: Float, dataSetIndex: Int, dataIndex: Int, callListener: Boolean) {
        super.highlightValue(Highlight(x, dataSetIndex, dataIndex), false)
    }

    override val barData: BarData?
        get() = mData

    /**
     * Adds half of the bar width to each side of the x-axis range in order to allow the bars of the barchart to be
     * fully displayed.
     * Default: false
     *
     */
    fun setFitBars(enabled: Boolean) {
        mFitBars = enabled
    }

    /**
     * Groups all BarDataSet objects this data object holds together by modifying the x-value of their entries.
     * Previously set x-values of entries will be overwritten. Leaves space between bars and groups as specified
     * by the parameters.
     * Calls notifyDataSetChanged() afterwards.
     *
     * @param fromX      the starting point on the x-axis where the grouping should begin
     * @param groupSpace the space between groups of bars in values (not pixels) e.g. 0.8f for bar width 1f
     * @param barSpace   the space between individual bars in values (not pixels) e.g. 0.1f for bar width 1f
     */
    fun groupBars(fromX: Float, groupSpace: Float, barSpace: Float) {
        if (barData == null) {
            throw RuntimeException("You need to set data for the chart before grouping bars.")
        } else {
            barData!!.groupBars(fromX, groupSpace, barSpace)
            notifyDataSetChanged()
        }
    }

    /**
     * Used to enable rounded bar chart bars and set the radius of the rounded bars
     *
     * @param mRoundedBarRadius - the radius of the rounded bars
     */
    fun setRoundedBarRadius(mRoundedBarRadius: Float) {
        this.mRoundedBarRadius = mRoundedBarRadius
        this.mDrawRoundedBars = true
        mRenderer = BarChartRenderer(this, mAnimator, viewPortHandler, mDrawRoundedBars, mRoundedBarRadius)
    }

    override val accessibilityDescription: String?
        get() {
            val barData = barData
            if (barData == null) {
                return ""
            }

            val entryCount = barData.entryCount

            // Find the min and max index
            val yAxisValueFormatter = axisLeft.valueFormatter
            val minVal = yAxisValueFormatter.getFormattedValue(barData.yMin, null)
            val maxVal = yAxisValueFormatter.getFormattedValue(barData.yMax, null)

            // Data range...
            val xAxisValueFormatter = xAxis.valueFormatter
            val minRange = xAxisValueFormatter.getFormattedValue(barData.xMin, null)
            val maxRange = xAxisValueFormatter.getFormattedValue(barData.xMax, null)

            val entries = if (entryCount == 1) "entry" else "entries"

            // Format the values of min and max; to recite them back
            return String.format(
                Locale.getDefault(), "The bar chart has %d %s. " +
                        "The minimum value is %s and maximum value is %s." +
                        "Data ranges from %s to %s.",
                entryCount, entries, minVal, maxVal, minRange, maxRange
            )
        }
}
