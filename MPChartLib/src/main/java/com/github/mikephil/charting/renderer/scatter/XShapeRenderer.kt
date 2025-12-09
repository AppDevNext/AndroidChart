package com.github.mikephil.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class XShapeRenderer : IShapeRenderer {
    override fun renderShape(
        canvas: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler?,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = Utils.convertDpToPixel(dataSet.getScatterShapeSize()) / 2f

        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = Utils.convertDpToPixel(1f)

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