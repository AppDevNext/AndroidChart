package com.github.mikephil.charting.components

import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.utils.calcTextHeight
import com.github.mikephil.charting.utils.calcTextWidth
import com.github.mikephil.charting.utils.convertDpToPixel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Class representing the y-axis labels settings and its entries. Only use the setter methods to
 * modify it. Do not access public variables directly. Be aware that not all features the YLabels class provides
 * are suitable for the RadarChart. Customizations that affect the value range of the axis need to be applied before
 * setting data for the chart.
 */
class YAxis : AxisBase {
    /**
     * returns true if drawing the bottom y-axis label entry is enabled
     */
    /**
     * indicates if the bottom y-label entry is drawn or not
     */
    val isDrawBottomYLabelEntryEnabled: Boolean = true

    /**
     * returns true if drawing the top y-axis label entry is enabled
     */
    /**
     * indicates if the top y-label entry is drawn or not
     */
    var isDrawTopYLabelEntryEnabled: Boolean = true
        private set

    /**
     * If this returns true, the y-axis is inverted.
     */
    /**
     * If this is set to true, the y-axis is inverted which means that low values are on top of
     * the chart, high values
     * on bottom.
     */
    /**
     * flag that indicates if the axis is inverted or not
     */
    var isInverted: Boolean = false

    /**
     * flag that indicates if the zero-line should be drawn regardless of other grid lines
     */
    var isDrawZeroLineEnabled: Boolean = false
        protected set

    /**
     * Returns true if autoscale restriction for axis min value is enabled
     */
    /**
     * Sets autoscale restriction for axis min value as enabled/disabled
     */
    /**
     * flag indicating that auto scale min restriction should be used
     */
    @get:Deprecated("")
    @set:Deprecated("")
    var isUseAutoScaleMinRestriction: Boolean = false

    /**
     * Returns true if autoscale restriction for axis max value is enabled
     */
    /**
     * Sets autoscale restriction for axis max value as enabled/disabled
     */
    /**
     * flag indicating that auto scale max restriction should be used
     */
    @get:Deprecated("")
    @set:Deprecated("")
    var isUseAutoScaleMaxRestriction: Boolean = false

    /**
     * Sets the color of the zero line
     */
    /**
     * Color of the zero line
     */
    var zeroLineColor: Int = Color.GRAY

    /**
     * Width of the zero line in pixels
     */
    protected var mZeroLineWidth: Float = 1f

    /**
     * Returns the top axis space in percent of the full range. Default 10f
     */
    /**
     * Sets the top axis space in percent of the full range. Default 10f
     */
    /**
     * axis space from the largest value to the top in percent of the total axis range
     */
    var spaceTop: Float = 10f

    /**
     * Returns the bottom axis space in percent of the full range. Default 10f
     */
    /**
     * Sets the bottom axis space in percent of the full range. Default 10f
     */
    /**
     * axis space from the smallest value to the bottom in percent of the total axis range
     */
    var spaceBottom: Float = 10f

    /**
     * returns the position of the y-labels
     */
    /**
     * the position of the y-labels relative to the chart
     */
    var labelPosition: YAxisLabelPosition? = YAxisLabelPosition.OUTSIDE_CHART
        private set

    /**
     * returns the horizontal offset of the y-label
     */
    /**
     * sets the horizontal offset of the y-label
     */
    /**
     * the horizontal offset of the y-label
     */
    var labelXOffset: Float = 0.0f

    /**
     * enum for the position of the y-labels relative to the chart
     */
    enum class YAxisLabelPosition {
        OUTSIDE_CHART, INSIDE_CHART
    }

    /**
     * the side this axis object represents
     */
    val axisDependency: AxisDependency?

    /**
     * @return the minimum width that the axis should take (in dp).
     */
    /**
     * Sets the minimum width that the axis should take (in dp).
     */
    /**
     * the minimum width that the axis should take (in dp).
     *
     *
     * default: 0.0
     */
    var minWidth: Float = 0f

    /**
     * @return the maximum width that the axis can take (in dp).
     */
    /**
     * Sets the maximum width that the axis can take (in dp).
     */
    /**
     * the maximum width that the axis can take (in dp).
     * use Inifinity for disabling the maximum
     * default: Float.POSITIVE_INFINITY (no maximum specified)
     */
    var maxWidth: Float = Float.POSITIVE_INFINITY

    /**
     * Enum that specifies the axis a DataSet should be plotted against, either LEFT or RIGHT.
     *
     * @author Philipp Jahoda
     */
    enum class AxisDependency {
        LEFT, RIGHT
    }

    constructor() : super() {
        // default left
        this.axisDependency = AxisDependency.LEFT
        this.mYOffset = 0f
    }

    constructor(position: AxisDependency?) : super() {
        this.axisDependency = position
        this.mYOffset = 0f
    }

    /**
     * sets the position of the y-labels
     */
    fun setPosition(pos: YAxisLabelPosition?) {
        this.labelPosition = pos
    }

    /**
     * set this to true to enable drawing the top y-label entry. Disabling this can be helpful
     * when the top y-label and
     * left x-label interfere with each other. default: true
     */
    fun setDrawTopYLabelEntry(enabled: Boolean) {
        this.isDrawTopYLabelEntryEnabled = enabled
    }

    /**
     * This method is deprecated.
     * Use setAxisMinimum(...) / setAxisMaximum(...) instead.
     */
    @Deprecated("")
    fun setStartAtZero(startAtZero: Boolean) {
        if (startAtZero) axisMinimum = 0f
        else resetAxisMinimum()
    }

    /**
     * Set this to true to draw the zero-line regardless of weather other
     * grid-lines are enabled or not. Default: false
     */
    fun setDrawZeroLine(mDrawZeroLine: Boolean) {
        this.isDrawZeroLineEnabled = mDrawZeroLine
    }

    var zeroLineWidth: Float
        get() = mZeroLineWidth
        /**
         * Sets the width of the zero line in dp
         */
        set(width) {
            this.mZeroLineWidth = width.convertDpToPixel()
        }

    /**
     * This is for normal (not horizontal) charts horizontal spacing.
     */
    fun getRequiredWidthSpace(p: Paint): Float {
        p.textSize = mTextSize

        val label = getLongestLabel(p)
        var width = p.calcTextWidth(label).toFloat() + xOffset * 2f

        var minWidth = this.minWidth
        var maxWidth = this.maxWidth

        if (minWidth > 0f) minWidth = minWidth.convertDpToPixel()

        if (maxWidth > 0f && maxWidth != Float.POSITIVE_INFINITY) maxWidth = maxWidth.convertDpToPixel()

        width = max(minWidth, min(width, if (maxWidth > 0.0) maxWidth else width))

        return width
    }

    /**
     * This is for HorizontalBarChart vertical spacing.
     */
    fun getRequiredHeightSpace(p: Paint): Float {
        p.textSize = mTextSize

        val label = getLongestLabel(p)
        return p.calcTextHeight(label).toFloat() + yOffset * 2f
    }

    /**
     * Returns true if this axis needs horizontal offset, false if no offset is needed.
     */
    fun needsOffset(): Boolean {
        return isEnabled && isDrawLabelsEnabled && this.labelPosition == YAxisLabelPosition.OUTSIDE_CHART
    }


    override fun calculate(dataMin: Float, dataMax: Float) {
        var min = dataMin
        var max = dataMax

        // Make sure max is greater than min
        // Discussion: https://github.com/danielgindi/Charts/pull/3650#discussion_r221409991
        if (min > max) {
            if (isAxisMaxCustom && isAxisMinCustom) {
                val t = min
                min = max
                max = t
            } else if (isAxisMaxCustom) {
                min = if (max < 0f) max * 1.5f else max * 0.5f
            } else if (isAxisMinCustom) {
                max = if (min < 0f) min * 0.5f else min * 1.5f
            }
        }

        var range = abs(max - min)

        // in case all values are equal
        if (range == 0f) {
            max = max + 1f
            min = min - 1f
        }

        // recalculate
        range = abs(max - min)

        // calc extra spacing
        this.mAxisMinimum = if (isAxisMinCustom) this.mAxisMinimum else min - (range / 100f) * this.spaceBottom
        this.mAxisMaximum = if (isAxisMaxCustom) this.mAxisMaximum else max + (range / 100f) * this.spaceTop

        this.mAxisRange = abs(this.mAxisMinimum - this.mAxisMaximum)
    }
}
