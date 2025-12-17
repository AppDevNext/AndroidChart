package com.github.mikephil.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.convertDpToPixel

/**
 * This is the base dataset of all DataSets. It's purpose is to implement critical methods
 * provided by the IDataSet interface.
 */
abstract class BaseDataSet<T : Entry?>() : IDataSet<T> {
    /**
     * List representing all colors that are used for this DataSet
     */
    protected var mColors: MutableList<Int>

    /**
     * List representing all colors that are used for drawing the actual values for this DataSet
     */
    protected var mValueColors: MutableList<Int>

    /**
     * label that describes the DataSet or the data the DataSet represents
     */
    private var mLabel = "DataSet"

    /**
     * this specifies which axis this DataSet should be plotted against
     */
    protected var mAxisDependency: AxisDependency = AxisDependency.LEFT

    /**
     * if true, value highlighting is enabled
     */
    protected var mHighlightEnabled: Boolean = true

    /**
     * custom formatter that is used instead of the auto-formatter if set
     */
    @Transient
    protected var mValueFormatter: IValueFormatter? = null

    /**
     * the typeface used for the value text
     */
    protected var mValueTypeface: Typeface? = null

    private var mForm = LegendForm.DEFAULT
    private var mFormSize = Float.NaN
    private var mFormLineWidth = Float.NaN
    private var mFormLineDashEffect: DashPathEffect? = null
    private var mIsVisible = true
    private var mIsDrawValues = true
    private var mIsDrawIcons = true

    /**
     * if true, y-values are drawn on the chart
     */
    protected var mDrawValues: Boolean = true

    /**
     * if true, y-icons are drawn on the chart
     */
    protected var mDrawIcons: Boolean = true

    /**
     * the offset for drawing icons (in dp)
     */
    protected var mIconsOffset: MPPointF = MPPointF()

    /**
     * the size of the value-text labels
     */
    protected var mValueTextSize: Float = 17f

    /**
     * flag that indicates if the DataSet is visible or not
     */
    protected var mVisible: Boolean = true

    init {
        mColors = ArrayList()
        mValueColors = ArrayList()

        // default color
        mColors.add(Color.rgb(140, 234, 255))
        mValueColors.add(Color.BLACK)
    }

    constructor(label: String) : this() {
        this.mLabel = label
    }

    /**
     * Use this method to tell the data set that the underlying data has changed.
     */
    fun notifyDataSetChanged() {
        calcMinMax()
    }

    /**
     * Sets a color with a specific alpha value.
     *
     * @param color
     * @param alpha from 0-255
     */
    fun setColor(color: Int, alpha: Int) {
        mColors.clear()
        mColors.add(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)))
    }

    override var color: Int
        get() = mColors[0]
        set(value) {
            mColors.clear()
            mColors.add(value)
        }

    override val colors: MutableList<Int>
        get() = mColors

    override fun getColorByIndex(index: Int): Int {
        return mColors[index % mColors.size]
    }

    override var formLineDashEffect: DashPathEffect?
        get() = mFormLineDashEffect
        set(value) {
            mFormLineDashEffect = value
        }

    override var isDrawValues: Boolean
        get() = mIsDrawValues
        set(value) {
            mIsDrawValues = value
        }

    override var isDrawIcons: Boolean
        get() = mIsDrawIcons
        set(value) {
            mIsDrawIcons = value
        }

    override var iconsOffset: MPPointF
        get() = mIconsOffset
        set(value) {
            mIconsOffset = value
        }

    override var formLineWidth: Float
        get() = mFormLineWidth
        set(value) {
            mFormLineWidth = value
        }

    /**
     * Sets the colors that should be used fore this DataSet. Colors are reused
     * as soon as the number of Entries the DataSet represents is higher than
     * the size of the colors array. If you are using colors from the resources,
     * make sure that the colors are already prepared (by calling
     * ContextCompat.getColor(context,..) before adding them to the DataSet.
     *
     * @param colors
     */
    fun setColors(colors: MutableList<Int>) {
        this.mColors = colors
    }

    /**
     * Sets the colors that should be used fore this DataSet. Colors are reused
     * as soon as the number of Entries the DataSet represents is higher than
     * the size of the colors array. If you are using colors from the resources,
     * make sure that the colors are already prepared (by calling
     * ContextCompat.getColor(context,...)) before adding them to the DataSet.
     *
     * @param colors
     */
    fun setColors(vararg colors: Int) {
        this.mColors = ColorTemplate.createColors(colors)
    }

    /**
     * Sets the colors that should be used fore this DataSet. Colors are reused
     * as soon as the number of Entries the DataSet represents is higher than
     * the size of the colors array. You can use
     * "new int[] { R.color.red, R.color.green, ... }" to provide colors for
     * this method. Internally, the colors are resolved using
     * ContextCompat.getColor(context,...)
     *
     * @param colors
     */
    fun setColors(colors: IntArray, context: Context) {
        mColors.clear()

        for (color in colors) {
            mColors.add(ContextCompat.getColor(context, color))
        }
    }

    /**
     * Adds a new color to the colors array of the DataSet.
     *
     * @param color
     */
    fun addColor(color: Int) {
        mColors.add(color)
    }

    /**
     * Sets colors with a specific alpha value.
     *
     * @param colors
     * @param alpha
     */
    fun setColors(colors: IntArray, alpha: Int) {
        resetColors()
        for (color in colors) {
            addColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)))
        }
    }

    /**
     * Resets all colors of this DataSet and recreates the colors array.
     */
    fun resetColors() {
        mColors.clear()
    }

    override var label: String
        get() = mLabel
        set(value) {
            mLabel = value
        }
    override var axisDependency: AxisDependency
        get() = mAxisDependency
        set(value) {
            mAxisDependency = value
        }

    override var isHighlightEnabled: Boolean
        get() = mHighlightEnabled
        set(value) {
            mHighlightEnabled = value
        }

    override var valueFormatter: IValueFormatter
        get() = if (needsFormatter())
            Utils.getDefaultValueFormatter()
        else
            mValueFormatter!!
        set(value) {
            mValueFormatter = value
        }

    override fun needsFormatter(): Boolean {
        return mValueFormatter == null
    }

    override fun setSingleValueTextColor(value: Int) {
            mValueColors.clear()
            mValueColors.add(value)
    }

    override var valueTextColors: MutableList<Int>
        get() = mValueColors
        set(value) {
            mValueColors = value
        }

    override fun getValueTextColor(index: Int): Int {
        return mValueColors[index % mValueColors.size]
    }

    override var valueTypeface: Typeface?
        get() = mValueTypeface
        set(value) {
            mValueTypeface = value
        }

    override var valueTextSize: Float
        get() = mValueTextSize
        set(value) {
            mValueTextSize = value.convertDpToPixel()
        }

    override var form: LegendForm
        get() = mForm
        set(value) {
            mForm = value
        }

    override var formSize: Float
        get() = mFormSize
        set(value) {
            mFormSize = value
        }

    override var isVisible: Boolean
        get() = mIsVisible
        set(value) {
            mIsVisible = value
        }

    override fun getIndexInEntries(xIndex: Int): Int {
        for (i in 0..<entryCount) {
            if (xIndex.toFloat() == getEntryForIndex(i)!!.x) return i
        }

        return -1
    }

    override fun removeFirst(): Boolean {
        if (entryCount > 0) {
            val entry = getEntryForIndex(0)
            return if (entry != null) removeEntry(entry) else false
        } else return false
    }

    override fun removeLast(): Boolean {
        if (entryCount > 0) {
            val entry = getEntryForIndex(entryCount - 1)
            return if (entry != null) removeEntry(entry) else false
        } else return false
    }

    override fun removeEntryByXValue(xValue: Float): Boolean {
        val entry = getEntryForXValue(xValue, Float.NaN)
        return if (entry != null) removeEntry(entry) else false
    }

    override fun removeEntry(index: Int): Boolean {
        val entry = getEntryForIndex(index)
        return if (entry != null) removeEntry(entry) else false
    }

    override fun contains(entry: T): Boolean {
        for (i in 0..<entryCount) {
            if (getEntryForIndex(i) == entry) return true
        }

        return false
    }

    protected fun copy(baseDataSet: BaseDataSet<*>) {
        baseDataSet.mAxisDependency = mAxisDependency
        baseDataSet.mColors = mColors
        baseDataSet.mDrawIcons = mDrawIcons
        baseDataSet.mDrawValues = mDrawValues
        baseDataSet.mForm = mForm
        baseDataSet.mFormLineDashEffect = mFormLineDashEffect
        baseDataSet.formLineWidth = formLineWidth
        baseDataSet.mFormSize = mFormSize
        baseDataSet.mHighlightEnabled = mHighlightEnabled
        baseDataSet.mIconsOffset = mIconsOffset
        baseDataSet.mValueColors = mValueColors
        baseDataSet.mValueFormatter = mValueFormatter
        baseDataSet.mValueTextSize = mValueTextSize
        baseDataSet.mVisible = mVisible
    }
}
