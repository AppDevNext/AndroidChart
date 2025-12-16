
package com.github.mikephil.charting.data;

import android.graphics.Paint;

import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.UtilsKtKt;

import java.util.ArrayList;
import java.util.List;

/**
 * DataSet for the CandleStickChart.
 *
 * @author Philipp Jahoda
 */
public class CandleDataSet extends LineScatterCandleRadarDataSet<CandleEntry> implements ICandleDataSet {

    /**
     * the width of the shadow of the candle
     */
    private float mShadowWidth = 3f;

    /**
     * should the candle bars show?
     * when false, only "ticks" will show
     * <p/>
     * - default: true
     */
    private boolean mShowCandleBar = true;

    /**
     * the space between the candle entries, default 0.1f (10%)
     */
    private float mBarSpace = 0.1f;

    /**
     * use candle color for the shadow
     */
    private boolean mShadowColorSameAsCandle = false;

    /**
     * paint style when open < close
     * increasing candlesticks are traditionally hollow
     */
    protected Paint.Style mIncreasingPaintStyle = Paint.Style.STROKE;

    /**
     * paint style when open > close
     * descreasing candlesticks are traditionally filled
     */
    protected Paint.Style mDecreasingPaintStyle = Paint.Style.FILL;

    /**
     * color for open == close
     */
    protected int mNeutralColor = ColorTemplate.COLOR_SKIP;

    /**
     * color for open < close
     */
    protected int mIncreasingColor = ColorTemplate.COLOR_SKIP;

    /**
     * color for open > close
     */
    protected int mDecreasingColor = ColorTemplate.COLOR_SKIP;

    /**
     * shadow line color, set -1 for backward compatibility and uses default
     * color
     */
    protected int mShadowColor = ColorTemplate.COLOR_SKIP;

    public CandleDataSet(List<CandleEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public DataSet<CandleEntry> copy() {
        List<CandleEntry> entries = new ArrayList<>();
        for (int i = 0; i < mEntries.size(); i++) {
            entries.add(mEntries.get(i).copy());
        }
        CandleDataSet copied = new CandleDataSet(entries, getLabel());
        copy(copied);
        return copied;
    }

    protected void copy(CandleDataSet candleDataSet) {
        super.copy((BaseDataSet<?>) candleDataSet);
        candleDataSet.mShadowWidth = mShadowWidth;
        candleDataSet.mShowCandleBar = mShowCandleBar;
        candleDataSet.mBarSpace = mBarSpace;
        candleDataSet.mShadowColorSameAsCandle = mShadowColorSameAsCandle;
        candleDataSet.mHighLightColor = mHighLightColor;
        candleDataSet.mIncreasingPaintStyle = mIncreasingPaintStyle;
        candleDataSet.mDecreasingPaintStyle = mDecreasingPaintStyle;
        candleDataSet.mNeutralColor = mNeutralColor;
        candleDataSet.mIncreasingColor = mIncreasingColor;
        candleDataSet.mDecreasingColor = mDecreasingColor;
        candleDataSet.mShadowColor = mShadowColor;
    }

    @Override
    protected void calcMinMax(CandleEntry e) {

        if (e.getLow() < mYMin)
            mYMin = e.getLow();

        if (e.getHigh() > mYMax)
            mYMax = e.getHigh();

        calcMinMaxX(e);
    }

    @Override
    protected void calcMinMaxY(CandleEntry entry) {

        if (entry.getHigh() < mYMin)
            mYMin = entry.getHigh();

        if (entry.getHigh() > mYMax)
            mYMax = entry.getHigh();

        if (entry.getLow() < mYMin)
            mYMin = entry.getLow();

        if (entry.getLow() > mYMax)
            mYMax = entry.getLow();
    }

    /**
     * Sets the space that is left out on the left and right side of each
     * candle, default 0.1f (10%), max 0.45f, min 0f
     */
    public void setBarSpace(float space) {

        if (space < 0f)
            space = 0f;
        if (space > 0.45f)
            space = 0.45f;

        mBarSpace = space;
    }

    @Override
    public float getBarSpace() {
        return mBarSpace;
    }

    /**
     * Sets the width of the candle-shadow-line in pixels. Default 3f.
     */
    public void setShadowWidth(float width) {
        mShadowWidth = UtilsKtKt.convertDpToPixel(width);
    }

    @Override
    public float getShadowWidth() {
        return mShadowWidth;
    }

    /**
     * Sets whether the candle bars should show?
     */
    public void setShowCandleBar(boolean showCandleBar) {
        mShowCandleBar = showCandleBar;
    }

    @Override
    public boolean getShowCandleBar() {
        return mShowCandleBar;
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open == close.
     */
    public void setNeutralColor(int color) {
        mNeutralColor = color;
    }

    @Override
    public int getNeutralColor() {
        return mNeutralColor;
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open <= close.
     */
    public void setIncreasingColor(int color) {
        mIncreasingColor = color;
    }

    @Override
    public int getIncreasingColor() {
        return mIncreasingColor;
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open > close.
     */
    public void setDecreasingColor(int color) {
        mDecreasingColor = color;
    }

    @Override
    public int getDecreasingColor() {
        return mDecreasingColor;
    }

    @Override
    public Paint.Style getIncreasingPaintStyle() {
        return mIncreasingPaintStyle;
    }

    /**
     * Sets paint style when open < close
     */
    public void setIncreasingPaintStyle(Paint.Style paintStyle) {
        this.mIncreasingPaintStyle = paintStyle;
    }

    @Override
    public Paint.Style getDecreasingPaintStyle() {
        return mDecreasingPaintStyle;
    }

    /**
     * Sets paint style when open > close
     */
    public void setDecreasingPaintStyle(Paint.Style decreasingPaintStyle) {
        this.mDecreasingPaintStyle = decreasingPaintStyle;
    }

    @Override
    public int getShadowColor() {
        return mShadowColor;
    }

    /**
     * Sets shadow color for all entries
     */
    public void setShadowColor(int shadowColor) {
        this.mShadowColor = shadowColor;
    }

    @Override
    public boolean getShadowColorSameAsCandle() {
        return mShadowColorSameAsCandle;
    }

    /**
     * Sets shadow color to be the same color as the candle color
     */
    public void setShadowColorSameAsCandle(boolean shadowColorSameAsCandle) {
        this.mShadowColorSameAsCandle = shadowColorSameAsCandle;
    }
}
