package info.appdev.charting.components

import android.graphics.Paint.Align
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.convertDpToPixel

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
     */
    /**
     * the custom position of the description text
     */
    var position: PointF? = null
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
        mTextSize = 8f.convertDpToPixel()
    }

    /**
     * Sets a custom position for the description text in pixels on the screen.
     *
     * @param x - xcoordinate
     * @param y - ycoordinate
     */
    fun setPosition(x: Float, y: Float) {
        if (this.position == null) {
            this.position = PointF.getInstance(x, y)
        } else {
            position!!.x = x
            position!!.y = y
        }
    }
}
