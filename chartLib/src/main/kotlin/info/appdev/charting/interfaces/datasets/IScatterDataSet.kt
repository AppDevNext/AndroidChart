package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.Entry
import info.appdev.charting.renderer.scatter.IShapeRenderer

interface IScatterDataSet : ILineScatterCandleRadarDataSet<Entry, Float> {
    /**
     * the currently set scatter shape size
     */
    val scatterShapeSize: Float

    /**
     * radius of the hole in the shape
     */
    val scatterShapeHoleRadius: Float

    /**
     * the color for the hole in the shape
     */
    val scatterShapeHoleColor: Int

    /**
     * the IShapeRenderer responsible for rendering this DataSet.
     */
    val shapeRenderer: IShapeRenderer?
}
