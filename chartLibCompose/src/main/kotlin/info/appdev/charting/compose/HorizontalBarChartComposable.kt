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
import info.appdev.charting.charts.HorizontalBarChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BarData
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [HorizontalBarChart].
 */
@Composable
fun HorizontalBarChart(
    data: BarData?,
    modifier: Modifier = Modifier,
    state: HorizontalBarChartState = rememberHorizontalBarChartState(),
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
    highlightFullBarEnabled: Boolean = false,
    drawValueAboveBar: Boolean = true,
    drawBarShadow: Boolean = false,
    animationDuration: Int = 0,
    animationEasing: Easing.EasingFunction? = null,
) {
    val context = LocalContext.current

    val chart = remember {
        HorizontalBarChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose { chart.clear() }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { horizontalBarChart ->
            horizontalBarChart.data = data

            horizontalBarChart.setBackgroundColor(backgroundColor.toArgb())
            horizontalBarChart.setDrawGridBackground(drawGridBackground)
            horizontalBarChart.setGridBackgroundColor(gridBackgroundColor.toArgb())
            horizontalBarChart.setTouchEnabled(touchEnabled)
            horizontalBarChart.isDragEnabled = dragEnabled
            horizontalBarChart.setScaleEnabled(scaleEnabled)
            horizontalBarChart.isHighlightFullBar = highlightFullBarEnabled
            horizontalBarChart.isDrawValueAboveBar = drawValueAboveBar
            horizontalBarChart.isDrawBarShadow = drawBarShadow

            horizontalBarChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            legend?.invoke(horizontalBarChart.legend)
            xAxisConfig?.invoke(horizontalBarChart.xAxis)
            leftAxisConfig?.invoke(horizontalBarChart.axisLeft)
            rightAxisConfig?.invoke(horizontalBarChart.axisRight)

            if (onValueSelected != null) {
                horizontalBarChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) = onValueSelected(entry, highlight)
                    override fun onNothingSelected() = onValueSelected(null, null)
                })
            }

            if (animationDuration > 0) {
                if (animationEasing != null) {
                    horizontalBarChart.animateY(animationDuration, animationEasing)
                } else {
                    horizontalBarChart.animateY(animationDuration)
                }
            }

            horizontalBarChart.invalidate()
        }
    )
}

