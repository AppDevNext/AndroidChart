package info.appdev.charting.data

import info.appdev.charting.utils.ChartIcon

@Deprecated(
    message = "The replacement is BubbleEntryFloat, or use BubbleEntryDouble for higher precision. BubbleEntry is retained for backward compatibility but will be removed in a future version.",
    replaceWith = ReplaceWith("BubbleEntryFloat", "info.appdev.charting.data.BubbleEntryFloat")
)
class BubbleEntry : BubbleEntryFloat {

    /**
     * Constructor.
     *
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param size The size of the bubble.
     */
    constructor(x: Float, y: Float, size: Float) : super(x, y, size) {
        this.size = size
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param size The size of the bubble.
     * @param data Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, size: Float, data: Any?) : super(x, y, size, data) {
        this.size = size
    }

    /**
     * Constructor.
     *
     * @param x The value on the x-axis.
     * @param y The value on the y-axis.
     * @param size The size of the bubble.
     * @param icon Icon image
     */
    constructor(x: Float, y: Float, size: Float, icon: ChartIcon?) : super(x, y, size, icon) {
        this.size = size
    }
}
