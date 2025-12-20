package com.github.mikephil.charting.interfaces.datasets;

import android.graphics.Paint;

import com.github.mikephil.charting.data.CandleEntry;

/**
 * Created by philipp on 21/10/15.
 */
public interface ICandleDataSet extends ILineScatterCandleRadarDataSet<CandleEntry> {

    /**
     * Returns the space that is left out on the left and right side of each
     * candle.
     */
    float getBarSpace();

    /**
     * Returns whether the candle bars should show?
     * When false, only "ticks" will show
     * - default: true
     */
    boolean getShowCandleBar();

    /**
     * Returns the width of the candle-shadow-line in pixels.
     */
    float getShadowWidth();

    /**
     * Returns shadow color for all entries
     */
    int getShadowColor();

    /**
     * Returns the neutral color (for open == close)
     */
    int getNeutralColor();

    /**
     * Returns the increasing color (for open < close).
     */
    int getIncreasingColor();

    /**
     * Returns the decreasing color (for open > close).
     */
    int getDecreasingColor();

    /**
     * Returns paint style when open < close
     */
    Paint.Style getIncreasingPaintStyle();

    /**
     * Returns paint style when open > close
     */
    Paint.Style getDecreasingPaintStyle();

    /**
     * Is the shadow color same as the candle color?
     */
    boolean getShadowColorSameAsCandle();
}
