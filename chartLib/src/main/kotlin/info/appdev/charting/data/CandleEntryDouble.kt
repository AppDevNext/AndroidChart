package info.appdev.charting.data

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import kotlin.math.abs

/**
 * High-precision candle entry that stores all OHLC values as Double, extending [CandleEntryFloat]
 * so it works seamlessly in the existing candlestick chart rendering pipeline.
 * Use [xDouble], [highDouble], [lowDouble], [openDouble], [closeDouble] for full-precision access.
 */
@SuppressLint("ParcelCreator")
open class CandleEntryDouble : CandleEntryFloat {

    var xDouble: Double = 0.0
    var highDouble: Double = 0.0
    var lowDouble: Double = 0.0
    var openDouble: Double = 0.0
    var closeDouble: Double = 0.0

    override var x: Float
        get() = xDouble.toFloat()
        set(value) { xDouble = value.toDouble() }

    constructor(
        x: Double, shadowH: Double, shadowL: Double, open: Double, close: Double
    ) : super(x.toFloat(), shadowH.toFloat(), shadowL.toFloat(), open.toFloat(), close.toFloat()) {
        xDouble = x; highDouble = shadowH; lowDouble = shadowL; openDouble = open; closeDouble = close
        this.high = shadowH.toFloat(); this.low = shadowL.toFloat()
        this.open = open.toFloat(); this.close = close.toFloat()
    }

    constructor(
        x: Double, shadowH: Double, shadowL: Double, open: Double, close: Double, data: Any?
    ) : super(x.toFloat(), shadowH.toFloat(), shadowL.toFloat(), open.toFloat(), close.toFloat(), data) {
        xDouble = x; highDouble = shadowH; lowDouble = shadowL; openDouble = open; closeDouble = close
        this.high = shadowH.toFloat(); this.low = shadowL.toFloat()
        this.open = open.toFloat(); this.close = close.toFloat()
    }

    constructor(
        x: Double, shadowH: Double, shadowL: Double, open: Double, close: Double, icon: Drawable?
    ) : super(x.toFloat(), shadowH.toFloat(), shadowL.toFloat(), open.toFloat(), close.toFloat(), icon) {
        xDouble = x; highDouble = shadowH; lowDouble = shadowL; openDouble = open; closeDouble = close
        this.high = shadowH.toFloat(); this.low = shadowL.toFloat()
        this.open = open.toFloat(); this.close = close.toFloat()
    }

    constructor(
        x: Double, shadowH: Double, shadowL: Double, open: Double, close: Double, icon: Drawable?, data: Any?
    ) : super(x.toFloat(), shadowH.toFloat(), shadowL.toFloat(), open.toFloat(), close.toFloat(), icon, data) {
        xDouble = x; highDouble = shadowH; lowDouble = shadowL; openDouble = open; closeDouble = close
        this.high = shadowH.toFloat(); this.low = shadowL.toFloat()
        this.open = open.toFloat(); this.close = close.toFloat()
    }

    /** Overall range between shadow-high and shadow-low at full precision. */
    val shadowRangeDouble: Double get() = abs(highDouble - lowDouble)

    /** Body size at full precision. */
    val bodyRangeDouble: Double get() = abs(openDouble - closeDouble)

    override fun copy(): CandleEntryDouble =
        CandleEntryDouble(xDouble, highDouble, lowDouble, openDouble, closeDouble, data)

    override fun toString(): String =
        "CandleEntryDouble xDouble=$xDouble highDouble=$highDouble lowDouble=$lowDouble openDouble=$openDouble closeDouble=$closeDouble"
}

