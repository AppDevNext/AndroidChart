package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.utils.convertDpToPixel

class PieDataSet(yVals: MutableList<PieEntry?>?, label: String) : DataSet<PieEntry?>(yVals, label), IPieDataSet {
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

    override fun copy(): DataSet<PieEntry?> {
        val entries: MutableList<PieEntry?> = ArrayList<PieEntry?>()
        for (i in mEntries!!.indices) {
            entries.add(mEntries!!.get(i)!!.copy())
        }
        val copied = PieDataSet(entries, label)
        return copied
    }

    protected fun copy(pieDataSet: PieDataSet?) {
        super.copy((pieDataSet as BaseDataSet<*>?)!!)
    }

    override fun calcMinMax(entry: PieEntry?) {
        if (entry == null) return

        calcMinMaxY(entry)
    }

    /**
     * Sets the space that is left out between the piechart-slices in dp.
     * Default: 0 --> no space, maximum 20f
     */
    fun setSliceSpace(spaceDp: Float) {
        var spaceDp = spaceDp
        if (spaceDp > 20) spaceDp = 20f
        if (spaceDp < 0) spaceDp = 0f

        mSliceSpace = spaceDp.convertDpToPixel()
    }

    override fun getSliceSpace(): Float {
        return mSliceSpace
    }

    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     */
    fun setAutomaticallyDisableSliceSpacing(autoDisable: Boolean) {
        mAutomaticallyDisableSliceSpacing = autoDisable
    }

    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     */
    override fun isAutomaticallyDisableSliceSpacingEnabled(): Boolean {
        return mAutomaticallyDisableSliceSpacing
    }

    /**
     * sets the distance the highlighted piechart-slice of this DataSet is
     * "shifted" away from the center of the chart, default 12f
     */
    fun setSelectionShift(shift: Float) {
        mShift = shift.convertDpToPixel()
    }

    override fun getSelectionShift(): Float {
        return mShift
    }

    override fun getXValuePosition(): ValuePosition? {
        return mXValuePosition
    }

    fun setXValuePosition(xValuePosition: ValuePosition?) {
        this.mXValuePosition = xValuePosition
    }

    override fun getYValuePosition(): ValuePosition? {
        return mYValuePosition
    }

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
         */
        set(enabled) {
            setUseValueColorForLine(enabled)
        }

    /**
     * When valuePosition is OutsideSlice, indicates line color
     */
    override fun getValueLineColor(): Int {
        return mValueLineColor
    }

    fun setValueLineColor(valueLineColor: Int) {
        this.mValueLineColor = valueLineColor
    }

    override fun isUseValueColorForLineEnabled(): Boolean {
        return mUseValueColorForLine
    }

    fun setUseValueColorForLine(enabled: Boolean) {
        mUseValueColorForLine = enabled
    }

    /**
     * When valuePosition is OutsideSlice, indicates line width
     */
    override fun getValueLineWidth(): Float {
        return mValueLineWidth
    }

    fun setValueLineWidth(valueLineWidth: Float) {
        this.mValueLineWidth = valueLineWidth
    }

    /**
     * When valuePosition is OutsideSlice, indicates offset as percentage out of the slice size
     */
    override fun getValueLinePart1OffsetPercentage(): Float {
        return mValueLinePart1OffsetPercentage
    }

    fun setValueLinePart1OffsetPercentage(valueLinePart1OffsetPercentage: Float) {
        this.mValueLinePart1OffsetPercentage = valueLinePart1OffsetPercentage
    }

    /**
     * When valuePosition is OutsideSlice, indicates length of first half of the line
     */
    override fun getValueLinePart1Length(): Float {
        return mValueLinePart1Length
    }

    fun setValueLinePart1Length(valueLinePart1Length: Float) {
        this.mValueLinePart1Length = valueLinePart1Length
    }

    /**
     * When valuePosition is OutsideSlice, indicates length of second half of the line
     */
    override fun getValueLinePart2Length(): Float {
        return mValueLinePart2Length
    }

    fun setValueLinePart2Length(valueLinePart2Length: Float) {
        this.mValueLinePart2Length = valueLinePart2Length
    }

    /**
     * When valuePosition is OutsideSlice, this allows variable line length
     */
    override fun isValueLineVariableLength(): Boolean {
        return mValueLineVariableLength
    }

    fun setValueLineVariableLength(valueLineVariableLength: Boolean) {
        this.mValueLineVariableLength = valueLineVariableLength
    }

    /** Gets the color for the highlighted sector  */
    override fun getHighlightColor(): Int? {
        return mHighlightColor
    }

    /** Sets the color for the highlighted sector (null for using entry color)  */
    fun setHighlightColor(color: Int?) {
        this.mHighlightColor = color
    }


    enum class ValuePosition {
        INSIDE_SLICE,
        OUTSIDE_SLICE
    }
}
