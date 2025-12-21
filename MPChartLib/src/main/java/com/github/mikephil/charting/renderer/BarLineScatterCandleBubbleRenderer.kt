package com.github.mikephil.charting.renderer

import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider
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
     */
    protected fun shouldDrawValues(set: IDataSet<*>): Boolean {
        return set.isVisible && (set.isDrawValues || set.isDrawIcons)
    }

    /**
     * Checks if the provided entry object is in bounds for drawing considering the current animation phase.
     */
    protected fun <T : Entry> isInBoundsX(entry: T, set: IBarLineScatterCandleBubbleDataSet<T>): Boolean {
        val entryIndex = set.getEntryIndex(entry).toFloat()

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
         */
        fun <T : Entry> set(chart: BarLineScatterCandleBubbleDataProvider, dataSet: IBarLineScatterCandleBubbleDataSet<T>) {
            val phaseX = max(0f, min(1f, animator.phaseX))

            val low = chart.lowestVisibleX
            val high = chart.highestVisibleX

            val entryFrom = dataSet.getEntryForXValue(low, Float.NaN, DataSet.Rounding.DOWN)
            val entryTo = dataSet.getEntryForXValue(high, Float.NaN, DataSet.Rounding.UP)

            min = if (entryFrom == null) 0 else dataSet.getEntryIndex(entryFrom)
            max = if (entryTo == null) 0 else dataSet.getEntryIndex(entryTo)
            range = ((max - min) * phaseX).toInt()
        }
    }
}
