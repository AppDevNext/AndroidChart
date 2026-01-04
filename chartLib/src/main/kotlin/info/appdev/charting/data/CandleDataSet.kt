package info.appdev.charting.data

import android.graphics.Paint
import androidx.annotation.ColorInt
import info.appdev.charting.interfaces.datasets.ICandleDataSet
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.convertDpToPixel

/**
 * DataSet for the CandleStickChart.
 */
open class CandleDataSet(yVals: MutableList<CandleEntry>, label: String = "") : LineScatterCandleRadarDataSet<CandleEntry>(yVals, label), ICandleDataSet {
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
     * decreasing candlesticks are traditionally filled
     */
    protected var mDecreasingPaintStyle: Paint.Style? = Paint.Style.FILL

    /**
     * color for open == close
     */
    @ColorInt
    protected var mNeutralColor: Int = ColorTemplate.COLOR_SKIP

    /**
     * color for open < close
     */
    @ColorInt
    protected var mIncreasingColor: Int = ColorTemplate.COLOR_SKIP

    /**
     * color for open > close
     */
    @ColorInt
    protected var mDecreasingColor: Int = ColorTemplate.COLOR_SKIP

    /**
     * shadow line color, set -1 for backward compatibility and uses default color
     */
    @ColorInt
    protected var mShadowColor: Int = ColorTemplate.COLOR_SKIP

    override fun copy(): DataSet<CandleEntry> {
        val entries: MutableList<CandleEntry> = mutableListOf()
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

    override fun calcMinMax(entry: CandleEntry) {
        entry.let {
            if (entry.low < yMin) yMin = entry.low

            if (entry.high > yMax) yMax = entry.high
        }
        calcMinMaxX(entry)
    }

    override fun calcMinMaxY(entry: CandleEntry) {
        entry.let {
            if (entry.high < yMin) yMin = entry.high

            if (entry.high > yMax) yMax = entry.high

            if (entry.low < yMin) yMin = entry.low

            if (entry.low > yMax) yMax = entry.low
        }
    }

    override var barSpace: Float
        get() = mBarSpace
        set(value) {
            /**
             * Sets the space that is left out on the left and right side of each
             * candle, default 0.1f (10%), max 0.45f, min 0f
             */
            var space = value
            if (space < 0f) space = 0f
            if (space > 0.45f) space = 0.45f

            mBarSpace = space
        }

    override var showCandleBar: Boolean
        get() = mShowCandleBar
        set(value) {
            mShowCandleBar = value
        }
    override var shadowWidth: Float
        get() = mShadowWidth
        set(value) {
            mShadowWidth = value.convertDpToPixel()
        }
    override var shadowColor: Int
        get() = mShadowColor
        set(value) {
            mShadowColor = value
        }
    override var neutralColor: Int
        get() = mNeutralColor
        set(value) {
            /**
             * Sets the one and ONLY color that should be used for this DataSet when
             * open == close.
             */
            mNeutralColor = value
        }
    override var increasingColor: Int
        get() = mIncreasingColor
        set(value) {
            /**
             * Sets the one and ONLY color that should be used for this DataSet when
             * open <= close.
             */
            mIncreasingColor = value
        }
    override var decreasingColor: Int
        get() = mDecreasingColor
        set(value) {
            /**
             * Sets the one and ONLY color that should be used for this DataSet when
             * open > close.
             */
            mDecreasingColor = value
        }
    override var increasingPaintStyle: Paint.Style?
        get() = mIncreasingPaintStyle
        set(value) {
            /**
             * Sets paint style when open < close
             */
            mIncreasingPaintStyle = value
        }
    override var decreasingPaintStyle: Paint.Style?
        get() = mDecreasingPaintStyle
        set(value) {
            /**
             * Sets paint style when open > close
             */
            mDecreasingPaintStyle = value
        }
    override var shadowColorSameAsCandle: Boolean
        get() = mShadowColorSameAsCandle
        set(value) {
            /**
             * Sets shadow color to be the same color as the candle color
             */
            mShadowColorSameAsCandle = value
        }
}
