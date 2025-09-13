package com.github.mikephil.charting.highlight

/**
 * Created by philipp on 10/06/16.
 */
interface IHighlighter {
    /**
     * Returns a Highlight object corresponding to the given x- and y- touch positions in pixels.
     *
     * @param x
     * @param y
     * @return
     */
    fun getHighlight(x: Float, y: Float): Highlight?
}
