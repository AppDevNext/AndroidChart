package info.appdev.charting.data

import android.graphics.drawable.Drawable
import info.appdev.charting.highlight.RangeDouble
import kotlin.math.abs

/**
 * High-precision bar entry that stores x and y as Double, extending [BarEntryFloat]
 * so it works seamlessly in the existing bar chart rendering pipeline.
 * Use [xDouble] and [yDouble] for full-precision access.
 * For stacked bars use [yValsDouble] and [rangesDouble].
 */
open class BarEntryDouble : BarEntryFloat {

    var xDouble: Double = 0.0
    var yDouble: Double = 0.0

    override var x: Float
        get() = xDouble.toFloat()
        set(value) { xDouble = value.toDouble() }

    /**
     * Returns the double-precision stacked values, or null for simple bars.
     */
    var yValsDouble: DoubleArray? = null
        private set

    /**
     * Double-precision ranges for stacked bars.
     */
    var rangesDouble: Array<RangeDouble> = arrayOf()
        private set

    var negativeDoubleSum: Double = 0.0
        private set

    var positiveDoubleSum: Double = 0.0
        private set

    // ── Simple bar constructors ──────────────────────────────────────────────

    constructor(x: Double, y: Double) : super(x.toFloat(), y.toFloat()) {
        xDouble = x; yDouble = y
    }

    constructor(x: Double, y: Double, data: Any?) : super(x.toFloat(), y.toFloat(), data) {
        xDouble = x; yDouble = y
    }

    constructor(x: Double, y: Double, icon: Drawable?) : super(x.toFloat(), y.toFloat(), icon) {
        xDouble = x; yDouble = y
    }

    constructor(x: Double, y: Double, icon: Drawable?, data: Any?) : super(x.toFloat(), y.toFloat(), icon, data) {
        xDouble = x; yDouble = y
    }

    // ── Stacked bar constructors ─────────────────────────────────────────────

    constructor(x: Double, vals: DoubleArray?) : super(x.toFloat(), calcDoubleSum(vals).toFloat()) {
        xDouble = x
        yDouble = calcDoubleSum(vals)
        yValsDouble = vals
        calcDoublePosNegSum()
        calcDoubleRanges()
    }

    constructor(x: Double, vals: DoubleArray?, data: Any?) : super(x.toFloat(), calcDoubleSum(vals).toFloat(), data) {
        xDouble = x
        yDouble = calcDoubleSum(vals)
        yValsDouble = vals
        calcDoublePosNegSum()
        calcDoubleRanges()
    }

    constructor(x: Double, vals: DoubleArray?, icon: Drawable?) : super(x.toFloat(), calcDoubleSum(vals).toFloat(), icon) {
        xDouble = x
        yDouble = calcDoubleSum(vals)
        yValsDouble = vals
        calcDoublePosNegSum()
        calcDoubleRanges()
    }

    // ── y override (always reflects yDouble) ────────────────────────────────

    override var y: Float
        get() = yDouble.toFloat()
        set(value) { yDouble = value.toDouble() }

    // ── Stack helpers ────────────────────────────────────────────────────────

    val isStackedDouble: Boolean get() = yValsDouble != null

    fun setValsDouble(vals: DoubleArray?) {
        yDouble = calcDoubleSum(vals)
        yValsDouble = vals
        calcDoublePosNegSum()
        calcDoubleRanges()
    }

    fun getSumBelowDouble(stackIndex: Int): Double {
        val vals = yValsDouble ?: return 0.0
        var remainder = 0.0
        var index = vals.size - 1
        while (index > stackIndex && index >= 0) {
            remainder += vals[index]
            index--
        }
        return remainder
    }

    private fun calcDoublePosNegSum() {
        val vals = yValsDouble
        if (vals == null) { negativeDoubleSum = 0.0; positiveDoubleSum = 0.0; return }
        var sumNeg = 0.0; var sumPos = 0.0
        for (v in vals) { if (v <= 0.0) sumNeg += abs(v) else sumPos += v }
        negativeDoubleSum = sumNeg; positiveDoubleSum = sumPos
    }

    private fun calcDoubleRanges() {
        val values = yValsDouble
        if (values == null || values.isEmpty()) return

        rangesDouble = Array(values.size) { RangeDouble(0.0, 0.0) }
        var negRemain = -negativeDoubleSum
        var posRemain = 0.0

        for (i in values.indices) {
            val value = values[i]
            if (value < 0) {
                rangesDouble[i] = RangeDouble(negRemain, negRemain - value)
                negRemain -= value
            } else {
                rangesDouble[i] = RangeDouble(posRemain, posRemain + value)
                posRemain += value
            }
        }
    }

    override fun toString(): String = "BarEntryDouble xDouble=$xDouble yDouble=$yDouble yValsDouble=${yValsDouble?.contentToString()}"

    companion object {
        private fun calcDoubleSum(vals: DoubleArray?): Double = vals?.sum() ?: 0.0
    }
}

