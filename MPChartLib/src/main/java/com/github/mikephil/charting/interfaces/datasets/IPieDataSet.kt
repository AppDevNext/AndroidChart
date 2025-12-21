package com.github.mikephil.charting.interfaces.datasets

import com.github.mikephil.charting.data.PieDataSet.ValuePosition
import com.github.mikephil.charting.data.PieEntry

interface IPieDataSet : IDataSet<PieEntry> {
    /**
     * Returns the space that is set to be between the piechart-slices of this
     * DataSet, in pixels.
     */
    val sliceSpace: Float

    /**
     * When enabled, slice spacing will be 0.0 when the smallest value is going to be
     * smaller than the slice spacing itself.
     */
    val isAutomaticallyDisableSliceSpacingEnabled: Boolean

    /**
     * Returns the distance a highlighted piechart slice is "shifted" away from
     * the chart-center in dp.
     */
    val selectionShift: Float

    val xValuePosition: ValuePosition?

    val yValuePosition: ValuePosition?

    /**
     * When valuePosition is OutsideSlice, indicates line color
     */
    val valueLineColor: Int

    /**
     * When valuePosition is OutsideSlice and enabled, line will have the same color as the slice
     */
    val isUseValueColorForLineEnabled: Boolean

    /**
     * When valuePosition is OutsideSlice, indicates line width
     */
    val valueLineWidth: Float

    /**
     * When valuePosition is OutsideSlice, indicates offset as percentage out of the slice size
     */
    val valueLinePart1OffsetPercentage: Float

    /**
     * When valuePosition is OutsideSlice, indicates length of first half of the line
     */
    val valueLinePart1Length: Float

    /**
     * When valuePosition is OutsideSlice, indicates length of second half of the line
     */
    val valueLinePart2Length: Float

    /**
     * When valuePosition is OutsideSlice, this allows variable line length
     */
    val isValueLineVariableLength: Boolean

    /**
     * Gets the color for the highlighted sector
     */
    val highlightColor: Int?
}

