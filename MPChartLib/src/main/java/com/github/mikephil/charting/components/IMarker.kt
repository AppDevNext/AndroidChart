package com.github.mikephil.charting.components

import android.graphics.Canvas
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

interface IMarker {
    /**
     * @return The desired (general) offset you wish the IMarker to have on the x- and y-axis.
     * By returning x: -(width / 2) you will center the IMarker horizontally.
     * By returning y: -(height / 2) you will center the IMarker vertically.
     */
    val offset: MPPointF?

    /**
     * @return The offset for drawing at the specific `point`. This allows conditional adjusting of the Marker position.
     * If you have no adjustments to make, return getOffset().
     *
     * @param posX This is the X position at which the marker wants to be drawn.
     * You can adjust the offset conditionally based on this argument.
     * @param posY This is the X position at which the marker wants to be drawn.
     * You can adjust the offset conditionally based on this argument.
     */
    fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF?

    /**
     * This method enables a specified custom IMarker to update it's content every time the IMarker is redrawn.
     *
     * @param e         The Entry the IMarker belongs to. This can also be any subclass of Entry, like BarEntry or
     * CandleEntry, simply cast it at runtime.
     * @param highlight The highlight object contains information about the highlighted value such as it's dataset-index, the
     * selected range or stack-index (only stacked bar entries).
     */
    fun refreshContent(e: Entry?, highlight: Highlight?)

    /**
     * Draws the IMarker on the given position on the screen with the given Canvas object.
     *
     * @param canvas
     * @param posX
     * @param posY
     */
    fun draw(canvas: Canvas?, posX: Float, posY: Float)
}
