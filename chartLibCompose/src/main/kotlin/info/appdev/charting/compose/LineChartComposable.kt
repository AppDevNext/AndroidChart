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
import info.appdev.charting.charts.LineChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [LineChart].
 *
 * @param data The line chart data to display
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
 * @param dragDecelerationEnabled Whether drag deceleration is enabled
 * @param dragDecelerationFrictionCoef Drag deceleration friction coefficient
 * @param maxVisibleValueCount Maximum number of values to display
 * @param autoScaleMinMax Whether to auto-scale min/max values
 * @param keepPositionOnRotation Whether to keep position on rotation
 * @param animationDuration Animation duration in milliseconds
 * @param animationEasing Animation easing function
 */
@Composable
fun LineChart(
    data: LineData?,
    modifier: Modifier = Modifier,
    state: LineChartState = rememberLineChartState(),
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
    dragDecelerationEnabled: Boolean = true,
    dragDecelerationFrictionCoef: Float = 0.9f,
    maxVisibleValueCount: Int = 100,
    autoScaleMinMax: Boolean = false,
    keepPositionOnRotation: Boolean = false,
    animationDuration: Int = 0,
    animationEasing: Easing.EasingFunction? = null,
) {
    val context = LocalContext.current

    val chart = remember {
        LineChart(context).apply {
            // Initial setup
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose {
            // Clean up chart resources
            chart.clear()
        }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { lineChart ->
            // Update data
            lineChart.data = data

            // Update configuration
            lineChart.setBackgroundColor(backgroundColor.toArgb())
            lineChart.setDrawGridBackground(drawGridBackground)
            lineChart.setGridBackgroundColor(gridBackgroundColor.toArgb())

            // Touch settings
            lineChart.setTouchEnabled(touchEnabled)
            lineChart.isDragEnabled = dragEnabled
            lineChart.setScaleEnabled(scaleEnabled)
            lineChart.isScaleXEnabled = scaleXEnabled
            lineChart.isScaleYEnabled = scaleYEnabled
            lineChart.isPinchZoom = pinchZoomEnabled
            lineChart.isDoubleTapToZoomEnabled = doubleTapToZoomEnabled
            lineChart.isHighlightPerTap = highlightPerTapEnabled
            lineChart.isDragDeceleration = dragDecelerationEnabled
            lineChart.dragDecelerationFrictionCoef = dragDecelerationFrictionCoef

            // Display settings
            lineChart.setMaxVisibleValueCount(maxVisibleValueCount)
            lineChart.isAutoScaleMinMax = autoScaleMinMax
            lineChart.isKeepPositionOnRotation = keepPositionOnRotation

            // Description
            lineChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            // Legend
            legend?.invoke(lineChart.legend)

            // Axes
            xAxisConfig?.invoke(lineChart.xAxis)
            leftAxisConfig?.invoke(lineChart.axisLeft)
            rightAxisConfig?.invoke(lineChart.axisRight)

            // Selection listener
            if (onValueSelected != null) {
                lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) {
                        onValueSelected(entry, highlight)
                    }

                    override fun onNothingSelected() {
                        onValueSelected(null, null)
                    }
                })
            } else {
                lineChart.setOnChartValueSelectedListener(null)
            }

            // Animation
            if (animationDuration > 0) {
                if (animationEasing != null) {
                    lineChart.animateX(animationDuration, animationEasing)
                } else {
                    lineChart.animateX(animationDuration)
                }
            }

            // Refresh
            lineChart.invalidate()
        }
    )
}

