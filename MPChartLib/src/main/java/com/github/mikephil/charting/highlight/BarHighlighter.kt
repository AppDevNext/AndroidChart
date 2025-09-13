package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.MPPointD
import kotlin.math.abs
import kotlin.math.max

/**
 * Created by Philipp Jahoda on 22/07/15.
 */
open class BarHighlighter(chart: BarDataProvider?) : ChartHighlighter<BarDataProvider?>(chart) {
    override fun getHighlight(x: Float, y: Float): Highlight? {
        val high = super.getHighlight(x, y)

        if (high == null) {
            return null
        }

        val pos = getValsForTouch(x, y)

        val barData = mChart!!.barData

        val set = barData!!.getDataSetByIndex(high.dataSetIndex)
        if (set!!.isStacked) {
            return getStackedHighlight(
                high,
                set,
                pos.x.toFloat(),
                pos.y.toFloat()
            )
        }

        MPPointD.Companion.recycleInstance(pos)

        return high
    }

    /**
     * This method creates the Highlight object that also indicates which value of a stacked BarEntry has been
     * selected.
     *
     * @param high the Highlight to work with looking for stacked values
     * @param set
     * @param xVal
     * @param yVal
     * @return
     */
    fun getStackedHighlight(high: Highlight, set: IBarDataSet, xVal: Float, yVal: Float): Highlight? {
        val entry = set.getEntryForXValue(xVal, yVal)

        if (entry == null) return null

        // not stacked
        if (entry.yVals == null) {
            return high
        } else {
            val ranges = entry.ranges

            if (ranges.isNotEmpty()) {
                val stackIndex = getClosestStackIndex(ranges, yVal)

                val pixels = mChart!!.getTransformer(set.axisDependency)!!.getPixelForValues(high.x, ranges[stackIndex].to)

                val stackedHigh = Highlight(
                    entry.x,
                    entry.y,
                    pixels.x.toFloat(),
                    pixels.y.toFloat(),
                    high.dataSetIndex,
                    stackIndex,
                    high.axis
                )

                MPPointD.Companion.recycleInstance(pixels)

                return stackedHigh
            }
        }

        return null
    }

    /**
     * Returns the index of the closest value inside the values array / ranges (stacked barchart) to the value
     * given as
     * a parameter.
     *
     * @param ranges
     * @param value
     * @return
     */
    protected fun getClosestStackIndex(ranges: Array<Range>?, value: Float): Int {
        if (ranges == null || ranges.size == 0) return 0

        var stackIndex = 0

        for (range in ranges) {
            if (range.contains(value)) return stackIndex
            else stackIndex++
        }

        val length = max(ranges.size - 1, 0)

        return if (value > ranges[length].to) length else 0
    }

    //    /**
    //     * Splits up the stack-values of the given bar-entry into Range objects.
    //     *
    //     * @param entry
    //     * @return
    //     */
    //    protected Range[] getRanges(BarEntry entry) {
    //
    //        float[] values = entry.getYVals();
    //
    //        if (values == null || values.length == 0)
    //            return new Range[0];
    //
    //        Range[] ranges = new Range[values.length];
    //
    //        float negRemain = -entry.getNegativeSum();
    //        float posRemain = 0f;
    //
    //        for (int i = 0; i < ranges.length; i++) {
    //
    //            float value = values[i];
    //
    //            if (value < 0) {
    //                ranges[i] = new Range(negRemain, negRemain + Math.abs(value));
    //                negRemain += Math.abs(value);
    //            } else {
    //                ranges[i] = new Range(posRemain, posRemain + value);
    //                posRemain += value;
    //            }
    //        }
    //
    //        return ranges;
    //    }
    override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return abs(x1 - x2)
    }

    override val data
        get() = mChart!!.barData
}
