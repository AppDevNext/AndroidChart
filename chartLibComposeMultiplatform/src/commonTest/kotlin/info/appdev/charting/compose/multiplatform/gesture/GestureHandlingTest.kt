package info.appdev.charting.compose.multiplatform.gesture

import androidx.compose.ui.geometry.Offset
import info.appdev.charting.utils.Matrix
import info.appdev.charting.utils.ViewPortHandler
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Verifies the pure matrix-math helper behind the Step A.8 proof-of-concept gesture handling,
 * without needing a Compose UI test harness (no real pointer/gesture input is simulated here,
 * only [chartGestureMatrix]'s pan/zoom-to-matrix math).
 */
class GestureHandlingTest {

    private fun setUpViewPortHandler(): ViewPortHandler {
        val viewPortHandler = ViewPortHandler()
        viewPortHandler.setChartDimens(100f, 100f)
        viewPortHandler.restrainViewPort(0f, 0f, 0f, 0f)
        return viewPortHandler
    }

    @Test
    fun chartGestureMatrix_appliesPanTranslationOnTopOfIdentityMatrix() {
        val viewPortHandler = setUpViewPortHandler()

        val matrix = chartGestureMatrix(
            viewPortHandler = viewPortHandler,
            pan = Offset(10f, 5f),
            zoom = 1f,
            centroid = Offset(50f, 50f)
        )

        val values = FloatArray(9)
        matrix.getValues(values)
        assertEquals(10f, values[Matrix.MTRANS_X], absoluteTolerance = 0.001f)
        assertEquals(5f, values[Matrix.MTRANS_Y], absoluteTolerance = 0.001f)
        assertEquals(1f, values[Matrix.MSCALE_X], absoluteTolerance = 0.001f)
        assertEquals(1f, values[Matrix.MSCALE_Y], absoluteTolerance = 0.001f)
    }

    @Test
    fun chartGestureMatrix_appliesZoomAboutCentroidOnTopOfIdentityMatrix() {
        val viewPortHandler = setUpViewPortHandler()

        val matrix = chartGestureMatrix(
            viewPortHandler = viewPortHandler,
            pan = Offset.Zero,
            zoom = 2f,
            centroid = Offset(50f, 50f)
        )

        val values = FloatArray(9)
        matrix.getValues(values)
        assertEquals(2f, values[Matrix.MSCALE_X], absoluteTolerance = 0.001f)
        assertEquals(2f, values[Matrix.MSCALE_Y], absoluteTolerance = 0.001f)
        // pivot (50, 50) scaled by 2 -> translation = pivot * (1 - scale)
        assertEquals(-50f, values[Matrix.MTRANS_X], absoluteTolerance = 0.001f)
        assertEquals(-50f, values[Matrix.MTRANS_Y], absoluteTolerance = 0.001f)
    }

    @Test
    fun chartGestureMatrix_disabledAxesIgnorePanAndZoomOnThatAxis() {
        val viewPortHandler = setUpViewPortHandler()

        val matrix = chartGestureMatrix(
            viewPortHandler = viewPortHandler,
            pan = Offset(10f, 5f),
            zoom = 2f,
            centroid = Offset(50f, 50f),
            scaleXEnabled = false,
            dragYEnabled = false
        )

        val values = FloatArray(9)
        matrix.getValues(values)
        // X scale disabled -> stays 1; Y drag disabled -> stays 0
        assertEquals(1f, values[Matrix.MSCALE_X], absoluteTolerance = 0.001f)
        assertEquals(2f, values[Matrix.MSCALE_Y], absoluteTolerance = 0.001f)
        assertEquals(10f, values[Matrix.MTRANS_X], absoluteTolerance = 0.001f)
        // y-translation comes only from the Y-scale pivot, since dragY is disabled
        assertEquals(-50f, values[Matrix.MTRANS_Y], absoluteTolerance = 0.001f)
    }
}
