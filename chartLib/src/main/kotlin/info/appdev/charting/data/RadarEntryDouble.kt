package info.appdev.charting.data

import android.annotation.SuppressLint

/**
 * High-precision radar entry that stores the value as Double, extending [RadarEntryFloat]
 * so it works seamlessly in the existing radar chart rendering pipeline.
 * Use [valueDouble] for full-precision access.
 * Radar entries have no meaningful x-axis value.
 */
@Suppress("DEPRECATION")
@SuppressLint("ParcelCreator")
open class RadarEntryDouble : RadarEntryFloat {

    var valueDouble: Double = 0.0

    override var y: Float
        get() = valueDouble.toFloat()
        set(value) { valueDouble = value.toDouble() }

    @get:Deprecated("")
    @set:Deprecated("")
    override var x: Float
        get() = super.x
        set(x) { super.x = x }

    constructor(value: Double) : super(value.toFloat()) { valueDouble = value }

    constructor(value: Double, data: Any?) : super(value.toFloat(), data) { valueDouble = value }

    /** Full-precision value (same as [valueDouble]). */
    val valuePrecise: Double get() = valueDouble

    override fun copy(): RadarEntryDouble = RadarEntryDouble(valueDouble, data)

    override fun toString(): String = "RadarEntryDouble valueDouble=$valueDouble"
}

