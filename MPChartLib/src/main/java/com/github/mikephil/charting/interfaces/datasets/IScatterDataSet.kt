package com.github.mikephil.charting.interfaces.datasets

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer

/**
 * Created by philipp on 21/10/15.
 */
interface IScatterDataSet : ILineScatterCandleRadarDataSet<Entry> {
    /**
     * Returns the currently set scatter shape size
     *
     * @return
     */
    val scatterShapeSize: Float

    /**
     * Returns radius of the hole in the shape
     *
     * @return
     */
    val scatterShapeHoleRadius: Float

    /**
     * Returns the color for the hole in the shape
     *
     * @return
     */
    val scatterShapeHoleColor: Int

    /**
     * Returns the IShapeRenderer responsible for rendering this DataSet.
     *
     * @return
     */
    val shapeRenderer: IShapeRenderer?
}
