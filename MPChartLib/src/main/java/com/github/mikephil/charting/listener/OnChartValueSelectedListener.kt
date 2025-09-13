package com.github.mikephil.charting.listener

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

/**
 * Listener for callbacks when selecting values inside the chart by
 * touch-gesture.
 *
 * @author Philipp Jahoda
 */
interface OnChartValueSelectedListener {
    /**
     * Called when a value has been selected inside the chart.
     *
     * @param e The selected Entry
     * @param h The corresponding highlight object that contains information
     * about the highlighted position such as dataSetIndex, ...
     */
    fun onValueSelected(e: Entry?, h: Highlight?)

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    fun onNothingSelected()
}
