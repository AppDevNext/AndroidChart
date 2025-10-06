package com.github.mikephil.charting.data

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.interfaces.datasets.ILineRadarDataSet
import com.github.mikephil.charting.utils.Utils

/**
 * Base dataset for line and radar DataSets.
 *
 * @author Philipp Jahoda
 */
abstract class LineRadarDataSet<T : Entry>(yVals: MutableList<T>, label: String) : LineScatterCandleRadarDataSet<T>(yVals, label), ILineRadarDataSet<T> {
    // TODO: Move to using `Fill` class
    /**
     * the color that is used for filling the line surface
     */
    private var mFillColor = Color.rgb(140, 234, 255)

    /**
     * the drawable to be used for filling the line surface
     */
    protected var mFillDrawable: Drawable? = null

    /**
     * transparency used for filling line surface
     */
    private var mFillAlpha = 85

    /**
     * the width of the drawn data lines
     */
    private var mLineWidth = 2.5f

    /**
     * if true, the data will also be drawn filled
     */
    private var mDrawFilled = false

    override var fillColor: Int
        get() = mFillColor
        set(value) {
            mFillColor = value
            mFillDrawable = null
        }

    override var fillDrawable: Drawable?
        get() = mFillDrawable
        set(value) {
            mFillDrawable = value
        }

    override var fillAlpha: Int
        get() = mFillAlpha
        set(value) {
            mFillAlpha = value
        }

    override var lineWidth: Float
        get() = mLineWidth
        set(value) {
            var width = value
            if (width < 0.0f) width = 0.0f
            if (width > 10.0f) width = 10.0f
            mLineWidth = Utils.convertDpToPixel(width)
        }

    override var isDrawFilledEnabled: Boolean
        get() = mDrawFilled
        set(value) {
            mDrawFilled = value
        }

    protected fun copy(lineRadarDataSet: LineRadarDataSet<*>) {
        super.copy(lineRadarDataSet)
        lineRadarDataSet.mDrawFilled = mDrawFilled
        lineRadarDataSet.mFillAlpha = mFillAlpha
        lineRadarDataSet.mFillColor = mFillColor
        lineRadarDataSet.mFillDrawable = mFillDrawable
        lineRadarDataSet.mLineWidth = mLineWidth
    }
}
