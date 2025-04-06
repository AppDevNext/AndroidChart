package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Baseclass of all axis renderers.
 */
abstract class AxisRenderer(
    viewPortHandler: ViewPortHandler,
    /** transformer to transform values to screen pixels and return  */
    var transformer: Transformer?,
    /** base axis this axis renderer works with  */
    @JvmField protected var mAxis: AxisBase
) : Renderer(viewPortHandler) {
    /**
     * Returns the Transformer object used for transforming the axis values.
     *
     * @return
     */

    /**
     * Returns the Paint object that is used for drawing the grid-lines of the
     * axis.
     *
     * @return
     */
    /**
     * paint object for the grid lines
     */
    var paintGrid: Paint? = null
        protected set

    /**
     * Returns the Paint object used for drawing the axis (labels).
     *
     * @return
     */
    /**
     * paint for the x-label values
     */
    var paintAxisLabels: Paint? = null
        protected set

    /**
     * Returns the Paint object that is used for drawing the axis-line that goes
     * alongside the axis.
     *
     * @return
     */
    /**
     * paint for the line surrounding the chart
     */
    var paintAxisLine: Paint? = null
        protected set

    /**
     * paint used for the limit lines
     */
    @JvmField
    protected var mLimitLinePaint: Paint? = null

    init {
        paintAxisLabels = Paint(Paint.ANTI_ALIAS_FLAG)

        paintGrid = Paint()
        paintGrid!!.color = Color.GRAY
        paintGrid!!.strokeWidth = 1f
        paintGrid!!.style = Paint.Style.STROKE
        paintGrid!!.alpha = 90

        paintAxisLine = Paint()
        paintAxisLine!!.color = Color.BLACK
        paintAxisLine!!.strokeWidth = 1f
        paintAxisLine!!.style = Paint.Style.STROKE

        mLimitLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mLimitLinePaint!!.style = Paint.Style.STROKE
    }

    /**
     * Computes the axis values.
     *
     * @param min - the minimum value in the data object for this axis
     * @param max - the maximum value in the data object for this axis
     */
    open fun computeAxis(min: Float, max: Float, inverted: Boolean) {
        // calculate the starting and entry point of the y-labels (depending on
        // zoom / contentrect bounds)

        var minLocal = min
        var maxLocal = max
        if (viewPortHandler.contentWidth() > 10 && !viewPortHandler.isFullyZoomedOutY) {
            transformer?.let {
                val p1 = it.getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop())
                val p2 = it.getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentBottom())

                if (!inverted) {
                    minLocal = p2.y.toFloat()
                    maxLocal = p1.y.toFloat()
                } else {
                    minLocal = p1.y.toFloat()
                    maxLocal = p2.y.toFloat()
                }

                MPPointD.recycleInstance(p1)
                MPPointD.recycleInstance(p2)
            }
        }

        computeAxisValues(minLocal, maxLocal)
    }

    /**
     * Sets up the axis values. Computes the desired number of labels between the two given extremes.
     *
     * @return
     */
    protected open fun computeAxisValues(min: Float, max: Float) {
        val yMin = min
        val yMax = max

        val labelCount = mAxis.labelCount
        val range = abs((yMax - yMin).toDouble())

        if (labelCount == 0 || range <= 0 || java.lang.Double.isInfinite(range)) {
            mAxis.mEntries = floatArrayOf()
            mAxis.mCenteredEntries = floatArrayOf()
            mAxis.mEntryCount = 0
            return
        }

        // Find out how much spacing (in y value space) between axis values
        val rawInterval = range / labelCount
        var interval = Utils.roundToNextSignificant(rawInterval).toDouble()

        // If granularity is enabled, then do not allow the interval to go below specified granularity.
        // This is used to avoid repeated values when rounding values for display.
        if (mAxis.isGranularityEnabled) interval = if (interval < mAxis.granularity) mAxis.granularity.toDouble() else interval

        // Normalize interval
        val intervalMagnitude = Utils.roundToNextSignificant(10.0.pow(log10(interval).toInt().toDouble())).toDouble()
        val intervalSigDigit = (interval / intervalMagnitude).toInt()
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            // if it's 0.0 after floor(), we use the old value
            interval = if (floor(10.0 * intervalMagnitude) == 0.0)
                interval
            else floor(10.0 * intervalMagnitude)
        }

        var n = if (mAxis.isCenterAxisLabelsEnabled) 1 else 0

        // force label count
        if (mAxis.isForceLabelsEnabled) {
            interval = (range.toFloat() / (labelCount - 1).toFloat()).toDouble()
            // When force label is enabled
            // If granularity is enabled, then do not allow the interval to go below specified granularity.
            if (mAxis.isGranularityEnabled) interval = if (interval < mAxis.granularity) mAxis.granularity.toDouble() else interval

            mAxis.mEntryCount = labelCount

            // Ensure stops contains at least numStops elements.
            mAxis.mEntries = FloatArray(labelCount)

            var v = min

            for (i in 0..<labelCount) {
                mAxis.mEntries[i] = v
                v += interval.toFloat()
            }

            n = labelCount

            // no forced count
        } else {
            var first = if (interval == 0.0) 0.0 else ceil(yMin / interval) * interval
            if (mAxis.isCenterAxisLabelsEnabled) {
                first -= interval
            }

            val last = if (interval == 0.0) 0.0 else Utils.nextUp(floor(yMax / interval) * interval)

            var f: Double

            if (interval != 0.0 && last != first) {
                f = first
                while (f <= last) {
                    ++n
                    f += interval
                }
            } else if (last == first && n == 0) {
                n = 1
            }

            mAxis.mEntryCount = n

            mAxis.mEntries = FloatArray(n)

            f = first
            var i = 0
            while (i < n) {
                if (f == 0.0)  // Fix for negative zero case (Where value == -0.0, and 0.0 == -0.0)
                    f = 0.0

                mAxis.mEntries[i] = f.toFloat()
                f += interval
                ++i
            }
        }

        // set decimals
        if (interval < 1) {
            mAxis.mDecimals = ceil(-log10(interval)).toInt()
        } else {
            mAxis.mDecimals = 0
        }

        if (mAxis.isCenterAxisLabelsEnabled) {
            if (mAxis.mCenteredEntries.size < n) {
                mAxis.mCenteredEntries = FloatArray(n)
            }

            val offset = interval.toFloat() / 2f

            for (i in 0..<n) {
                mAxis.mCenteredEntries[i] = mAxis.mEntries[i] + offset
            }
        }
    }

    /**
     * Draws the axis labels to the screen.
     *
     * @param c
     */
    abstract fun renderAxisLabels(c: Canvas)

    /**
     * Draws the grid lines belonging to the axis.
     *
     * @param c
     */
    abstract fun renderGridLines(c: Canvas)

    /**
     * Draws the line that goes alongside the axis.
     *
     * @param c
     */
    abstract fun renderAxisLine(c: Canvas)

    /**
     * Draws the LimitLines associated with this axis to the screen.
     *
     * @param c
     */
    abstract fun renderLimitLines(c: Canvas)

    /**
     * Sets the text color to use for the labels. Make sure to use
     * getResources().getColor(...) when using a color from the resources.
     *
     * @param color
     */
    fun setTextColor(color: Int) {
        mAxis.textColor = color
    }
}
