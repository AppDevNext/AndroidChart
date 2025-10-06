package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.utils.Utils

open class PieDataSet(yVals: MutableList<PieEntry>, label: String) : DataSet<PieEntry>(yVals, label), IPieDataSet {
    /**
     * the space in pixels between the chart-slices, default 0f
     */
    private var mSliceSpace = 0f
    private var mAutomaticallyDisableSliceSpacing = false

    /**
     * indicates the selection distance of a pie slice
     */
    private var mShift = 18f

    private var mXValuePosition: ValuePosition? = ValuePosition.INSIDE_SLICE
    private var mYValuePosition: ValuePosition? = ValuePosition.INSIDE_SLICE
    private var mValueLineColor = -0x1000000
    private var mUseValueColorForLine = false
    private var mValueLineWidth = 1.0f
    private var mValueLinePart1OffsetPercentage = 75f
    private var mValueLinePart1Length = 0.3f
    private var mValueLinePart2Length = 0.4f
    private var mValueLineVariableLength = true
    private var mHighlightColor: Int? = null

    override fun copy(): DataSet<PieEntry> {
        val entries: MutableList<PieEntry> = ArrayList()
        for (i in mEntries.indices) {
            entries.add(mEntries[i].copy())
        }
        val copied = PieDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(pieDataSet: PieDataSet) {
        super.copy(pieDataSet)
    }

    override fun calcMinMax(e: PieEntry) {
        calcMinMaxY(e)
    }

    /**
     * Sets the space that is left out between the piechart-slices in dp.
     * Default: 0 --> no space, maximum 20f
     *
     * @param spaceDp
     */
    fun setSliceSpace(spaceDp: Float) {
        var spaceDp = spaceDp
        if (spaceDp > 20) spaceDp = 20f
        if (spaceDp < 0) spaceDp = 0f

        mSliceSpace = Utils.convertDpToPixel(spaceDp)
    }

    override val sliceSpace: Float
        get() = mSliceSpace

    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     *
     * @param autoDisable
     */
    fun setAutomaticallyDisableSliceSpacing(autoDisable: Boolean) {
        mAutomaticallyDisableSliceSpacing = autoDisable
    }

    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     *
     * @return
     */
    override val isAutomaticallyDisableSliceSpacingEnabled: Boolean
        get() = mAutomaticallyDisableSliceSpacing

    /**
     * sets the distance the highlighted piechart-slice of this DataSet is
     * "shifted" away from the center of the chart, default 12f
     *
     * @param shift
     */
    fun setSelectionShift(shift: Float) {
        mShift = Utils.convertDpToPixel(shift)
    }

    override val selectionShift: Float
        get() = mShift

    override val xValuePosition: ValuePosition?
        get() = mXValuePosition

    fun setXValuePosition(xValuePosition: ValuePosition?) {
        this.mXValuePosition = xValuePosition
    }

    override val yValuePosition: ValuePosition?
        get() = mYValuePosition

    fun setYValuePosition(yValuePosition: ValuePosition?) {
        this.mYValuePosition = yValuePosition
    }

    @get:Deprecated("")
    @set:Deprecated("")
    var isUsingSliceColorAsValueLineColor: Boolean
        /**
         * This method is deprecated.
         * Use isUseValueColorForLineEnabled() instead.
         */
        get() = isUseValueColorForLineEnabled
        /**
         * This method is deprecated.
         * Use setUseValueColorForLine(...) instead.
         *
         * @param enabled
         */
        set(enabled) {
            setUseValueColorForLine(enabled)
        }

    /**
     * When valuePosition is OutsideSlice, indicates line color
     */
    override val valueLineColor: Int
        get() = mValueLineColor

    fun setValueLineColor(valueLineColor: Int) {
        this.mValueLineColor = valueLineColor
    }

    override val isUseValueColorForLineEnabled: Boolean
        get() = mUseValueColorForLine

    fun setUseValueColorForLine(enabled: Boolean) {
        mUseValueColorForLine = enabled
    }

    /**
     * When valuePosition is OutsideSlice, indicates line width
     */
    override val valueLineWidth: Float
        get() = mValueLineWidth

    fun setValueLineWidth(valueLineWidth: Float) {
        this.mValueLineWidth = valueLineWidth
    }

    /**
     * When valuePosition is OutsideSlice, indicates offset as percentage out of the slice size
     */
    override val valueLinePart1OffsetPercentage: Float
        get() = mValueLinePart1OffsetPercentage

    fun setValueLinePart1OffsetPercentage(valueLinePart1OffsetPercentage: Float) {
        this.mValueLinePart1OffsetPercentage = valueLinePart1OffsetPercentage
    }

    /**
     * When valuePosition is OutsideSlice, indicates length of first half of the line
     */
    override val valueLinePart1Length: Float
        get() = mValueLinePart1Length

    fun setValueLinePart1Length(valueLinePart1Length: Float) {
        this.mValueLinePart1Length = valueLinePart1Length
    }

    /**
     * When valuePosition is OutsideSlice, indicates length of second half of the line
     */
    override val valueLinePart2Length: Float
        get() = mValueLinePart2Length

    fun setValueLinePart2Length(valueLinePart2Length: Float) {
        this.mValueLinePart2Length = valueLinePart2Length
    }

    /**
     * When valuePosition is OutsideSlice, this allows variable line length
     */
    override val isValueLineVariableLength: Boolean
        get() = mValueLineVariableLength

    fun setValueLineVariableLength(valueLineVariableLength: Boolean) {
        this.mValueLineVariableLength = valueLineVariableLength
    }

    /** Gets the color for the highlighted sector  */
    override val highlightColor: Int?
        get() = mHighlightColor

    /** Sets the color for the highlighted sector (null for using entry color)  */
    fun setHighlightColor(color: Int?) {
        this.mHighlightColor = color
    }


    enum class ValuePosition {
        INSIDE_SLICE,
        OUTSIDE_SLICE
    }
}
