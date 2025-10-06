package com.github.mikephil.charting.interfaces.datasets

import android.graphics.Paint
import com.github.mikephil.charting.data.CandleEntry

/**
 * Created by philipp on 21/10/15.
 */
interface ICandleDataSet : ILineScatterCandleRadarDataSet<CandleEntry> {
    /**
     * Returns the space that is left out on the left and right side of each
     * candle.
     *
     * @return
     */
    val barSpace: Float

    /**
     * Returns whether the candle bars should show?
     * When false, only "ticks" will show
     *
     * - default: true
     *
     * @return
     */
    val showCandleBar: Boolean

    /**
     * Returns the width of the candle-shadow-line in pixels.
     *
     * @return
     */
    val shadowWidth: Float

    /**
     * Returns shadow color for all entries
     *
     * @return
     */
    val shadowColor: Int

    /**
     * Returns the neutral color (for open == close)
     *
     * @return
     */
    val neutralColor: Int

    /**
     * Returns the increasing color (for open < close).
     *
     * @return
     */
    val increasingColor: Int

    /**
     * Returns the decreasing color (for open > close).
     *
     * @return
     */
    val decreasingColor: Int

    /**
     * Returns paint style when open < close
     *
     * @return
     */
    val increasingPaintStyle: Paint.Style?

    /**
     * Returns paint style when open > close
     *
     * @return
     */
    val decreasingPaintStyle: Paint.Style?

    /**
     * Is the shadow color same as the candle color?
     *
     * @return
     */
    val shadowColorSameAsCandle: Boolean
}
