package info.appdev.charting.compose.multiplatform.gesture

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import info.appdev.charting.utils.Matrix
import info.appdev.charting.utils.ViewPortHandler

/**
 * Proof-of-concept Compose Multiplatform gesture handling (Step A.8): drives
 * [ViewPortHandler]'s existing pan/zoom matrix math from Compose's
 * `detectTransformGestures`, replacing `chartLib`'s `MotionEvent`-based
 * `ChartTouchListener`/`BarLineChartTouchListener`.
 *
 * `ViewPortHandler.zoom`/`translate`/`refresh` (and the bounds-clamping in
 * `limitTransAndScale`) already live in `chartLibCore` and needed no changes for this
 * proof-of-concept: Compose's `detectTransformGestures` conveniently reports one combined
 * pan+zoom+centroid callback per frame (unlike the touch listener's separate
 * `performDrag`/`performZoom`), so this module only needs to combine the two into a single
 * matrix update and feed it to [ViewPortHandler.refresh].
 *
 * **Deliberately out of scope for this proof-of-concept** (same scoping choice as the earlier
 * axis/data renderer slices): rotation gestures, independent X/Y-only zoom modes
 * (`X_ZOOM`/`Y_ZOOM`), highlight-on-drag, double-tap zoom, fling/deceleration, and
 * `OnChartGestureListener` callbacks.
 */

/**
 * Computes the pixel-space touch matrix that results from applying [pan] (in pixels) and
 * [zoom] (a uniform scale factor, pivoting on [centroid]) to [viewPortHandler]'s current
 * touch matrix. Extracted as a pure function, separate from the gesture-detection wiring
 * itself, so the matrix math can be unit-tested without needing a Compose UI test harness.
 *
 * Mirrors `chartLib`'s `BarLineChartTouchListener.performDrag`/`performZoom` combined into a
 * single step: `matrix.postTranslate(pan.x, pan.y)` then
 * `matrix.postScale(zoom, zoom, centroid.x, centroid.y)`, applied on top of the current
 * `matrixTouch` (not a "saved" matrix from gesture-start, since Compose already reports
 * incremental per-frame pan/zoom deltas rather than cumulative ones since gesture-start).
 */
fun chartGestureMatrix(
    viewPortHandler: ViewPortHandler,
    pan: Offset,
    zoom: Float,
    centroid: Offset,
    scaleXEnabled: Boolean = true,
    scaleYEnabled: Boolean = true,
    dragXEnabled: Boolean = true,
    dragYEnabled: Boolean = true
): Matrix {
    val matrix = Matrix()
    matrix.set(viewPortHandler.matrixTouch)

    val dx = if (dragXEnabled) pan.x else 0f
    val dy = if (dragYEnabled) pan.y else 0f
    matrix.postTranslate(dx, dy)

    val scaleX = if (scaleXEnabled) zoom else 1f
    val scaleY = if (scaleYEnabled) zoom else 1f
    if (scaleX != 1f || scaleY != 1f) {
        matrix.postScale(scaleX, scaleY, centroid.x, centroid.y)
    }

    return matrix
}

/**
 * [Modifier] that recognizes pan/pinch-zoom gestures and applies them to [viewPortHandler]
 * via [chartGestureMatrix] + [ViewPortHandler.refresh], invoking [onGesture] afterwards
 * (typically to trigger a Compose recomposition/redraw of the chart).
 */
fun Modifier.chartTransformGestures(
    viewPortHandler: ViewPortHandler,
    scaleXEnabled: Boolean = true,
    scaleYEnabled: Boolean = true,
    dragXEnabled: Boolean = true,
    dragYEnabled: Boolean = true,
    onGesture: () -> Unit
): Modifier = this.pointerInput(viewPortHandler) {
    detectTransformGestures { centroid, pan, zoom, _ ->
        val matrix = chartGestureMatrix(
            viewPortHandler = viewPortHandler,
            pan = pan,
            zoom = zoom,
            centroid = centroid,
            scaleXEnabled = scaleXEnabled,
            scaleYEnabled = scaleYEnabled,
            dragXEnabled = dragXEnabled,
            dragYEnabled = dragYEnabled
        )
        viewPortHandler.refresh(matrix, onGesture, true)
    }
}
