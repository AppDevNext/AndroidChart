package com.github.mikephil.charting.renderer

import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.DataSet.Rounding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.max
import kotlin.math.min

abstract class BarLineScatterCandleBubbleRenderer(animator: ChartAnimator, viewPortHandler: ViewPortHandler) : DataRenderer(animator, viewPortHandler) {
    /**
     * buffer for storing the current minimum and maximum visible x
     */
    protected var xBounds: XBounds = XBounds()

    /**
     * Returns true if the DataSet values should be drawn, false if not.
     *
     * @param set
     * @return
     */
    protected fun shouldDrawValues(set: IDataSet<*>): Boolean {
        return set.isVisible && (set.isDrawValuesEnabled || set.isDrawIconsEnabled)
    }

    /**
     * Checks if the provided entry object is in bounds for drawing considering the current animation phase.
     *
     * @param e
     * @param set
     * @return
     */
    protected fun <T : Entry> isInBoundsX(e: T?, set: IBarLineScatterCandleBubbleDataSet<T>): Boolean {
        if (e == null) return false

        val entryIndex = set.getEntryIndex(e).toFloat()

        return if (entryIndex >= set.entryCount * animator.phaseX) {
            false
        } else {
            true
        }
    }

    /**
     * Class representing the bounds of the current viewport in terms of indices in the values array of a DataSet.
     */
    protected inner class XBounds {
        /**
         * minimum visible entry index
         */
        var min: Int = 0

        /**
         * maximum visible entry index
         */
        var max: Int = 0

        /**
         * range of visible entry indices
         */
        var range: Int = 0

        /**
         * Calculates the minimum and maximum x values as well as the range between them.
         *
         * @param chart
         * @param dataSet
         */
        operator fun <T : Entry> set(chart: BarLineScatterCandleBubbleDataProvider, dataSet: IBarLineScatterCandleBubbleDataSet<T>) {
            val phaseX = max(0f, min(1f, animator.phaseX))

            val low = chart.lowestVisibleX
            val high = chart.highestVisibleX

            val entryFrom = dataSet.getEntryForXValue(low, Float.Companion.NaN, Rounding.DOWN)
            val entryTo = dataSet.getEntryForXValue(high, Float.Companion.NaN, Rounding.UP)

            min = if (entryFrom == null) 0 else dataSet.getEntryIndex(entryFrom)
            max = if (entryTo == null) 0 else dataSet.getEntryIndex(entryTo)
            range = ((max - min) * phaseX).toInt()
        }
    }
}
