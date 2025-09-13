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

    override val fillColor: Int
        get() = mFillColor

    /**
     * Sets the color that is used for filling the area below the line.
     * Resets an eventually set "fillDrawable".
     *
     * @param color
     */
    fun setFillColor(color: Int) {
        mFillColor = color
        mFillDrawable = null
    }

    override val fillDrawable: Drawable?
        get() = mFillDrawable

    /**
     * Sets the drawable to be used to fill the area below the line.
     *
     * @param drawable
     */
    fun setFillDrawable(drawable: Drawable?) {
        this.mFillDrawable = drawable
    }

    override val fillAlpha: Int
        get() = mFillAlpha

    /**
     * sets the alpha value (transparency) that is used for filling the line
     * surface (0-255), default: 85
     *
     * @param alpha
     */
    fun setFillAlpha(alpha: Int) {
        mFillAlpha = alpha
    }

    /**
     * set the line width of the chart (min = 0.2f, max = 10f); default 1f NOTE:
     * thinner line == better performance, thicker line == worse performance
     *
     * @param width
     */
    fun setLineWidth(width: Float) {
        var width = width
        if (width < 0.0f) width = 0.0f
        if (width > 10.0f) width = 10.0f
        mLineWidth = Utils.convertDpToPixel(width)
    }

    override val lineWidth: Float
        get() = mLineWidth

    override fun setDrawFilled(enabled: Boolean) {
        mDrawFilled = enabled
    }

    override val isDrawFilledEnabled: Boolean
        get() = mDrawFilled

    protected fun copy(lineRadarDataSet: LineRadarDataSet<*>) {
        super.copy(lineRadarDataSet)
        lineRadarDataSet.mDrawFilled = mDrawFilled
        lineRadarDataSet.mFillAlpha = mFillAlpha
        lineRadarDataSet.mFillColor = mFillColor
        lineRadarDataSet.mFillDrawable = mFillDrawable
        lineRadarDataSet.mLineWidth = mLineWidth
    }
}
