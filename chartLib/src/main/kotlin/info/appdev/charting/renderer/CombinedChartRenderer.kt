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
open class CombinedChartRenderer(
    chart: CombinedChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : DataRenderer(animator, viewPortHandler) {
    /**
     * all renderers for the different kinds of data this combined-renderer can draw
     */
    protected var dataRenderers: MutableList<DataRenderer> = mutableListOf()
    protected var highlightBuffer: MutableList<Highlight> = mutableListOf()
    protected var weakChart: WeakReference<Chart<*>> = WeakReference(chart)

    init {
        createRenderers()
    }

    /**
     * Creates the renderers needed for this combined-renderer in the required order. Also takes the DrawOrder into
     * consideration.
     */
    fun createRenderers() {
        dataRenderers.clear()

        val chart = weakChart.get() as CombinedChart? ?: return

        chart.drawOrder?.let {
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
        dataRenderers.forEach {
            it.initBuffers()
        }
    }

    override fun drawData(canvas: Canvas) {
        dataRenderers.forEach {
            it.drawData(canvas)
        }
    }

    override fun drawValues(canvas: Canvas) {
        dataRenderers.forEach {
            it.drawValues(canvas)
        }
    }

    override fun drawExtras(canvas: Canvas) {
        dataRenderers.forEach {
            it.drawExtras(canvas)
        }
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val chart = weakChart.get() ?: return

        dataRenderers.forEach { renderer ->
            var data: ChartData<*>? = null

            when (renderer) {
                is BarChartRenderer -> data = renderer.dataProvider.barData
                is LineChartRenderer -> data = renderer.dataProvider.lineData
                is CandleStickChartRenderer -> data = renderer.dataProvider.candleData
                is ScatterChartRenderer -> data = renderer.dataProvider.scatterData
                is BubbleChartRenderer -> data = renderer.dataProvider.bubbleData
            }

            val dataIndex = if (data == null)
                -1
            else
                (chart.data as CombinedData).allData.indexOf(data)

            highlightBuffer.clear()

            for (h in indices) {
                if (h.dataIndex == dataIndex || h.dataIndex == -1)
                    highlightBuffer.add(h)
            }

            renderer.drawHighlighted(canvas, highlightBuffer.toTypedArray<Highlight>())
        }
    }

    /**
     * Returns the sub-renderer object at the specified index.
     */
    @Suppress("unused")
    fun getSubRenderer(index: Int): DataRenderer? {
        return if (index >= dataRenderers.size || index < 0)
            null
        else
            dataRenderers[index]
    }

}
