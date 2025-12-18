package com.github.mikephil.charting.data

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.interfaces.datasets.ILineRadarDataSet
import com.github.mikephil.charting.utils.convertDpToPixel

/**
 * Base dataset for line and radar DataSets.
 */
abstract class LineRadarDataSet<T : Entry?>(yVals: MutableList<T?>?, label: String) : LineScatterCandleRadarDataSet<T?>(yVals, label), ILineRadarDataSet<T?> {
    // TODO: Move to using `Fill` class
    /**
     * the color that is used for filling the line surface
     */
    private var mFillColor = Color.rgb(140, 234, 255)

    /**
     * Sets the drawable to be used to fill the area below the line.
     */
    /**
     * the drawable to be used for filling the line surface
     */
    override var fillDrawable: Drawable? = null

    /**
     * sets the alpha value (transparency) that is used for filling the line
     * surface (0-255), default: 85
     */
    /**
     * transparency used for filling line surface
     */
    override var fillAlpha: Int = 85

    /**
     * the width of the drawn data lines
     */
    private var mLineWidth = 2.5f

    /**
     * if true, the data will also be drawn filled
     */
    override var isDrawFilledEnabled: Boolean = false

    override var fillColor: Int
        get() = mFillColor
        /**
         * Sets the color that is used for filling the area below the line.
         * Resets an eventually set "fillDrawable".
         */
        set(color) {
            mFillColor = color
            this.fillDrawable = null
        }

    override var lineWidth: Float
        get() = mLineWidth
        /**
         * set the line width of the chart (min = 0.2f, max = 10f); default 1f NOTE:
         * thinner line == better performance, thicker line == worse performance
         */
        set(width) {
            var width = width
            if (width < 0.0f) width = 0.0f
            if (width > 10.0f) width = 10.0f
            mLineWidth = width.convertDpToPixel()
        }

    override fun setDrawFilled(enabled: Boolean) {
        this.isDrawFilledEnabled = enabled
    }

    protected fun copy(lineRadarDataSet: LineRadarDataSet<*>) {
        super.copy((lineRadarDataSet as BaseDataSet<*>?)!!)
        lineRadarDataSet.isDrawFilledEnabled = this.isDrawFilledEnabled
        lineRadarDataSet.fillAlpha = this.fillAlpha
        lineRadarDataSet.mFillColor = mFillColor
        lineRadarDataSet.fillDrawable = this.fillDrawable
        lineRadarDataSet.mLineWidth = mLineWidth
    }
}
