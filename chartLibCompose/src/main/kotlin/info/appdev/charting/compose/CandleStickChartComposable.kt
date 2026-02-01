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
import info.appdev.charting.charts.CandleStickChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.CandleData
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [CandleStickChart].
 */
@Composable
fun CandleStickChart(
    data: CandleData?,
    modifier: Modifier = Modifier,
    state: CandleStickChartState = rememberCandleStickChartState(),
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
        CandleStickChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose { chart.clear() }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { candleChart ->
            candleChart.data = data

            candleChart.setBackgroundColor(backgroundColor.toArgb())
            candleChart.setDrawGridBackground(drawGridBackground)
            candleChart.setGridBackgroundColor(gridBackgroundColor.toArgb())
            candleChart.setTouchEnabled(touchEnabled)
            candleChart.isDragEnabled = dragEnabled
            candleChart.setScaleEnabled(scaleEnabled)
            candleChart.isScaleXEnabled = scaleXEnabled
            candleChart.isScaleYEnabled = scaleYEnabled
            candleChart.isPinchZoom = pinchZoomEnabled
            candleChart.isDoubleTapToZoomEnabled = doubleTapToZoomEnabled
            candleChart.isHighlightPerTap = highlightPerTapEnabled

            candleChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            legend?.invoke(candleChart.legend)
            xAxisConfig?.invoke(candleChart.xAxis)
            leftAxisConfig?.invoke(candleChart.axisLeft)
            rightAxisConfig?.invoke(candleChart.axisRight)

            if (onValueSelected != null) {
                candleChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) = onValueSelected(entry, highlight)
                    override fun onNothingSelected() = onValueSelected(null, null)
                })
            }

            if (animationDuration > 0) {
                if (animationEasing != null) {
                    candleChart.animateY(animationDuration, animationEasing)
                } else {
                    candleChart.animateY(animationDuration)
                }
            }

            candleChart.invalidate()
        }
    )
}

