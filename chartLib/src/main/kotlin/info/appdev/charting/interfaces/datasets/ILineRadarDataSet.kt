package info.appdev.charting.interfaces.datasets

import android.graphics.drawable.Drawable
import info.appdev.charting.data.Entry

interface ILineRadarDataSet<T : Entry> : ILineScatterCandleRadarDataSet<T> {
    /**
     * Returns the color that is used for filling the line surface area.
     */
    val fillColor: Int

    /**
     * Returns the drawable used for filling the area below the line.
     */
    val fillDrawable: Drawable?

    /**
     * Returns the alpha value that is used for filling the line surface,
     * default: 85
     */
    val fillAlpha: Int

    /**
     * Returns the stroke-width of the drawn line
     */
    val lineWidth: Float

    /**
     * Returns true if filled drawing is enabled, false if not
     */
    var isDrawFilledEnabled: Boolean

}
