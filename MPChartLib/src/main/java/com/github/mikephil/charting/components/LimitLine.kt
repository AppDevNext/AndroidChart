package com.github.mikephil.charting.components

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.github.mikephil.charting.utils.Utils

/**
 * The limit line is an additional feature for all Line-, Bar- and
 * ScatterCharts. It allows the displaying of an additional line in the chart
 * that marks a certain maximum / limit on the specified axis (x- or y-axis).
 *
 * @author Philipp Jahoda
 */
class LimitLine : ComponentBase {
    /**
     * Returns the limit that is set for this line.
     *
     * @return
     */
    /** limit / maximum (the y-value or xIndex)  */
    var limit: Float = 0f
        private set

    /** the width of the limit line  */
    private var mLineWidth = 2f

    /**
     * Returns the color that is used for this LimitLine
     *
     * @return
     */
    /**
     * Sets the linecolor for this LimitLine. Make sure to use
     * getResources().getColor(...)
     *
     * @param color
     */
    /** the color of the limit line  */
    var lineColor: Int = Color.rgb(237, 91, 91)

    /**
     * Returns the color of the value-text that is drawn next to the LimitLine.
     *
     * @return
     */
    /**
     * Sets the color of the value-text that is drawn next to the LimitLine.
     * Default: Paint.Style.FILL_AND_STROKE
     *
     * @param style
     */
    /** the style of the label text  */
    var textStyle: Paint.Style? = Paint.Style.FILL_AND_STROKE

    /**
     * Returns the label that is drawn next to the limit line.
     *
     * @return
     */
    /**
     * Sets the label that is drawn next to the limit line. Provide "" if no
     * label is required.
     *
     * @param label
     */
    /** label string that is drawn next to the limit line  */
    var label: String? = ""

    /**
     * returns the DashPathEffect that is set for this LimitLine
     *
     * @return
     */
    /** the path effect of this LimitLine that makes dashed lines possible  */
    var dashPathEffect: DashPathEffect? = null
        private set

    /**
     * Returns the position of the LimitLine label (value).
     *
     * @return
     */
    /**
     * Sets the position of the LimitLine value label (either on the right or on
     * the left edge of the chart). Not supported for RadarChart.
     *
     * @param pos
     */
    /** indicates the position of the LimitLine label  */
    var labelPosition: LimitLabelPosition? = LimitLabelPosition.RIGHT_TOP

    /** enum that indicates the position of the LimitLine label  */
    enum class LimitLabelPosition {
        LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

    /**
     * Constructor with limit.
     *
     * @param limit - the position (the value) on the y-axis (y-value) or x-axis
     * (xIndex) where this line should appear
     */
    constructor(limit: Float) {
        this.limit = limit
    }

    /**
     * Constructor with limit and label.
     *
     * @param limit - the position (the value) on the y-axis (y-value) or x-axis
     * (xIndex) where this line should appear
     * @param label - provide "" if no label is required
     */
    constructor(limit: Float, label: String?) {
        this.limit = limit
        this.label = label
    }

    var lineWidth: Float
        /**
         * returns the width of limit line
         *
         * @return
         */
        get() = mLineWidth
        /**
         * set the line width of the chart (min = 0.2f, max = 12f); default 2f NOTE:
         * thinner line == better performance, thicker line == worse performance
         *
         * @param width
         */
        set(width) {
            var width = width
            if (width < 0.2f) width = 0.2f
            if (width > 12.0f) width = 12.0f
            mLineWidth = Utils.convertDpToPixel(width)
        }

    /**
     * Enables the line to be drawn in dashed mode, e.g. like this "- - - - - -"
     *
     * @param lineLength the length of the line pieces
     * @param spaceLength the length of space inbetween the pieces
     * @param phase offset, in degrees (normally, use 0)
     */
    fun enableDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
        this.dashPathEffect = DashPathEffect(
            floatArrayOf(
                lineLength, spaceLength
            ), phase
        )
    }

    /**
     * Disables the line to be drawn in dashed mode.
     */
    fun disableDashedLine() {
        this.dashPathEffect = null
    }

    val isDashedLineEnabled: Boolean
        /**
         * Returns true if the dashed-line effect is enabled, false if not. Default:
         * disabled
         *
         * @return
         */
        get() = this.dashPathEffect != null
}
