package info.appdev.charting.charts

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarEntry
import info.appdev.charting.highlight.BarHighlighter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.renderer.BarChartRenderer
import timber.log.Timber
import java.util.Locale

/**
 * Chart that draws bars.
 */
open class BarChart : BarLineChartBase<BarData>, BarDataProvider {

    /**
     * Set this to true to make the highlight operation full-bar oriented, false to make it highlight single values (relevant
     * only for stacked). If enabled, highlighting operations will highlight the whole bar, even if only a single stack entry
     * was tapped.
     * Default: false
     */
    override var isHighlightFullBarEnabled: Boolean = false

    /**
     * if set to true, all values are drawn above their bars, instead of below their top
     */
    override var isDrawValueAboveBarEnabled: Boolean = true

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the maximum value
     */
    override var isDrawBarShadowEnabled: Boolean = false

    /**
     * if set to true, the bar chart's bars would be round on all corners instead of rectangular
     */
    private var mDrawRoundedBars = false

    /**
     * the radius of the rounded bar chart bars
     */
    private var mRoundedBarRadius = 0f

    private var mFitBars = false

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        dataRenderer = BarChartRenderer(this, mAnimator, viewPortHandler, mDrawRoundedBars, mRoundedBarRadius)

        setHighlighter(BarHighlighter(this))

        xAxis.spaceMin = 0.5f
        xAxis.spaceMax = 0.5f
    }

    override fun calcMinMax() {
        mData?.let { barData ->
            if (mFitBars) {
                mXAxis.calculate(barData.xMin - barData.barWidth / 2f, barData.xMax + barData.barWidth / 2f)
            } else {
                mXAxis.calculate(barData.xMin, barData.xMax)
            }

            // calculate axis range (min / max) according to provided data
            mAxisLeft.calculate(barData.getYMin(YAxis.AxisDependency.LEFT), barData.getYMax(YAxis.AxisDependency.LEFT))
            mAxisRight.calculate(
                barData.getYMin(YAxis.AxisDependency.RIGHT), barData.getYMax(
                    YAxis.AxisDependency
                        .RIGHT
                )
            )
        }
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
     * point
     * inside the BarChart.
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            Timber.e("Can't select by touch. No data set.")
            return null
        } else {
            highlighter?.let {
                val h = it.getHighlight(x, y)
                if (h == null || !isHighlightFullBarEnabled)
                    return h

                // For isHighlightFullBarEnabled, remove stackIndex
                return Highlight(
                    h.x, h.y,
                    h.xPx, h.yPx,
                    h.dataSetIndex, -1, h.axis
                )
            }
        }
        return null
    }

    override val accessibilityDescription: String
        get() {
            barData?.let { barData ->
                val entryCount = barData.entryCount

                // Find the min and max index
                val yAxisValueFormatter = axisLeft.valueFormatter
                val minVal = yAxisValueFormatter!!.getFormattedValue(barData.yMin, null)
                val maxVal = yAxisValueFormatter.getFormattedValue(barData.yMax, null)

                // Data range...
                val xAxisValueFormatter = xAxis.valueFormatter
                val minRange = xAxisValueFormatter!!.getFormattedValue(barData.xMin, null)
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
            return ""
        }

    /**
     * Returns the bounding box of the specified Entry in the specified DataSet. Returns null if the Entry could not be
     * found in the charts data.  Performance-intensive code should use void getBarBounds(BarEntry, RectF) instead.
     */
    fun getBarBounds(barEntry: BarEntry): RectF {
        val bounds = RectF()
        getBarBounds(barEntry, bounds)

        return bounds
    }

    /**
     * The passed outputRect will be assigned the values of the bounding box of the specified Entry in the specified DataSet.
     * The rect will be assigned Float.MIN_VALUE in all locations if the Entry could not be found in the charts data.
     */
    open fun getBarBounds(barEntry: BarEntry, outputRect: RectF) {
        mData?.let { barData ->
            val set = barData.getDataSetForEntry(barEntry)

            if (set == null) {
                outputRect.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)
                return
            }

            val y = barEntry.y
            val x = barEntry.x

            val barWidth = barData.barWidth

            val left = x - barWidth / 2f
            val right = x + barWidth / 2f
            val top = if (y >= 0) y else 0f
            val bottom = if (y <= 0) y else 0f

            outputRect.set(left, top, right, bottom)

            getTransformer(set.axisDependency).rectValueToPixel(outputRect)
        }
    }

    /**
     * If set to true, all values are drawn above their bars, instead of below their top.
     */
    fun setDrawValueAboveBar(enabled: Boolean) {
        this.isDrawValueAboveBarEnabled = enabled
    }

    /**
     * If set to true, a grey area is drawn behind each bar that indicates the maximum value. Enabling his will reduce
     * performance by about 50%.
     */
    fun setDrawBarShadow(enabled: Boolean) {
        this.isDrawBarShadowEnabled = enabled
    }

    /**
     * Highlights the value at the given x-value in the given DataSet. Provide
     * -1 as the dataSetIndex to undo all highlighting.
     * @param dataIndex   the index inside the stack - only relevant for stacked entries
     */
    override fun getHighlightValue(x: Float, dataSetIndex: Int, dataIndex: Int) {
        highlightValue(Highlight(x, dataSetIndex, dataIndex), false)
    }

    override val barData: BarData?
        get() = mData

    /**
     * Adds half of the bar width to each side of the x-axis range in order to allow the bars of the barchart to be
     * fully displayed.
     * Default: false
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
        barData?.groupBars(fromX, groupSpace, barSpace)
        notifyDataSetChanged()
    }

    /**
     * Used to enable rounded bar chart bars and set the radius of the rounded bars
     *
     * @param mRoundedBarRadius - the radius of the rounded bars
     */
    fun setRoundedBarRadius(mRoundedBarRadius: Float) {
        this.mRoundedBarRadius = mRoundedBarRadius
        this.mDrawRoundedBars = true
        init()
    }
}
