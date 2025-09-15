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
    var fillColor: Int

    /**
     * Returns the drawable used for filling the area below the line.
     *
     * @return
     */
    var fillDrawable: Drawable?

    /**
     * Returns the alpha value that is used for filling the line surface,
     * default: 85
     *
     * @return
     */
    var fillAlpha: Int

    /**
     * Returns the stroke-width of the drawn line
     *
     * @return
     */
    var lineWidth: Float

    /**
     * Returns true if filled drawing is enabled, false if not
     *
     * @return
     */
    var isDrawFilledEnabled: Boolean
}
