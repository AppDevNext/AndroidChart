package info.appdev.charting.demokmp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import info.appdev.charting.compose.multiplatform.gesture.chartTransformGestures
import info.appdev.charting.compose.multiplatform.renderer.drawBarChartDataSet
import info.appdev.charting.compose.multiplatform.renderer.drawXAxisGridLines
import info.appdev.charting.compose.multiplatform.renderer.drawYAxisGridLines
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntryFloat
import info.appdev.charting.utils.TransformerCore
import info.appdev.charting.utils.ViewPortHandler

/**
 * `demoKmp` — Step A.9's shared Compose Multiplatform demo screen, built entirely on the
 * proof-of-concept renderers/gesture handling added in Steps A.7/A.8
 * (`chartLibComposeMultiplatform`), rather than `chartLib`/`chartLibCompose` (Android-only).
 *
 * This intentionally mirrors the `app`/`chartLibCompose` demo apps' spirit (show a chart with
 * a couple of example datasets, backed by `androidx.compose.material3`) while only using
 * portable (`chartLibCore`) chart types under the hood, so the exact same `@Composable`
 * renders identically on Android, Desktop, and iOS.
 */
@Composable
fun DemoKmpApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BarChartDemo()
        }
    }
}

@Composable
private fun BarChartDemo() {
    val dataSet = remember {
        BarDataSet(
            mutableListOf(
                BarEntryFloat(0f, 4f),
                BarEntryFloat(1f, 8f),
                BarEntryFloat(2f, 6f),
                BarEntryFloat(3f, 10f),
                BarEntryFloat(4f, 3f)
            ),
            "demoKmp bar dataset"
        )
    }

    val viewPortHandler = remember { ViewPortHandler() }
    val transformer = remember { TransformerCore(viewPortHandler) }
    val xAxis = remember { XAxis().apply { entries = floatArrayOf(0f, 1f, 2f, 3f, 4f) } }
    val yAxis = remember { YAxis().apply { entries = floatArrayOf(0f, 2f, 4f, 6f, 8f, 10f) } }

    // Bumped after every gesture to trigger a redraw (Canvas isn't itself observing
    // ViewPortHandler's mutable internal matrix state).
    var redrawTick by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .chartTransformGestures(viewPortHandler) { redrawTick++ }
        ) {
            redrawTick // read to establish a recomposition dependency on gesture updates

            viewPortHandler.setChartDimens(size.width, size.height)
            viewPortHandler.restrainViewPort(40f, 20f, 20f, 40f)
            transformer.prepareMatrixValuePx(xChartMin = -0.5f, deltaX = 5f, deltaY = 11f, yChartMin = -0.5f)
            transformer.prepareMatrixOffset(inverted = false)

            drawXAxisGridLines(xAxis, viewPortHandler, transformer)
            drawYAxisGridLines(yAxis, viewPortHandler, transformer)
            drawBarChartDataSet(dataSet, barWidth = 0.6f, transformer = transformer)
        }

        Text(
            text = "demoKmp: BarDataSet + axis grid lines rendered via chartLibComposeMultiplatform. " +
                "Pinch/drag to pan & zoom.",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
    }
}
