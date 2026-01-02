package info.appdev.charting.data

import androidx.annotation.ColorInt
import info.appdev.charting.charts.ScatterChart.ScatterShape
import info.appdev.charting.interfaces.datasets.IScatterDataSet
import info.appdev.charting.renderer.scatter.ChevronDownShapeRenderer
import info.appdev.charting.renderer.scatter.ChevronUpShapeRenderer
import info.appdev.charting.renderer.scatter.CircleShapeRenderer
import info.appdev.charting.renderer.scatter.CrossShapeRenderer
import info.appdev.charting.renderer.scatter.IShapeRenderer
import info.appdev.charting.renderer.scatter.SquareShapeRenderer
import info.appdev.charting.renderer.scatter.TriangleShapeRenderer
import info.appdev.charting.renderer.scatter.XShapeRenderer
import info.appdev.charting.utils.ColorTemplate

open class ScatterDataSet(yVals: MutableList<Entry>?, label: String = "") : LineScatterCandleRadarDataSet<Entry>(yVals, label), IScatterDataSet {
    /**
     * the size the scatterShape will have, in density pixels
     */
    private var shapeSize = 15f

    /**
     * Renderer responsible for rendering this DataSet, default: square
     */
    protected var mShapeRenderer: IShapeRenderer? = SquareShapeRenderer()

    /**
     * The radius of the hole in the shape (applies to Square, Circle and Triangle)
     * - default: 0.0
     */
    private var mScatterShapeHoleRadius = 0f

    /**
     * Color for the hole in the shape.
     * Setting to `ColorTemplate.COLOR_NONE` will behave as transparent.
     * - default: ColorTemplate.COLOR_NONE
     */
    @ColorInt
    private var mScatterShapeHoleColor = ColorTemplate.COLOR_NONE

    override fun copy(): DataSet<Entry> {
        val entries: MutableList<Entry> = mutableListOf()
        mEntries?.let {
            for (i in it.indices) {
                entries.add(it[i].copy())
            }
        }
        val copied = ScatterDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(scatterDataSet: ScatterDataSet) {
        super.copy((scatterDataSet as BaseDataSet<*>?)!!)
        scatterDataSet.shapeSize = shapeSize
        scatterDataSet.mShapeRenderer = mShapeRenderer
        scatterDataSet.mScatterShapeHoleRadius = mScatterShapeHoleRadius
        scatterDataSet.mScatterShapeHoleColor = mScatterShapeHoleColor
    }

    /**
     * Sets the ScatterShape this DataSet should be drawn with. This will search for an available IShapeRenderer and set this
     * renderer for the DataSet.
     */
    fun setScatterShape(shape: ScatterShape) {
        mShapeRenderer = getRendererForShape(shape)
    }

    /**
     * Sets the size in density pixels the drawn scatterShape will have. This
     * only applies for non custom shapes.
     */
    override var scatterShapeSize: Float
        get() = shapeSize
        set(value) {
            shapeSize = value
        }
    override var scatterShapeHoleRadius: Float
        get() = mScatterShapeHoleRadius
        set(value) {
            mScatterShapeHoleRadius = value
        }
    override var scatterShapeHoleColor: Int
        get() = mScatterShapeHoleColor
        set(value) {
            mScatterShapeHoleColor = value
        }

    /**
     * Sets a new IShapeRenderer responsible for drawing this DataSet.
     * This can also be used to set a custom IShapeRenderer aside from the default ones.
     */
    override var shapeRenderer: IShapeRenderer?
        get() = mShapeRenderer
        set(value) {
            mShapeRenderer = value
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
