package info.appdev.charting.highlight

interface IHighlighter {
    /**
     * Returns a Highlight object corresponding to the given x- and y- touch positions in pixels.
     */
    fun getHighlight(x: Float, y: Float): Highlight?
}
