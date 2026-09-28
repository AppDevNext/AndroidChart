package info.appdev.charting.compose.multiplatform.renderer

import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntryFloat
import info.appdev.charting.utils.TransformerCore
import info.appdev.charting.utils.ViewPortHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the pure pixel-math helper behind the Step A.7 proof-of-concept bar chart data
 * renderer, without needing a Compose UI test harness: a 100x100 content rect mapping the
 * axis-value range [0, 10] on both axes should place a bar's value-space rect at the expected
 * pixel-space rect, matching the non-stacked branch of `chartLib`'s `buffer.BarBuffer.feed`.
 */
class BarChartRendererTest {

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
    fun barPixelRects_mapsPositiveEntryValueRectToContentRectPixels() {
        val (viewPortHandler, transformer) = setUpViewPortAndTransformer()

        val dataSet = BarDataSet(mutableListOf(BarEntryFloat(5f, 10f)), "positive")

        val positions = barPixelRects(dataSet, barWidth = 2f, transformer = transformer)

        assertEquals(4, positions.size)
        // value-space rect: left=4, top=10, right=6, bottom=0
        assertEquals(viewPortHandler.contentLeft() + 40f, positions[0], absoluteTolerance = 0.001f) // left
        assertEquals(viewPortHandler.contentTop(), positions[1], absoluteTolerance = 0.001f) // top (value 10 -> chart top)
        assertEquals(viewPortHandler.contentLeft() + 60f, positions[2], absoluteTolerance = 0.001f) // right
        assertEquals(viewPortHandler.contentBottom(), positions[3], absoluteTolerance = 0.001f) // bottom (value 0 -> chart bottom)
    }

    @Test
    fun barPixelRects_mapsNegativeEntryValueRectToContentRectPixels() {
        val (viewPortHandler, transformer) = setUpViewPortAndTransformer()

        // Prepare a matrix spanning [-10, 10] so a negative entry stays within the viewport.
        transformer.prepareMatrixValuePx(xChartMin = -10f, deltaX = 20f, deltaY = 20f, yChartMin = -10f)
        transformer.prepareMatrixOffset(inverted = false)

        val dataSet = BarDataSet(mutableListOf(BarEntryFloat(0f, -5f)), "negative")

        val positions = barPixelRects(dataSet, barWidth = 2f, transformer = transformer)

        assertEquals(4, positions.size)
        // value-space rect: top=0, bottom=-5 -> pixel top must stay above (numerically less than) pixel bottom
        assertEquals(true, positions[1] < positions[3])
    }
}
