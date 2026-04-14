package info.appdev.charting.data

import android.graphics.drawable.Drawable

@Deprecated(
    message = "The replacement is CandleEntryFloat, or use CandleEntryDouble for higher precision. CandleEntry is retained for backward compatibility but will be removed in a future version.",
    replaceWith = ReplaceWith("CandleEntryFloat", "info.appdev.charting.data.CandleEntryFloat")
)
class CandleEntry : CandleEntryFloat {

    /**
     * Constructor.
     *
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param open The open value
     * @param close The close value
     */
    constructor(x: Float, shadowH: Float, shadowL: Float, open: Float, close: Float) : super(x, shadowH, shadowL, open, close) {
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
    ) : super(x, shadowH, shadowL, open, close) {
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
    ) : super(x, shadowH, shadowL, open, close) {
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
    ) : super(x, shadowH, shadowL, open, close) {
        this.high = shadowH
        this.low = shadowL
        this.open = open
        this.close = close
    }

}
