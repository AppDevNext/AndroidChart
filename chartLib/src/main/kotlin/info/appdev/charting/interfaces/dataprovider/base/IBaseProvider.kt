package info.appdev.charting.interfaces.dataprovider.base

import android.graphics.RectF
import info.appdev.charting.data.ChartData
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.utils.MPPointF

/**
 * Interface that provides everything there is to know about the dimensions,
 * bounds, and range of the chart.
 */
interface IBaseProvider {
    /**
     * Returns the minimum x value of the chart, regardless of zoom or translation.
     */
    val xChartMin: Float

    /**
     * Returns the maximum x value of the chart, regardless of zoom or translation.
     */
    val xChartMax: Float

    val xRange: Float

    /**
     * Returns the minimum y value of the chart, regardless of zoom or translation.
     */
    val yChartMin: Float

    /**
     * Returns the maximum y value of the chart, regardless of zoom or translation.
     */
    val yChartMax: Float

    /**
     * Returns the maximum distance in screen dp a touch can be away from an entry to cause it to get highlighted.
     */
    var maxHighlightDistance: Float

    val centerOfView: MPPointF?

    val centerOffsets: MPPointF

    val contentRect: RectF?

    val defaultValueFormatter: IValueFormatter?

    fun getData(): ChartData<*>?

    val maxVisibleCount: Int
}
