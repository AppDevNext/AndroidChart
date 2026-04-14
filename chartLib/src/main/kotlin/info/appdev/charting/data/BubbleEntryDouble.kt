package info.appdev.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import kotlin.math.abs

/**
 * High-precision bubble entry that stores x, y and size as Double, extending [BubbleEntryFloat]
 * so it works seamlessly in the existing bubble chart rendering pipeline.
 * Use [xDouble], [yDouble] and [sizeDouble] for full-precision access.
 */
@SuppressLint("ParcelCreator")
open class BubbleEntryDouble : BubbleEntryFloat {

    var xDouble: Double = 0.0
    var yDouble: Double = 0.0
    var sizeDouble: Double = 0.0

    override var x: Float
        get() = xDouble.toFloat()
        set(value) { xDouble = value.toDouble() }

    override var y: Float
        get() = yDouble.toFloat()
        set(value) { yDouble = value.toDouble() }

    constructor(x: Double, y: Double, size: Double) : super(x.toFloat(), y.toFloat(), size.toFloat()) {
        xDouble = x; yDouble = y; sizeDouble = size
        this.size = size.toFloat()
    }

    constructor(x: Double, y: Double, size: Double, data: Any?) : super(x.toFloat(), y.toFloat(), size.toFloat(), data) {
        xDouble = x; yDouble = y; sizeDouble = size
        this.size = size.toFloat()
    }

    constructor(x: Double, y: Double, size: Double, icon: Drawable?) : super(x.toFloat(), y.toFloat(), size.toFloat(), icon) {
        xDouble = x; yDouble = y; sizeDouble = size
        this.size = size.toFloat()
    }

    override fun copy(): BubbleEntryDouble = BubbleEntryDouble(xDouble, yDouble, sizeDouble, data)

    override fun toString(): String = "BubbleEntryDouble xDouble=$xDouble yDouble=$yDouble sizeDouble=$sizeDouble"
}

