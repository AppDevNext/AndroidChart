package com.github.mikephil.charting.interfaces.datasets

import android.graphics.DashPathEffect
import com.github.mikephil.charting.data.Entry

interface ILineScatterCandleRadarDataSet<T : Entry?> : IBarLineScatterCandleBubbleDataSet<T> {
    /**
     * Returns true if vertical highlight indicator lines are enabled (drawn)
     */
    val isVerticalHighlightIndicatorEnabled: Boolean

    /**
     * Returns true if vertical highlight indicator lines are enabled (drawn)
     */
    val isHorizontalHighlightIndicatorEnabled: Boolean

    /**
     * Returns the line-width in which highlight lines are to be drawn.
     */
    val highlightLineWidth: Float

    /**
     * Returns the DashPathEffect that is used for highlighting.
     */
    val dashPathEffectHighlight: DashPathEffect?
}
