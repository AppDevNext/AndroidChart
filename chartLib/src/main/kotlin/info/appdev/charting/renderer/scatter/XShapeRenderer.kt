package info.appdev.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import info.appdev.charting.interfaces.datasets.IScatterDataSet
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel

class XShapeRenderer : IShapeRenderer {
    override fun renderShape(
        canvas: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler?,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.scatterShapeSize.convertDpToPixel() / 2f

        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = 1f.convertDpToPixel()

        canvas.drawLine(
            posX - shapeHalf,
            posY - shapeHalf,
            posX + shapeHalf,
            posY + shapeHalf,
            renderPaint
        )
        canvas.drawLine(
            posX + shapeHalf,
            posY - shapeHalf,
            posX - shapeHalf,
            posY + shapeHalf,
            renderPaint
        )
    }
}