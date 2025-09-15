package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.highlight.CombinedHighlighter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.CombinedDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.renderer.CombinedChartRenderer


/**
 * This chart class allows the combination of lines, bars, scatter and candle
 * data all displayed in one chart area.
 *
 * @author Philipp Jahoda
 */
@Suppress("unused")
open class CombinedChart : BarLineChartBase<Entry, IBarLineScatterCandleBubbleDataSet<Entry>, CombinedData>, CombinedDataProvider {
    /**
     * if set to true, all values are drawn above their bars, instead of below
     * their top
     */
    private var mDrawValueAboveBar = true


    /**
     * flag that indicates whether the highlight should be full-bar oriented, or single-value?
     */
    protected var mHighlightFullBarEnabled: Boolean = false

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the
     * maximum value
     */
    private var mDrawBarShadow = false

    protected var mDrawOrder: Array<DrawOrder> = arrayOf<DrawOrder>(
        DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.LINE, DrawOrder.CANDLE, DrawOrder.SCATTER
    )

    /**
     * enum that allows to specify the order in which the different data objects
     * for the combined-chart are drawn
     */
    enum class DrawOrder {
        BAR, BUBBLE, LINE, CANDLE, SCATTER
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        setHighlighter(CombinedHighlighter(this, this))

        // Old default behaviour
        mHighlightFullBarEnabled = true

        mRenderer = CombinedChartRenderer(this, mAnimator, viewPortHandler)
    }

    override val combinedData: CombinedData?
        get() = mData

    override fun setData(data: CombinedData?) {
        super.setData(data)
        setHighlighter(CombinedHighlighter(this, this))
        (mRenderer as? CombinedChartRenderer)?.createRenderers()
        mRenderer?.initBuffers()
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
     * point
     * inside the CombinedChart.
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.")
            return null
        } else {
            val h = highlighter?.getHighlight(x, y)
            if (h == null || !isHighlightFullBarEnabled) {
                return h
            }

            // For isHighlightFullBarEnabled, remove stackIndex
            return Highlight(
                h.x, h.y,
                h.xPx, h.yPx,
                h.dataSetIndex, -1, h.axis
            )
        }
    }

    override var lineData: LineData?
        get() = mData?.lineData
        set(value) {
            mData?.setData(value)
        }

    override var barData: BarData?
        get() = mData?.barData
        set(value) {
            mData?.setData(value)
        }

    override var scatterData: ScatterData?
        get() = mData?.scatterData
        set(value) {
            mData?.setData(value)
        }

    override var candleData: CandleData?
        get() = mData?.candleData
        set(value) {
            mData?.setData(value)
        }

    override var bubbleData: BubbleData?
        get() = mData?.bubbleData
        set(value) {
            mData?.setData(value)
        }

    override var isDrawBarShadowEnabled: Boolean
        get() = mDrawBarShadow
        set(value) {
            mDrawBarShadow = value
        }

    override var isDrawValueAboveBarEnabled: Boolean
        get() = mDrawValueAboveBar
        set(value) {
            mDrawValueAboveBar = value
        }

    /**
     * @return true the highlight operation is be full-bar oriented, false if single-value
     */
    override var isHighlightFullBarEnabled: Boolean
        get() = mHighlightFullBarEnabled
        set(value) {
            mHighlightFullBarEnabled = value
        }

    var drawOrder: Array<DrawOrder>
        /**
         * Returns the currently set draw order.
         */
        get() = mDrawOrder
        /**
         * Sets the order in which the provided data objects should be drawn. The
         * earlier you place them in the provided array, the further they will be in
         * the background. e.g. if you provide new DrawOrer[] { DrawOrder.BAR,
         * DrawOrder.LINE }, the bars will be drawn behind the lines.
         */
        set(order) {
            if (order.isEmpty()) {
                return
            }
            mDrawOrder = order
        }

    /**
     * draws all MarkerViews on the highlighted positions
     */
    override fun drawMarkers(canvas: Canvas?) {
        // if there is no marker view or drawing marker is disabled

        if (!isDrawMarkersEnabled || !valuesToHighlight()) {
            return
        }

        val highlighted = highlighted ?: return

        for (i in highlighted.indices) {
            val highlight = highlighted[i]
            val set = mData?.getDataSetByHighlight(highlight)

            val e = mData?.getEntryForHighlight(highlight)
            if (e == null || set == null) {
                continue
            }

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
                val markerItem: IMarker = marker[i % marker.size]
                markerItem.refreshContent(e, highlight)

                // draw the marker
                markerItem.draw(canvas, pos[0], pos[1])
            }
        }
    }

    override val accessibilityDescription: String?
        get() = "This is a combined chart"
}
