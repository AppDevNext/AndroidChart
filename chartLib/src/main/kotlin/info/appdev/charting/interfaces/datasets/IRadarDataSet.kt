package info.appdev.charting.interfaces.datasets

import info.appdev.charting.data.RadarEntry

interface IRadarDataSet : ILineRadarDataSet<RadarEntry> {
    /** flag indicating whether highlight circle should be drawn or not */
    /** Sets whether highlight circle should be drawn or not */
    var isDrawHighlightCircleEnabled: Boolean

    val highlightCircleFillColor: Int

    /** The stroke color for highlight circle.
     * If Utils.COLOR_NONE, the color of the dataset is taken. */
    val highlightCircleStrokeColor: Int

    val highlightCircleStrokeAlpha: Int

    val highlightCircleInnerRadius: Float

    val highlightCircleOuterRadius: Float

    val highlightCircleStrokeWidth: Float
}
