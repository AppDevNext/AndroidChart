package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.highlight.RadarHighlighter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.renderer.RadarChartRenderer
import com.github.mikephil.charting.renderer.XAxisRendererRadarChart
import com.github.mikephil.charting.renderer.YAxisRendererRadarChart
import com.github.mikephil.charting.utils.Utils
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of the RadarChart, a "spidernet"-like chart. It works best
 * when displaying 5-10 entries per DataSet.
 *
 * @author Philipp Jahoda
 */
open class RadarChart : PieRadarChartBase<RadarEntry, IRadarDataSet, RadarData> {
    /**
     * width of the main web lines
     */
    private var mWebLineWidth = 2.5f

    /**
     * width of the inner web lines
     */
    private var mInnerWebLineWidth = 1.5f

    /**
     * Sets the color for the web lines that come from the center. Don't forget
     * to use getResources().getColor(...) when loading a color from the
     * resources. Default: Color.rgb(122, 122, 122)
     *
     * @param color
     */
    /**
     * color for the main web lines
     */
    var webColor: Int = Color.rgb(122, 122, 122)

    /**
     * Sets the color for the web lines in between the lines that come from the
     * center. Don't forget to use getResources().getColor(...) when loading a
     * color from the resources. Default: Color.rgb(122, 122, 122)
     *
     * @param color
     */
    /**
     * color for the inner web
     */
    var webColorInner: Int = Color.rgb(122, 122, 122)

    /**
     * Returns the alpha value for all web lines.
     *
     * @return
     */
    /**
     * Sets the transparency (alpha) value for all web lines, default: 150, 255
     * = 100% opaque, 0 = 100% transparent
     *
     * @param alpha
     */
    /**
     * transparency the grid is drawn with (0-255)
     */
    var webAlpha: Int = 150

    /**
     * flag indicating if the web lines should be drawn or not
     */
    private var mDrawWeb = true

    /**
     * modulus that determines how many labels and web-lines are skipped before the next is drawn
     */
    private var mSkipWebLineCount = 0

    /**
     * the object reprsenting the y-axis labels
     */
    private val mYAxis: YAxis = YAxis(AxisDependency.LEFT)

    private var colorList: MutableList<Int> = arrayListOf()

    protected val mYAxisRenderer: YAxisRendererRadarChart = YAxisRendererRadarChart(viewPortHandler, mYAxis, this)
    protected val mXAxisRenderer: XAxisRendererRadarChart = XAxisRendererRadarChart(viewPortHandler, mXAxis, this)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mYAxis.labelXOffset = 10f

        mWebLineWidth = Utils.convertDpToPixel(1.5f)
        mInnerWebLineWidth = Utils.convertDpToPixel(0.75f)

        mRenderer = RadarChartRenderer(this, mAnimator, viewPortHandler)

        highlighter = RadarHighlighter(this)
    }

    override fun calcMinMax() {
        super.calcMinMax()

        mData?.let { mData ->
            mYAxis.calculate(mData.getYMin(AxisDependency.LEFT), mData.getYMax(AxisDependency.LEFT))
            mXAxis.calculate(0f, mData.maxEntryCountSet?.entryCount?.toFloat() ?: 0f)
        }
    }

    override fun notifyDataSetChanged() {
        if (mData == null) return

        calcMinMax()

        mYAxisRenderer.computeAxis(mYAxis.mAxisMinimum, mYAxis.mAxisMaximum, mYAxis.isInverted)
        mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false)

        if (!legend.isLegendCustom) legendRenderer.computeLegend(mData!!)

        calculateOffsets()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mData == null) return

        //        if (mYAxis.isEnabled())
//            mYAxisRenderer.computeAxis(mYAxis.mAxisMinimum, mYAxis.mAxisMaximum, mYAxis.isInverted());
        if (mXAxis.isEnabled) mXAxisRenderer.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false)

        mXAxisRenderer.renderAxisLabels(canvas)

        if (mDrawWeb) mRenderer?.drawExtras(canvas)

        if (mYAxis.isEnabled && mYAxis.isDrawLimitLinesBehindDataEnabled) mYAxisRenderer.renderLimitLines(canvas)

        mRenderer?.drawData(canvas)

        if (valuesToHighlight()) mRenderer?.drawHighlighted(canvas, highlighted!!)

        if (mYAxis.isEnabled && !mYAxis.isDrawLimitLinesBehindDataEnabled) mYAxisRenderer.renderLimitLines(canvas)

        mYAxisRenderer.renderAxisLabels(canvas)

        mRenderer?.drawValues(canvas)

        legendRenderer.renderLegend(canvas)

        drawDescription(canvas)

        drawMarkers(canvas)
    }

    val factor: Float
        /**
         * Returns the factor that is needed to transform values into pixels.
         *
         * @return
         */
        get() {
            val content = viewPortHandler.contentRect
            return min(content.width() / 2f, content.height() / 2f) / mYAxis.mAxisRange
        }

    val sliceAngle: Float
        /**
         * Returns the angle that each slice in the radar chart occupies.
         *
         * @return
         */
        get() = 360f / (mData?.maxEntryCountSet?.entryCount?.toFloat() ?: 1f)


    val isCustomLayerColorEnable: Boolean
        get() {
            if (mData == null) {
                return false
            }
            return colorList.size == this.yAxis.mEntryCount
        }

    var layerColorList: MutableList<Int>
        get() = colorList
        set(colorList) {
            if (colorList.isEmpty()) {
                return
            }
            this.colorList = colorList
        }

    override fun getIndexForAngle(angle: Float): Int {
        // take the current angle of the chart into consideration

        val a = Utils.getNormalizedAngle(angle - rotationAngle)

        val sliceangle = this.sliceAngle

        val max = mData?.maxEntryCountSet?.entryCount ?: return -1

        var index = 0

        for (i in 0..<max) {
            val referenceAngle = sliceangle * (i + 1) - sliceangle / 2f

            if (referenceAngle > a) {
                index = i
                break
            }
        }

        return index
    }

    val yAxis: YAxis
        /**
         * Returns the object that represents all y-labels of the RadarChart.
         *
         * @return
         */
        get() = mYAxis

    var webLineWidth: Float
        get() = mWebLineWidth
        /**
         * Sets the width of the web lines that come from the center.
         *
         * @param width
         */
        set(width) {
            mWebLineWidth = Utils.convertDpToPixel(width)
        }

    var webLineWidthInner: Float
        get() = mInnerWebLineWidth
        /**
         * Sets the width of the web lines that are in between the lines coming from
         * the center.
         *
         * @param width
         */
        set(width) {
            mInnerWebLineWidth = Utils.convertDpToPixel(width)
        }

    /**
     * If set to true, drawing the web is enabled, if set to false, drawing the
     * whole web is disabled. Default: true
     *
     * @param enabled
     */
    fun setDrawWeb(enabled: Boolean) {
        mDrawWeb = enabled
    }

    var skipWebLineCount: Int
        /**
         * Returns the modulus that is used for skipping web-lines.
         *
         * @return
         */
        get() = mSkipWebLineCount
        /**
         * Sets the number of web-lines that should be skipped on chart web before the
         * next one is drawn. This targets the lines that come from the center of the RadarChart.
         *
         * @param count if count = 1 -> 1 line is skipped in between
         */
        set(count) {
            mSkipWebLineCount = max(0, count)
        }

    override val requiredLegendOffset: Float
        get() = legendRenderer.labelPaint.textSize * 4f

    override val requiredBaseOffset: Float
        get() = if (mXAxis.isEnabled && mXAxis.isDrawLabelsEnabled) mXAxis.mLabelWidth.toFloat() else Utils.convertDpToPixel(10f)

    override val radius: Float
        get() {
            val content = viewPortHandler.contentRect
            return min(content.width() / 2f, content.height() / 2f)
        }

    /**
     * Returns the maximum value this chart can display on it's y-axis.
     */
    override val yChartMax: Float
        get() = mYAxis.mAxisMaximum

    /**
     * Returns the minimum value this chart can display on it's y-axis.
     */
    override val yChartMin: Float
        get() = mYAxis.mAxisMinimum

    val yRange: Float
        /**
         * Returns the range of y-values this chart can display.
         *
         * @return
         */
        get() = mYAxis.mAxisRange

    override val accessibilityDescription: String?
        get() = "This is a Radar chart"
}
