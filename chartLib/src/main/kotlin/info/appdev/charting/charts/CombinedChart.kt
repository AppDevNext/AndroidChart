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
import info.appdev.charting.interfaces.dataprovider.CombinedDataProvider
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.renderer.CombinedChartRenderer
import timber.log.Timber

/**
 * This chart class allows the combination of lines, bars, scatter and candle
 * data all displayed in one chart area.
 */
@Suppress("unused")
open class CombinedChart : BarLineChartBase<CombinedData>, CombinedDataProvider {
    /**
     * if set to true, all values are drawn above their bars, instead of below
     * their top
     */
    override var isDrawValueAboveBarEnabled: Boolean = true


    /**
     * @return true the highlight operation is be full-bar oriented, false if single-value
     */
    /**
     * Set this to true to make the highlight operation full-bar oriented,
     * false to make it highlight single values (relevant only for stacked).
     */
    /**
     * flag that indicates whether the highlight should be full-bar oriented, or single-value?
     */
    override var isHighlightFullBarEnabled: Boolean = false

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the
     * maximum value
     */
    override var isDrawBarShadowEnabled: Boolean = false

    protected var mDrawOrder: MutableList<DrawOrder>? = null

    /**
     * enum that allows to specify the order in which the different data objects
     * for the combined-chart are drawn
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
        mDrawOrder = mutableListOf<DrawOrder>(
            DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.LINE, DrawOrder.CANDLE, DrawOrder.SCATTER
        )

        setHighlighter(CombinedHighlighter(this, this))

        // Old default behaviour
        isHighlightFullBarEnabled = true

        mRenderer = CombinedChartRenderer(this, mAnimator, viewPortHandler)
    }

    override val combinedData: CombinedData?
        get() = mData

    override fun setData(data: CombinedData?) {
        super.setData(data)
        setHighlighter(CombinedHighlighter(this, this))
        (mRenderer as CombinedChartRenderer).createRenderers()
        mRenderer?.initBuffers()
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
     * point
     * inside the CombinedChart.
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
     * If set to true, all values are drawn above their bars, instead of below
     * their top.
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

    var drawOrder: MutableList<DrawOrder>?
        get() = mDrawOrder
        /**
         * Sets the order in which the provided data objects should be drawn. The
         * earlier you place them in the provided array, the further they will be in
         * the background. e.g. if you provide new DrawOrer[] { DrawOrder.BAR,
         * DrawOrder.LINE }, the bars will be drawn behind the lines.
         */
        set(order) {
            if (order == null || order.isEmpty()) {
                return
            }
            mDrawOrder = order
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

                val e = mData!!.getEntryForHighlight(highlight)
                if (e == null || dataset == null) {
                    continue
                }

                @Suppress("UNCHECKED_CAST")
                val set = dataset as IDataSet<Entry>
                val entryIndex = set.getEntryIndex(e)

                // make sure entry not null
                if (entryIndex > set.entryCount * mAnimator.phaseX) {
                    continue
                }

                val pos = getMarkerPosition(highlight)

                // check bounds
                if (!viewPortHandler.isInBounds(pos[0], pos[1])) {
                    continue
                }

                // callbacks to update the content
                if (!marker.isEmpty()) {
                    val markerItem = marker[i % marker.size]
                    markerItem.refreshContent(e, highlight)

                    // draw the marker
                    markerItem.draw(canvas, pos[0], pos[1])
                }
            }
        }
    }

    override val accessibilityDescription: String
        get() = "This is a combined chart"

}
