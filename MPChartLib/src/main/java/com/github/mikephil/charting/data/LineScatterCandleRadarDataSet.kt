package com.github.mikephil.charting.data

import android.graphics.DashPathEffect
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.mikephil.charting.utils.convertDpToPixel

abstract class LineScatterCandleRadarDataSet<T : Entry>(yVals: MutableList<T>?, label: String) : BarLineScatterCandleBubbleDataSet<T>(yVals, label),
    ILineScatterCandleRadarDataSet<T> {
    override var isVerticalHighlightIndicatorEnabled: Boolean = true
        protected set
    override var isHorizontalHighlightIndicatorEnabled: Boolean = true
        protected set

    /** the width of the highlight indicator lines  */
    protected var mHighlightLineWidth: Float

    /** the path effect for dashed highlight-lines  */
    override var dashPathEffectHighlight: DashPathEffect? = null
        protected set


    init {
        mHighlightLineWidth = 0.5f.convertDpToPixel()
    }

    /**
     * Enables / disables the horizontal highlight-indicator. If disabled, the indicator is not drawn.
     */
    fun setDrawHorizontalHighlightIndicator(enabled: Boolean) {
        this.isHorizontalHighlightIndicatorEnabled = enabled
    }

    /**
     * Enables / disables the vertical highlight-indicator. If disabled, the indicator is not drawn.
     */
    fun setDrawVerticalHighlightIndicator(enabled: Boolean) {
        this.isVerticalHighlightIndicatorEnabled = enabled
    }

    /**
     * Enables / disables both vertical and horizontal highlight-indicators.
     */
    fun setDrawHighlightIndicators(enabled: Boolean) {
        setDrawVerticalHighlightIndicator(enabled)
        setDrawHorizontalHighlightIndicator(enabled)
    }

    override var highlightLineWidth: Float
        get() = mHighlightLineWidth
        /**
         * Sets the width of the highlight line in dp.
         */
        set(width) {
            mHighlightLineWidth = width.convertDpToPixel()
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
         */
        get() = if (this.dashPathEffectHighlight == null) false else true

    protected fun copy(lineScatterCandleRadarDataSet: LineScatterCandleRadarDataSet<*>) {
        super.copy((lineScatterCandleRadarDataSet as BaseDataSet<*>?)!!)
        lineScatterCandleRadarDataSet.isHorizontalHighlightIndicatorEnabled = this.isHorizontalHighlightIndicatorEnabled
        lineScatterCandleRadarDataSet.isVerticalHighlightIndicatorEnabled = this.isVerticalHighlightIndicatorEnabled
        lineScatterCandleRadarDataSet.mHighlightLineWidth = mHighlightLineWidth
        lineScatterCandleRadarDataSet.dashPathEffectHighlight = this.dashPathEffectHighlight
    }
}
