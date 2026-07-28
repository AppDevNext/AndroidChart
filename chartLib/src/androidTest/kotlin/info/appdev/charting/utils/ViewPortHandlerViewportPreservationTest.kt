package info.appdev.charting.utils

import android.graphics.Matrix
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Regression coverage for the viewport jumping sideways when the content rect is resized
 * (see [ViewPortHandler.restrainViewPort] / preserveViewPortOnResize).
 *
 * Scenario reproduced from a real chart: the user zooms into a long x-axis (e.g. a
 * session-length line chart) and pans to some point in the middle. Zooming the y-axis widens
 * the y-axis labels, so on the following layout the left offset grows and the content rect
 * shrinks. Because the pan is stored in the touch matrix as a *pixel* translation while the
 * value-to-pixel scale is derived from the content rect width, resizing the rect without
 * re-mapping the translation makes the same pixel offset denote a different x-value, and the
 * visible range slides.
 *
 * These are instrumented tests because they rely on the real [android.graphics.Matrix]; the
 * library's local JVM unit tests stub Android graphics out and cannot exercise the transform.
 */
@RunWith(AndroidJUnit4::class)
class ViewPortHandlerViewportPreservationTest {

    // ---- Chart geometry (pixels) ---------------------------------------------------------
    // Note: deliberately not named chartWidth/chartHeight - those would shadow the receiver's
    // properties inside ViewPortHandler.apply { ... }.
    private val chartWidthPx = 1000f
    private val chartHeightPx = 600f
    private val topOffset = 20f
    private val rightOffset = 30f
    private val bottomOffset = 40f

    // Left axis label width before and after a y-zoom widens the labels. The extra 40px is
    // what shrinks the content rect and used to drag the chart sideways.
    private val leftOffsetBefore = 60f
    private val leftOffsetAfter = 100f

    // Bottom (x-axis label) height. Growing it as well shrinks the content height, which
    // exercises the vertical half of the same rescaling.
    private val bottomOffsetAfter = 80f

    // ---- Data range mapped onto the chart ------------------------------------------------
    // e.g. a one-hour session in seconds; deltaX is large, so a small rect change is a very
    // visible x shift.
    private val xChartMin = 0f
    private val deltaX = 3600f
    private val yChartMin = 0f
    private val deltaY = 100f

    private val zoomX = 100f
    private val zoomY = 5f

    // The x-value we pan to the left edge before the resize happens.
    private val targetLeftX = 1800f

    @Test
    fun visibleXRange_survivesContentRectResize_whenYAxisLabelsGrow() {
        val handler = ViewPortHandler().apply {
            setChartDimens(chartWidthPx, chartHeightPx)
            setMinMaxScaleX(1f, 1000f)
            setMinMaxScaleY(1f, 1000f)
        }

        // Narrow-label ("before y-zoom") layout, then zoom and pan to targetLeftX.
        handler.restrainViewPort(leftOffsetBefore, topOffset, rightOffset, bottomOffset)
        zoom(handler, zoomX, zoomY)
        panLeftEdgeTo(handler, targetLeftX)

        val lowestVisibleBefore = lowestVisibleX(handler)
        // Sanity: the pan actually put targetLeftX at the left edge.
        assertEquals(targetLeftX.toDouble(), lowestVisibleBefore, 1.0)

        // Capture the touch matrix as it stood before the resize so we can measure what the
        // pre-fix behaviour (rect resized, translation carried over unchanged) would produce.
        val touchBeforeResize = Matrix(handler.matrixTouch)

        // Wide-label ("after y-zoom") layout: left offset grows, content rect shrinks. With
        // the fix, restrainViewPort rescales the pan so the visible range stays put.
        handler.restrainViewPort(leftOffsetAfter, topOffset, rightOffset, bottomOffset)
        val lowestVisibleAfter = lowestVisibleX(handler)

        // What the unfixed library did: same shrunk rect, but the translation not re-mapped.
        val lowestVisibleWithoutFix = lowestVisibleXForResizeWithoutRescale(touchBeforeResize)

        val fixedShift = abs(lowestVisibleAfter - lowestVisibleBefore)
        val unfixedShift = abs(lowestVisibleWithoutFix - lowestVisibleBefore)

        // The fix holds the left edge to within a fraction of an x-unit: the tolerance of 1.0
        // (out of a 3600-unit range) only absorbs floating-point rounding in the matrix math.
        assertTrue(
            "Visible left edge shifted by $fixedShift x-units after the content rect resize " +
                "(before=$lowestVisibleBefore, after=$lowestVisibleAfter)",
            fixedShift < 1.0
        )

        // Without the fix the same resize (a 40px left-offset growth on a 910px content rect)
        // slides the chart by tens of x-units - here ~83 out of 3600 - proving the bug is real
        // and that this test would fail on the unpatched code.
        assertTrue(
            "Expected a large pre-fix shift to demonstrate the bug, but it was only " +
                "$unfixedShift x-units (would-be lowestVisibleX=$lowestVisibleWithoutFix)",
            unfixedShift > 25.0
        )
    }

    @Test
    fun restrainViewPort_rescalesPanProportionallyOnBothAxes() {
        val handler = ViewPortHandler().apply {
            setChartDimens(chartWidthPx, chartHeightPx)
            setMinMaxScaleX(1f, 1000f)
            setMinMaxScaleY(1f, 1000f)
        }

        handler.restrainViewPort(leftOffsetBefore, topOffset, rightOffset, bottomOffset)
        zoom(handler, zoomX, zoomY)
        panLeftEdgeTo(handler, targetLeftX)
        // Also pan vertically so transY is non-zero and the y-axis rescaling is actually tested.
        panVerticallyBy(handler, 1000f)

        val widthBefore = handler.contentWidth()
        val heightBefore = handler.contentHeight()
        val transXBefore = handler.transX
        val transYBefore = handler.transY

        // Grow both the left (y-axis) and bottom (x-axis) labels, shrinking the rect on both axes.
        handler.restrainViewPort(leftOffsetAfter, topOffset, rightOffset, bottomOffsetAfter)

        val widthAfter = handler.contentWidth()
        val heightAfter = handler.contentHeight()
        val transXAfter = handler.transX
        val transYAfter = handler.transY

        // Guard against a vacuous test: the rect must actually have changed size on both axes.
        assertTrue(widthAfter < widthBefore && heightAfter < heightBefore)
        assertTrue(transXBefore != 0f && transYBefore != 0f)

        // The visible left fraction is -transX / (contentWidth * scaleX); with scaleX fixed
        // across a pure resize it reduces to transX / contentWidth staying constant.
        assertEquals(
            transXBefore / widthBefore,
            transXAfter / widthAfter,
            1e-3f
        )

        // The fix rescales the vertical pan by the same ratio, so transY / contentHeight is
        // preserved too.
        assertEquals(
            transYBefore / heightBefore,
            transYAfter / heightAfter,
            1e-3f
        )
    }

    // ---- helpers -------------------------------------------------------------------------

    /** Applies a zoom about the origin, matching a pinch that leaves the pan untouched. */
    private fun zoom(handler: ViewPortHandler, scaleX: Float, scaleY: Float) {
        val zoomed = handler.zoom(scaleX, scaleY)
        handler.refresh(zoomed, null, false)
    }

    /** Pans the current viewport so that [valueX] sits exactly at the content left edge. */
    private fun panLeftEdgeTo(handler: ViewPortHandler, valueX: Float) {
        val transformer = preparedTransformer(handler)
        val pixel = transformer.getPixelForValues(valueX, yChartMin)
        val dx = (handler.contentLeft() - pixel.x).toFloat()

        val panned = Matrix(handler.matrixTouch).apply { postTranslate(dx, 0f) }
        handler.refresh(panned, null, false)
    }

    /** Pans the viewport vertically by [pixels] (positive = content shifts down). */
    private fun panVerticallyBy(handler: ViewPortHandler, pixels: Float) {
        val panned = Matrix(handler.matrixTouch).apply { postTranslate(0f, pixels) }
        handler.refresh(panned, null, false)
    }

    /** The chart's lowestVisibleX: value at the bottom-left corner of the content rect. */
    private fun lowestVisibleX(handler: ViewPortHandler): Double {
        val transformer = preparedTransformer(handler)
        return transformer
            .getValuesByTouchPoint(handler.contentLeft(), handler.contentBottom())
            .x
    }

    /**
     * Reproduces the pre-fix behaviour: shrink the content rect to the wide-label layout but
     * keep the pre-resize touch matrix unchanged (i.e. the translation is NOT re-mapped). Uses
     * a throwaway handler already sized to the narrow rect so no rescale is triggered.
     */
    private fun lowestVisibleXForResizeWithoutRescale(touchBeforeResize: Matrix): Double {
        val handler = ViewPortHandler().apply {
            setChartDimens(chartWidthPx, chartHeightPx)
            setMinMaxScaleX(1f, 1000f)
            setMinMaxScaleY(1f, 1000f)
            // Size straight to the wide-label layout, then drop in the untouched matrix.
            restrainViewPort(leftOffsetAfter, topOffset, rightOffset, bottomOffset)
            refresh(Matrix(touchBeforeResize), null, false)
        }
        return lowestVisibleX(handler)
    }

    private fun preparedTransformer(handler: ViewPortHandler): Transformer =
        Transformer(handler).apply {
            prepareMatrixOffset(false)
            prepareMatrixValuePx(xChartMin, deltaX, deltaY, yChartMin)
        }
}
