
package com.github.mikephil.charting.data;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

/**
 * Subclass of Entry that holds all values for one entry in a CandleStickChart.
 * 
 * @author Philipp Jahoda
 */
@SuppressLint("ParcelCreator")
public class CandleEntry extends Entry {

    /** shadow-high value */
    private float mShadowHigh;

    /** shadow-low value */
    private float mShadowLow;

    /** close value */
    private float mClose;

    /** open value */
    private float mOpen;

    /**
     * Constructor.
     * 
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param open The open value
     * @param close The close value
     */
    public CandleEntry(float x, float shadowH, float shadowL, float open, float close) {
        super(x, (shadowH + shadowL) / 2f);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param data Spot for additional data this Entry represents
     */
    public CandleEntry(float x, float shadowH, float shadowL, float open, float close,
                       Object data) {
        super(x, (shadowH + shadowL) / 2f, data);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis
     * @param shadowH The (shadow) high value
     * @param shadowL The (shadow) low value
     * @param icon Icon image
     */
    public CandleEntry(float x, float shadowH, float shadowL, float open, float close,
                       Drawable icon) {
        super(x, (shadowH + shadowL) / 2f, icon);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
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
    public CandleEntry(float x, float shadowH, float shadowL, float open, float close,
                       Drawable icon, Object data) {
        super(x, (shadowH + shadowL) / 2f, icon, data);

        this.mShadowHigh = shadowH;
        this.mShadowLow = shadowL;
        this.mOpen = open;
        this.mClose = close;
    }

    /**
     * Returns the overall range (difference) between shadow-high and
     * shadow-low.
     */
    public float getShadowRange() {
        return Math.abs(mShadowHigh - mShadowLow);
    }

    /**
     * Returns the body size (difference between open and close).
     */
    public float getBodyRange() {
        return Math.abs(mOpen - mClose);
    }

    /**
     * Returns the center value of the candle. (Middle value between high and low)
     */
    @Override
    public float getY() {
        return super.getY();
    }

    public CandleEntry copy() {
		return new CandleEntry(getX(), mShadowHigh, mShadowLow, mOpen, mClose, getData());
    }

    /**
     * Returns the upper shadows highest value.
     */
    public float getHigh() {
        return mShadowHigh;
    }

    public void setHigh(float mShadowHigh) {
        this.mShadowHigh = mShadowHigh;
    }

    /**
     * Returns the lower shadows lowest value.
     */
    public float getLow() {
        return mShadowLow;
    }

    public void setLow(float mShadowLow) {
        this.mShadowLow = mShadowLow;
    }

    /**
     * Returns the bodies close value.
     */
    public float getClose() {
        return mClose;
    }

    public void setClose(float mClose) {
        this.mClose = mClose;
    }

    /**
     * Returns the bodies open value.
     */
    public float getOpen() {
        return mOpen;
    }

    public void setOpen(float mOpen) {
        this.mOpen = mOpen;
    }
}
