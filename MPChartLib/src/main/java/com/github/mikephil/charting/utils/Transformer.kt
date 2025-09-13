package com.github.mikephil.charting.utils

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import kotlin.Boolean
import kotlin.FloatArray
import kotlin.Int

/**
 * Transformer class that contains all matrices and is responsible for
 * transforming values into pixels on the screen and backwards.
 *
 * @author Philipp Jahoda
 */
open class Transformer(protected var mViewPortHandler: ViewPortHandler) {
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
     *
     * @param xChartMin
     * @param deltaX
     * @param deltaY
     * @param yChartMin
     */
    fun prepareMatrixValuePx(xChartMin: Float, deltaX: Float, deltaY: Float, yChartMin: Float) {
        var scaleX = mViewPortHandler.contentWidth() / deltaX
        var scaleY = mViewPortHandler.contentHeight() / deltaY

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
     *
     * @param inverted
     */
    open fun prepareMatrixOffset(inverted: Boolean) {
        offsetMatrix.reset()

        // offset.postTranslate(mOffsetLeft, getHeight() - mOffsetBottom);
        if (!inverted) offsetMatrix.postTranslate(
            mViewPortHandler.offsetLeft(),
            mViewPortHandler.chartHeight - mViewPortHandler.offsetBottom()
        )
        else {
            offsetMatrix
                .setTranslate(mViewPortHandler.offsetLeft(), -mViewPortHandler.offsetTop())
            offsetMatrix.postScale(1.0f, -1.0f)
        }
    }

    protected var valuePointsForGenerateTransformedValuesScatter: FloatArray = FloatArray(1)

    /**
     * Transforms an List of Entry into a float array containing the x and
     * y values transformed with all matrices for the SCATTERCHART.
     *
     * @param data
     * @return
     */
    fun generateTransformedValuesScatter(
        data: IScatterDataSet, phaseX: kotlin.Float,
        phaseY: kotlin.Float, from: Int, to: Int
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
     *
     * @param data
     * @return
     */
    fun generateTransformedValuesBubble(data: IBubbleDataSet, phaseY: kotlin.Float, from: Int, to: Int): FloatArray {
        val count = (to - from + 1) * 2 // (int) Math.ceil((to - from) * phaseX) * 2;

        if (valuePointsForGenerateTransformedValuesBubble.size != count) {
            valuePointsForGenerateTransformedValuesBubble = FloatArray(count)
        }
        val valuePoints = valuePointsForGenerateTransformedValuesBubble

        var j = 0
        while (j < count) {
            val e: Entry = data.getEntryForIndex(j / 2 + from)

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
     *
     * @param data
     * @return
     */
    fun generateTransformedValuesLine(
        data: ILineDataSet,
        phaseX: kotlin.Float, phaseY: kotlin.Float,
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
     *
     * @param data
     * @return
     */
    fun generateTransformedValuesCandle(
        data: ICandleDataSet,
        phaseX: kotlin.Float, phaseY: kotlin.Float, from: Int, to: Int
    ): FloatArray {
        val count = ((to - from) * phaseX + 1).toInt() * 2

        if (valuePointsForGenerateTransformedValuesCandle.size != count) {
            valuePointsForGenerateTransformedValuesCandle = FloatArray(count)
        }
        val valuePoints = valuePointsForGenerateTransformedValuesCandle

        var j = 0
        while (j < count) {
            val e = data.getEntryForIndex(j / 2 + from)

            valuePoints[j] = e.x
            valuePoints[j + 1] = e.high * phaseY
            j += 2
        }

        this.valueToPixelMatrix.mapPoints(valuePoints)

        return valuePoints
    }

    /**
     * transform a path with all the given matrices VERY IMPORTANT: keep order
     * to value-touch-offset
     *
     * @param path
     */
    fun pathValueToPixel(path: Path) {
        path.transform(this.valueMatrix)
        path.transform(mViewPortHandler.matrixTouch)
        path.transform(this.offsetMatrix)
    }

    /**
     * Transforms multiple paths will all matrices.
     *
     * @param paths
     */
    fun pathValuesToPixel(paths: MutableList<Path?>) {
        for (i in paths.indices) {
            pathValueToPixel(paths.get(i)!!)
        }
    }

    /**
     * Transform an array of points with all matrices. VERY IMPORTANT: Keep
     * matrix order "value-touch-offset" when transforming.
     *
     * @param pts
     */
    fun pointValuesToPixel(pts: FloatArray?) {
        valueMatrix.mapPoints(pts)
        mViewPortHandler.matrixTouch.mapPoints(pts)
        offsetMatrix.mapPoints(pts)
    }

    /**
     * Transform a rectangle with all matrices.
     *
     * @param r
     */
    fun rectValueToPixel(r: RectF?) {
        valueMatrix.mapRect(r)
        mViewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     *
     * @param r
     * @param phaseY
     */
    fun rectToPixelPhase(r: RectF, phaseY: kotlin.Float) {
        // multiply the height of the rect with the phase

        r.top *= phaseY
        r.bottom *= phaseY

        valueMatrix.mapRect(r)
        mViewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    fun rectToPixelPhaseHorizontal(r: RectF, phaseY: kotlin.Float) {
        // multiply the height of the rect with the phase

        r.left *= phaseY
        r.right *= phaseY

        valueMatrix.mapRect(r)
        mViewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     *
     * @param r
     */
    fun rectValueToPixelHorizontal(r: RectF?) {
        valueMatrix.mapRect(r)
        mViewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * Transform a rectangle with all matrices with potential animation phases.
     *
     * @param r
     * @param phaseY
     */
    fun rectValueToPixelHorizontal(r: RectF, phaseY: kotlin.Float) {
        // multiply the height of the rect with the phase

        r.left *= phaseY
        r.right *= phaseY

        valueMatrix.mapRect(r)
        mViewPortHandler.matrixTouch.mapRect(r)
        offsetMatrix.mapRect(r)
    }

    /**
     * transforms multiple rects with all matrices
     *
     * @param rects
     */
    fun rectValuesToPixel(rects: MutableList<RectF?>) {
        val m = this.valueToPixelMatrix

        for (i in rects.indices) m.mapRect(rects.get(i))
    }

    protected var mPixelToValueMatrixBuffer: Matrix = Matrix()

    /**
     * Transforms the given array of touch positions (pixels) (x, y, x, y, ...)
     * into values on the chart.
     *
     * @param pixels
     */
    fun pixelsToValue(pixels: FloatArray?) {
        val tmp = mPixelToValueMatrixBuffer
        tmp.reset()

        // invert all matrixes to convert back to the original value
        offsetMatrix.invert(tmp)
        tmp.mapPoints(pixels)

        mViewPortHandler.matrixTouch.invert(tmp)
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
     *
     * @param x
     * @param y
     * @return
     */
    fun getValuesByTouchPoint(x: kotlin.Float, y: kotlin.Float): MPPointD {
        val result: MPPointD = MPPointD.Companion.getInstance(0.0, 0.0)
        getValuesByTouchPoint(x, y, result)
        return result
    }

    fun getValuesByTouchPoint(x: kotlin.Float, y: kotlin.Float, outputPoint: MPPointD) {
        ptsBuffer[0] = x
        ptsBuffer[1] = y

        pixelsToValue(ptsBuffer)

        outputPoint.x = ptsBuffer[0].toDouble()
        outputPoint.y = ptsBuffer[1].toDouble()
    }

    /**
     * Returns a recyclable MPPointD instance.
     * Returns the x and y coordinates (pixels) for a given x and y value in the chart.
     *
     * @param x
     * @param y
     * @return
     */
    fun getPixelForValues(x: kotlin.Float, y: kotlin.Float): MPPointD {
        ptsBuffer[0] = x
        ptsBuffer[1] = y

        pointValuesToPixel(ptsBuffer)

        val xPx = ptsBuffer[0].toDouble()
        val yPx = ptsBuffer[1].toDouble()

        return MPPointD.Companion.getInstance(xPx, yPx)
    }

    private val mMBuffer1 = Matrix()

    val valueToPixelMatrix: Matrix
        get() {
            mMBuffer1.set(this.valueMatrix)
            mMBuffer1.postConcat(mViewPortHandler.matrixTouch)
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
