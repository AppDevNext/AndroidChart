package com.github.mikephil.charting.data

import android.graphics.drawable.Drawable

abstract class BaseEntry {

    protected var yBase: Float = 0f
    open var y: Float
        get() = yBase
        set(value) {
            yBase = value
        }

    var data: Any? = null

    var icon: Drawable? = null

    constructor()

    constructor(y: Float) {
        this.yBase = y
    }

    constructor(y: Float, data: Any?) : this(y) {
        this.data = data
    }

    constructor(y: Float, icon: Drawable?) : this(y) {
        this.icon = icon
    }

    constructor(y: Float, icon: Drawable?, data: Any?) : this(y) {
        this.icon = icon
        this.data = data
    }
}
