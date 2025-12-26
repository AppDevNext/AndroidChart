package info.appdev.charting.renderer

import android.graphics.Canvas
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.charts.Chart
import info.appdev.charting.charts.CombinedChart
import info.appdev.charting.charts.CombinedChart.DrawOrder
import info.appdev.charting.data.ChartData
import info.appdev.charting.data.CombinedData
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.utils.ViewPortHandler
import java.lang.ref.WeakReference

/**
 * Renderer class that is responsible for rendering multiple different data-types.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class CombinedChartRenderer(chart: CombinedChart, animator: ChartAnimator, viewPortHandler: ViewPortHandler) : DataRenderer(animator, viewPortHandler) {
    /**
     * all rederers for the different kinds of data this combined-renderer can draw
     */
    protected var dataRenderers: MutableList<DataRenderer> = ArrayList(5)

    protected var weakChart: WeakReference<Chart<*>> = WeakReference(chart)

    /**
     * Creates the renderers needed for this combined-renderer in the required order. Also takes the DrawOrder into
     * consideration.
     */
    fun createRenderers() {
        dataRenderers.clear()

        val chart = weakChart.get() as CombinedChart? ?: return

        val orders = chart.drawOrder

        orders?.let {
            for (order in it) {
                when (order) {
                    DrawOrder.BAR -> dataRenderers.add(BarChartRenderer(chart, animator, viewPortHandler))
                    DrawOrder.BUBBLE -> if (chart.bubbleData != null) dataRenderers.add(BubbleChartRenderer(chart, animator, viewPortHandler))
                    DrawOrder.LINE -> dataRenderers.add(LineChartRenderer(chart, animator, viewPortHandler))
                    DrawOrder.CANDLE -> if (chart.candleData != null) dataRenderers.add(CandleStickChartRenderer(chart, animator, viewPortHandler))
                    DrawOrder.SCATTER -> if (chart.scatterData != null) dataRenderers.add(ScatterChartRenderer(chart, animator, viewPortHandler))
                }
            }
        }
    }

    override fun initBuffers() {
        for (renderer in dataRenderers) renderer.initBuffers()
    }

    override fun drawData(canvas: Canvas) {
        for (renderer in dataRenderers) renderer.drawData(canvas)
    }

    override fun drawValues(canvas: Canvas) {
        for (renderer in dataRenderers) renderer.drawValues(canvas)
    }

    override fun drawExtras(canvas: Canvas) {
        for (renderer in dataRenderers) renderer.drawExtras(canvas)
    }

    protected var mHighlightBuffer: MutableList<Highlight> = ArrayList()

    init {
        createRenderers()
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val chart = weakChart.get() ?: return

        for (renderer in dataRenderers) {
            var data: ChartData<*>? = null

            if (renderer is BarChartRenderer) data = renderer.dataProvider.barData
            else if (renderer is LineChartRenderer) data = renderer.dataProvider.lineData
            else if (renderer is CandleStickChartRenderer) data = renderer.dataProvider.candleData
            else if (renderer is ScatterChartRenderer) data = renderer.dataProvider.scatterData
            else if (renderer is BubbleChartRenderer) data = renderer.dataProvider.bubbleData

            val dataIndex = if (data == null)
                -1
            else
                (chart.getData() as CombinedData).allData.indexOf(data)

            mHighlightBuffer.clear()

            for (h in indices) {
                if (h.dataIndex == dataIndex || h.dataIndex == -1) mHighlightBuffer.add(h)
            }

            renderer.drawHighlighted(canvas, mHighlightBuffer.toTypedArray<Highlight>())
        }
    }

    /**
     * Returns the sub-renderer object at the specified index.
     */
    fun getSubRenderer(index: Int): DataRenderer? {
        return if (index >= dataRenderers.size || index < 0)
            null
        else
            dataRenderers[index]
    }

    val subRenderers: List<DataRenderer>
        /**
         * Returns all sub-renderers.
         */
        get() = dataRenderers

    fun setSubRenderers(renderers: MutableList<DataRenderer>) {
        this.dataRenderers = renderers
    }
}
