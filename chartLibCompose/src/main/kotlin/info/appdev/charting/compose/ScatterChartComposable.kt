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
import info.appdev.charting.charts.ScatterChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.Entry
import info.appdev.charting.data.ScatterData
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [ScatterChart].
 */
@Composable
fun ScatterChart(
    data: ScatterData?,
    modifier: Modifier = Modifier,
    state: ScatterChartState = rememberScatterChartState(),
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
        ScatterChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose { chart.clear() }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { scatterChart ->
            scatterChart.data = data

            scatterChart.setBackgroundColor(backgroundColor.toArgb())
            scatterChart.setDrawGridBackground(drawGridBackground)
            scatterChart.setGridBackgroundColor(gridBackgroundColor.toArgb())
            scatterChart.setTouchEnabled(touchEnabled)
            scatterChart.isDragEnabled = dragEnabled
            scatterChart.setScaleEnabled(scaleEnabled)
            scatterChart.isScaleXEnabled = scaleXEnabled
            scatterChart.isScaleYEnabled = scaleYEnabled
            scatterChart.isPinchZoom = pinchZoomEnabled
            scatterChart.isDoubleTapToZoomEnabled = doubleTapToZoomEnabled
            scatterChart.isHighlightPerTap = highlightPerTapEnabled

            scatterChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            legend?.invoke(scatterChart.legend)
            xAxisConfig?.invoke(scatterChart.xAxis)
            leftAxisConfig?.invoke(scatterChart.axisLeft)
            rightAxisConfig?.invoke(scatterChart.axisRight)

            if (onValueSelected != null) {
                scatterChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) = onValueSelected(entry, highlight)
                    override fun onNothingSelected() = onValueSelected(null, null)
                })
            }

            if (animationDuration > 0) {
                if (animationEasing != null) {
                    scatterChart.animateXY(animationDuration, animationDuration, animationEasing, animationEasing)
                } else {
                    scatterChart.animateXY(animationDuration, animationDuration)
                }
            }

            scatterChart.invalidate()
        }
    )
}

