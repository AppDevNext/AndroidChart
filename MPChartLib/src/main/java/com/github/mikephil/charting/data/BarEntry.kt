package com.github.mikephil.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.highlight.Range
import kotlin.math.abs

/**
 * Entry class for the BarChart. (especially stacked bars)
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ParcelCreator")
open class BarEntry : Entry {
    /**
     * Returns the stacked values this BarEntry represents, or null, if only a single value is represented (then, use
     * getY()).
     *
     * @return
     */
    /**
     * the values the stacked barchart holds
     */
    var yVals: FloatArray? = null
        private set

    /**
     * Returns the ranges of the individual stack-entries. Will return null if this entry is not stacked.
     *
     * @return
     */
    /**
     * the ranges for the individual stack values - automatically calculated
     */
    var ranges: Array<Range> = arrayOf()
        private set

    /**
     * Returns the sum of all negative values this entry (if stacked) contains. (this is a positive number)
     *
     * @return
     */
    /**
     * the sum of all negative values this entry (if stacked) contains
     */
    var negativeSum: Float = 0f
        private set

    /**
     * Reuturns the sum of all positive values this entry (if stacked) contains.
     *
     * @return
     */
    /**
     * the sum of all positive values this entry (if stacked) contains
     */
    var positiveSum: Float = 0f
        private set

    /**
     * Constructor for normal bars (not stacked).
     *
     * @param x
     * @param y
     */
    constructor(x: Float, y: Float) : super(x, y)

    /**
     * Constructor for normal bars (not stacked).
     *
     * @param x
     * @param y
     * @param data - Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, data: Any?) : super(x, y, data)

    /**
     * Constructor for normal bars (not stacked).
     *
     * @param x
     * @param y
     * @param icon - icon image
     */
    constructor(x: Float, y: Float, icon: Drawable?) : super(x, y, icon)

    /**
     * Constructor for normal bars (not stacked).
     *
     * @param x
     * @param y
     * @param icon - icon image
     * @param data - Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, icon: Drawable?, data: Any?) : super(x, y, icon, data)

    /**
     * Constructor for stacked bar entries. One data object for whole stack
     *
     * @param x
     * @param vals - the stack values, use at least 2
     */
    constructor(x: Float, vals: FloatArray?) : super(x, calcSum(vals)) {
        this.yVals = vals
        calcPosNegSum()
        calcRanges()
    }

    /**
     * Constructor for stacked bar entries. One data object for whole stack
     *
     * @param x
     * @param vals - the stack values, use at least 2
     * @param data - Spot for additional data this Entry represents.
     */
    constructor(x: Float, vals: FloatArray?, data: Any?) : super(x, calcSum(vals), data) {
        this.yVals = vals
        calcPosNegSum()
        calcRanges()
    }

    /**
     * Constructor for stacked bar entries. One data object for whole stack
     *
     * @param x
     * @param vals - the stack values, use at least 2
     * @param icon - icon image
     */
    constructor(x: Float, vals: FloatArray?, icon: Drawable?) : super(x, calcSum(vals), icon) {
        this.yVals = vals
        calcPosNegSum()
        calcRanges()
    }

    /**
     * Constructor for stacked bar entries. One data object for whole stack
     *
     * @param x
     * @param vals - the stack values, use at least 2
     * @param icon - icon image
     * @param data - Spot for additional data this Entry represents.
     */
    constructor(x: Float, vals: FloatArray?, icon: Drawable?, data: Any?) : super(x, calcSum(vals), icon, data) {
        this.yVals = vals
        calcPosNegSum()
        calcRanges()
    }

    /**
     * Returns an exact copy of the BarEntry.
     */
    override fun copy(): BarEntry {
        val copied = BarEntry(x, y, data)
        copied.setVals(this.yVals)
        return copied
    }

    /**
     * Set the array of values this BarEntry should represent.
     *
     * @param vals
     */
    fun setVals(vals: FloatArray?) {
        y = calcSum(vals)
        this.yVals = vals
        calcPosNegSum()
        calcRanges()
    }

    val isStacked: Boolean
        /**
         * Returns true if this BarEntry is stacked (has a values array), false if not.
         *
         * @return
         */
        get() = this.yVals != null

    /**
     * Use `getSumBelow(stackIndex)` instead.
     */
    @Deprecated("")
    fun getBelowSum(stackIndex: Int): Float {
        return getSumBelow(stackIndex)
    }

    fun getSumBelow(stackIndex: Int): Float {
        if (this.yVals == null) return 0f

        var remainder = 0f
        var index = yVals!!.size - 1

        while (index > stackIndex && index >= 0) {
            remainder += this.yVals!![index]
            index--
        }

        return remainder
    }

    private fun calcPosNegSum() {
        if (this.yVals == null) {
            this.negativeSum = 0f
            this.positiveSum = 0f
            return
        }

        var sumNeg = 0f
        var sumPos = 0f

        for (f in this.yVals) {
            if (f <= 0f) sumNeg += abs(f)
            else sumPos += f
        }

        this.negativeSum = sumNeg
        this.positiveSum = sumPos
    }

    protected fun calcRanges() {
        val values = this.yVals

        if (values == null || values.isEmpty()) return

        var negRemain = -this.negativeSum
        var posRemain = 0f

        this.ranges = Array(values.size) { i ->
            val value = values[i]

            if (value < 0) {
                Range(negRemain, negRemain - value).also {
                    negRemain -= value
                }
            } else {
                Range(posRemain, posRemain + value).also {
                    posRemain += value
                }
            }
        }
    }

    companion object {
        /**
         * Calculates the sum across all values of the given stack.
         *
         * @param vals
         * @return
         */
        private fun calcSum(vals: FloatArray?): Float {
            if (vals == null) return 0f

            var sum = 0f

            for (f in vals) sum += f

            return sum
        }
    }
}


