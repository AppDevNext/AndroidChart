package com.github.mikephil.charting.components

import android.graphics.Paint.Align
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils

/**
 * Created by Philipp Jahoda on 17/09/16.
 */
class Description : ComponentBase() {
    /**
     * Returns the description text.
     *
     * @return
     */
    /**
     * Sets the text to be shown as the description.
     * Never set this to null as this will cause nullpointer exception when drawing with Android Canvas.
     *
     * @param text
     */
    /**
     * the text used in the description
     */
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
     * Returns the text alignment of the description.
     *
     * @return
     */
    /**
     * Sets the text alignment of the description text. Default RIGHT.
     *
     * @param align
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
            this.position = MPPointF.Companion.getInstance(x, y)
        } else {
            position!!.x = x
            position!!.y = y
        }
    }
}
