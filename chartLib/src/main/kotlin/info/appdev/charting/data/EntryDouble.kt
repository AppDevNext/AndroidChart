package info.appdev.charting.data

import android.graphics.drawable.Drawable
import android.os.Build
import java.io.Serializable
import kotlin.math.abs
import info.appdev.charting.utils.Utils

/**
 * High-precision entry that stores x and y as Double, but extends EntryFloat
 * so it works seamlessly in the existing chart rendering pipeline.
 * Use [xDouble] and [yDouble] to access the full-precision values.
 */
open class EntryDouble : EntryFloat, Serializable {

    var xDouble: Double = 0.0
    var yDouble: Double = 0.0

    /**
     * Override x to store/return via xDouble for high precision;
     * the base chart pipeline reads x.toFloat() transparently.
     */
    override var x: Float
        get() = xDouble.toFloat()
        set(value) {
            xDouble = value.toDouble()
        }

    /**
     * Override y to store/return via yDouble for high precision;
     * the base chart pipeline reads y.toFloat() transparently.
     */
    override var y: Float
        get() = yDouble.toFloat()
        set(value) {
            yDouble = value.toDouble()
        }

    constructor() : super()

    constructor(x: Double, y: Double) : super() {
        this.xDouble = x
        this.yDouble = y
    }

    constructor(x: Double, y: Double, data: Any?) : super() {
        this.xDouble = x
        this.yDouble = y
        this.data = data
    }

    constructor(x: Double, y: Double, icon: Drawable?) : super() {
        this.xDouble = x
        this.yDouble = y
        this.icon = icon
    }

    constructor(x: Double, y: Double, icon: Drawable?, data: Any?) : super() {
        this.xDouble = x
        this.yDouble = y
        this.icon = icon
        this.data = data
    }

    open fun copyDouble(): EntryDouble = EntryDouble(xDouble, yDouble, data)

    fun equalTo(other: EntryDouble?): Boolean {
        if (other == null) return false
        if (other.data !== this.data) return false
        if (abs(other.xDouble - this.xDouble) > Utils.DOUBLE_EPSILON) return false
        if (abs(other.yDouble - this.yDouble) > Utils.DOUBLE_EPSILON) return false
        return true
    }

    override fun toString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "${this.javaClass.typeName.substringAfterLast(".")} xDouble=$xDouble yDouble=$yDouble"
        } else {
            "EntryDouble xDouble=$xDouble yDouble=$yDouble"
        }
    }
}
