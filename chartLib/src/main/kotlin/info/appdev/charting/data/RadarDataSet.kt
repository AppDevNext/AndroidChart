package info.appdev.charting.data

import android.graphics.Color
import info.appdev.charting.interfaces.datasets.IRadarDataSet
import info.appdev.charting.utils.ColorTemplate

open class RadarDataSet(yVals: MutableList<RadarEntry>?, label: String = "") : LineRadarDataSet<RadarEntry>(yVals, label), IRadarDataSet {
    /** flag indicating whether highlight circle should be drawn or not */
    protected var mDrawHighlightCircleEnabled: Boolean = false

    protected var mHighlightCircleFillColor: Int = Color.WHITE

    /** The stroke color for highlight circle.
     * If Utils.COLOR_NONE, the color of the dataset is taken. */
    protected var mHighlightCircleStrokeColor: Int = ColorTemplate.COLOR_NONE

    protected var mHighlightCircleStrokeAlpha: Int = (0.3 * 255).toInt()
    protected var mHighlightCircleInnerRadius: Float = 3.0f
    protected var mHighlightCircleOuterRadius: Float = 4.0f
    protected var mHighlightCircleStrokeWidth: Float = 2.0f

    override var isDrawHighlightCircleEnabled: Boolean
        get() = mDrawHighlightCircleEnabled
        set(value) {
            mDrawHighlightCircleEnabled = value
        }
    override var highlightCircleFillColor: Int
        get() = mHighlightCircleFillColor
        set(value) {
            mHighlightCircleFillColor = value
        }
    override var highlightCircleStrokeColor: Int
        get() = mHighlightCircleStrokeColor
        set(value) {
            mHighlightCircleStrokeColor = value
        }
    override var highlightCircleStrokeAlpha: Int
        get() = mHighlightCircleStrokeAlpha
        set(value) {
            mHighlightCircleStrokeAlpha = value
        }
    override var highlightCircleInnerRadius: Float
        get() = mHighlightCircleInnerRadius
        set(value) {
            mHighlightCircleInnerRadius = value
        }
    override var highlightCircleOuterRadius: Float
        get() = mHighlightCircleOuterRadius
        set(value) {
            mHighlightCircleOuterRadius = value
        }
    override var highlightCircleStrokeWidth: Float
        get() = mHighlightCircleStrokeWidth
        set(value) {
            mHighlightCircleStrokeWidth = value
        }

    override fun copy(): DataSet<RadarEntry> {
        val entries: MutableList<RadarEntry> = mutableListOf()
        mEntries?.let {
            for (i in it.indices) {
                entries.add(it[i].copy())
            }
        }
        val copied = RadarDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(radarDataSet: RadarDataSet) {
        super.copy((radarDataSet as BaseDataSet<*>?)!!)
        radarDataSet.mDrawHighlightCircleEnabled = mDrawHighlightCircleEnabled
        radarDataSet.mHighlightCircleFillColor = mHighlightCircleFillColor
        radarDataSet.mHighlightCircleInnerRadius = mHighlightCircleInnerRadius
        radarDataSet.mHighlightCircleStrokeAlpha = mHighlightCircleStrokeAlpha
        radarDataSet.mHighlightCircleStrokeColor = mHighlightCircleStrokeColor
        radarDataSet.mHighlightCircleStrokeWidth = mHighlightCircleStrokeWidth
    }

}
