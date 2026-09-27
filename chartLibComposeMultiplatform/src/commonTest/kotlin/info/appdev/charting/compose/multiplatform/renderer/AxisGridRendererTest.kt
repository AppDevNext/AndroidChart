package info.appdev.charting.compose.multiplatform.renderer

import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.utils.TransformerCore
import info.appdev.charting.utils.ViewPortHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the pure pixel-math helpers behind the Step A.7 proof-of-concept grid line
 * renderer, without needing a Compose UI test harness: a 100x100 content rect mapping the
 * axis-value range [0, 10] should place value 0 at the left/bottom edge and value 10 at the
 * right/top edge, matching `chartLib`'s `XAxisRenderer`/`YAxisRenderer` pixel math.
 */
class AxisGridRendererTest {

    private fun setUpViewPortAndTransformer(): Pair<ViewPortHandler, TransformerCore> {
        val viewPortHandler = ViewPortHandler()
        viewPortHandler.setChartDimens(100f, 100f)
        viewPortHandler.restrainViewPort(0f, 0f, 0f, 0f)

        val transformer = TransformerCore(viewPortHandler)
        transformer.prepareMatrixValuePx(xChartMin = 0f, deltaX = 10f, deltaY = 10f, yChartMin = 0f)
        transformer.prepareMatrixOffset(inverted = false)
        return viewPortHandler to transformer
    }

    @Test
    fun xAxisGridPixelPositions_mapsAxisValuesToContentRectPixels() {
        val (viewPortHandler, transformer) = setUpViewPortAndTransformer()

        val xAxis = XAxis().apply {
            entries = floatArrayOf(0f, 10f)
        }

        val positions = xAxisGridPixelPositions(xAxis, transformer)

        assertEquals(4, positions.size)
        assertEquals(viewPortHandler.contentLeft(), positions[0], absoluteTolerance = 0.001f)
        assertEquals(viewPortHandler.contentRight(), positions[2], absoluteTolerance = 0.001f)
    }

    @Test
    fun yAxisGridPixelPositions_mapsAxisValuesToContentRectPixels() {
        val (viewPortHandler, transformer) = setUpViewPortAndTransformer()

        val yAxis = YAxis().apply {
            entries = floatArrayOf(0f, 10f)
        }

        val positions = yAxisGridPixelPositions(yAxis, transformer)

        assertEquals(4, positions.size)
        // axis value 0 maps to the bottom of the content rect, value 10 to the top
        // (y-pixel axis is inverted relative to chart-value axis).
        assertEquals(viewPortHandler.contentBottom(), positions[1], absoluteTolerance = 0.001f)
        assertEquals(viewPortHandler.contentTop(), positions[3], absoluteTolerance = 0.001f)
    }

    private fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float) {
        kotlin.test.assertTrue(
            kotlin.math.abs(expected - actual) <= absoluteTolerance,
            "expected=$expected actual=$actual"
        )
    }
}
