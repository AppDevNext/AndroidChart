package info.appdev.charting.data

import android.graphics.drawable.Drawable


@Deprecated(
    message = "The replacement is BarEntryFloat, or use BarEntryDouble for higher precision. BarEntry is retained for backward compatibility but will be removed in a future version.",
    replaceWith = ReplaceWith("BarEntryFloat", "info.appdev.charting.data.BarEntryFloat")
)
open class BarEntry : BarEntryFloat {

    /**
     * Constructor for normal bars (not stacked).
     */
    constructor(x: Float, y: Float) : super(x, y)

    /**
     * Constructor for normal bars (not stacked).
     *
     * @param x
     * @param y
     * @param data - Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, data: Any?) : super(x, y, data)

    /**
     * Constructor for normal bars (not stacked).
     *
     * @param x
     * @param y
     * @param icon - icon image
     */
    constructor(x: Float, y: Float, icon: Drawable?) : super(x, y, icon)

    /**
     * Constructor for normal bars (not stacked).
     *
     * @param x
     * @param y
     * @param icon - icon image
     * @param data - Spot for additional data this Entry represents.
     */
    constructor(x: Float, y: Float, icon: Drawable?, data: Any?) : super(x, y, icon, data)

}
