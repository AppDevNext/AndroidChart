package info.appdev.charting.data

import info.appdev.charting.utils.ChartIcon
import kotlin.math.abs

/** Smallest positive Float greater than zero, used for approximate equality checks. */
internal val ENTRY_FLOAT_EPSILON: Float = Float.fromBits(1)

/** Smallest positive Double greater than zero, used for approximate equality checks. */
internal val ENTRY_DOUBLE_EPSILON: Double = Double.fromBits(1L)

/**
 * Class representing one entry in the chart. Might contain multiple values.
 * Might only contain a single value depending on the used constructor.
 */
open class EntryFloat : BaseEntry<Float> {

    constructor()

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    constructor(x: Float, y: Float) : super(x = x, y = y)

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x    the x value
     * @param y    the y value (the actual value of the entry)
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, data: Any?) : super(x = x, y = y, data = data)

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     */
    constructor(x: Float, y: Float, icon: ChartIcon?) : super(x = x, y = y, icon = icon)

    /**
     * An EntryFloat represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     * @param icon icon image
     * @param data Spot for additional data this EntryFloat represents.
     */
    constructor(x: Float, y: Float, icon: ChartIcon?, data: Any?) : super(x = x, y = y, icon = icon, data = data)

    /**
     * returns an exact copy of the entry
     */
    open fun copy(): EntryFloat {
        val e = EntryFloat(
            x = x,
            y = y,
            data = data
        )
        return e
    }

    /**
     * Compares value, xIndex and data of the entries. Returns true if entries
     * are equal in those points, false if not. Does not check by hash-code like
     * it's done by the "equals" method.
     */
    fun equalTo(entryFloat: EntryFloat?): Boolean {
        if (entryFloat == null)
            return false

        if (entryFloat.data !== this.data)
            return false

        if (abs((entryFloat.x - this.x).toDouble()) > ENTRY_FLOAT_EPSILON)
            return false

        if (abs((entryFloat.y - this.y).toDouble()) > ENTRY_FLOAT_EPSILON)
            return false

        return true
    }

    /**
     * returns a string representation of the entry containing x-index and value
     */
    override fun toString(): String {
        return "${this::class.simpleName} x=$x y=$y"
    }
}
