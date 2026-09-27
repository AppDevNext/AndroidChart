package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.BubbleEntryFloat

interface IBubbleDataSet : IBarLineScatterCandleBubbleDataSet<BubbleEntryFloat> {
    val maxSize: Float

    val isNormalizeSizeEnabled: Boolean

    /**
     * Sets the width of the circle that surrounds the bubble when highlighted in dp.
     */
    var highlightCircleWidth: Float
}
