package com.github.mikephil.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.utils.convertDpToPixel

class ChevronDownShapeRenderer : IShapeRenderer {
    override fun renderShape(
        canvas: Canvas, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler?,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = dataSet.getScatterShapeSize().convertDpToPixel() / 2f

        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = 1f.convertDpToPixel()

        canvas.drawLine(
            posX,
            posY + (2 * shapeHalf),
            posX + (2 * shapeHalf),
            posY,
            renderPaint
        )

        canvas.drawLine(
            posX,
            posY + (2 * shapeHalf),
            posX - (2 * shapeHalf),
            posY,
            renderPaint
        )
    }
}
