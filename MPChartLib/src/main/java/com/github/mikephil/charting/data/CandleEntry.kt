package com.github.mikephil.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import kotlin.math.abs

/**
 * Subclass of Entry that holds all values for one entry in a CandleStickChart.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ParcelCreator")
class CandleEntry : Entry {
    /**
     * Returns the upper shadows highest value.
     *
     * @return
     */
    /** shadow-high value  */
    var high: Float = 0f

    /**
     * Returns the lower shadows lowest value.
     *
     * @return
     */
    /** shadow-low value  */
    var low: Float = 0f

    /**
     * Returns the bodys close value.
     *
     * @return
     */
    /** close value  */
    var close: Float = 0f

    /**
     * Returns the bodys open value.
     *
     * @return
     */
    /** open value  */
    var open: Float = 0f

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
     * @param open
     * @param close
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
     * @param open
     * @param close
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
     * @param open
     * @param close
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

    val shadowRange: Float
        /**
         * Returns the overall range (difference) between shadow-high and
         * shadow-low.
         *
         * @return
         */
        get() = abs(this.high - this.low)

    val bodyRange: Float
        /**
         * Returns the body size (difference between open and close).
         *
         * @return
         */
        get() = abs(this.open - this.close)

    override fun copy(): CandleEntry {
        val c = CandleEntry(
            x, this.high, this.low, this.open,
            this.close, data
        )

        return c
    }
}
