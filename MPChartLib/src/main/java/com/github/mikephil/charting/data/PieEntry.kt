package com.github.mikephil.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log

/**
 * @author Philipp Jahoda
 */
@SuppressLint("ParcelCreator")
class PieEntry : Entry {
    var label: String? = null

    constructor(value: Float) : super(0f, value)

    constructor(value: Float, data: Any?) : super(0f, value, data)

    constructor(value: Float, icon: Drawable?) : super(0f, value, icon)

    constructor(value: Float, icon: Drawable?, data: Any?) : super(0f, value, icon, data)

    constructor(value: Float, label: String?) : super(0f, value) {
        this.label = label
    }

    constructor(value: Float, label: String?, data: Any?) : super(0f, value, data) {
        this.label = label
    }

    constructor(value: Float, label: String?, icon: Drawable?) : super(0f, value, icon) {
        this.label = label
    }

    constructor(value: Float, label: String?, icon: Drawable?, data: Any?) : super(0f, value, icon, data) {
        this.label = label
    }

    val value: Float
        /**
         * This is the same as getY(). Returns the value of the PieEntry.
         *
         * @return
         */
        get() = y

    @get:Deprecated("")
    @set:Deprecated("")
    override var x: Float
        get() {
            Log.i("DEPRECATED", "Pie entries do not have x values")
            return super.x
        }
        set(x) {
            super.x = x
            Log.i("DEPRECATED", "Pie entries do not have x values")
        }

    override fun copy(): PieEntry {
        val e = PieEntry(y, label, data)
        return e
    }
}
