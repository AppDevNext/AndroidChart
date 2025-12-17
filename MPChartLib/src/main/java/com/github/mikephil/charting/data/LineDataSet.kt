package com.github.mikephil.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.util.Log
import com.github.mikephil.charting.formatter.DefaultFillFormatter
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.convertDpToPixel

class LineDataSet(yVals: MutableList<Entry?>?, label: String?) : LineRadarDataSet<Entry?>(yVals, label), ILineDataSet {
    /**
     * Drawing mode for this line dataset
     */
    private var mLineDataSetMode: Mode? = Mode.LINEAR

    /**
     * returns all colors specified for the circles
     */
    /**
     * Sets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. Make sure that the colors
     * are already prepared (by calling getResources().getColor(...)) before
     * adding them to the DataSet.
     */
    /**
     * List representing all colors that are used for the circles
     */
    var circleColors: MutableList<Int?>? = null

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
    private var mFillFormatter: IFillFormatter? = DefaultFillFormatter()

    /**
     * if true, drawing circles is enabled
     */
    private var mDrawCircles = true

    private var mDrawCircleHole = true


    init {
        // mCircleRadius = UtilsKtKt.convertDpToPixel(4f);
        // mLineWidth = UtilsKtKt.convertDpToPixel(1f);
        if (this.circleColors == null) {
            this.circleColors = ArrayList<Int?>()
        }
        circleColors!!.clear()

        // default colors
        // mColors.add(Color.rgb(192, 255, 140));
        // mColors.add(Color.rgb(255, 247, 140));
        circleColors!!.add(Color.rgb(140, 234, 255))
    }

    override fun copy(): DataSet<Entry?> {
        val entries: MutableList<Entry?> = ArrayList<Entry?>()
        for (i in mEntries!!.indices) {
            entries.add(mEntries!![i]!!.copy())
        }
        val copied = LineDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(lineDataSet: LineDataSet) {
        super.copy((lineDataSet as BaseDataSet<*>?)!!)
        lineDataSet.circleColors = this.circleColors
        lineDataSet.mCircleHoleColor = mCircleHoleColor
        lineDataSet.mCircleHoleRadius = mCircleHoleRadius
        lineDataSet.mCircleRadius = mCircleRadius
        lineDataSet.mCubicIntensity = mCubicIntensity
        lineDataSet.mDashPathEffect = mDashPathEffect
        lineDataSet.mDrawCircleHole = mDrawCircleHole
        lineDataSet.mDrawCircles = mDrawCircleHole
        lineDataSet.mFillFormatter = mFillFormatter
        lineDataSet.mLineDataSetMode = mLineDataSetMode
    }

    /**
     * Returns the drawing mode for this line dataset
     */
    override fun getLineMode(): Mode? {
        return mLineDataSetMode
    }

    /**
     * Returns the drawing mode for this LineDataSet
     */
    fun setLineMode(mode: Mode?) {
        mLineDataSetMode = mode
    }

    /**
     * Sets the intensity for cubic lines (if enabled). Max = 1f = very cubic,
     * Min = 0.05f = low cubic effect, Default: 0.2f
     */
    fun setCubicIntensity(intensity: Float) {
        var intensity = intensity
        if (intensity > 1f) {
            intensity = 1f
        }
        if (intensity < 0.05f) {
            intensity = 0.05f
        }

        mCubicIntensity = intensity
    }

    override fun getCubicIntensity(): Float {
        return mCubicIntensity
    }


    /**
     * Sets the radius of the drawn circles.
     * Default radius = 4f, Min = 1f
     */
    fun setCircleRadius(radius: Float) {
        if (radius >= 1f) {
            mCircleRadius = radius.convertDpToPixel()
        } else {
            Log.e("LineDataSet", "Circle radius cannot be < 1")
        }
    }

    override fun getCircleRadius(): Float {
        return mCircleRadius
    }

    /**
     * Sets the hole radius of the drawn circles.
     * Default radius = 2f, Min = 0.5f
     */
    fun setCircleHoleRadius(holeRadius: Float) {
        if (holeRadius >= 0.5f) {
            mCircleHoleRadius = holeRadius.convertDpToPixel()
        } else {
            Log.e("LineDataSet", "Circle radius cannot be < 0.5")
        }
    }

    override fun getCircleHoleRadius(): Float {
        return mCircleHoleRadius
    }

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

    override fun isDashedLineEnabled(): Boolean {
        return mDashPathEffect != null
    }

    override fun getDashPathEffect(): DashPathEffect? {
        return mDashPathEffect
    }

    /**
     * set this to true to enable the drawing of circle indicators for this
     * DataSet, default true
     */
    fun setDrawCircles(enabled: Boolean) {
        this.mDrawCircles = enabled
    }

    override fun isDrawCirclesEnabled(): Boolean {
        return mDrawCircles
    }

    @Deprecated("")
    override fun isDrawCubicEnabled(): Boolean {
        return mLineDataSetMode == Mode.CUBIC_BEZIER
    }

    @Deprecated("")
    override fun isDrawSteppedEnabled(): Boolean {
        return mLineDataSetMode == Mode.STEPPED
    }

    override fun getCircleColor(index: Int): Int {
        return circleColors!!.get(index)!!
    }

    override fun getCircleColorCount(): Int {
        return circleColors!!.size
    }

    /**
     * Sets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. Make sure that the colors
     * are already prepared (by calling getResources().getColor(...)) before
     * adding them to the DataSet.
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
     */
    fun setCircleColors(colors: IntArray, c: Context) {
        var clrs = this.circleColors
        if (clrs == null) {
            clrs = ArrayList<Int?>()
        }
        clrs.clear()

        for (color in colors) {
            clrs.add(c.resources.getColor(color))
        }

        this.circleColors = clrs
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet.
     * Internally, this recreates the colors array and adds the specified color.
     */
    fun setCircleColor(color: Int) {
        resetCircleColors()
        circleColors!!.add(color)
    }

    /**
     * resets the circle-colors array and creates a new one
     */
    fun resetCircleColors() {
        if (this.circleColors == null) {
            this.circleColors = ArrayList<Int?>()
        }
        circleColors!!.clear()
    }

    /**
     * Sets the color of the inner circle of the line-circles.
     */
    fun setCircleHoleColor(color: Int) {
        mCircleHoleColor = color
    }

    override fun getCircleHoleColor(): Int {
        return mCircleHoleColor
    }

    /**
     * Set this to true to allow drawing a hole in each data circle.
     */
    fun setDrawCircleHole(enabled: Boolean) {
        mDrawCircleHole = enabled
    }

    override fun isDrawCircleHoleEnabled(): Boolean {
        return mDrawCircleHole
    }

    /**
     * Sets a custom IFillFormatter to the chart that handles the position of the
     * filled-line for each DataSet. Set this to null to use the default logic.
     */
    fun setFillFormatter(formatter: IFillFormatter?) {
        if (formatter == null) {
            mFillFormatter = DefaultFillFormatter()
        } else {
            mFillFormatter = formatter
        }
    }

    override fun getFillFormatter(): IFillFormatter? {
        return mFillFormatter
    }

    enum class Mode {
        LINEAR,
        STEPPED,
        CUBIC_BEZIER,
        HORIZONTAL_BEZIER
    }
}
