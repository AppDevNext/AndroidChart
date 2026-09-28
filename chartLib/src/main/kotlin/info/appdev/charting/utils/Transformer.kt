package info.appdev.charting.utils

import android.annotation.SuppressLint
import android.graphics.Path
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.interfaces.datasets.IBubbleDataSet
import info.appdev.charting.interfaces.datasets.ICandleDataSet
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.interfaces.datasets.IScatterDataSet

/**
 * Transformer class that contains all matrices and is responsible for
 * transforming values into pixels on the screen and backwards.
 *
 * The platform-agnostic matrix/rect logic lives in the common [TransformerCore] base class
 * (`chartLibCore`). This subclass adds the pieces that still depend on Android-only types
 * (`android.graphics.Path`) or on the dataset family (not yet available in `commonMain`).
 */
open class Transformer(viewPortHandler: ViewPortHandler) : TransformerCore(viewPortHandler) {
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
            val e: EntryFloat? = data.getEntryForIndex(j / 2 + from)

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
        @SuppressLint("RawTypeDataSet") data: ILineDataSet<*>,
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
        path.transform(this.valueMatrix.toAndroidMatrix())
        path.transform(viewPortHandler.matrixTouch.toAndroidMatrix())
        path.transform(this.offsetMatrix.toAndroidMatrix())
    }

    /**
     * Transforms multiple paths will all matrices.
     */
    fun pathValuesToPixel(paths: MutableList<Path?>) {
        for (i in paths.indices) {
            pathValueToPixel(paths[i]!!)
        }
    }
}
