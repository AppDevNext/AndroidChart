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
import info.appdev.charting.charts.RadarChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.Entry
import info.appdev.charting.data.RadarData
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [RadarChart].
 */
@Composable
fun RadarChart(
    data: RadarData?,
    modifier: Modifier = Modifier,
    state: RadarChartState = rememberRadarChartState(),
    onValueSelected: ((Entry?, Highlight?) -> Unit)? = null,
    description: String? = null,
    legend: ((Legend) -> Unit)? = null,
    xAxisConfig: ((XAxis) -> Unit)? = null,
    yAxisConfig: ((YAxis) -> Unit)? = null,
    backgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    touchEnabled: Boolean = true,
    rotationEnabled: Boolean = true,
    webLineWidth: Float = 2.5f,
    webLineWidthInner: Float = 1.5f,
    webAlpha: Int = 150,
    skipWebLineCount: Int = 0,
    animationDuration: Int = 0,
    animationEasing: Easing.EasingFunction? = null,
) {
    val context = LocalContext.current

    val chart = remember {
        RadarChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose { chart.clear() }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { radarChart ->
            radarChart.data = data

            radarChart.setBackgroundColor(backgroundColor.toArgb())
            radarChart.setTouchEnabled(touchEnabled)
            radarChart.isRotationEnabled = rotationEnabled
            radarChart.webLineWidth = webLineWidth
            radarChart.webLineWidthInner = webLineWidthInner
            radarChart.webAlpha = webAlpha
            radarChart.skipWebLineCount = skipWebLineCount

            radarChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            legend?.invoke(radarChart.legend)
            xAxisConfig?.invoke(radarChart.xAxis)
            yAxisConfig?.invoke(radarChart.yAxis)

            if (onValueSelected != null) {
                radarChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) = onValueSelected(entry, highlight)
                    override fun onNothingSelected() = onValueSelected(null, null)
                })
            }

            if (animationDuration > 0) {
                if (animationEasing != null) {
                    radarChart.animateXY(animationDuration, animationDuration, animationEasing, animationEasing)
                } else {
                    radarChart.animateXY(animationDuration, animationDuration)
                }
            }

            radarChart.invalidate()
        }
    )
}

