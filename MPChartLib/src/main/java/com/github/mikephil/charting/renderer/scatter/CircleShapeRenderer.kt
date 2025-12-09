package com.github.mikephil.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class CircleShapeRenderer : IShapeRenderer {
    override fun renderShape(
        canvas: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler?,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeSize = Utils.convertDpToPixel(dataSet.getScatterShapeSize())
        val shapeHalf = shapeSize / 2f
        val shapeHoleSizeHalf = Utils.convertDpToPixel(dataSet.getScatterShapeHoleRadius())
        val shapeHoleSize = shapeHoleSizeHalf * 2f
        val shapeStrokeSize = (shapeSize - shapeHoleSize) / 2f
        val shapeStrokeSizeHalf = shapeStrokeSize / 2f

        val shapeHoleColor = dataSet.getScatterShapeHoleColor()

        if (shapeSize > 0.0) {
            renderPaint.style = Paint.Style.STROKE
            renderPaint.strokeWidth = shapeStrokeSize

            canvas.drawCircle(
                posX,
                posY,
                shapeHoleSizeHalf + shapeStrokeSizeHalf,
                renderPaint
            )

            if (shapeHoleColor != ColorTemplate.COLOR_NONE) {
                renderPaint.style = Paint.Style.FILL

                renderPaint.color = shapeHoleColor
                canvas.drawCircle(
                    posX,
                    posY,
                    shapeHoleSizeHalf,
                    renderPaint
                )
            }
        } else {
            renderPaint.style = Paint.Style.FILL

            canvas.drawCircle(
                posX,
                posY,
                shapeHalf,
                renderPaint
            )
        }
    }
}
