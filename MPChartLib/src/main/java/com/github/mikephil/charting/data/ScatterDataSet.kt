package com.github.mikephil.charting.data

import com.github.mikephil.charting.charts.ScatterChart.ScatterShape
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.renderer.scatter.ChevronDownShapeRenderer
import com.github.mikephil.charting.renderer.scatter.ChevronUpShapeRenderer
import com.github.mikephil.charting.renderer.scatter.CircleShapeRenderer
import com.github.mikephil.charting.renderer.scatter.CrossShapeRenderer
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer
import com.github.mikephil.charting.renderer.scatter.SquareShapeRenderer
import com.github.mikephil.charting.renderer.scatter.TriangleShapeRenderer
import com.github.mikephil.charting.renderer.scatter.XShapeRenderer
import com.github.mikephil.charting.utils.ColorTemplate

open class ScatterDataSet(yVals: MutableList<Entry>, label: String) : LineScatterCandleRadarDataSet<Entry>(yVals, label), IScatterDataSet {
    /**
     * the size the scattershape will have, in density pixels
     */
    override var scatterShapeSize = 15f

    /**
     * Renderer responsible for rendering this DataSet, default: square
     */
    override var shapeRenderer: IShapeRenderer? = SquareShapeRenderer()

    /**
     * The radius of the hole in the shape (applies to Square, Circle and Triangle)
     * - default: 0.0
     */
    override var scatterShapeHoleRadius = 0f

    /**
     * Color for the hole in the shape.
     * Setting to `ColorTemplate.COLOR_NONE` will behave as transparent.
     * - default: ColorTemplate.COLOR_NONE
     */
    override var scatterShapeHoleColor = ColorTemplate.COLOR_NONE

    override fun copy(): DataSet<Entry> {
        val entries: MutableList<Entry> = ArrayList()
        for (i in mEntries.indices) {
            entries.add(mEntries[i].copy())
        }
        val copied = ScatterDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(scatterDataSet: ScatterDataSet) {
        super.copy(scatterDataSet)
        scatterDataSet.scatterShapeSize = scatterShapeSize
        scatterDataSet.shapeRenderer = shapeRenderer
        scatterDataSet.scatterShapeHoleRadius = scatterShapeHoleRadius
        scatterDataSet.scatterShapeHoleColor = scatterShapeHoleColor
    }

    /**
     * Sets the ScatterShape this DataSet should be drawn with. This will search for an available IShapeRenderer and set this
     * renderer for the DataSet.
     *
     * @param shape
     */
    fun setScatterShape(shape: ScatterShape) {
        shapeRenderer = getRendererForShape(shape)
    }

    companion object {
        fun getRendererForShape(shape: ScatterShape): IShapeRenderer? {
            return when (shape) {
                ScatterShape.SQUARE -> SquareShapeRenderer()
                ScatterShape.CIRCLE -> CircleShapeRenderer()
                ScatterShape.TRIANGLE -> TriangleShapeRenderer()
                ScatterShape.CROSS -> CrossShapeRenderer()
                ScatterShape.X -> XShapeRenderer()
                ScatterShape.CHEVRON_UP -> ChevronUpShapeRenderer()
                ScatterShape.CHEVRON_DOWN -> ChevronDownShapeRenderer()
            }
        }
    }
}
