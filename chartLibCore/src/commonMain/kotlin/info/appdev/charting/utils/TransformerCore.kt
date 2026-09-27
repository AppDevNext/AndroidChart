package info.appdev.charting.utils

/**
 * Transformer class that contains all matrices and is responsible for
 * transforming values into pixels on the screen and backwards.
 *
 * This common base holds every matrix operation that does not depend on Android-only types
 * (`Path`) or on the dataset family (`ILineDataSet`, `IBubbleDataSet`, `ICandleDataSet`,
 * `IScatterDataSet`), which are not yet available in `commonMain`. The Android-specific pieces
 * live in `info.appdev.charting.utils.Transformer` in the `chartLib` module, which extends this
 * class.
 */
open class TransformerCore(protected var viewPortHandler: ViewPortHandler) {
    /**
     * matrix to map the values to the screen pixels
     */
    var valueMatrix: Matrix = Matrix()
        protected set

    /**
     * matrix for handling the different offsets of the chart
     */
    var offsetMatrix: Matrix = Matrix()
        protected set

    /**
     * Prepares the matrix that transforms values to pixels. Calculates the
     * scale factors from the charts size and offsets.
     */
    fun prepareMatrixValuePx(xChartMin: Float, deltaX: Float, deltaY: Float, yChartMin: Float) {
        var scaleX = viewPortHandler.contentWidth() / deltaX
        var scaleY = viewPortHandler.contentHeight() / deltaY

        if (scaleX.isInfinite()) {
            scaleX = 0f
        }
        if (scaleY.isInfinite()) {
            scaleY = 0f
        }

        // setup all matrices
        valueMatrix.reset()
        valueMatrix.postTranslate(-xChartMin, -yChartMin)
        valueMatrix.postScale(scaleX, -scaleY)
    }

    /**
     * Prepares the matrix that contains all offsets.
     */
    open fun prepareMatrixOffset(inverted: Boolean) {
        offsetMatrix.reset()

        // offset.postTranslate(mOffsetLeft, getHeight() - mOffsetBottom);
        if (!inverted) offsetMatrix.postTranslate(
            viewPortHandler.offsetLeft(),
            viewPortHandler.chartHeight - viewPortHandler.offsetBottom()
        )
        else {
            offsetMatrix
                .setTranslate(viewPortHandler.offsetLeft(), -viewPortHandler.offsetTop())
            offsetMatrix.postScale(1.0f, -1.0f)
        }
    }

    /**
     * Transform an array of points with all matrices. VERY IMPORTANT: Keep
     * matrix order "value-touch-offset" when transforming.
     */
    fun pointValuesToPixel(pts: FloatArray) {
        valueMatrix.mapPoints(pts)
        viewPortHandler.matrixTouch.mapPoints(pts)
        offsetMatrix.mapPoints(pts)
    }

    /**
     * Transform a rectangle with all matrices.
     */
    fun rectValueToPixel(r: RectF) {
        valueMatrix.mapRect(r)
        viewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     */
    fun rectToPixelPhase(r: RectF, phaseY: Float) {
        // multiply the height of the rect with the phase

        r.top *= phaseY
        r.bottom *= phaseY

        valueMatrix.mapRect(r)
        viewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    fun rectToPixelPhaseHorizontal(r: RectF, phaseY: Float) {
        // multiply the height of the rect with the phase

        r.left *= phaseY
        r.right *= phaseY

        valueMatrix.mapRect(r)
        viewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     */
    fun rectValueToPixelHorizontal(r: RectF) {
        valueMatrix.mapRect(r)
        viewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     */
    fun rectValueToPixelHorizontal(r: RectF, phaseY: Float) {
        // multiply the height of the rect with the phase

        r.left *= phaseY
        r.right *= phaseY

        valueMatrix.mapRect(r)
        viewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * transforms multiple rects with all matrices
     */
    fun rectValuesToPixel(rects: MutableList<RectF>) {
        val m = this.valueToPixelMatrix

        for (i in rects.indices) m.mapRect(rects[i])
    }

    protected var mPixelToValueMatrixBuffer: Matrix = Matrix()

    /**
     * Transforms the given array of touch positions (pixels) (x, y, x, y, ...)
     * into values on the chart.
     */
    fun pixelsToValue(pixels: FloatArray) {
        val tmp = mPixelToValueMatrixBuffer
        tmp.reset()

        // invert all matrixes to convert back to the original value
        offsetMatrix.invert(tmp)
        tmp.mapPoints(pixels)

        viewPortHandler.matrixTouch.invert(tmp)
        tmp.mapPoints(pixels)

        valueMatrix.invert(tmp)
        tmp.mapPoints(pixels)
    }

    /**
     * buffer for performance
     */
    var ptsBuffer: FloatArray = FloatArray(2)

    /**
     * Returns a recyclable PointD instance.
     * returns the x and y values in the chart at the given touch point
     * (encapsulated in a PointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelForValues(...).
     */
    fun getValuesByTouchPoint(x: Float, y: Float): PointD {
        val result = PointD.getInstance(0.0, 0.0)
        getValuesByTouchPoint(x, y, result)
        return result
    }

    fun getValuesByTouchPoint(x: Float, y: Float, outputPoint: PointD) {
        ptsBuffer[0] = x
        ptsBuffer[1] = y

        pixelsToValue(ptsBuffer)

        outputPoint.x = ptsBuffer[0].toDouble()
        outputPoint.y = ptsBuffer[1].toDouble()
    }

    /**
     * Returns a recyclable PointD instance.
     * Returns the x and y coordinates (pixels) for a given x and y value in the chart.
     */
    fun getPixelForValues(x: Float, y: Float): PointD {
        ptsBuffer[0] = x
        ptsBuffer[1] = y

        pointValuesToPixel(ptsBuffer)

        val xPx = ptsBuffer[0].toDouble()
        val yPx = ptsBuffer[1].toDouble()

        return PointD.getInstance(xPx, yPx)
    }

    private val mMBuffer1 = Matrix()

    val valueToPixelMatrix: Matrix
        get() {
            mMBuffer1.set(this.valueMatrix)
            mMBuffer1.postConcat(viewPortHandler.matrixTouch)
            mMBuffer1.postConcat(this.offsetMatrix)
            return mMBuffer1
        }

    private val mMBuffer2 = Matrix()

    val pixelToValueMatrix: Matrix
        get() {
            this.valueToPixelMatrix.invert(mMBuffer2)
            return mMBuffer2
        }
}
