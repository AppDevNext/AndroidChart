package com.github.mikephil.charting.data

import android.graphics.Color
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

open class RadarDataSet(yVals: MutableList<RadarEntry?>?, label: String = "") : LineRadarDataSet<RadarEntry?>(yVals, label), IRadarDataSet {
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

    /** Returns true if highlight circle should be drawn, false if not */
    override fun isDrawHighlightCircleEnabled(): Boolean {
        return mDrawHighlightCircleEnabled
    }

    /** Sets whether highlight circle should be drawn or not */
    override fun setDrawHighlightCircleEnabled(enabled: Boolean) {
        mDrawHighlightCircleEnabled = enabled
    }

    override fun getHighlightCircleFillColor(): Int {
        return mHighlightCircleFillColor
    }

    /** Returns the stroke color for highlight circle.
     * If Utils.COLOR_NONE, the color of the dataset is taken. */
    override fun getHighlightCircleStrokeColor(): Int {
        return mHighlightCircleStrokeColor
    }

    override fun getHighlightCircleStrokeAlpha(): Int {
        return mHighlightCircleStrokeAlpha
    }

    override fun getHighlightCircleInnerRadius(): Float {
        return mHighlightCircleInnerRadius
    }

    override fun getHighlightCircleOuterRadius(): Float {
        return mHighlightCircleOuterRadius
    }

    override fun getHighlightCircleStrokeWidth(): Float {
        return mHighlightCircleStrokeWidth
    }

    override fun copy(): DataSet<RadarEntry?> {
        val entries: MutableList<RadarEntry?> = ArrayList()
        for (i in mEntries!!.indices) {
            entries.add(mEntries!![i]!!.copy())
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
