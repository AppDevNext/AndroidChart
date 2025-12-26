package info.appdev.charting.utils

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import info.appdev.charting.data.Entry
import info.appdev.charting.interfaces.datasets.IBubbleDataSet
import info.appdev.charting.interfaces.datasets.ICandleDataSet
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.interfaces.datasets.IScatterDataSet
import info.appdev.charting.utils.MPPointD.Companion.getInstance
import kotlin.Boolean
import kotlin.FloatArray
import kotlin.Int

/**
 * Transformer class that contains all matrices and is responsible for
 * transforming values into pixels on the screen and backwards.
 */
open class Transformer(@JvmField protected var viewPortHandler: ViewPortHandler) {
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

    protected var valuePointsForGenerateTransformedValuesScatter: FloatArray = FloatArray(1)

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the SCATTERCHART.
     */
    fun generateTransformedValuesScatter(
        data: IScatterDataSet, phaseX: Float,
        phaseY: Float, from: Int, to: Int
    ): FloatArray {
        val count = ((to - from) * phaseX + 1).toInt() * 2

        if (valuePointsForGenerateTransformedValuesScatter.size != count) {
            valuePointsForGenerateTransformedValuesScatter = FloatArray(count)
        }
        val valuePoints = valuePointsForGenerateTransformedValuesScatter

        var j = 0
        while (j < count) {
            val e = data.getEntryForIndex(j / 2 + from)

            if (e != null) {
                valuePoints[j] = e.x
                valuePoints[j + 1] = e.y * phaseY
            } else {
                valuePoints[j] = 0f
                valuePoints[j + 1] = 0f
            }
            j += 2
        }

        this.valueToPixelMatrix.mapPoints(valuePoints)

        return valuePoints
    }

    protected var valuePointsForGenerateTransformedValuesBubble: FloatArray = FloatArray(1)

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the BUBBLECHART.
     */
    fun generateTransformedValuesBubble(data: IBubbleDataSet, phaseY: Float, from: Int, to: Int): FloatArray {
        val count = (to - from + 1) * 2 // (int) Math.ceil((to - from) * phaseX) * 2;

        if (valuePointsForGenerateTransformedValuesBubble.size != count) {
            valuePointsForGenerateTransformedValuesBubble = FloatArray(count)
        }
        val valuePoints = valuePointsForGenerateTransformedValuesBubble

        var j = 0
        while (j < count) {
            val e: Entry? = data.getEntryForIndex(j / 2 + from)

            if (e != null) {
                valuePoints[j] = e.x
                valuePoints[j + 1] = e.y * phaseY
            } else {
                valuePoints[j] = 0f
                valuePoints[j + 1] = 0f
            }
            j += 2
        }

        this.valueToPixelMatrix.mapPoints(valuePoints)

        return valuePoints
    }

    protected var valuePointsForGenerateTransformedValuesLine: FloatArray = FloatArray(1)

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the LINECHART.
     */
    fun generateTransformedValuesLine(
        data: ILineDataSet,
        phaseX: Float, phaseY: Float,
        min: Int, max: Int
    ): FloatArray {
        var count = (((max - min) * phaseX).toInt() + 1) * 2
        if (count < 0) count = 0

        if (valuePointsForGenerateTransformedValuesLine.size != count) {
            valuePointsForGenerateTransformedValuesLine = FloatArray(count)
        }
        val valuePoints = valuePointsForGenerateTransformedValuesLine

        var j = 0
        while (j < count) {
            val e = data.getEntryForIndex(j / 2 + min)

            if (e != null) {
                valuePoints[j] = e.x
                valuePoints[j + 1] = e.y * phaseY
            } else {
                valuePoints[j] = 0f
                valuePoints[j + 1] = 0f
            }
            j += 2
        }

        this.valueToPixelMatrix.mapPoints(valuePoints)

        return valuePoints
    }

    protected var valuePointsForGenerateTransformedValuesCandle: FloatArray = FloatArray(1)

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the CANDLESTICKCHART.
     */
    fun generateTransformedValuesCandle(
        data: ICandleDataSet,
        phaseX: Float, phaseY: Float, from: Int, to: Int
    ): FloatArray {
        val count = ((to - from) * phaseX + 1).toInt() * 2

        if (valuePointsForGenerateTransformedValuesCandle.size != count) {
            valuePointsForGenerateTransformedValuesCandle = FloatArray(count)
        }
        val valuePoints = valuePointsForGenerateTransformedValuesCandle

        var j = 0
        while (j < count) {
            val e = data.getEntryForIndex(j / 2 + from)

            if (e != null) {
                valuePoints[j] = e.x
                valuePoints[j + 1] = e.high * phaseY
            } else {
                valuePoints[j] = 0f
                valuePoints[j + 1] = 0f
            }
            j += 2
        }

        this.valueToPixelMatrix.mapPoints(valuePoints)

        return valuePoints
    }

    /**
     * transform a path with all the given matrices VERY IMPORTANT: keep order
     * to value-touch-offset
     */
    fun pathValueToPixel(path: Path) {
        path.transform(this.valueMatrix)
        path.transform(viewPortHandler.matrixTouch)
        path.transform(this.offsetMatrix)
    }

    /**
     * Transforms multiple paths will all matrices.
     */
    fun pathValuesToPixel(paths: MutableList<Path?>) {
        for (i in paths.indices) {
            pathValueToPixel(paths.get(i)!!)
        }
    }

    /**
     * Transform an array of points with all matrices. VERY IMPORTANT: Keep
     * matrix order "value-touch-offset" when transforming.
     */
    fun pointValuesToPixel(pts: FloatArray?) {
        valueMatrix.mapPoints(pts)
        viewPortHandler.matrixTouch.mapPoints(pts)
        offsetMatrix.mapPoints(pts)
    }

    /**
     * Transform a rectangle with all matrices.
     */
    fun rectValueToPixel(r: RectF?) {
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
    fun rectValueToPixelHorizontal(r: RectF?) {
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
    fun rectValuesToPixel(rects: MutableList<RectF?>) {
        val m = this.valueToPixelMatrix

        for (i in rects.indices) m.mapRect(rects.get(i))
    }

    protected var mPixelToValueMatrixBuffer: Matrix = Matrix()

    /**
     * Transforms the given array of touch positions (pixels) (x, y, x, y, ...)
     * into values on the chart.
     */
    fun pixelsToValue(pixels: FloatArray?) {
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
     * Returns a recyclable MPPointD instance.
     * returns the x and y values in the chart at the given touch point
     * (encapsulated in a MPPointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelForValues(...).
     */
    fun getValuesByTouchPoint(x: Float, y: Float): MPPointD {
        val result = getInstance(0.0, 0.0)
        getValuesByTouchPoint(x, y, result)
        return result
    }

    fun getValuesByTouchPoint(x: Float, y: Float, outputPoint: MPPointD) {
        ptsBuffer[0] = x
        ptsBuffer[1] = y

        pixelsToValue(ptsBuffer)

        outputPoint.x = ptsBuffer[0].toDouble()
        outputPoint.y = ptsBuffer[1].toDouble()
    }

    /**
     * Returns a recyclable MPPointD instance.
     * Returns the x and y coordinates (pixels) for a given x and y value in the chart.
     */
    fun getPixelForValues(x: Float, y: Float): MPPointD {
        ptsBuffer[0] = x
        ptsBuffer[1] = y

        pointValuesToPixel(ptsBuffer)

        val xPx = ptsBuffer[0].toDouble()
        val yPx = ptsBuffer[1].toDouble()

        return getInstance(xPx, yPx)
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
