package info.appdev.charting.interfaces.datasets

import android.graphics.Paint
import info.appdev.charting.data.CandleEntry

interface ICandleDataSet : ILineScatterCandleRadarDataSet<CandleEntry> {
    /**
     * Returns the space that is left out on the left and right side of each candle.
     */
    val barSpace: Float

    /**
     * Returns whether the candle bars should show?
     * When false, only "ticks" will show
     * - default: true
     */
    val showCandleBar: Boolean

    /**
     * Returns the width of the candle-shadow-line in pixels.
     */
    val shadowWidth: Float

    /**
     * Returns shadow color for all entries
     */
    val shadowColor: Int

    /**
     * Returns the neutral color (for open == close)
     */
    val neutralColor: Int

    /**
     * Returns the increasing color (for open < close).
     */
    val increasingColor: Int

    /**
     * Returns the decreasing color (for open > close).
     */
    val decreasingColor: Int

    /**
     * Returns paint style when open < close
     */
    val increasingPaintStyle: Paint.Style?

    /**
     * Returns paint style when open > close
     */
    val decreasingPaintStyle: Paint.Style?

    /**
     * Is the shadow color same as the candle color?
     */
    val shadowColorSameAsCandle: Boolean
}
