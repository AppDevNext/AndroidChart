package info.appdev.charting.data

import android.graphics.drawable.Drawable

abstract class BaseEntry<T> where T : Number, T : Comparable<T> {

    protected var yBase: T? = null
    protected var xBase: T? = null

    open var y: T
        get() = yBase ?: throw IllegalStateException("y not initialized")
        set(value) {
            yBase = value
        }

    open var x: T
        get() = xBase ?: throw IllegalStateException("x not initialized")
        set(value) {
            xBase = value
        }

    var data: Any? = null

    var icon: Drawable? = null

    constructor()

    constructor(y: T) {
        this.yBase = y
    }

    constructor(y: T, data: Any?) : this(y) {
        this.data = data
    }

    constructor(y: T, icon: Drawable?) : this(y) {
        this.icon = icon
    }

    constructor(y: T, icon: Drawable?, data: Any?) : this(y) {
        this.icon = icon
        this.data = data
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    constructor(x: T, y: T) {
        this.xBase = x
        this.yBase = y
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: T, y: T, data: Any?) {
        this.xBase = x
        this.yBase = y
        this.data = data
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     */
    constructor(x: T, y: T, icon: Drawable?) {
        this.xBase = x
        this.yBase = y
        this.icon = icon
    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: T, y: T, icon: Drawable?, data: Any?)  {
        this.xBase = x
        this.yBase = y
        this.icon = icon
        this.data = data
    }
}
