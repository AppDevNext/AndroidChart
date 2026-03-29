package info.appdev.charting.interfaces.datasets

import android.graphics.DashPathEffect
import info.appdev.charting.data.BaseEntry

interface ILineScatterCandleRadarDataSet<T : BaseEntry<Float>> : IBarLineScatterCandleBubbleDataSet<T> {
    /**
     * Returns true if vertical highlight indicator lines are enabled (drawn)
     */
    val isVerticalHighlightIndicator: Boolean

    /**
     * Returns true if vertical highlight indicator lines are enabled (drawn)
     */
    val isHorizontalHighlightIndicator: Boolean

    /**
     * Returns the line-width in which highlight lines are to be drawn.
     */
    val highlightLineWidth: Float

    /**
     * Returns the DashPathEffect that is used for highlighting.
     */
    val dashPathEffectHighlight: DashPathEffect?
}
