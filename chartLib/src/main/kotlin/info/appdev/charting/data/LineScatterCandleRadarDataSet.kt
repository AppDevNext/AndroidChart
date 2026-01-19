package info.appdev.charting.data

import android.graphics.DashPathEffect
import info.appdev.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import info.appdev.charting.utils.convertDpToPixel

abstract class LineScatterCandleRadarDataSet<T, N_XAxis>(yVals: MutableList<T>, label: String) : BarLineScatterCandleBubbleDataSet<T, N_XAxis>(yVals, label),
    ILineScatterCandleRadarDataSet<T, N_XAxis> where T : BaseEntry<N_XAxis>, N_XAxis : Number, N_XAxis : Comparable<N_XAxis> {
    override var isVerticalHighlightIndicator: Boolean = true
    override var isHorizontalHighlightIndicator: Boolean = true

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
    @Deprecated("Use property isHorizontalHighlightIndicator instead")
    fun setDrawHorizontalHighlightIndicator(enabled: Boolean) {
        this.isHorizontalHighlightIndicator = enabled
    }

    /**
     * Enables / disables the vertical highlight-indicator. If disabled, the indicator is not drawn.
     */
    @Deprecated("Use property isVerticalHighlightIndicator instead")
    fun setDrawVerticalHighlightIndicator(enabled: Boolean) {
        this.isVerticalHighlightIndicator = enabled
    }

    /**
     * Enables / disables both vertical and horizontal highlight-indicators.
     */
    fun setDrawHighlightIndicators(enabled: Boolean) {
        this.isHorizontalHighlightIndicator = enabled
        this.isVerticalHighlightIndicator = enabled
    }

    override var highlightLineWidth: Float
        get() = mHighlightLineWidth
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
            floatArrayOf(lineLength, spaceLength),
            phase
        )
    }

    /**
     * Disables the highlight-line to be drawn in dashed mode.
     */
    fun disableDashedHighlightLine() {
        this.dashPathEffectHighlight = null
    }

    /**
     * Returns true if the dashed-line effect is enabled for highlight lines, false if not.
     * Default: disabled
     */
    val isDashedHighlightLineEnabled: Boolean
        get() = this.dashPathEffectHighlight != null

    protected fun copy(lineScatterCandleRadarDataSet: LineScatterCandleRadarDataSet<*, *>) {
        super.copy((lineScatterCandleRadarDataSet as BaseDataSet<*, *>?)!!)
        lineScatterCandleRadarDataSet.isHorizontalHighlightIndicator = this.isHorizontalHighlightIndicator
        lineScatterCandleRadarDataSet.isVerticalHighlightIndicator = this.isVerticalHighlightIndicator
        lineScatterCandleRadarDataSet.mHighlightLineWidth = mHighlightLineWidth
        lineScatterCandleRadarDataSet.dashPathEffectHighlight = this.dashPathEffectHighlight
    }
}
