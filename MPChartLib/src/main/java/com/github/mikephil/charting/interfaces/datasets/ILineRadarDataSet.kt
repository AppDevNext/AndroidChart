package com.github.mikephil.charting.interfaces.datasets

import android.graphics.drawable.Drawable
import com.github.mikephil.charting.data.Entry

/**
 * Created by Philipp Jahoda on 21/10/15.
 */
interface ILineRadarDataSet<T : Entry> : ILineScatterCandleRadarDataSet<T> {
    /**
     * Returns the color that is used for filling the line surface area.
     *
     * @return
     */
    val fillColor: Int

    /**
     * Returns the drawable used for filling the area below the line.
     *
     * @return
     */
    val fillDrawable: Drawable?

    /**
     * Returns the alpha value that is used for filling the line surface,
     * default: 85
     *
     * @return
     */
    val fillAlpha: Int

    /**
     * Returns the stroke-width of the drawn line
     *
     * @return
     */
    val lineWidth: Float

    /**
     * Returns true if filled drawing is enabled, false if not
     *
     * @return
     */
    val isDrawFilledEnabled: Boolean

    /**
     * Set to true if the DataSet should be drawn filled (surface), and not just
     * as a line, disabling this will give great performance boost. Please note that this method
     * uses the canvas.clipPath(...) method for drawing the filled area.
     * For devices with API level < 18 (Android 4.3), hardware acceleration of the chart should
     * be turned off. Default: false
     *
     * @param enabled
     */
    fun setDrawFilled(enabled: Boolean)
}
