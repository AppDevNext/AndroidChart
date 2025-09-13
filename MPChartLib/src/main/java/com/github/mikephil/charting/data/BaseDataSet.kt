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

/**
 * This is the base dataset of all DataSets. It's purpose is to implement critical methods
 * provided by the IDataSet interface.
 */
abstract class BaseDataSet<T : Entry>() : IDataSet<T> {
    /**
     * List representing all colors that are used for this DataSet
     */
    protected var mColors: MutableList<Int>

    /**
     * List representing all colors that are used for drawing the actual values for this DataSet
     */
    protected var mValueColors: MutableList<Int>

    /**
     * this specifies which axis this DataSet should be plotted against
     */
    protected var mAxisDependency: AxisDependency = AxisDependency.LEFT

    /**
     * Default constructor.
     */
    init {
        mColors = ArrayList()
        mValueColors = ArrayList()

        // default color
        mColors.add(Color.rgb(140, 234, 255))
        mValueColors.add(Color.BLACK)
    }

    /**
     * Constructor with label.
     *
     * @param label
     */
    constructor(label: String) : this() {
        this.label = label
    }

    /**
     * Use this method to tell the data set that the underlying data has changed.
     */
    fun notifyDataSetChanged() {
        calcMinMax()
    }

    override val colors: MutableList<Int>
        get() = mColors

    val valueColors: List<Int>
        get() = mValueColors

    override val color: Int
        get() = mColors[0]

    override fun getColor(index: Int): Int {
        return mColors[index % mColors.size]
    }

    /**
     * ###### ###### COLOR SETTING RELATED METHODS ##### ######
     */
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
     * Sets the one and ONLY color that should be used for this DataSet.
     * Internally, this recreates the colors array and adds the specified color.
     *
     * @param color
     */
    fun setColor(color: Int) {
        resetColors()
        mColors.add(color)
    }

    /**
     * Sets a color with a specific alpha value.
     *
     * @param color
     * @param alpha from 0-255
     */
    fun setColor(color: Int, alpha: Int) {
        setColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)))
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

    /**
     * ###### ###### OTHER STYLING RELATED METHODS ##### ######
     */
    override var label: String = "DataSet"

    override var isHighlightEnabled: Boolean = true

    @Transient
    override var valueFormatter: IValueFormatter = Utils.defaultValueFormatter

    override var valueTextColor: Int
        get() = mValueColors[0]
        set(value) {
            mValueColors.clear()
            mValueColors.add(value)
        }

    override fun setValueTextColors(colors: MutableList<Int>) {
        mValueColors = colors
    }

    override var valueTypeface: Typeface? = null

    override var valueTextSize: Float = 17f
        set(value) {
            field = Utils.convertDpToPixel(value)
        }

    override fun getValueTextColor(index: Int): Int {
        return mValueColors[index % mValueColors.size]
    }

    override var form: LegendForm? = LegendForm.DEFAULT

    override var formSize: Float = Float.NaN

    override var formLineWidth: Float = Float.NaN

    override var formLineDashEffect: DashPathEffect? = null

    override var isDrawValuesEnabled: Boolean = true

    override var isDrawIconsEnabled: Boolean = true

    override var iconsOffset: MPPointF = MPPointF()

    override var isVisible: Boolean = true

    override var axisDependency: AxisDependency? = AxisDependency.LEFT

    /**
     * ###### ###### DATA RELATED METHODS ###### ######
     */
    override fun getIndexInEntries(xIndex: Int): Int {
        for (i in 0..<entryCount) {
            if (xIndex.toFloat() == getEntryForIndex(i)!!.x) return i
        }

        return -1
    }

    override fun removeFirst(): Boolean {
        if (entryCount > 0) {
            val entry = getEntryForIndex(0)
            return removeEntry(entry)
        } else return false
    }

    override fun removeLast(): Boolean {
        if (entryCount > 0) {
            val e = getEntryForIndex(entryCount - 1)
            return removeEntry(e)
        } else return false
    }

    override fun removeEntryByXValue(xValue: Float): Boolean {
        val e = getEntryForXValue(xValue, Float.NaN)
        return removeEntry(e)
    }

    override fun removeEntry(index: Int): Boolean {
        val e = getEntryForIndex(index)
        return removeEntry(e)
    }

    override fun contains(entry: T?): Boolean {
        for (i in 0..<entryCount) {
            if (getEntryForIndex(i) == entry) return true
        }

        return false
    }

    protected fun copy(baseDataSet: BaseDataSet<*>) {
        baseDataSet.mAxisDependency = mAxisDependency
        baseDataSet.mColors = mColors
        baseDataSet.isDrawIconsEnabled = isDrawIconsEnabled
        baseDataSet.isDrawValuesEnabled = isDrawValuesEnabled
        baseDataSet.form = form
        baseDataSet.formLineDashEffect = formLineDashEffect
        baseDataSet.formLineWidth = formLineWidth
        baseDataSet.formSize = formSize
        baseDataSet.isHighlightEnabled = isHighlightEnabled
        baseDataSet.iconsOffset = iconsOffset
        baseDataSet.mValueColors = mValueColors
        baseDataSet.valueFormatter = valueFormatter
        baseDataSet.valueTextSize = valueTextSize
        baseDataSet.isVisible = isVisible
    }
}
