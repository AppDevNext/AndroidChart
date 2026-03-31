package info.appdev.charting.data

import android.graphics.drawable.Drawable

abstract class BaseEntry<T> where T : Number, T : Comparable<T> {

    protected lateinit var xBase: T
    open var x: T
        get() = xBase
        set(value) {
            xBase = value
        }

    protected lateinit var yBase: T
    open var y: T
        get() = yBase
        set(value) {
            yBase = value
        }

    var data: Any? = null

    var icon: Drawable? = null

    constructor()

    constructor(y: T) {
        this.yBase = y
    }

    constructor(x: T, y: T) {
        this.xBase = x
        this.yBase = y
    }

    constructor(y: T, data: Any?) : this(y) {
        this.data = data
    }

    constructor(y: T, icon: Drawable?) : this(y) {
        this.icon = icon
    }

    constructor(y: T, icon: Drawable?, data: Any?) : this(y = y) {
        this.icon = icon
        this.data = data
    }

    constructor(x: T, y: T, data: Any?) : this(x = x, y = y) {
        this.data = data
    }

    constructor(x: T, y: T, icon: Drawable?) : this(x = x, y = y) {
        this.icon = icon
    }

    constructor(x: T, y: T, icon: Drawable?, data: Any?) : this(x = x, y = y) {
        this.icon = icon
        this.data = data
    }
}
