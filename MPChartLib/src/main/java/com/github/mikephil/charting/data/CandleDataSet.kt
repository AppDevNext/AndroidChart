package com.github.mikephil.charting.data

import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils

/**
 * DataSet for the CandleStickChart.
 *
 * @author Philipp Jahoda
 */
class CandleDataSet(yVals: MutableList<CandleEntry>, label: String) : LineScatterCandleRadarDataSet<CandleEntry>(yVals, label), ICandleDataSet {
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

    override fun copy(): DataSet<CandleEntry> {
        val entries: MutableList<CandleEntry> = ArrayList()
        for (i in mEntries.indices) {
            entries.add(mEntries[i].copy())
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

    override fun calcMinMax(e: CandleEntry) {
        if (e.low < mYMin) mYMin = e.low

        if (e.high > mYMax) mYMax = e.high

        calcMinMaxX(e)
    }

    override fun calcMinMaxY(e: CandleEntry) {
        if (e.high < mYMin) mYMin = e.high

        if (e.high > mYMax) mYMax = e.high

        if (e.low < mYMin) mYMin = e.low

        if (e.low > mYMax) mYMax = e.low
    }

    /**
     * Sets the space that is left out on the left and right side of each
     * candle, default 0.1f (10%), max 0.45f, min 0f
     *
     * @param space
     */
    fun setBarSpace(space: Float) {
        var space = space
        if (space < 0f) space = 0f
        if (space > 0.45f) space = 0.45f

        mBarSpace = space
    }

    override val barSpace: Float
        get() = mBarSpace

    /**
     * Sets the width of the candle-shadow-line in pixels. Default 3f.
     *
     * @param width
     */
    fun setShadowWidth(width: Float) {
        mShadowWidth = Utils.convertDpToPixel(width)
    }

    override val shadowWidth: Float
        get() = mShadowWidth

    /**
     * Sets whether the candle bars should show?
     *
     * @param showCandleBar
     */
    fun setShowCandleBar(showCandleBar: Boolean) {
        mShowCandleBar = showCandleBar
    }

    override val showCandleBar: Boolean
        get() = mShowCandleBar

    // TODO
    /**
     * It is necessary to implement ColorsList class that will encapsulate
     * colors list functionality, because It's wrong to copy paste setColor,
     * addColor, ... resetColors for each time when we want to add a coloring
     * options for one of objects
     *
     * @author Mesrop
     */
    /** BELOW THIS COLOR HANDLING  */
    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open == close.
     *
     * @param color
     */
    fun setNeutralColor(color: Int) {
        mNeutralColor = color
    }

    override val neutralColor: Int
        get() = mNeutralColor

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open <= close.
     *
     * @param color
     */
    fun setIncreasingColor(color: Int) {
        mIncreasingColor = color
    }

    override val increasingColor: Int
        get() = mIncreasingColor

    /**
     * Sets the one and ONLY color that should be used for this DataSet when
     * open > close.
     *
     * @param color
     */
    fun setDecreasingColor(color: Int) {
        mDecreasingColor = color
    }

    override val decreasingColor: Int
        get() = mDecreasingColor

    override val increasingPaintStyle: Paint.Style?
        get() = mIncreasingPaintStyle

    /**
     * Sets paint style when open < close
     *
     * @param paintStyle
     */
    fun setIncreasingPaintStyle(paintStyle: Paint.Style?) {
        this.mIncreasingPaintStyle = paintStyle
    }

    override val decreasingPaintStyle: Paint.Style?
        get() = mDecreasingPaintStyle

    /**
     * Sets paint style when open > close
     *
     * @param decreasingPaintStyle
     */
    fun setDecreasingPaintStyle(decreasingPaintStyle: Paint.Style?) {
        this.mDecreasingPaintStyle = decreasingPaintStyle
    }

    override val shadowColor: Int
        get() = mShadowColor

    /**
     * Sets shadow color for all entries
     *
     * @param shadowColor
     */
    fun setShadowColor(shadowColor: Int) {
        this.mShadowColor = shadowColor
    }

    override val shadowColorSameAsCandle: Boolean
        get() = mShadowColorSameAsCandle

    /**
     * Sets shadow color to be the same color as the candle color
     *
     * @param shadowColorSameAsCandle
     */
    fun setShadowColorSameAsCandle(shadowColorSameAsCandle: Boolean) {
        this.mShadowColorSameAsCandle = shadowColorSameAsCandle
    }
}
