package info.appdev.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import info.appdev.charting.components.YAxis
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.data.RadarData
import info.appdev.charting.highlight.RadarHighlighter
import info.appdev.charting.renderer.RadarChartRenderer
import info.appdev.charting.renderer.XAxisRendererRadarChart
import info.appdev.charting.renderer.YAxisRendererRadarChart
import info.appdev.charting.utils.convertDpToPixel
import info.appdev.charting.utils.getNormalizedAngle
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of the RadarChart, a "spidernet"-like chart. It works best when displaying 5-10 entries per DataSet.
 */
open class RadarChart : PieRadarChartBase<RadarData> {
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
     */
    /**
     * color for the main web lines
     */
    @ColorInt
    var webColor: Int = Color.rgb(122, 122, 122)

    /**
     * Sets the color for the web lines in between the lines that come from the
     * center. Don't forget to use getResources().getColor(...) when loading a
     * color from the resources. Default: Color.rgb(122, 122, 122)
     *
     */
    /**
     * color for the inner web
     */
    var webColorInner: Int = Color.rgb(122, 122, 122)

    /**
     * Returns the alpha value for all web lines.
     *
     */
    /**
     * Sets the transparency (alpha) value for all web lines, default: 150, 255
     * = 100% opaque, 0 = 100% transparent
     *
     */
    /**
     * transparency the grid is drawn with (0-255)
     */
    var webAlpha: Int = 150

    /**
     * flag indicating if the web lines should be drawn or not
     */
    private var drawWeb = true

    /**
     * modulus that determines how many labels and web-lines are skipped before the next is drawn
     */
    private var mSkipWebLineCount = 0

    /**
     * the object representing the y-axis labels
     */
    private var mYAxis: YAxis? = null

    private var colorList: MutableList<Int> = mutableListOf()

    protected var mYAxisRenderer: YAxisRendererRadarChart? = null
    protected var mXAxisRenderer: XAxisRendererRadarChart? = null

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mYAxis = YAxis(AxisDependency.LEFT)
        mYAxis!!.labelXOffset = 10f

        mWebLineWidth = 1.5f.convertDpToPixel()
        mInnerWebLineWidth = 0.75f.convertDpToPixel()

        dataRenderer = RadarChartRenderer(this, mAnimator, viewPortHandler)
        mYAxisRenderer = YAxisRendererRadarChart(viewPortHandler, mYAxis!!, this)
        mXAxisRenderer = XAxisRendererRadarChart(viewPortHandler, mXAxis, this)

        highlighter = RadarHighlighter(this)
    }

    override fun calcMinMax() {
        super.calcMinMax()
        mData?.let { data ->
            mYAxis!!.calculate(data.getYMin(AxisDependency.LEFT), data.getYMax(AxisDependency.LEFT))
            mXAxis.calculate(0f, data.maxEntryCountSet!!.entryCount.toFloat())
        }
    }

    override fun notifyDataSetChanged() {
        mData?.let { data ->
            calcMinMax()

            mYAxisRenderer!!.computeAxis(mYAxis!!.mAxisMinimum, mYAxis!!.mAxisMaximum, mYAxis!!.isInverted)
            mXAxisRenderer!!.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false)

            if (!legend.isLegendCustom)
                legendRenderer?.computeLegend(data)

            calculateOffsets()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mData == null)
            return

        //        if (mYAxis.isEnabled())
//            mYAxisRenderer.computeAxis(mYAxis.mAxisMinimum, mYAxis.mAxisMaximum, mYAxis.isInverted());
        if (mXAxis.isEnabled) mXAxisRenderer!!.computeAxis(mXAxis.mAxisMinimum, mXAxis.mAxisMaximum, false)

        mXAxisRenderer!!.renderAxisLabels(canvas)

        if (drawWeb)
            dataRenderer?.drawExtras(canvas)

        if (mYAxis!!.isEnabled && mYAxis!!.isDrawLimitLinesBehindDataEnabled) mYAxisRenderer!!.renderLimitLines(canvas)

        dataRenderer?.drawData(canvas)

        if (valuesToHighlight())
            dataRenderer?.drawHighlighted(canvas, highlighted!!)

        if (mYAxis!!.isEnabled && !mYAxis!!.isDrawLimitLinesBehindDataEnabled) mYAxisRenderer!!.renderLimitLines(canvas)

        mYAxisRenderer!!.renderAxisLabels(canvas)

        dataRenderer?.drawValues(canvas)

        legendRenderer?.renderLegend(canvas)

        drawDescription(canvas)

        drawMarkers(canvas)
    }

    val factor: Float
        /**
         * Returns the factor that is needed to transform values into pixels.
         *
         */
        get() {
            val content = viewPortHandler.contentRect
            return min(content.width() / 2f, content.height() / 2f) / mYAxis!!.axisRange
        }

    val sliceAngle: Float
        /**
         * Returns the angle that each slice in the radar chart occupies.
         *
         */
        get() = 360f / mData?.maxEntryCountSet!!.entryCount.toFloat()


    val isCustomLayerColorEnable: Boolean
        get() {
            if (mData == null) {
                return false
            }
            return colorList.size == this.yAxis.entryCount
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

        val a = (angle - rotationAngle).getNormalizedAngle()

        val sliceAngle = this.sliceAngle

        val max = mData?.maxEntryCountSet!!.entryCount

        var index = 0

        for (i in 0..<max) {
            val referenceAngle = sliceAngle * (i + 1) - sliceAngle / 2f

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
         */
        get() = mYAxis!!

    var webLineWidth: Float
        get() = mWebLineWidth
        /**
         * Sets the width of the web lines that come from the center.
         *
         */
        set(width) {
            mWebLineWidth = width.convertDpToPixel()
        }

    var webLineWidthInner: Float
        get() = mInnerWebLineWidth
        /**
         * Sets the width of the web lines that are in between the lines coming from
         * the center.
         *
         */
        set(width) {
            mInnerWebLineWidth = width.convertDpToPixel()
        }

    var skipWebLineCount: Int
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
        get() {
            legendRenderer?.let {
                return it.labelPaint.textSize * 4f
            }
            return 0f
        }

    override val requiredBaseOffset: Float
        get() = if (mXAxis.isEnabled && mXAxis.isDrawLabelsEnabled) mXAxis.mLabelWidth.toFloat() else 10f.convertDpToPixel()

    override val radius: Float
        get() {
            val content = viewPortHandler.contentRect
            return min(content.width() / 2f, content.height() / 2f)
        }

    /**
     * Returns the maximum value this chart can display on it's y-axis.
     */
    override val yChartMax: Float
        get() = mYAxis!!.mAxisMaximum

    override val accessibilityDescription: String
        get() = "This is a Radar chart"

    /**
     * Returns the minimum value this chart can display on it's y-axis.
     */
    override val yChartMin: Float
        get() = mYAxis!!.mAxisMinimum

    /**
     * Range of y-values this chart can display.
     */
    val yRange: Float
        get() = mYAxis!!.axisRange
}
