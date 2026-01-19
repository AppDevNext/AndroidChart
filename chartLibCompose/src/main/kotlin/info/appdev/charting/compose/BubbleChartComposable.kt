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
import info.appdev.charting.charts.BubbleChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BubbleData
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [BubbleChart].
 */
@Composable
fun BubbleChart(
    data: BubbleData?,
    modifier: Modifier = Modifier,
    state: BubbleChartState = rememberBubbleChartState(),
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
    animationDuration: Int = 0,
    animationEasing: Easing.EasingFunction? = null,
) {
    val context = LocalContext.current

    val chart = remember {
        BubbleChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose { chart.clear() }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { bubbleChart ->
            bubbleChart.data = data

            bubbleChart.setBackgroundColor(backgroundColor.toArgb())
            bubbleChart.setDrawGridBackground(drawGridBackground)
            bubbleChart.setGridBackgroundColor(gridBackgroundColor.toArgb())
            bubbleChart.setTouchEnabled(touchEnabled)
            bubbleChart.isDragEnabled = dragEnabled
            bubbleChart.setScaleEnabled(scaleEnabled)
            bubbleChart.isScaleXEnabled = scaleXEnabled
            bubbleChart.isScaleYEnabled = scaleYEnabled
            bubbleChart.isPinchZoom = pinchZoomEnabled
            bubbleChart.isDoubleTapToZoomEnabled = doubleTapToZoomEnabled
            bubbleChart.isHighlightPerTap = highlightPerTapEnabled

            bubbleChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            legend?.invoke(bubbleChart.legend)
            xAxisConfig?.invoke(bubbleChart.xAxis)
            leftAxisConfig?.invoke(bubbleChart.axisLeft)
            rightAxisConfig?.invoke(bubbleChart.axisRight)

            if (onValueSelected != null) {
                bubbleChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) = onValueSelected(entry, highlight)
                    override fun onNothingSelected() = onValueSelected(null, null)
                })
            }

            if (animationDuration > 0) {
                if (animationEasing != null) {
                    bubbleChart.animateXY(animationDuration, animationDuration, animationEasing, animationEasing)
                } else {
                    bubbleChart.animateXY(animationDuration, animationDuration)
                }
            }

            bubbleChart.invalidate()
        }
    )
}

