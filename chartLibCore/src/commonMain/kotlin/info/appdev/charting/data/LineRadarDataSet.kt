package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ILineRadarDataSet
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.convertDpToPixel

/**
 * Base dataset for line and radar DataSets.
 *
 * Note: the Android-only `fillDrawable` property (drawable-based line/area fill, as opposed
 * to a solid [fillColor]) lives outside this common class, as an extension property in
 * `chartLib`'s `LineRadarDataSetAndroid.kt` (`android.graphics.drawable.Drawable` has no
 * portable equivalent). As a result, setting [fillColor] here no longer implicitly clears a
 * previously set `fillDrawable`, and [copy] no longer propagates `fillDrawable` to the copy;
 * callers relying on either behavior should set `fillDrawable` explicitly.
 */
abstract class LineRadarDataSet<T : BaseEntry<Float>>(yVals: MutableList<T>, label: String) : LineScatterCandleRadarDataSet<T>(yVals, label), ILineRadarDataSet<T> {
    // TODO: Move to using `Fill` class
    /**
     * the color that is used for filling the line surface
     */
    private var mFillColor = ColorTemplate.argb(140, 234, 255)

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
    override var isDrawFilled: Boolean = false

    override var fillColor: Int
        get() = mFillColor
        /**
         * Sets the color that is used for filling the area below the line.
         */
        set(color) {
            mFillColor = color
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

    protected fun copy(lineRadarDataSet: LineRadarDataSet<*>) {
        super.copy((lineRadarDataSet as BaseDataSet<*>?)!!)
        lineRadarDataSet.isDrawFilled = this.isDrawFilled
        lineRadarDataSet.fillAlpha = this.fillAlpha
        lineRadarDataSet.mFillColor = mFillColor
        lineRadarDataSet.mLineWidth = mLineWidth
    }
}
