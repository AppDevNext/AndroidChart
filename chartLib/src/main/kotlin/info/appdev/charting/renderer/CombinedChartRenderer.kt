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
@Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")
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

        val combinedChart = weakChart.get() as CombinedChart? ?: return

        combinedChart.drawOrder?.let {
            for (order in it) {
                when (order) {
                    DrawOrder.BAR -> dataRenderers.add(BarChartRenderer(combinedChart.barDataProvider, animator, viewPortHandler))
                    DrawOrder.BUBBLE -> dataRenderers.add(BubbleChartRenderer(combinedChart.bubbleDataProvider, animator, viewPortHandler))
                    DrawOrder.LINE -> dataRenderers.add(LineChartRenderer(combinedChart.lineDataProvider, animator, viewPortHandler))
                    DrawOrder.CANDLE -> dataRenderers.add(CandleStickChartRenderer(combinedChart.candleDataProvider, animator, viewPortHandler))
                    DrawOrder.SCATTER -> dataRenderers.add(ScatterChartRenderer(combinedChart.scatterDataProvider, animator, viewPortHandler))
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
