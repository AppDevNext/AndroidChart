package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.BubbleEntry

interface IBubbleDataSet : IBarLineScatterCandleBubbleDataSet<BubbleEntry> {
    val maxSize: Float

    val isNormalizeSizeEnabled: Boolean

    /**
     * Sets the width of the circle that surrounds the bubble when highlighted in dp.
     */
    var highlightCircleWidth: Float
}
