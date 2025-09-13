package com.github.mikephil.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.util.Log
import com.github.mikephil.charting.formatter.DefaultFillFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Utils

open class LineDataSet(yVals: MutableList<Entry>, label: String) : LineRadarDataSet<Entry>(yVals, label), ILineDataSet {
    /**
     * Drawing mode for this line dataset
     */
    private var mMode: Mode? = Mode.LINEAR

    /**
     * returns all colors specified for the circles
     *
     * @return
     */
    /**
     * Sets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. Make sure that the colors
     * are already prepared (by calling getResources().getColor(...)) before
     * adding them to the DataSet.
     *
     * @param colors
     */
    /**
     * List representing all colors that are used for the circles
     */
    var circleColors: MutableList<Int> = mutableListOf()

    /**
     * the color of the inner circles
     */
    private var mCircleHoleColor = Color.WHITE

    /**
     * the radius of the circle-shaped value indicators
     */
    private var mCircleRadius = 8f

    /**
     * the hole radius of the circle-shaped value indicators
     */
    private var mCircleHoleRadius = 4f

    /**
     * sets the intensity of the cubic lines
     */
    private var mCubicIntensity = 0.2f

    /**
     * the path effect of this DataSet that makes dashed lines possible
     */
    private var mDashPathEffect: DashPathEffect? = null

    /**
     * formatter for customizing the position of the fill-line
     */
    private var mFillFormatter: IFillFormatter = DefaultFillFormatter()

    /**
     * if true, drawing circles is enabled
     */
    private var mDrawCircles = true

    private var mDrawCircleHole = true


    init {
        // mCircleRadius = Utils.convertDpToPixel(4f);
        // mLineWidth = Utils.convertDpToPixel(1f);
        circleColors.clear()

        // default colors
        // mColors.add(Color.rgb(192, 255, 140));
        // mColors.add(Color.rgb(255, 247, 140));
        circleColors.add(Color.rgb(140, 234, 255))
    }

    override fun copy(): DataSet<Entry> {
        val entries: MutableList<Entry> = ArrayList()
        for (i in mEntries.indices) {
            entries.add(mEntries[i].copy())
        }
        val copied = LineDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(lineDataSet: LineDataSet) {
        super.copy(lineDataSet)
        lineDataSet.circleColors = this.circleColors
        lineDataSet.mCircleHoleColor = mCircleHoleColor
        lineDataSet.mCircleHoleRadius = mCircleHoleRadius
        lineDataSet.mCircleRadius = mCircleRadius
        lineDataSet.mCubicIntensity = mCubicIntensity
        lineDataSet.mDashPathEffect = mDashPathEffect
        lineDataSet.mDrawCircleHole = mDrawCircleHole
        lineDataSet.mDrawCircles = mDrawCircleHole
        lineDataSet.mFillFormatter = mFillFormatter
        lineDataSet.mMode = mMode
    }

    /**
     * Returns the drawing mode for this line dataset
     *
     * @return
     */
    override val mode: Mode?
        get() = mMode

    /**
     * Returns the drawing mode for this LineDataSet
     *
     * @return
     */
    fun setMode(mode: Mode?) {
        mMode = mode
    }

    /**
     * Sets the intensity for cubic lines (if enabled). Max = 1f = very cubic,
     * Min = 0.05f = low cubic effect, Default: 0.2f
     *
     * @param intensity
     */
    fun setCubicIntensity(intensity: Float) {
        var intensity = intensity
        if (intensity > 1f) intensity = 1f
        if (intensity < 0.05f) intensity = 0.05f

        mCubicIntensity = intensity
    }

    override val cubicIntensity: Float
        get() = mCubicIntensity


    /**
     * Sets the radius of the drawn circles.
     * Default radius = 4f, Min = 1f
     *
     * @param radius
     */
    fun setCircleRadius(radius: Float) {
        if (radius >= 1f) {
            mCircleRadius = Utils.convertDpToPixel(radius)
        } else {
            Log.e("LineDataSet", "Circle radius cannot be < 1")
        }
    }

    override val circleRadius: Float
        get() = mCircleRadius

    /**
     * Sets the hole radius of the drawn circles.
     * Default radius = 2f, Min = 0.5f
     *
     * @param holeRadius
     */
    fun setCircleHoleRadius(holeRadius: Float) {
        if (holeRadius >= 0.5f) {
            mCircleHoleRadius = Utils.convertDpToPixel(holeRadius)
        } else {
            Log.e("LineDataSet", "Circle radius cannot be < 0.5")
        }
    }

    override val circleHoleRadius: Float
        get() = mCircleHoleRadius

    @get:Deprecated("")
    @set:Deprecated("")
    var circleSize: Float
        /**
         * This function is deprecated because of unclarity. Use getCircleRadius instead.
         */
        get() = circleRadius
        /**
         * sets the size (radius) of the circle shpaed value indicators,
         * default size = 4f
         *
         *
         * This method is deprecated because of unclarity. Use setCircleRadius instead.
         *
         * @param size
         */
        set(size) {
            setCircleRadius(size)
        }

    /**
     * Enables the line to be drawn in dashed mode, e.g. like this
     * "- - - - - -". THIS ONLY WORKS IF HARDWARE-ACCELERATION IS TURNED OFF.
     * Keep in mind that hardware acceleration boosts performance.
     *
     * @param lineLength  the length of the line pieces
     * @param spaceLength the length of space in between the pieces
     * @param phase       offset, in degrees (normally, use 0)
     */
    fun enableDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
        mDashPathEffect = DashPathEffect(
            floatArrayOf(
                lineLength, spaceLength
            ), phase
        )
    }

    /**
     * Disables the line to be drawn in dashed mode.
     */
    fun disableDashedLine() {
        mDashPathEffect = null
    }

    override val isDashedLineEnabled: Boolean
        get() = mDashPathEffect != null

    override val dashPathEffect: DashPathEffect?
        get() = mDashPathEffect

    /**
     * set this to true to enable the drawing of circle indicators for this
     * DataSet, default true
     *
     * @param enabled
     */
    fun setDrawCircles(enabled: Boolean) {
        this.mDrawCircles = enabled
    }

    override val isDrawCirclesEnabled: Boolean
        get() = mDrawCircles

    @Deprecated("")
    override val isDrawCubicEnabled: Boolean
        get() = mMode == Mode.CUBIC_BEZIER

    @Deprecated("")
    override val isDrawSteppedEnabled: Boolean
        get() = mMode == Mode.STEPPED

    /** ALL CODE BELOW RELATED TO CIRCLE-COLORS  */

    override fun getCircleColor(index: Int): Int {
        return circleColors[index]
    }

    override val circleColorCount: Int
        get() = circleColors.size

    /**
     * Sets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. Make sure that the colors
     * are already prepared (by calling getResources().getColor(...)) before
     * adding them to the DataSet.
     *
     * @param colors
     */
    fun setCircleColors(vararg colors: Int) {
        this.circleColors = ColorTemplate.createColors(colors)
    }

    /**
     * ets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. You can use
     * "new String[] { R.color.red, R.color.green, ... }" to provide colors for
     * this method. Internally, the colors are resolved using
     * getResources().getColor(...)
     *
     * @param colors
     */
    fun setCircleColors(colors: IntArray, c: Context) {
        val clrs = this.circleColors
        clrs.clear()

        for (color in colors) {
            clrs.add(c.resources.getColor(color))
        }

        this.circleColors = clrs
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet.
     * Internally, this recreates the colors array and adds the specified color.
     *
     * @param color
     */
    fun setCircleColor(color: Int) {
        resetCircleColors()
        circleColors.add(color)
    }

    /**
     * resets the circle-colors array and creates a new one
     */
    fun resetCircleColors() {
        circleColors.clear()
    }

    /**
     * Sets the color of the inner circle of the line-circles.
     *
     * @param color
     */
    fun setCircleHoleColor(color: Int) {
        mCircleHoleColor = color
    }

    override val circleHoleColor: Int
        get() = mCircleHoleColor

    /**
     * Set this to true to allow drawing a hole in each data circle.
     *
     * @param enabled
     */
    fun setDrawCircleHole(enabled: Boolean) {
        mDrawCircleHole = enabled
    }

    override val isDrawCircleHoleEnabled: Boolean
        get() = mDrawCircleHole

    /**
     * Sets a custom IFillFormatter to the chart that handles the position of the
     * filled-line for each DataSet. Set this to null to use the default logic.
     *
     * @param formatter
     */
    fun setFillFormatter(formatter: IFillFormatter?) {
        mFillFormatter = formatter ?: DefaultFillFormatter()
    }

    override val fillFormatter: IFillFormatter
        get() = mFillFormatter

    enum class Mode {
        LINEAR,
        STEPPED,
        CUBIC_BEZIER,
        HORIZONTAL_BEZIER
    }
}
