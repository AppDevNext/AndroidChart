package com.github.mikephil.charting.data

import android.graphics.DashPathEffect
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.mikephil.charting.utils.Utils

/**
 * Created by Philipp Jahoda on 11/07/15.
 */
abstract class LineScatterCandleRadarDataSet<T : Entry>(yVals: MutableList<T>, label: String) : BarLineScatterCandleBubbleDataSet<T>(yVals, label),
    ILineScatterCandleRadarDataSet<T> {
    override var isVerticalHighlightIndicatorEnabled: Boolean = true
        protected set
    override var isHorizontalHighlightIndicatorEnabled: Boolean = true
        protected set

    /** the width of the highlight indicator lines  */
    protected var mHighlightLineWidth: Float = 0.5f

    /** the path effect for dashed highlight-lines  */
    override var dashPathEffectHighlight: DashPathEffect? = null
        protected set


    init {
        mHighlightLineWidth = Utils.convertDpToPixel(0.5f)
    }

    /**
     * Enables / disables the horizontal highlight-indicator. If disabled, the indicator is not drawn.
     * @param enabled
     */
    fun setDrawHorizontalHighlightIndicator(enabled: Boolean) {
        this.isHorizontalHighlightIndicatorEnabled = enabled
    }

    /**
     * Enables / disables the vertical highlight-indicator. If disabled, the indicator is not drawn.
     * @param enabled
     */
    fun setDrawVerticalHighlightIndicator(enabled: Boolean) {
        this.isVerticalHighlightIndicatorEnabled = enabled
    }

    /**
     * Enables / disables both vertical and horizontal highlight-indicators.
     * @param enabled
     */
    fun setDrawHighlightIndicators(enabled: Boolean) {
        setDrawVerticalHighlightIndicator(enabled)
        setDrawHorizontalHighlightIndicator(enabled)
    }

    override var highlightLineWidth: Float
        get() = mHighlightLineWidth
        /**
         * Sets the width of the highlight line in dp.
         * @param width
         */
        set(width) {
            mHighlightLineWidth = Utils.convertDpToPixel(width)
        }

    /**
     * Enables the highlight-line to be drawn in dashed mode, e.g. like this "- - - - - -"
     *
     * @param lineLength the length of the line pieces
     * @param spaceLength the length of space inbetween the line-pieces
     * @param phase offset, in degrees (normally, use 0)
     */
    fun enableDashedHighlightLine(lineLength: Float, spaceLength: Float, phase: Float) {
        this.dashPathEffectHighlight = DashPathEffect(
            floatArrayOf(
                lineLength, spaceLength
            ), phase
        )
    }

    /**
     * Disables the highlight-line to be drawn in dashed mode.
     */
    fun disableDashedHighlightLine() {
        this.dashPathEffectHighlight = null
    }

    val isDashedHighlightLineEnabled: Boolean
        /**
         * Returns true if the dashed-line effect is enabled for highlight lines, false if not.
         * Default: disabled
         *
         * @return
         */
        get() = this.dashPathEffectHighlight != null

    protected fun copy(lineScatterCandleRadarDataSet: LineScatterCandleRadarDataSet<*>) {
        super.copy(lineScatterCandleRadarDataSet)
        lineScatterCandleRadarDataSet.isHorizontalHighlightIndicatorEnabled = this.isHorizontalHighlightIndicatorEnabled
        lineScatterCandleRadarDataSet.isVerticalHighlightIndicatorEnabled = this.isVerticalHighlightIndicatorEnabled
        lineScatterCandleRadarDataSet.mHighlightLineWidth = mHighlightLineWidth
        lineScatterCandleRadarDataSet.dashPathEffectHighlight = this.dashPathEffectHighlight
    }
}
