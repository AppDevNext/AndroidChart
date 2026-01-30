package info.appdev.charting.data

import androidx.annotation.ColorInt
import info.appdev.charting.interfaces.datasets.IPieDataSet
import info.appdev.charting.utils.convertDpToPixel

open class PieDataSet(yVals: MutableList<PieEntry>, label: String) : DataSet<PieEntry, Float>(yVals, label), IPieDataSet {
    /**
     * the space in pixels between the chart-slices, default 0f
     */
    private var mSliceSpace = 0f
    private var mIsAutomaticallyDisableSliceSpacing = false

    /**
     * indicates the selection distance of a pie slice
     */
    private var mSelectionShift = 18f

    private var mXValuePosition: ValuePosition? = ValuePosition.INSIDE_SLICE
    private var mYValuePosition: ValuePosition? = ValuePosition.INSIDE_SLICE
    private var mValueLineColor = -0x1000000
    private var mIsUseValueColorForLine = false
    private var mValueLineWidth = 1.0f
    private var mValueLinePart1OffsetPercentage = 75f
    private var mValueLinePart1Length = 0.3f
    private var mValueLinePart2Length = 0.4f
    private var mIsValueLineVariableLength = true

    @ColorInt
    private var mHighlightColor: Int? = null

    override fun copy(): DataSet<PieEntry, Float> {
        val entries: MutableList<PieEntry> = mutableListOf()
        for (i in mEntries.indices) {
            entries.add(mEntries[i].copy())
        }
        val copied = PieDataSet(entries, label)
        return copied
    }

    protected fun copy(pieDataSet: PieDataSet?) {
        super.copy((pieDataSet as BaseDataSet<*, *>?)!!)
    }

    override fun calcMinMax(entry: PieEntry) {
        calcMinMaxY(entry)
    }

    /**
     * Sets the space that is left out between the PieChart-slices in dp.
     * Default: 0 --> no space, maximum 20f
     */
    override var sliceSpace: Float
        get() = mSliceSpace
        set(value) {
            var spaceDp = value
            if (spaceDp > 20) spaceDp = 20f
            if (spaceDp < 0) spaceDp = 0f

            mSliceSpace = spaceDp.convertDpToPixel()
        }

    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     */
    override var isAutomaticallyDisableSliceSpacingEnabled: Boolean
        get() = mIsAutomaticallyDisableSliceSpacing
        set(value) {
            mIsAutomaticallyDisableSliceSpacing = value
        }

    /**
     * sets the distance the highlighted PieChart-slice of this DataSet is
     * "shifted" away from the center of the chart, default 12f
     */
    override var selectionShift: Float
        get() = mSelectionShift
        set(value) {
            mSelectionShift = value
        }
    override var xValuePosition: ValuePosition?
        get() = mXValuePosition
        set(value) {
            mXValuePosition = value
        }
    override var yValuePosition: ValuePosition?
        get() = mYValuePosition
        set(value) {
            mYValuePosition = value
        }
    override var valueLineColor: Int
        get() = mValueLineColor
        set(value) {
            mValueLineColor = value
        }
    override var isUseValueColorForLine: Boolean
        get() = mIsUseValueColorForLine
        set(value) {
            mIsUseValueColorForLine = value
        }
    override var valueLineWidth: Float
        get() = mValueLineWidth
        set(value) {
            mValueLineWidth = value
        }
    override var valueLinePart1OffsetPercentage: Float
        get() = mValueLinePart1OffsetPercentage
        set(value) {
            mValueLinePart1OffsetPercentage = value
        }
    override var valueLinePart1Length: Float
        get() = mValueLinePart1Length
        set(value) {
            mValueLinePart1Length = value
        }
    override var valueLinePart2Length: Float
        get() = mValueLinePart2Length
        set(value) {
            mValueLinePart2Length = value
        }
    override var isValueLineVariableLength: Boolean
        get() = mIsValueLineVariableLength
        set(value) {
            mIsValueLineVariableLength = value
        }
    override var highlightColor: Int?
        get() = mHighlightColor
        set(value) {
            mHighlightColor = value
        }

    enum class ValuePosition {
        INSIDE_SLICE,
        OUTSIDE_SLICE
    }
}
