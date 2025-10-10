package com.github.mikephil.charting.utils

/**
 * Transformer class for the HorizontalBarChart.
 *
 * @author Philipp Jahoda
 */
class TransformerHorizontalBarChart(viewPortHandler: ViewPortHandler) : Transformer(viewPortHandler) {
    /**
     * Prepares the matrix that contains all offsets.
     *
     * @param inverted
     */
    override fun prepareMatrixOffset(inverted: Boolean) {
        offsetMatrix.reset()

        // offset.postTranslate(mOffsetLeft, getHeight() - mOffsetBottom);
        if (!inverted) offsetMatrix.postTranslate(
            mViewPortHandler.offsetLeft(),
            mViewPortHandler.chartHeight - mViewPortHandler.offsetBottom()
        )
        else {
            offsetMatrix
                .setTranslate(
                    -(mViewPortHandler.chartWidth - mViewPortHandler.offsetRight()),
                    mViewPortHandler.chartHeight - mViewPortHandler.offsetBottom()
                )
            offsetMatrix.postScale(-1.0f, 1.0f)
        }

        // mMatrixOffset.set(offset);

        // mMatrixOffset.reset();
        //
        // mMatrixOffset.postTranslate(mOffsetLeft, getHeight() -
        // mOffsetBottom);
    }
}
