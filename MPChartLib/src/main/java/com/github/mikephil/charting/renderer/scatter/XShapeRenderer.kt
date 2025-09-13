package com.github.mikephil.charting.renderer.scatter

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * Created by wajdic on 15/06/2016.
 * Created at Time 09:08
 */
class XShapeRenderer : IShapeRenderer {
    override fun renderShape(
        c: Canvas?, dataSet: IScatterDataSet, viewPortHandler: ViewPortHandler,
        posX: Float, posY: Float, renderPaint: Paint
    ) {
        val shapeHalf = Utils.convertDpToPixel(dataSet.scatterShapeSize) / 2f

        renderPaint.style = Paint.Style.STROKE
        renderPaint.strokeWidth = Utils.convertDpToPixel(1f)

        c?.drawLine(
            posX - shapeHalf,
            posY - shapeHalf,
            posX + shapeHalf,
            posY + shapeHalf,
            renderPaint
        )
        c?.drawLine(
            posX + shapeHalf,
            posY - shapeHalf,
            posX - shapeHalf,
            posY + shapeHalf,
            renderPaint
        )
    }
}