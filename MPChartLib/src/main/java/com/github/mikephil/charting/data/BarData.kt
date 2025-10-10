package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

/**
 * Data object that represents all data for the BarChart.
 *
 * @author Philipp Jahoda
 */
class BarData : BarLineScatterCandleBubbleData<BarEntry, IBarDataSet> {
    /**
     * Sets the width each bar should have on the x-axis (in values, not pixels).
     * Default 0.85f
     *
     * @param this.barWidth
     */
    /**
     * the width of the bars on the x-axis, in values (not pixels)
     */
    var barWidth: Float = 0.85f

    constructor() : super()

    constructor(vararg dataSets: IBarDataSet) : super(*dataSets)

    constructor(dataSets: MutableList<IBarDataSet>) : super(dataSets)

    /**
     * Groups all BarDataSet objects this data object holds together by modifying the x-value of their entries.
     * Previously set x-values of entries will be overwritten. Leaves space between bars and groups as specified
     * by the parameters.
     * Do not forget to call notifyDataSetChanged() on your BarChart object after calling this method.
     *
     * @param fromX      the starting point on the x-axis where the grouping should begin
     * @param groupSpace the space between groups of bars in values (not pixels) e.g. 0.8f for bar width 1f
     * @param barSpace   the space between individual bars in values (not pixels) e.g. 0.1f for bar width 1f
     */
    fun groupBars(fromX: Float, groupSpace: Float, barSpace: Float) {
        var fromX = fromX
        val setCount = dataSets.size
        if (setCount <= 1) {
            throw RuntimeException("BarData needs to hold at least 2 BarDataSets to allow grouping.")
        }

        val max = maxEntryCountSet ?: return
        val maxEntryCount = max.entryCount

        val groupSpaceWidthHalf = groupSpace / 2f
        val barSpaceHalf = barSpace / 2f
        val barWidthHalf = this.barWidth / 2f

        val interval = getGroupWidth(groupSpace, barSpace)

        for (i in 0..<maxEntryCount) {
            val start = fromX
            fromX += groupSpaceWidthHalf

            for (set in dataSets) {
                fromX += barSpaceHalf
                fromX += barWidthHalf

                if (i < set.entryCount) {
                    val entry = set.getEntryForIndex(i)

                    entry.x = fromX
                }

                fromX += barWidthHalf
                fromX += barSpaceHalf
            }

            fromX += groupSpaceWidthHalf
            val end = fromX
            val innerInterval = end - start
            val diff = interval - innerInterval

            // correct rounding errors
            if (diff > 0 || diff < 0) {
                fromX += diff
            }
        }

        notifyDataChanged()
    }

    /**
     * In case of grouped bars, this method returns the space an individual group of bar needs on the x-axis.
     *
     * @param groupSpace
     * @param barSpace
     * @return
     */
    fun getGroupWidth(groupSpace: Float, barSpace: Float): Float {
        return dataSets.size * (this.barWidth + barSpace) + groupSpace
    }
}
