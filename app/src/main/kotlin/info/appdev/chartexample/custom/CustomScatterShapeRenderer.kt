package info.appdev.chartexample.custom

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * Custom shape renderer that draws a single line.
 */
class CustomScatterShapeRenderer : IShapeRenderer {
    override fun renderShape(
        canvas: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler?,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = Utils.convertDpToPixel(dataSet.getScatterShapeSize()) / 2f

        canvas.drawLine(
            posX - shapeHalf,
            posY - shapeHalf,
            posX + shapeHalf,
            posY + shapeHalf,
            renderPaint
        )
    }
}
