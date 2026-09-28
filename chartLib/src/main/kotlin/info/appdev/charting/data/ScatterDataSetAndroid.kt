package info.appdev.charting.data

import info.appdev.charting.charts.ScatterShape
import info.appdev.charting.interfaces.datasets.IScatterDataSet
import info.appdev.charting.renderer.scatter.ChevronDownShapeRenderer
import info.appdev.charting.renderer.scatter.ChevronUpShapeRenderer
import info.appdev.charting.renderer.scatter.CircleShapeRenderer
import info.appdev.charting.renderer.scatter.CrossShapeRenderer
import info.appdev.charting.renderer.scatter.IShapeRenderer
import info.appdev.charting.renderer.scatter.SquareShapeRenderer
import info.appdev.charting.renderer.scatter.TriangleShapeRenderer
import info.appdev.charting.renderer.scatter.XShapeRenderer
import java.util.WeakHashMap

/**
 * Android-only side channels for [IScatterDataSet]/[ScatterDataSet], mirroring the pattern already
 * established for `ILineRadarDataSet.fillDrawable`/`ILineDataSet.fillFormatter`.
 *
 * [IShapeRenderer] is fundamentally Canvas/Paint-coupled and has no portable equivalent, so it is
 * kept as a `WeakHashMap`-backed extension property here instead of a stored property on the
 * common class. Unlike `fillFormatter`'s "null means default" semantics, `shapeRenderer` can be
 * legitimately `null` (`ScatterChartRenderer` treats a `null` renderer as "don't draw"), so the map
 * distinguishes "never set" (falls back to the default `SquareShapeRenderer()`) from "explicitly
 * set to null" via [MutableMap.containsKey] rather than [getOrPut][kotlin.collections.getOrPut]
 * (which cannot tell a stored `null` apart from an absent key).
 */
private val shapeRenderers = WeakHashMap<IScatterDataSet, IShapeRenderer?>()

/**
 * The [IShapeRenderer] responsible for rendering this DataSet. Default: [SquareShapeRenderer].
 * Can be set to a custom [IShapeRenderer] aside from the default ones, or to `null` to skip
 * drawing entirely.
 */
var IScatterDataSet.shapeRenderer: IShapeRenderer?
    get() = if (shapeRenderers.containsKey(this)) {
        shapeRenderers[this]
    } else {
        SquareShapeRenderer().also { shapeRenderers[this] = it }
    }
    set(value) {
        shapeRenderers[this] = value
    }

/**
 * Sets the ScatterShape this DataSet should be drawn with. This will search for an available
 * IShapeRenderer and set this renderer for the DataSet.
 */
fun ScatterDataSet.setScatterShape(shape: ScatterShape) {
    this.shapeRenderer = getRendererForShape(shape)
}

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
