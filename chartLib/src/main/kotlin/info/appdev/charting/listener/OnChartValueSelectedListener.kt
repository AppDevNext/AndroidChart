package info.appdev.charting.listener

import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight

/**
 * Listener for callbacks when selecting values inside the chart by
 * touch-gesture.
 */
interface OnChartValueSelectedListener {
    /**
     * Called when a value has been selected inside the chart.
     *
     * @param entry The selected Entry
     * @param highlight The corresponding highlight object that contains information
     * about the highlighted position such as dataSetIndex, ...
     */
    fun onValueSelected(entry: Entry, highlight: Highlight)

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    fun onNothingSelected()
}
