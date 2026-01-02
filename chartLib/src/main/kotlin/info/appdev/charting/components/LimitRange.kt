package info.appdev.charting.components

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.annotation.ColorInt
import info.appdev.charting.components.LimitLine.LimitLabelPosition
import info.appdev.charting.utils.convertDpToPixel

/**
 * The limit line is an additional feature for all Line-, Bar- and
 * ScatterCharts. It allows the displaying of an additional line in the chart
 * that marks a certain maximum / limit on the specified axis (x- or y-axis).
 */
class LimitRange : ComponentBase {
    class Range internal constructor(r1: Float, r2: Float) {
        val low: Float
        val high: Float

        init {
            if (r1 < r2) {
                this.low = r1
                this.high = r2
            } else {
                this.low = r2
                this.high = r1
            }
        }
    }

    /**
     * limit / maximum (the y-value or xIndex)
     */
    val limit: Range

    /**
     * the width of the limit line
     */
    private var mLineWidth = 0f

    /**
     * the color of the limit line
     */
    @ColorInt
    var lineColor: Int = Color.rgb(237, 91, 91)

    /**
     * the color of the Range
     */
    @ColorInt
    var rangeColor: Int = Color.rgb(128, 128, 128)

    /**
     * Sets the color of the value-text that is drawn next to the LimitLine.
     * Default: Paint.Style.FILL_AND_STROKE
     */
    var textStyle: Paint.Style? = Paint.Style.FILL

    /**
     * Sets the label that is drawn next to the limit line. Provide "" if no
     * label is required.
     */
    var label: String? = ""

    /**
     * the path effect of this LimitLine that makes dashed lines possible
     */
    var dashPathEffect: DashPathEffect? = null
        private set

    /**
     * Sets the position of the LimitLine value label (either on the right or on
     * the left edge of the chart). Not supported for RadarChart.
     */
    var labelPosition: LimitLabelPosition? = LimitLabelPosition.RIGHT_TOP

    /**
     * Constructor with limit.
     *
     * @param firstLimit  - the position (the value) on the y-axis (y-value) or x-axis
     * (xIndex) where this line should appear
     * @param secondLimit - the position (the value) on the y-axis (y-value) or x-axis
     * (xIndex) where this line should appear
     */
    constructor(firstLimit: Float, secondLimit: Float) {
        this.limit = Range(firstLimit, secondLimit)
    }

    /**
     * Constructor with limit and label.
     *
     * @param firstLimit  - the position (the value) on the y-axis (y-value) or x-axis
     * (xIndex) where this line should appear
     * @param secondLimit - the position (the value) on the y-axis (y-value) or x-axis
     * (xIndex) where this line should appear
     * @param label       - provide "" if no label is required
     */
    constructor(firstLimit: Float, secondLimit: Float, label: String?) {
        this.limit = Range(firstLimit, secondLimit)
        this.label = label
    }

    /**
     * set the line width of the chart (min = 0.2f, max = 12f); default 2f NOTE:
     * thinner line == better performance, thicker line == worse performance
     */
    var lineWidth: Float
        get() = mLineWidth
        set(value) {
            var width = value
            if (width > 12.0f) {
                width = 12.0f
            }
            mLineWidth = width.convertDpToPixel()
        }

    /**
     * Enables the line to be drawn in dashed mode, e.g. like this "- - - - - -"
     *
     * @param lineLength  the length of the line pieces
     * @param spaceLength the length of space inbetween the pieces
     * @param phase       offset, in degrees (normally, use 0)
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

    /**
     * Returns true if the dashed-line effect is enabled, false if not. Default:
     * disabled
     */
    val isDashedLineEnabled: Boolean
        get() = this.dashPathEffect != null
}
