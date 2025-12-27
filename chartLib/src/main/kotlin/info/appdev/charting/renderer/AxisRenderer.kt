package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import info.appdev.charting.components.AxisBase
import info.appdev.charting.utils.PointD
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.roundToNextSignificant
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.nextUp
import kotlin.math.pow

/**
 * Baseclass of all axis renderers.
 */
abstract class AxisRenderer(
    viewPortHandler: ViewPortHandler,
    /** transformer to transform values to screen pixels and return  */
    var transformer: Transformer?,
    /** base axis this axis renderer works with  */
    protected var axis: AxisBase
) : Renderer(viewPortHandler) {

    /**
     * paint object for the grid lines
     */
    var paintGrid = Paint().apply {
        color = Color.GRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
        alpha = 90
    }
        protected set

    /**
     * paint for the x-label values
     */
    var paintAxisLabels = Paint(Paint.ANTI_ALIAS_FLAG)
        protected set

    /**
     * paint for the line surrounding the chart
     */
    var paintAxisLine = Paint().apply {
        color = Color.BLACK
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
        protected set

    /**
     * paint used for the limit lines
     */
    protected var limitLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    /**
     * paint used for the limit ranges
     */
    protected var limitRangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    /**
     * paint used for the limit range fill
     */
    protected var limitRangePaintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /**
     * Computes the axis values.
     *
     * @param min - the minimum value in the data object for this axis
     * @param max - the maximum value in the data object for this axis
     */
    open fun computeAxis(min: Float, max: Float, inverted: Boolean) {
        // calculate the starting and entry point of the y-labels (depending on
        // zoom / content rect bounds)

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

                PointD.recycleInstance(p1)
                PointD.recycleInstance(p2)
            }
        }
        computeAxisValues(minLocal, maxLocal)
    }

    /**
     * Sets up the axis values. Computes the desired number of labels between the two given extremes.
     */
    protected open fun computeAxisValues(min: Float, max: Float) {
        val labelCount = axis.labelCount
        val range = abs((max - min).toDouble())

        if (labelCount == 0 || range <= 0 || java.lang.Double.isInfinite(range)) {
            axis.entries = floatArrayOf()
            axis.centeredEntries = floatArrayOf()
            axis.entryCount = 0
            return
        }

        // Find out how much spacing (in y value space) between axis values
        val rawInterval = range / labelCount
        var interval = rawInterval.roundToNextSignificant().toDouble()

        // If granularity is enabled, then do not allow the interval to go below specified granularity.
        // This is used to avoid repeated values when rounding values for display.
        if (axis.isGranularityEnabled) interval = if (interval < axis.granularity) axis.granularity.toDouble() else interval

        // Normalize interval
        val intervalMagnitude = 10.0.pow(log10(interval).toInt().toDouble()).roundToNextSignificant().toDouble()
        val intervalSigDigit = (interval / intervalMagnitude).toInt()
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
            // if it's 0.0 after floor(), we use the old value
            interval = if (floor(10.0 * intervalMagnitude) == 0.0)
                interval
            else floor(10.0 * intervalMagnitude)
        }

        var n = if (axis.isCenterAxisLabelsEnabled) 1 else 0

        // force label count
        if (axis.isForceLabelsEnabled) {
            interval = (range.toFloat() / (labelCount - 1).toFloat()).toDouble()
            // When force label is enabled
            // If granularity is enabled, then do not allow the interval to go below specified granularity.
            if (axis.isGranularityEnabled) interval = if (interval < axis.granularity) axis.granularity.toDouble() else interval

            axis.entryCount = labelCount

            // Ensure stops contains at least numStops elements.
            axis.entries = FloatArray(labelCount)

            var v = min

            for (i in 0..<labelCount) {
                axis.entries[i] = v
                v += interval.toFloat()
            }

            n = labelCount

            // no forced count
        } else {
            var first = if (interval == 0.0) 0.0 else ceil(min / interval) * interval
            if (axis.isCenterAxisLabelsEnabled) {
                first -= interval
            }

            val last = if (interval == 0.0)
                0.0
            else
                (floor(max / interval) * interval).nextUp()

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

            axis.entryCount = n

            axis.entries = FloatArray(n)

            f = first
            var i = 0
            while (i < n) {
                if (f == 0.0)  // Fix for negative zero case (Where value == -0.0, and 0.0 == -0.0)
                    f = 0.0

                axis.entries[i] = f.toFloat()
                f += interval
                ++i
            }
        }

        // set decimals
        if (interval < 1) {
            axis.mDecimals = ceil(-log10(interval)).toInt()
        } else {
            axis.mDecimals = 0
        }

        if (axis.isCenterAxisLabelsEnabled) {
            if (axis.centeredEntries.size < n) {
                axis.centeredEntries = FloatArray(n)
            }

            val offset = interval.toFloat() / 2f

            for (i in 0..<n) {
                axis.centeredEntries[i] = axis.entries[i] + offset
            }
        }
    }

    /**
     * Draws the axis labels to the screen.
     */
    abstract fun renderAxisLabels(canvas: Canvas)

    /**
     * Draws the grid lines belonging to the axis.
     */
    abstract fun renderGridLines(canvas: Canvas)

    /**
     * Draws the line that goes alongside the axis.
     */
    abstract fun renderAxisLine(canvas: Canvas)

    /**
     * Draws the LimitLines associated with this axis to the screen.
     */
    abstract fun renderLimitLines(canvas: Canvas)

    /**
     * Sets the text color to use for the labels. Make sure to use
     * ContextCompat.getColor(context,...) when using a color from the resources.
     */
    fun setTextColor(color: Int) {
        axis.textColor = color
    }
}
