package com.github.mikephil.charting.data

import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

open class RadarDataSet(yVals: MutableList<RadarEntry>, label: String) : LineRadarDataSet<RadarEntry>(yVals, label), IRadarDataSet {
    /** The stroke color for highlight circle.
     * If Utils.COLOR_NONE, the color of the dataset is taken. */
    override var highlightCircleStrokeColor: Int = ColorTemplate.COLOR_NONE

    override var highlightCircleStrokeAlpha: Int = (0.3 * 255).toInt()
    override var highlightCircleInnerRadius: Float = 3.0f
    override var highlightCircleOuterRadius: Float = 4.0f
    override var highlightCircleStrokeWidth: Float = 2.0f

    /** Returns true if highlight circle should be drawn, false if not */
    override var isDrawHighlightCircleEnabled: Boolean = false

    override var highlightCircleFillColor: Int = ColorTemplate.COLOR_NONE

    override fun copy(): DataSet<RadarEntry> {
        val entries: MutableList<RadarEntry> = ArrayList()
        for (i in mEntries.indices) {
            entries.add(mEntries[i].copy())
        }
        val copied = RadarDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(radarDataSet: RadarDataSet) {
        super.copy((radarDataSet as BaseDataSet<*>?)!!)
        radarDataSet.isDrawHighlightCircleEnabled = isDrawHighlightCircleEnabled
        radarDataSet.highlightCircleFillColor = highlightCircleFillColor
        radarDataSet.highlightCircleInnerRadius = highlightCircleInnerRadius
        radarDataSet.highlightCircleStrokeAlpha = highlightCircleStrokeAlpha
        radarDataSet.highlightCircleStrokeColor = highlightCircleStrokeColor
        radarDataSet.highlightCircleStrokeWidth = highlightCircleStrokeWidth
    }
}
