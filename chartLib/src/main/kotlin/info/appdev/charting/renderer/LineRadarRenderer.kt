package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.getSDKInt

abstract class LineRadarRenderer(animator: ChartAnimator, viewPortHandler: ViewPortHandler) :
    LineScatterCandleRadarRenderer(animator, viewPortHandler) {
    /**
     * Draws the provided path in filled mode with the provided drawable.
     *
     * @param canvas
     * @param filledPath
     * @param drawable
     */
    protected fun drawFilledPath(canvas: Canvas, filledPath: Path, drawable: Drawable) {
        if (clipPathSupported()) {
            canvas.withClip(filledPath) {
                drawable.setBounds(
                    viewPortHandler.contentLeft().toInt(),
                    viewPortHandler.contentTop().toInt(),
                    viewPortHandler.contentRight().toInt(),
                    viewPortHandler.contentBottom().toInt()
                )
                drawable.draw(canvas)
            }
        } else {
            throw RuntimeException("Fill-drawables not (yet) supported below API level 18, this code was run on API level ${getSDKInt()}")
        }
    }

    /**
     * Draws the provided path in filled mode with the provided color and alpha.
     * Special thanks to Angelo Suzuki (https://github.com/tinsukE) for this.
     *
     * @param canvas
     * @param filledPath
     * @param fillColor
     * @param fillAlpha
     */
    protected fun drawFilledPath(canvas: Canvas, filledPath: Path, fillColor: Int, fillAlpha: Int) {
        val color = (fillAlpha shl 24) or (fillColor and 0xffffff)

        if (clipPathSupported()) {
            canvas.withClip(filledPath) {
                canvas.drawColor(color)
            }
        } else {
            // save

            val previous = paintRender.style
            val previousColor = paintRender.color

            // set
            paintRender.style = Paint.Style.FILL
            paintRender.color = color

            canvas.drawPath(filledPath, paintRender)

            // restore
            paintRender.color = previousColor
            paintRender.style = previous
        }
    }

    /**
     * Clip path with hardware acceleration only working properly on API level 18 and above.
     */
    private fun clipPathSupported(): Boolean {
        return getSDKInt() >= 18
    }
}
