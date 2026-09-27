package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.EntryFloat

interface IScatterDataSet : ILineScatterCandleRadarDataSet<EntryFloat> {
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
}
