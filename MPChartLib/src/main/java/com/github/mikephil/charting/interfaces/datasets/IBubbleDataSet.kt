package com.github.mikephil.charting.interfaces.datasets

import com.github.mikephil.charting.data.BubbleEntry

interface IBubbleDataSet : IBarLineScatterCandleBubbleDataSet<BubbleEntry> {
    val maxSize: Float

    val isNormalizeSizeEnabled: Boolean

    /**
     * Returns the width of the highlight-circle that surrounds the bubble
     */
    /**
     * Sets the width of the circle that surrounds the bubble when highlighted,
     * in dp.
     */
    var highlightCircleWidth: Float
}
