package com.github.mikephil.charting.components

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.Log
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.utils.Utils
import kotlin.math.abs

/**
 * Base-class of all axes (previously called labels).
 *
 * @author Philipp Jahoda
 */
abstract class AxisBase : ComponentBase() {
    /**
     * custom formatter that is used instead of the auto-formatter if set
     */
    protected var mAxisValueFormatter: IAxisValueFormatter = DefaultAxisValueFormatter(1)

    /**
     * Returns the color of the grid lines for this axis (the horizontal lines
     * coming from each label).
     *
     * @return
     */
    /**
     * Sets the color of the grid lines for this axis (the horizontal lines
     * coming from each label).
     *
     * @param color
     */
    var gridColor: Int = Color.GRAY

    private var mGridLineWidth = 1f

    /**
     * Returns the color of the axis line (line alongside the axis).
     *
     * @return
     */
    /**
     * Sets the color of the border surrounding the chart.
     *
     * @param color
     */
    var axisLineColor: Int = Color.GRAY

    private var mAxisLineWidth = 1f

    /**
     * the actual array of entries
     */
    var mEntries: FloatArray = floatArrayOf()

    /**
     * axis label entries only used for centered labels
     */
    var mCenteredEntries: FloatArray = floatArrayOf()

    /**
     * the number of entries the legend contains
     */
    var mEntryCount: Int = 0

    /**
     * the number of decimal digits to use
     */
    var mDecimals: Int = 0

    /**
     * the number of label entries the axis should have, default 6
     */
    private var mLabelCount = 6

    /**
     * the minimum interval between axis values
     */
    protected var mGranularity: Float = 1.0f

    /**
     * @return true if granularity is enabled
     */
    /**
     * Enabled/disable granularity control on axis value intervals. If enabled, the axis
     * interval is not allowed to go below a certain granularity. Default: false
     *
     * @param enabled
     */
    /**
     * When true, axis labels are controlled by the `granularity` property.
     * When false, axis values could possibly be repeated.
     * This could happen if two adjacent axis values are rounded to same value.
     * If using granularity this could be avoided by having fewer axis values visible.
     */
    var isGranularityEnabled: Boolean = false

    /**
     * Returns true if focing the y-label count is enabled. Default: false
     *
     * @return
     */
    /**
     * if true, the set number of y-labels will be forced
     */
    var isForceLabelsEnabled: Boolean = false
        protected set

    /**
     * Returns true if drawing grid lines is enabled for this axis.
     *
     * @return
     */
    /**
     * flag indicating if the grid lines for this axis should be drawn
     */
    var isDrawGridLinesEnabled: Boolean = true
        protected set

    /**
     * Returns true if the line alongside the axis should be drawn.
     *
     * @return
     */
    /**
     * flag that indicates if the line alongside the axis is drawn or not
     */
    var isDrawAxisLineEnabled: Boolean = true
        protected set

    /**
     * Returns true if drawing the labels is enabled for this axis.
     *
     * @return
     */
    /**
     * flag that indicates of the labels of this axis should be drawn or not
     */
    var isDrawLabelsEnabled: Boolean = true
        protected set

    protected var mCenterAxisLabels: Boolean = false

    /**
     * returns the DashPathEffect that is set for axis line
     *
     * @return
     */
    /**
     * the path effect of the axis line that makes dashed lines possible
     */
    var axisLineDashPathEffect: DashPathEffect? = null
        private set

    /**
     * returns the DashPathEffect that is set for grid line
     *
     * @return
     */
    /**
     * the path effect of the grid lines that makes dashed lines possible
     */
    var gridDashPathEffect: DashPathEffect? = null
        private set

    /**
     * Returns the LimitLines of this axis.
     *
     * @return
     */
    /**
     * array of limit lines that can be set for the axis
     */
    var limitLines: MutableList<LimitLine>
        protected set

    /**
     * Returns the LimitRanges of this axis.
     *
     * @return
     */
    /**
     * array of limit ranges that can be set for the axis
     */
    var limitRanges: MutableList<LimitRange>
        protected set

    /**
     * flag indicating the limit lines layer depth
     */
    var isDrawLimitLinesBehindDataEnabled: Boolean = false
        protected set

    /**
     * flag indicating the grid lines layer depth
     */
    var isDrawGridLinesBehindDataEnabled: Boolean = true
        protected set

    /**
     * Gets extra spacing for `axisMinimum` to be added to automatically calculated `axisMinimum`
     */
    /**
     * Sets extra spacing for `axisMinimum` to be added to automatically calculated `axisMinimum`
     */
    /**
     * Extra spacing for `axisMinimum` to be added to automatically calculated `axisMinimum`
     */
    var spaceMin: Float = 0f

    /**
     * Gets extra spacing for `axisMaximum` to be added to automatically calculated `axisMaximum`
     */
    /**
     * Sets extra spacing for `axisMaximum` to be added to automatically calculated `axisMaximum`
     */
    /**
     * Extra spacing for `axisMaximum` to be added to automatically calculated `axisMaximum`
     */
    var spaceMax: Float = 0f

    /**
     * Returns true if the axis min value has been customized (and is not calculated automatically)
     *
     * @return
     */
    /**
     * flag indicating that the axis-min value has been customized
     */
    var isAxisMinCustom: Boolean = false
        protected set

    /**
     * Returns true if the axis max value has been customized (and is not calculated automatically)
     *
     * @return
     */
    /**
     * flag indicating that the axis-max value has been customized
     */
    var isAxisMaxCustom: Boolean = false
        protected set

    /**
     * don't touch this direclty, use setter
     */
    var mAxisMaximum: Float = 0f

    /**
     * don't touch this directly, use setter
     */
    var mAxisMinimum: Float = 0f

    /**
     * the total range of values this axis covers
     */
    var mAxisRange: Float = 0f

    private var mAxisMinLabels = 2
    private var mAxisMaxLabels = 25

    var axisMinLabels: Int
        /**
         * The minumum number of labels on the axis
         */
        get() = mAxisMinLabels
        /**
         * The minumum number of labels on the axis
         */
        set(labels) {
            if (labels > 0) mAxisMinLabels = labels
        }

    var axisMaxLabels: Int
        /**
         * The maximum number of labels on the axis
         */
        get() = mAxisMaxLabels
        /**
         * The maximum number of labels on the axis
         */
        set(labels) {
            if (labels > 0) mAxisMaxLabels = labels
        }

    /**
     * if set to true, labels and lines will be displayed at the specific positions passed in via setSpecificPositions
     */
    /**
     * if true, then labels and lines are displayed using specificPositions instead of computed ones
     */
    var isShowSpecificPositions: Boolean = false

    /**
     * specify to which values labels and lines must be displayed. has no effect if not used showSpecificPositions set to true
     */
    var specificPositions: FloatArray = floatArrayOf()

    /**
     * default constructor
     */
    init {
        this.mTextSize = Utils.convertDpToPixel(10f)
        this.mXOffset = Utils.convertDpToPixel(5f)
        this.mYOffset = Utils.convertDpToPixel(5f)
        this.limitLines = ArrayList<LimitLine>()
        this.limitRanges = ArrayList<LimitRange>()
    }

    /**
     * Set this to true to enable drawing the grid lines for this axis.
     *
     * @param enabled
     */
    fun setDrawGridLines(enabled: Boolean) {
        this.isDrawGridLinesEnabled = enabled
    }

    /**
     * Set this to true if the line alongside the axis should be drawn or not.
     *
     * @param enabled
     */
    fun setDrawAxisLine(enabled: Boolean) {
        this.isDrawAxisLineEnabled = enabled
    }

    /**
     * Centers the axis labels instead of drawing them at their original position.
     * This is useful especially for grouped BarChart.
     *
     * @param enabled
     */
    fun setCenterAxisLabels(enabled: Boolean) {
        mCenterAxisLabels = enabled
    }

    val isCenterAxisLabelsEnabled: Boolean
        get() = mCenterAxisLabels && mEntryCount > 0

    var axisLineWidth: Float
        /**
         * Returns the width of the axis line (line alongside the axis).
         *
         * @return
         */
        get() = mAxisLineWidth
        /**
         * Sets the width of the border surrounding the chart in dp.
         *
         * @param width
         */
        set(width) {
            mAxisLineWidth = Utils.convertDpToPixel(width)
        }

    var gridLineWidth: Float
        /**
         * Returns the width of the grid lines that are drawn away from each axis
         * label.
         *
         * @return
         */
        get() = mGridLineWidth
        /**
         * Sets the width of the grid lines that are drawn away from each axis
         * label.
         *
         * @param width
         */
        set(width) {
            mGridLineWidth = Utils.convertDpToPixel(width)
        }

    /**
     * Set this to true to enable drawing the labels of this axis (this will not
     * affect drawing the grid lines or axis lines).
     *
     * @param enabled
     */
    fun setDrawLabels(enabled: Boolean) {
        this.isDrawLabelsEnabled = enabled
    }

    /**
     * sets the number of label entries for the y-axis max = 25, min = 2, default: 6, be aware
     * that this number is not
     * fixed (if force == false) and can only be approximated.
     *
     * @param count the number of y-axis labels that should be displayed
     * @param force if enabled, the set label count will be forced, meaning that the exact
     * specified count of labels will
     * be drawn and evenly distributed alongside the axis - this might cause labels
     * to have uneven values
     */
    fun setLabelCount(count: Int, force: Boolean) {
        this.labelCount = count
        this.isForceLabelsEnabled = force
    }

    var labelCount: Int
        /**
         * Returns the number of label entries the y-axis should have
         *
         * @return
         */
        get() = mLabelCount
        /**
         * Sets the number of label entries for the y-axis max = 25, min = 2, default: 6, be aware
         * that this number is not fixed.
         *
         * @param count the number of y-axis labels that should be displayed
         */
        set(count) {
            var count = count
            if (count > this.axisMaxLabels) count = this.axisMaxLabels
            if (count < this.axisMinLabels) count = this.axisMinLabels

            mLabelCount = count
            this.isForceLabelsEnabled = false
        }

    var granularity: Float
        /**
         * @return the minimum interval between axis values
         */
        get() = mGranularity
        /**
         * Set a minimum interval for the axis when zooming in. The axis is not allowed to go below
         * that limit. This can be used to avoid label duplicating when zooming in.
         *
         * @param granularity
         */
        set(granularity) {
            mGranularity = granularity
            // set this to true if it was disabled, as it makes no sense to call this method with granularity disabled
            this.isGranularityEnabled = true
        }

    /**
     * Adds a new LimitLine to this axis.
     *
     * @param l
     */
    fun addLimitLine(l: LimitLine) {
        limitLines.add(l)

        if (limitLines.size > 6) {
            Log.e(
                "MPAndroiChart",
                "Warning! You have more than 6 LimitLines on your axis, do you really want that?"
            )
        }
    }

    /**
     * Adds a new LimitLine to this axis.
     */
    fun addLimitRange(l: LimitRange) {
        limitRanges.add(l)

        if (limitRanges.size > 6) {
            Log.e(
                "MPAndroiChart",
                "Warning! You have more than 6 LimitLines on your axis, do you really want that?"
            )
        }
    }

    /**
     * Removes the specified LimitLine from the axis.
     *
     * @param l
     */
    fun removeLimitLine(l: LimitLine?) {
        limitLines.remove(l)
    }

    /**
     * Removes all LimitLines from the axis.
     */
    fun removeAllLimitLines() {
        limitLines.clear()
    }

    /**
     * Removes the specified LimitRange from the axis.
     *
     * @param l
     */
    fun removeLimitRange(l: LimitRange?) {
        limitRanges.remove(l)
    }

    /**
     * Removes all LimitLines from the axis.
     */
    fun removeAllLimitRanges() {
        limitRanges.clear()
    }

    /**
     * If this is set to true, the LimitLines are drawn behind the actual data,
     * otherwise on top. Default: false
     *
     * @param enabled
     */
    fun setDrawLimitLinesBehindData(enabled: Boolean) {
        this.isDrawLimitLinesBehindDataEnabled = enabled
    }

    /**
     * If this is set to false, the grid lines are draw on top of the actual data,
     * otherwise behind. Default: true
     *
     * @param enabled
     */
    fun setDrawGridLinesBehindData(enabled: Boolean) {
        this.isDrawGridLinesBehindDataEnabled = enabled
    }

    val longestLabel: String
        /**
         * Returns the longest formatted label (in terms of characters), this axis
         * contains.
         *
         * @return
         */
        get() {
            var longest: String? = ""

            for (i in mEntries.indices) {
                val text = getFormattedLabel(i)

                if (text != null && longest!!.length < text.length) longest = text
            }

            return longest!!
        }

    /**
     * Returns the longest formatted label (in terms of px), this axis
     * contains.
     * If paint is null, then returns the longest formatted label (in terms of characters), this axis contains.
     *
     * @return
     */
    fun getLongestLabel(p: Paint?): String {
        if (p == null) {
            return this.longestLabel
        }
        var longest: String? = ""
        val max = 0f

        for (i in mEntries.indices) {
            val text = getFormattedLabel(i)
            if (text != null) {
                val width = p.measureText(text)
                if (max < width) {
                    longest = text
                }
            }
        }

        return longest!!
    }

    fun getFormattedLabel(index: Int): String {
        return if (index < 0 || index >= mEntries.size) ""
        else this.valueFormatter.getFormattedValue(mEntries[index], this)
    }

    var valueFormatter: IAxisValueFormatter
        /**
         * Returns the formatter used for formatting the axis labels.
         *
         * @return
         */
        get() {
            if (mAxisValueFormatter is DefaultAxisValueFormatter &&
                (mAxisValueFormatter as DefaultAxisValueFormatter).decimalDigits != mDecimals
            ) mAxisValueFormatter = DefaultAxisValueFormatter(mDecimals)

            return mAxisValueFormatter
        }
        /**
         * Sets the formatter to be used for formatting the axis labels. If no formatter is set, the
         * chart will
         * automatically determine a reasonable formatting (concerning decimals) for all the values
         * that are drawn inside
         * the chart. Use chart.getDefaultValueFormatter() to use the formatter calculated by the chart.
         *
         * @param f
         */
        set(f) {
            mAxisValueFormatter = f
        }

    /**
     * Enables the grid line to be drawn in dashed mode, e.g. like this
     * "- - - - - -". THIS ONLY WORKS IF HARDWARE-ACCELERATION IS TURNED OFF.
     * Keep in mind that hardware acceleration boosts performance.
     *
     * @param lineLength  the length of the line pieces
     * @param spaceLength the length of space in between the pieces
     * @param phase       offset, in degrees (normally, use 0)
     */
    fun enableGridDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
        this.gridDashPathEffect = DashPathEffect(
            floatArrayOf(
                lineLength, spaceLength
            ), phase
        )
    }

    /**
     * Enables the grid line to be drawn in dashed mode, e.g. like this
     * "- - - - - -". THIS ONLY WORKS IF HARDWARE-ACCELERATION IS TURNED OFF.
     * Keep in mind that hardware acceleration boosts performance.
     *
     * @param effect the DashPathEffect
     */
    fun setGridDashedLine(effect: DashPathEffect?) {
        this.gridDashPathEffect = effect
    }

    /**
     * Disables the grid line to be drawn in dashed mode.
     */
    fun disableGridDashedLine() {
        this.gridDashPathEffect = null
    }

    val isGridDashedLineEnabled: Boolean
        /**
         * Returns true if the grid dashed-line effect is enabled, false if not.
         *
         * @return
         */
        get() = this.gridDashPathEffect != null


    /**
     * Enables the axis line to be drawn in dashed mode, e.g. like this
     * "- - - - - -". THIS ONLY WORKS IF HARDWARE-ACCELERATION IS TURNED OFF.
     * Keep in mind that hardware acceleration boosts performance.
     *
     * @param lineLength  the length of the line pieces
     * @param spaceLength the length of space in between the pieces
     * @param phase       offset, in degrees (normally, use 0)
     */
    fun enableAxisLineDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
        this.axisLineDashPathEffect = DashPathEffect(
            floatArrayOf(
                lineLength, spaceLength
            ), phase
        )
    }

    /**
     * Enables the axis line to be drawn in dashed mode, e.g. like this
     * "- - - - - -". THIS ONLY WORKS IF HARDWARE-ACCELERATION IS TURNED OFF.
     * Keep in mind that hardware acceleration boosts performance.
     *
     * @param effect the DashPathEffect
     */
    fun setAxisLineDashedLine(effect: DashPathEffect?) {
        this.axisLineDashPathEffect = effect
    }

    /**
     * Disables the axis line to be drawn in dashed mode.
     */
    fun disableAxisLineDashedLine() {
        this.axisLineDashPathEffect = null
    }

    val isAxisLineDashedLineEnabled: Boolean
        /**
         * Returns true if the axis dashed-line effect is enabled, false if not.
         *
         * @return
         */
        get() = this.axisLineDashPathEffect != null

    var axisMaximum: Float
        /**
         * ###### BELOW CODE RELATED TO CUSTOM AXIS VALUES ######
         */
        get() = mAxisMaximum
        /**
         * Set a custom maximum value for this axis. If set, this value will not be calculated
         * automatically depending on
         * the provided data. Use resetAxisMaxValue() to undo this.
         *
         * @param max
         */
        set(max) {
            this.isAxisMaxCustom = true
            mAxisMaximum = max
            this.mAxisRange = abs(max - mAxisMinimum)
        }

    var axisMinimum: Float
        get() = mAxisMinimum
        /**
         * Set a custom minimum value for this axis. If set, this value will not be calculated
         * automatically depending on
         * the provided data. Use resetAxisMinValue() to undo this. Do not forget to call
         * setStartAtZero(false) if you use
         * this method. Otherwise, the axis-minimum value will still be forced to 0.
         *
         * @param min
         */
        set(min) {
            this.isAxisMinCustom = true
            mAxisMinimum = min
            this.mAxisRange = abs(mAxisMaximum - min)
        }

    /**
     * By calling this method, any custom maximum value that has been previously set is reseted,
     * and the calculation is
     * done automatically.
     */
    fun resetAxisMaximum() {
        this.isAxisMaxCustom = false
    }

    /**
     * By calling this method, any custom minimum value that has been previously set is reseted,
     * and the calculation is
     * done automatically.
     */
    fun resetAxisMinimum() {
        this.isAxisMinCustom = false
    }

    /**
     * Use setAxisMinimum(...) instead.
     *
     * @param min
     */
    @Deprecated("")
    fun setAxisMinValue(min: Float) {
        this.axisMinimum = min
    }

    /**
     * Use setAxisMaximum(...) instead.
     *
     * @param max
     */
    @Deprecated("")
    fun setAxisMaxValue(max: Float) {
        this.axisMaximum = max
    }

    /**
     * Calculates the minimum / maximum  and range values of the axis with the given
     * minimum and maximum values from the chart data.
     *
     * @param dataMin the min value according to chart data
     * @param dataMax the max value according to chart data
     */
    open fun calculate(dataMin: Float, dataMax: Float) {
        // if custom, use value as is, else use data value

        var min = if (this.isAxisMinCustom) mAxisMinimum else (dataMin - this.spaceMin)
        var max = if (this.isAxisMaxCustom) mAxisMaximum else (dataMax + this.spaceMax)

        // temporary range (before calculations)
        val range = abs(max - min)

        // in case all values are equal
        if (range == 0f) {
            max = max + 1f
            min = min - 1f
        }

        this.mAxisMinimum = min
        this.mAxisMaximum = max

        // actual range
        this.mAxisRange = abs(max - min)
    }
}
