package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IScatterDataSet
import info.appdev.charting.utils.ColorTemplate

/**
 * ScatterDataSet describes a scatter-chart-able DataSet.
 *
 * `setScatterShape(shape)`/`shapeRenderer`/`getRendererForShape(shape)` were removed from common
 * code entirely and replaced with Android-only side-channels in `chartLib`'s
 * `ScatterDataSetAndroid.kt`, since `IShapeRenderer` (the renderer interface these deal with) is
 * fundamentally Canvas/Paint-coupled and has no portable equivalent (same category of drawing
 * logic as the `Renderer`/`DataRenderer` hierarchy, permanently Android-only per Step A.4):
 * - `shapeRenderer` is now a `WeakHashMap`-backed extension property on `IScatterDataSet`, same
 *   side-channel pattern as `ILineRadarDataSet.fillDrawable`/`ILineDataSet.fillFormatter`. As a
 *   result, `.copy()` no longer propagates a custom `shapeRenderer` to the copy.
 * - `setScatterShape(shape: ScatterShape)` is now an extension function on `ScatterDataSet`.
 * - `getRendererForShape(shape: ScatterShape)` is now a top-level function in `chartLib` (was
 *   previously `ScatterDataSet.getRendererForShape(shape)` on the companion object).
 */
open class ScatterDataSet(yVals: MutableList<EntryFloat>, label: String = "") : LineScatterCandleRadarDataSet<EntryFloat>(yVals, label), IScatterDataSet {
    /**
     * the size the scatterShape will have, in density pixels
     */
    private var shapeSize = 15f

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
    private var mScatterShapeHoleColor = ColorTemplate.COLOR_NONE

    override fun copy(): DataSet<EntryFloat> {
        val entries: MutableList<EntryFloat> = mutableListOf()
        for (i in entriesInternal.indices) {
            entries.add(entriesInternal[i].copy())
        }
        val copied = ScatterDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(scatterDataSet: ScatterDataSet) {
        super.copy((scatterDataSet as BaseDataSet<*>?)!!)
        scatterDataSet.shapeSize = shapeSize
        scatterDataSet.mScatterShapeHoleRadius = mScatterShapeHoleRadius
        scatterDataSet.mScatterShapeHoleColor = mScatterShapeHoleColor
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
}
