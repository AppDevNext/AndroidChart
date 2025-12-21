package com.github.mikephil.charting.interfaces.datasets

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer

interface IScatterDataSet : ILineScatterCandleRadarDataSet<Entry> {
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
