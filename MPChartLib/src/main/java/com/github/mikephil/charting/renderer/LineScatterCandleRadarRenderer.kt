package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.mikephil.charting.utils.ViewPortHandler

abstract class LineScatterCandleRadarRenderer(animator: ChartAnimator?, viewPortHandler: ViewPortHandler?) :
    BarLineScatterCandleBubbleRenderer(animator, viewPortHandler) {
    /**
     * path that is used for drawing highlight-lines (drawLines(...) cannot be used because of dashes)
     */
    private val mHighlightLinePath = Path()

    /**
     * Draws vertical & horizontal highlight-lines if enabled.
     *
     * @param c
     * @param x x-position of the highlight line intersection
     * @param y y-position of the highlight line intersection
     * @param set the currently drawn dataset
     */
    protected fun drawHighlightLines(c: Canvas, x: Float, y: Float, set: ILineScatterCandleRadarDataSet<*>) {
        // set color and stroke-width

        paintHighlight.color = set.highLightColor
        paintHighlight.strokeWidth = set.highlightLineWidth

        // draw highlighted lines (if enabled)
        paintHighlight.setPathEffect(set.dashPathEffectHighlight)

        // draw vertical highlight lines
        if (set.isVerticalHighlightIndicatorEnabled) {
            // create vertical path

            mHighlightLinePath.reset()
            mHighlightLinePath.moveTo(x, viewPortHandler.contentTop())
            mHighlightLinePath.lineTo(x, viewPortHandler.contentBottom())

            c.drawPath(mHighlightLinePath, paintHighlight)
        }

        // draw horizontal highlight lines
        if (set.isHorizontalHighlightIndicatorEnabled) {
            // create horizontal path

            mHighlightLinePath.reset()
            mHighlightLinePath.moveTo(viewPortHandler.contentLeft(), y)
            mHighlightLinePath.lineTo(viewPortHandler.contentRight(), y)

            c.drawPath(mHighlightLinePath, paintHighlight)
        }
    }
}
