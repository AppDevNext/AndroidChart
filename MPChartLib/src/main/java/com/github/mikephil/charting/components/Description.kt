package com.github.mikephil.charting.components

import android.graphics.Paint.Align
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils

class Description : ComponentBase() {
    /**
     * Sets the text to be shown as the description.
     * Never set this to null as this will cause nullpointer exception when drawing with Android Canvas.
     *
     * @param text
     */
    @JvmField
    var text: String? = "Description Label"

    /**
     * Returns the customized position of the description, or null if none set.
     *
     * @return
     */
    /**
     * the custom position of the description text
     */
    var position: MPPointF? = null
        private set

    /**
     * Sets the text alignment of the description text. Default RIGHT.
     */
    /**
     * the alignment of the description text
     */
    var textAlign: Align? = Align.RIGHT

    init {
        // default size
        mTextSize = Utils.convertDpToPixel(8f)
    }

    /**
     * Sets a custom position for the description text in pixels on the screen.
     *
     * @param x - xcoordinate
     * @param y - ycoordinate
     */
    fun setPosition(x: Float, y: Float) {
        if (this.position == null) {
            this.position = MPPointF.getInstance(x, y)
        } else {
            position!!.x = x
            position!!.y = y
        }
    }
}
