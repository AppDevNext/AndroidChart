package info.appdev.charting.utils

/**
 * Transformer class for the HorizontalBarChart.
 */
class TransformerHorizontalBarChart(viewPortHandler: ViewPortHandler) : Transformer(viewPortHandler) {
    /**
     * Prepares the matrix that contains all offsets.
     */
    override fun prepareMatrixOffset(inverted: Boolean) {
        offsetMatrix.reset()

        if (!inverted) {
            offsetMatrix.postTranslate(
                viewPortHandler.offsetLeft(),
                viewPortHandler.chartHeight - viewPortHandler.offsetBottom()
            )
        } else {
            offsetMatrix.setTranslate(
                -(viewPortHandler.chartWidth - viewPortHandler.offsetRight()),
                viewPortHandler.chartHeight - viewPortHandler.offsetBottom()
            )
            offsetMatrix.postScale(-1.0f, 1.0f)
        }
    }
}
