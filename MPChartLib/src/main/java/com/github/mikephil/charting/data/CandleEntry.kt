package com.github.mikephil.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import kotlin.math.abs

/**
 * Subclass of Entry that holds all values for one entry in a CandleStickChart.
 */
@SuppressLint("ParcelCreator")
class CandleEntry : Entry {
    /**
     * Returns the upper shadows highest value.
     */
    /** shadow-high value  */
    var high: Float

    /**
     * Returns the lower shadows lowest value.
     */
    /** shadow-low value  */
    var low: Float

    /**
     * Returns the bodies close value.
     */
    /** close value  */
    var close: Float

    /**
     * Returns the bodies open value.
     */
    /** open value  */
    var open: Float

    /**
     * Constructor.
     *
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param open The open value
     * @param close The close value
     */
    constructor(x: Float, shadowH: Float, shadowL: Float, open: Float, close: Float) : super(x, (shadowH + shadowL) / 2f) {
        this.high = shadowH
        this.low = shadowL
        this.open = open
        this.close = close
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param data Spot for additional data this Entry represents
     */
    constructor(
        x: Float, shadowH: Float, shadowL: Float, open: Float, close: Float,
        data: Any?
    ) : super(x, (shadowH + shadowL) / 2f, data) {
        this.high = shadowH
        this.low = shadowL
        this.open = open
        this.close = close
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param icon Icon image
     */
    constructor(
        x: Float, shadowH: Float, shadowL: Float, open: Float, close: Float,
        icon: Drawable?
    ) : super(x, (shadowH + shadowL) / 2f, icon) {
        this.high = shadowH
        this.low = shadowL
        this.open = open
        this.close = close
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param icon Icon image
     * @param data Spot for additional data this Entry represents
     */
    constructor(
        x: Float, shadowH: Float, shadowL: Float, open: Float, close: Float,
        icon: Drawable?, data: Any?
    ) : super(x, (shadowH + shadowL) / 2f, icon, data) {
        this.high = shadowH
        this.low = shadowL
        this.open = open
        this.close = close
    }

    /**
     * Returns the overall range (difference) between shadow-high and
     * shadow-low.
     */
    val shadowRange: Float
        get() = abs(this.high - this.low)

    /**
     * Returns the body size (difference between open and close).
     */
    val bodyRange: Float
        get() = abs(this.open - this.close)

    /**
     * Returns the center value of the candle. (Middle value between high and low)
     */
    override var y: Float
        get() = super.y
        set(value) {
            super.y = value
        }

    override fun copy(): CandleEntry {
        return CandleEntry(x, this.high, this.low, this.open, this.close, data)
    }
}
