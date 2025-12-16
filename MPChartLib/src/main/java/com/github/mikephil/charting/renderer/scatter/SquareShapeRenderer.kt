package com.github.mikephil.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.utils.convertDpToPixel

class SquareShapeRenderer : IShapeRenderer {
    override fun renderShape(
        canvas: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler?,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeSize = dataSet.getScatterShapeSize().convertDpToPixel()
        val shapeHalf = shapeSize / 2f
        val shapeHoleSizeHalf = dataSet.getScatterShapeHoleRadius().convertDpToPixel()
        val shapeHoleSize = shapeHoleSizeHalf * 2f
        val shapeStrokeSize = (shapeSize - shapeHoleSize) / 2f
        val shapeStrokeSizeHalf = shapeStrokeSize / 2f

        val shapeHoleColor = dataSet.getScatterShapeHoleColor()

        if (shapeSize > 0.0) {
            renderPaint.style = Paint.Style.STROKE
            renderPaint.strokeWidth = shapeStrokeSize

            canvas.drawRect(
                posX - shapeHoleSizeHalf - shapeStrokeSizeHalf,
                posY - shapeHoleSizeHalf - shapeStrokeSizeHalf,
                posX + shapeHoleSizeHalf + shapeStrokeSizeHalf,
                posY + shapeHoleSizeHalf + shapeStrokeSizeHalf,
                renderPaint
            )

            if (shapeHoleColor != ColorTemplate.COLOR_NONE) {
                renderPaint.style = Paint.Style.FILL

                renderPaint.color = shapeHoleColor
                canvas.drawRect(
                    posX - shapeHoleSizeHalf,
                    posY - shapeHoleSizeHalf,
                    posX + shapeHoleSizeHalf,
                    posY + shapeHoleSizeHalf,
                    renderPaint
                )
            }
        } else {
            renderPaint.style = Paint.Style.FILL

            canvas.drawRect(
                posX - shapeHalf,
                posY - shapeHalf,
                posX + shapeHalf,
                posY + shapeHalf,
                renderPaint
            )
        }
    }
}
