package info.appdev.charting.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import info.appdev.charting.animation.Easing
import info.appdev.charting.charts.BarChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BarData
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [BarChart].
 *
 * @param data The bar chart data to display
 * @param modifier The modifier to be applied to the chart
 * @param state The state holder for the chart
 * @param onValueSelected Callback when a value is selected
 * @param description Description text for the chart
 * @param legend Configuration for the legend
 * @param xAxisConfig Configuration for the X axis
 * @param leftAxisConfig Configuration for the left Y axis
 * @param rightAxisConfig Configuration for the right Y axis
 * @param backgroundColor Background color for the chart
 * @param gridBackgroundColor Grid background color
 * @param drawGridBackground Whether to draw the grid background
 * @param touchEnabled Whether touch gestures are enabled
 * @param dragEnabled Whether drag gestures are enabled
 * @param scaleEnabled Whether scale/zoom gestures are enabled
 * @param scaleXEnabled Whether horizontal scale/zoom is enabled
 * @param scaleYEnabled Whether vertical scale/zoom is enabled
 * @param pinchZoomEnabled Whether pinch zoom is enabled
 * @param doubleTapToZoomEnabled Whether double tap to zoom is enabled
 * @param highlightPerTapEnabled Whether highlighting on tap is enabled
 * @param highlightFullBarEnabled Whether to highlight full bar or single stack entry
 * @param drawValueAboveBar Whether to draw values above bars
 * @param drawBarShadow Whether to draw bar shadows
 * @param fitBars Whether to fit bars within the chart viewport
 * @param animationDuration Animation duration in milliseconds
 * @param animationEasing Animation easing function
 */
@Composable
fun BarChart(
    data: BarData?,
    modifier: Modifier = Modifier,
    state: BarChartState = rememberBarChartState(),
    onValueSelected: ((Entry?, Highlight?) -> Unit)? = null,
    description: String? = null,
    legend: ((Legend) -> Unit)? = null,
    xAxisConfig: ((XAxis) -> Unit)? = null,
    leftAxisConfig: ((YAxis) -> Unit)? = null,
    rightAxisConfig: ((YAxis) -> Unit)? = null,
    backgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    gridBackgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.LightGray,
    drawGridBackground: Boolean = false,
    touchEnabled: Boolean = true,
    dragEnabled: Boolean = true,
    scaleEnabled: Boolean = true,
    scaleXEnabled: Boolean = true,
    scaleYEnabled: Boolean = true,
    pinchZoomEnabled: Boolean = true,
    doubleTapToZoomEnabled: Boolean = true,
    highlightPerTapEnabled: Boolean = true,
    highlightFullBarEnabled: Boolean = false,
    drawValueAboveBar: Boolean = true,
    drawBarShadow: Boolean = false,
    fitBars: Boolean = false,
    animationDuration: Int = 0,
    animationEasing: Easing.EasingFunction? = null,
) {
    val context = LocalContext.current

    val chart = remember {
        BarChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose {
            chart.clear()
        }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { barChart ->
            // Update data
            barChart.data = data

            // Update configuration
            barChart.setBackgroundColor(backgroundColor.toArgb())
            barChart.setDrawGridBackground(drawGridBackground)
            barChart.setGridBackgroundColor(gridBackgroundColor.toArgb())

            // Touch settings
            barChart.setTouchEnabled(touchEnabled)
            barChart.isDragEnabled = dragEnabled
            barChart.setScaleEnabled(scaleEnabled)
            barChart.isScaleXEnabled = scaleXEnabled
            barChart.isScaleYEnabled = scaleYEnabled
            barChart.isPinchZoom = pinchZoomEnabled
            barChart.isDoubleTapToZoomEnabled = doubleTapToZoomEnabled
            barChart.isHighlightPerTap = highlightPerTapEnabled

            // Bar-specific settings
            barChart.isHighlightFullBar = highlightFullBarEnabled
            barChart.isDrawValueAboveBar = drawValueAboveBar
            barChart.isDrawBarShadow = drawBarShadow
            barChart.setFitBars(fitBars)

            // Description
            barChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            // Legend
            legend?.invoke(barChart.legend)

            // Axes
            xAxisConfig?.invoke(barChart.xAxis)
            leftAxisConfig?.invoke(barChart.axisLeft)
            rightAxisConfig?.invoke(barChart.axisRight)

            // Selection listener
            if (onValueSelected != null) {
                barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) {
                        onValueSelected(entry, highlight)
                    }

                    override fun onNothingSelected() {
                        onValueSelected(null, null)
                    }
                })
            } else {
                barChart.setOnChartValueSelectedListener(null)
            }

            // Animation
            if (animationDuration > 0) {
                if (animationEasing != null) {
                    barChart.animateY(animationDuration, animationEasing)
                } else {
                    barChart.animateY(animationDuration)
                }
            }

            // Refresh
            barChart.invalidate()
        }
    )
}

