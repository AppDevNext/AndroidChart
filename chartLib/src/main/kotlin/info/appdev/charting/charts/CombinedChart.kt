package info.appdev.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BubbleData
import info.appdev.charting.data.CandleData
import info.appdev.charting.data.CombinedData
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.ScatterData
import info.appdev.charting.highlight.CombinedHighlighter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.dataprovider.BubbleDataProvider
import info.appdev.charting.interfaces.dataprovider.CandleDataProvider
import info.appdev.charting.interfaces.dataprovider.CombinedDataProvider
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.interfaces.dataprovider.ScatterDataProvider
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.renderer.CombinedChartRenderer
import timber.log.Timber

/**
 * This chart class allows the combination of lines, bars, scatter and candle data all displayed in one chart area.
 */
@Suppress("unused")
open class CombinedChart : BarLineChartBase<CombinedData>, CombinedDataProvider {
    /**
     * if set to true, all values are drawn above their bars, instead of below their top
     */
    var isDrawValueAboveBarEnabled: Boolean = true


    /**
     * Set this to true to make the highlight operation full-bar oriented,
     * false to make it highlight single values (relevant only for stacked).
     */
    var isHighlightFullBarEnabled: Boolean = false

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the maximum value
     */
    var isDrawBarShadowEnabled: Boolean = false

    protected var drawOrders: MutableList<DrawOrder>? = null

    lateinit var barDataProvider : BarDataProvider
    lateinit var lineDataProvider : LineDataProvider
    lateinit var bubbleDataProvider : BubbleDataProvider
    lateinit var scatterDataProvider : ScatterDataProvider
    lateinit var candleDataProvider : CandleDataProvider

    /**
     * enum that allows to specify the order in which the different data objects for the combined-chart are drawn
     */
    enum class DrawOrder {
        BAR, BUBBLE, LINE, CANDLE, SCATTER
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        // Default values are not ready here yet
        drawOrders = mutableListOf<DrawOrder>(
            DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.LINE, DrawOrder.CANDLE, DrawOrder.SCATTER
        )

        // Create BarDataProvider adapter for CombinedHighlighter
        barDataProvider = object : BarDataProvider {
            override val barData: BarData?
                get() = this@CombinedChart.barData

            override var isDrawBarShadowEnabled: Boolean
                get() = this@CombinedChart.isDrawBarShadowEnabled
                set(value) { this@CombinedChart.isDrawBarShadowEnabled = value }

            override var isDrawValueAboveBarEnabled: Boolean
                get() = this@CombinedChart.isDrawValueAboveBarEnabled
                set(value) { this@CombinedChart.isDrawValueAboveBarEnabled = value }

            override var isHighlightFullBarEnabled: Boolean
                get() = this@CombinedChart.isHighlightFullBarEnabled
                set(value) { this@CombinedChart.isHighlightFullBarEnabled = value }

            override var data: BarData?
                get() = this@CombinedChart.barData
                set(_) {}

            override fun getTransformer(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.getTransformer(axis)

            override fun isInverted(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.isInverted(axis)

            override val lowestVisibleX: Float
                get() = this@CombinedChart.lowestVisibleX

            override val highestVisibleX: Float
                get() = this@CombinedChart.highestVisibleX

            override val xChartMin: Float
                get() = this@CombinedChart.xChartMin

            override val xChartMax: Float
                get() = this@CombinedChart.xChartMax

            override val xRange: Float
                get() = this@CombinedChart.xRange

            override val yChartMin: Float
                get() = this@CombinedChart.yChartMin

            override val yChartMax: Float
                get() = this@CombinedChart.yChartMax

            override var maxHighlightDistance: Float
                get() = this@CombinedChart.maxHighlightDistance
                set(value) { this@CombinedChart.maxHighlightDistance = value }

            override val centerOfView: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOfView

            override val centerOffsets: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOffsets

            override val contentRect: android.graphics.RectF
                get() = this@CombinedChart.contentRect

            override val defaultValueFormatter: info.appdev.charting.formatter.IValueFormatter
                get() = this@CombinedChart.defaultValueFormatter

            override val maxVisibleCount: Int
                get() = this@CombinedChart.maxVisibleCount
        }

        lineDataProvider = object : LineDataProvider {
            override fun getTransformer(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.getTransformer(axis)

            override fun isInverted(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.isInverted(axis)

            override val lowestVisibleX: Float
                get() = this@CombinedChart.lowestVisibleX

            override val highestVisibleX: Float
                get() = this@CombinedChart.highestVisibleX

            override val xChartMin: Float
                get() = this@CombinedChart.xChartMin

            override val xChartMax: Float
                get() = this@CombinedChart.xChartMax

            override val xRange: Float
                get() = this@CombinedChart.xRange

            override val yChartMin: Float
                get() = this@CombinedChart.yChartMin

            override val yChartMax: Float
                get() = this@CombinedChart.yChartMax

            override var maxHighlightDistance: Float
                get() = this@CombinedChart.maxHighlightDistance
                set(value) { this@CombinedChart.maxHighlightDistance = value }

            override val centerOfView: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOfView

            override val centerOffsets: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOffsets

            override val contentRect: android.graphics.RectF
                get() = this@CombinedChart.contentRect

            override val defaultValueFormatter: info.appdev.charting.formatter.IValueFormatter
                get() = this@CombinedChart.defaultValueFormatter

            override var data: LineData?
                get() = this@CombinedChart.lineData
                set(value) {}

            override val maxVisibleCount: Int
                get() = this@CombinedChart.maxVisibleCount
            override val lineData: LineData?
                get() = this@CombinedChart.lineData
        }

        bubbleDataProvider = object : BubbleDataProvider {
            override fun getTransformer(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.getTransformer(axis)

            override fun isInverted(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.isInverted(axis)

            override val lowestVisibleX: Float
                get() = this@CombinedChart.lowestVisibleX

            override val highestVisibleX: Float
                get() = this@CombinedChart.highestVisibleX

            override val xChartMin: Float
                get() = this@CombinedChart.xChartMin

            override val xChartMax: Float
                get() = this@CombinedChart.xChartMax

            override val xRange: Float
                get() = this@CombinedChart.xRange

            override val yChartMin: Float
                get() = this@CombinedChart.yChartMin

            override val yChartMax: Float
                get() = this@CombinedChart.yChartMax

            override var maxHighlightDistance: Float
                get() = this@CombinedChart.maxHighlightDistance
                set(value) { this@CombinedChart.maxHighlightDistance = value }

            override val centerOfView: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOfView

            override val centerOffsets: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOffsets

            override val contentRect: android.graphics.RectF
                get() = this@CombinedChart.contentRect

            override val defaultValueFormatter: info.appdev.charting.formatter.IValueFormatter
                get() = this@CombinedChart.defaultValueFormatter

            override var data: BubbleData?
                get() = this@CombinedChart.bubbleData
                set(value) {}

            override val maxVisibleCount: Int
                get() = this@CombinedChart.maxVisibleCount
            override val bubbleData: BubbleData?
                get() = this@CombinedChart.bubbleData
        }

        scatterDataProvider = object : ScatterDataProvider {
            override fun getTransformer(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.getTransformer(axis)

            override fun isInverted(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.isInverted(axis)

            override val lowestVisibleX: Float
                get() = this@CombinedChart.lowestVisibleX

            override val highestVisibleX: Float
                get() = this@CombinedChart.highestVisibleX

            override val xChartMin: Float
                get() = this@CombinedChart.xChartMin

            override val xChartMax: Float
                get() = this@CombinedChart.xChartMax

            override val xRange: Float
                get() = this@CombinedChart.xRange

            override val yChartMin: Float
                get() = this@CombinedChart.yChartMin

            override val yChartMax: Float
                get() = this@CombinedChart.yChartMax

            override var maxHighlightDistance: Float
                get() = this@CombinedChart.maxHighlightDistance
                set(value) { this@CombinedChart.maxHighlightDistance = value }

            override val centerOfView: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOfView

            override val centerOffsets: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOffsets

            override val contentRect: android.graphics.RectF
                get() = this@CombinedChart.contentRect

            override val defaultValueFormatter: info.appdev.charting.formatter.IValueFormatter
                get() = this@CombinedChart.defaultValueFormatter

            override var data: ScatterData?
                get() = this@CombinedChart.scatterData
                set(value) {}

            override val maxVisibleCount: Int
                get() = this@CombinedChart.maxVisibleCount
            override val scatterData: ScatterData?
                get() = this@CombinedChart.scatterData
        }

        candleDataProvider = object : CandleDataProvider {
            override fun getTransformer(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.getTransformer(axis)

            override fun isInverted(axis: info.appdev.charting.components.YAxis.AxisDependency?) =
                this@CombinedChart.isInverted(axis)

            override val lowestVisibleX: Float
                get() = this@CombinedChart.lowestVisibleX

            override val highestVisibleX: Float
                get() = this@CombinedChart.highestVisibleX

            override val xChartMin: Float
                get() = this@CombinedChart.xChartMin

            override val xChartMax: Float
                get() = this@CombinedChart.xChartMax

            override val xRange: Float
                get() = this@CombinedChart.xRange

            override val yChartMin: Float
                get() = this@CombinedChart.yChartMin

            override val yChartMax: Float
                get() = this@CombinedChart.yChartMax

            override var maxHighlightDistance: Float
                get() = this@CombinedChart.maxHighlightDistance
                set(value) { this@CombinedChart.maxHighlightDistance = value }

            override val centerOfView: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOfView

            override val centerOffsets: info.appdev.charting.utils.PointF
                get() = this@CombinedChart.centerOffsets

            override val contentRect: android.graphics.RectF
                get() = this@CombinedChart.contentRect

            override val defaultValueFormatter: info.appdev.charting.formatter.IValueFormatter
                get() = this@CombinedChart.defaultValueFormatter

            override var data: CandleData?
                get() = this@CombinedChart.candleData
                set(value) {}

            override val maxVisibleCount: Int
                get() = this@CombinedChart.maxVisibleCount
            override val candleData: CandleData?
                get() = this@CombinedChart.candleData
        }

        setHighlighter(CombinedHighlighter(this, barDataProvider))

        // Old default behaviour
        isHighlightFullBarEnabled = true

        dataRenderer = CombinedChartRenderer(this, mAnimator, viewPortHandler)
    }

    override val combinedData: CombinedData?
        get() = mData

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
     * point inside the CombinedChart.
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            Timber.e("Can't select by touch. No data set.")
            return null
        } else {
            highlighter?.let {
                val highlight = it.getHighlight(x, y)
                if (highlight == null || !isHighlightFullBarEnabled) {
                    return highlight
                }

                // For isHighlightFullBarEnabled, remove stackIndex
                return Highlight(
                    highlight.x,
                    highlight.y,
                    highlight.xPx,
                    highlight.yPx,
                    highlight.dataSetIndex,
                    -1,
                    highlight.axis
                )
            }
        }
        return null
    }

    override val lineData: LineData?
        get() {
            if (mData == null) {
                return null
            }
            return mData!!.lineData!!
        }

    override val barData: BarData?
        get() {
            if (mData == null) {
                return null
            }
            return mData!!.barData
        }

    override val scatterData: ScatterData?
        get() {
            if (mData == null) {
                return null
            }
            return mData!!.scatterData
        }

    override val candleData: CandleData?
        get() {
            if (mData == null) {
                return null
            }
            return mData!!.candleData
        }

    override val bubbleData: BubbleData?
        get() {
            if (mData == null) {
                return null
            }
            return mData!!.bubbleData
        }

    /**
     * If set to true, all values are drawn above their bars, instead of below their top.
     */
    fun setDrawValueAboveBar(enabled: Boolean) {
        this.isDrawValueAboveBarEnabled = enabled
    }


    /**
     * If set to true, a grey area is drawn behind each bar that indicates the
     * maximum value. Enabling his will reduce performance by about 50%.
     */
    fun setDrawBarShadow(value: Boolean) {
        this.isDrawBarShadowEnabled = value
    }

    /**
     * The order in which the provided data objects should be drawn. The
     * earlier you place them in the provided array, the further they will be in
     * the background. e.g. if you provide DrawOrder { DrawOrder.BAR,
     * DrawOrder.LINE }, the bars will be drawn behind the lines.
     */
    var drawOrder: MutableList<DrawOrder>?
        get() = drawOrders
        set(value) {
            if (value == null || value.isEmpty()) {
                return
            }
            drawOrders = value
        }

    /**
     * draws all MarkerViews on the highlighted positions
     */
    override fun drawMarkers(canvas: Canvas) {
        // if there is no marker view or drawing marker is disabled
        if (!isDrawMarkersEnabled || !valuesToHighlight()) {
            return
        }

        highlighted?.let {
            for (i in it.indices) {
                val highlight = it[i]
                val dataset = mData!!.getDataSetByHighlight(highlight)

                val entry = mData!!.getEntryForHighlight(highlight)
                if (entry == null || dataset == null) {
                    continue
                }

                @Suppress("UNCHECKED_CAST")
                val set = dataset as IDataSet<Entry>
                val entryIndex = set.getEntryIndex(entry)

                // make sure entry not null
                if (entryIndex > set.entryCount * mAnimator.phaseX)
                    continue

                val pos = getMarkerPosition(highlight)

                // check bounds
                if (!viewPortHandler.isInBounds(pos[0], pos[1]))
                    continue

                // callbacks to update the content
                if (!marker.isEmpty()) {
                    val markerItem = marker[i % marker.size]
                    markerItem.refreshContent(entry, highlight)

                    // draw the marker
                    markerItem.draw(canvas, pos[0], pos[1])
                }
            }
        }
    }

    override val accessibilityDescription: String
        get() = "This is a combined chart"

}
