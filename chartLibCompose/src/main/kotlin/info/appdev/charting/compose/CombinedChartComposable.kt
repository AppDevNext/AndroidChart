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
import info.appdev.charting.charts.CombinedChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.CombinedData
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [CombinedChart].
 */
@Composable
fun CombinedChart(
    data: CombinedData?,
    modifier: Modifier = Modifier,
    state: CombinedChartState = rememberCombinedChartState(),
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
    drawOrder: Array<CombinedChart.DrawOrder> = arrayOf(
        CombinedChart.DrawOrder.BAR,
        CombinedChart.DrawOrder.BUBBLE,
        CombinedChart.DrawOrder.LINE,
        CombinedChart.DrawOrder.CANDLE,
        CombinedChart.DrawOrder.SCATTER
    ),
    drawBarShadow: Boolean = false,
    highlightFullBarEnabled: Boolean = false,
    drawValueAboveBar: Boolean = true,
    animationDuration: Int = 0,
    animationEasing: Easing.EasingFunction? = null,
) {
    val context = LocalContext.current

    val chart = remember {
        CombinedChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose { chart.clear() }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { combinedChart ->
            combinedChart.data = data

            combinedChart.setBackgroundColor(backgroundColor.toArgb())
            combinedChart.setDrawGridBackground(drawGridBackground)
            combinedChart.setGridBackgroundColor(gridBackgroundColor.toArgb())
            combinedChart.setTouchEnabled(touchEnabled)
            combinedChart.isDragEnabled = dragEnabled
            combinedChart.setScaleEnabled(scaleEnabled)
            combinedChart.isScaleXEnabled = scaleXEnabled
            combinedChart.isScaleYEnabled = scaleYEnabled
            combinedChart.isPinchZoom = pinchZoomEnabled
            combinedChart.isDoubleTapToZoomEnabled = doubleTapToZoomEnabled
            combinedChart.isHighlightPerTap = highlightPerTapEnabled

            // Combined-specific settings
            combinedChart.drawOrder = drawOrder.toMutableList()
            combinedChart.isDrawBarShadow = drawBarShadow
            combinedChart.isHighlightFullBar = highlightFullBarEnabled
            combinedChart.isDrawValueAboveBar = drawValueAboveBar

            combinedChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            legend?.invoke(combinedChart.legend)
            xAxisConfig?.invoke(combinedChart.xAxis)
            leftAxisConfig?.invoke(combinedChart.axisLeft)
            rightAxisConfig?.invoke(combinedChart.axisRight)

            if (onValueSelected != null) {
                combinedChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) = onValueSelected(entry, highlight)
                    override fun onNothingSelected() = onValueSelected(null, null)
                })
            }

            if (animationDuration > 0) {
                if (animationEasing != null) {
                    combinedChart.animateXY(animationDuration, animationDuration, animationEasing, animationEasing)
                } else {
                    combinedChart.animateXY(animationDuration, animationDuration)
                }
            }

            combinedChart.invalidate()
        }
    )
}

