package com.github.mikephil.charting.data

import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.convertDpToPixel

/**
 * DataSet for the CandleStickChart.
 *
 * @author Philipp Jahoda
 */
class CandleDataSet(yVals: MutableList<CandleEntry?>?, label: String?) : LineScatterCandleRadarDataSet<CandleEntry?>(yVals, label), ICandleDataSet {
    /**
     * the width of the shadow of the candle
     */
    private var mShadowWidth = 3f

    /**
     * should the candle bars show?
     * when false, only "ticks" will show
     *
     *
     * - default: true
     */
    private var mShowCandleBar = true

    /**
     * the space between the candle entries, default 0.1f (10%)
     */
    private var mBarSpace = 0.1f

    /**
     * use candle color for the shadow
     */
    private var mShadowColorSameAsCandle = false

    /**
     * paint style when open < close
     * increasing candlesticks are traditionally hollow
     */
    protected var mIncreasingPaintStyle: Paint.Style? = Paint.Style.STROKE

    /**
     * paint style when open > close
     * descreasing candlesticks are traditionally filled
     */
    protected var mDecreasingPaintStyle: Paint.Style? = Paint.Style.FILL

    /**
     * color for open == close
     */
    protected var mNeutralColor: Int = ColorTemplate.COLOR_SKIP

    /**
     * color for open < close
     */
    protected var mIncreasingColor: Int = ColorTemplate.COLOR_SKIP

    /**
     * color for open > close
     */
    protected var mDecreasingColor: Int = ColorTemplate.COLOR_SKIP

    /**
     * shadow line color, set -1 for backward compatibility and uses default
     * color
     */
    protected var mShadowColor: Int = ColorTemplate.COLOR_SKIP

    override fun copy(): DataSet<CandleEntry?> {
        val entries: MutableList<CandleEntry?> = ArrayList<CandleEntry?>()
        for (i in mEntries!!.indices) {
            entries.add(mEntries!!.get(i)!!.copy())
        }
        val copied = CandleDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(candleDataSet: CandleDataSet) {
        super.copy((candleDataSet as BaseDataSet<*>?)!!)
        candleDataSet.mShadowWidth = mShadowWidth
        candleDataSet.mShowCandleBar = mShowCandleBar
        candleDataSet.mBarSpace = mBarSpace
        candleDataSet.mShadowColorSameAsCandle = mShadowColorSameAsCandle
        candleDataSet.highLightColor = highLightColor
        candleDataSet.mIncreasingPaintStyle = mIncreasingPaintStyle
        candleDataSet.mDecreasingPaintStyle = mDecreasingPaintStyle
        candleDataSet.mNeutralColor = mNeutralColor
        candleDataSet.mIncreasingColor = mIncreasingColor
        candleDataSet.mDecreasingColor = mDecreasingColor
        candleDataSet.mShadowColor = mShadowColor
    }

    override fun calcMinMax(entry: CandleEntry?) {
        entry?.let {
            if (entry.low < yMin) yMin = entry.low

            if (entry.high > yMax) yMax = entry.high
        }
        calcMinMaxX(entry)
    }

    override fun calcMinMaxY(entry: CandleEntry?) {
        entry?.let {
            if (entry.high < yMin) yMin = entry.high

            if (entry.high > yMax) yMax = entry.high

            if (entry.low < yMin) yMin = entry.low

            if (entry.low > yMax) yMax = entry.low
        }
    }

    /**
     * Sets the space that is left out on the left and right side of each
     * candle, default 0.1f (10%), max 0.45f, min 0f
     */
    fun setBarSpace(space: Float) {
        var space = space
        if (space < 0f) space = 0f
        if (space > 0.45f) space = 0.45f

        mBarSpace = space
    }

    override fun getBarSpace(): Float {
        return mBarSpace
    }

    /**
     * Sets the width of the candle-shadow-line in pixels. Default 3f.
     */
    fun setShadowWidth(width: Float) {
        mShadowWidth = width.convertDpToPixel()
    }

    override fun getShadowWidth(): Float {
        return mShadowWidth
    }

    /**
     * Sets whether the candle bars should show?
     */
    fun setShowCandleBar(showCandleBar: Boolean) {
        mShowCandleBar = showCandleBar
    }

    override fun getShowCandleBar(): Boolean {
        return mShowCandleBar
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open == close.
     */
    fun setNeutralColor(color: Int) {
        mNeutralColor = color
    }

    override fun getNeutralColor(): Int {
        return mNeutralColor
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open <= close.
     */
    fun setIncreasingColor(color: Int) {
        mIncreasingColor = color
    }

    override fun getIncreasingColor(): Int {
        return mIncreasingColor
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open > close.
     */
    fun setDecreasingColor(color: Int) {
        mDecreasingColor = color
    }

    override fun getDecreasingColor(): Int {
        return mDecreasingColor
    }

    override fun getIncreasingPaintStyle(): Paint.Style? {
        return mIncreasingPaintStyle
    }

    /**
     * Sets paint style when open < close
     */
    fun setIncreasingPaintStyle(paintStyle: Paint.Style?) {
        this.mIncreasingPaintStyle = paintStyle
    }

    override fun getDecreasingPaintStyle(): Paint.Style? {
        return mDecreasingPaintStyle
    }

    /**
     * Sets paint style when open > close
     */
    fun setDecreasingPaintStyle(decreasingPaintStyle: Paint.Style?) {
        this.mDecreasingPaintStyle = decreasingPaintStyle
    }

    override fun getShadowColor(): Int {
        return mShadowColor
    }

    /**
     * Sets shadow color for all entries
     */
    fun setShadowColor(shadowColor: Int) {
        this.mShadowColor = shadowColor
    }

    override fun getShadowColorSameAsCandle(): Boolean {
        return mShadowColorSameAsCandle
    }

    /**
     * Sets shadow color to be the same color as the candle color
     */
    fun setShadowColorSameAsCandle(shadowColorSameAsCandle: Boolean) {
        this.mShadowColorSameAsCandle = shadowColorSameAsCandle
    }
}
