package info.appdev.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import timber.log.Timber

/**
 * High-precision pie entry that stores the value as Double, extending [PieEntryFloat]
 * so it works seamlessly in the existing pie chart rendering pipeline.
 * Use [valueDouble] for full-precision access.
 * Pie entries have no meaningful x-axis value.
 */
@SuppressLint("ParcelCreator")
open class PieEntryDouble : PieEntryFloat {

    var valueDouble: Double = 0.0

    override var y: Float
        get() = valueDouble.toFloat()
        set(value) { valueDouble = value.toDouble() }

    @get:Deprecated("")
    @set:Deprecated("")
    @Suppress("DEPRECATION")
    override var x: Float
        get() {
            Timber.i("Pie entries do not have x values")
            return super.x
        }
        set(x) {
            super.x = x
            Timber.i("Pie entries do not have x values")
        }

    constructor(value: Double) : super(value.toFloat()) { valueDouble = value }

    constructor(value: Double, data: Any?) : super(value.toFloat(), data) { valueDouble = value }

    constructor(value: Double, icon: Drawable?) : super(value.toFloat(), icon) { valueDouble = value }

    constructor(value: Double, icon: Drawable?, data: Any?) : super(value.toFloat(), icon, data) { valueDouble = value }

    constructor(value: Double, label: String?) : super(value.toFloat(), label) { valueDouble = value }

    constructor(value: Double, label: String?, data: Any?) : super(value.toFloat(), label, data) { valueDouble = value }

    constructor(value: Double, label: String?, icon: Drawable?) : super(value.toFloat(), label, icon) { valueDouble = value }

    constructor(value: Double, label: String?, icon: Drawable?, data: Any?) : super(value.toFloat(), label, icon, data) { valueDouble = value }

    /** Full-precision value (same as [valueDouble]). */
    val valuePrecise: Double get() = valueDouble

    override fun copy(): PieEntryDouble = PieEntryDouble(valueDouble, label, data)

    override fun toString(): String = "PieEntryDouble valueDouble=$valueDouble label=$label"
}


