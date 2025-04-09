package com.github.mikephil.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
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
    protected var mColors: MutableList<Int> = mutableListOf()

    /**
     * List representing all colors that are used for drawing the actual values for this DataSet
     */
    var valueColors: MutableList<Int> = mutableListOf()
        protected set

    /**
     * ###### ###### OTHER STYLING RELATED METHODS ##### ######
     */
    /**
     * label that describes the DataSet or the data the DataSet represents
     */
    final override var label: String = "DataSet"

    /**
     * this specifies which axis this DataSet should be plotted against
     */
    override var axisDependency: AxisDependency = AxisDependency.LEFT

    /**
     * if true, value highlightning is enabled
     */
    override var isHighlightEnabled: Boolean = true

    /**
     * custom formatter that is used instead of the auto-formatter if set
     */
    @Transient
    protected var mValueFormatter: IValueFormatter = Utils.getDefaultValueFormatter()

    /**
     * the typeface used for the value text
     */
    override var valueTypeface: Typeface? = null

    override var form: LegendForm = LegendForm.DEFAULT
    override var formSize: Float = Float.NaN
    override var formLineWidth: Float = Float.NaN
    override var formLineDashEffect: DashPathEffect? = null

    /**
     * if true, y-values are drawn on the chart
     */
    override var isDrawValuesEnabled: Boolean = true
        protected set

    /**
     * if true, y-icons are drawn on the chart
     */
    override var isDrawIconsEnabled: Boolean = true
        protected set

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
    override var isVisible: Boolean = true

    /**
     * Default constructor.
     */
    init {
        // default color
        mColors.add(Color.rgb(140, 234, 255))
        valueColors.add(Color.BLACK)
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


    override val colors: List<Int>
        /**
         * ###### ###### COLOR GETTING RELATED METHODS ##### ######
         */
        get() = mColors

    override var color: Int
        get() = mColors[0]
        /**
         * Sets the one and ONLY color that should be used for this DataSet.
         * Internally, this recreates the colors array and adds the specified color.
         *
         * @param color
         */
        set(color) {
            resetColors()
            mColors.add(color)
        }

    override fun getColor(index: Int): Int {
        return mColors[index % mColors.size]
    }

    /**
     * Sets the colors that should be used fore this DataSet. Colors are reused
     * as soon as the number of Entries the DataSet represents is higher than
     * the size of the colors array. If you are using colors from the resources,
     * make sure that the colors are already prepared (by calling
     * getResources().getColor(...)) before adding them to the DataSet.
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
     * getResources().getColor(...)) before adding them to the DataSet.
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
     * getResources().getColor(...)
     *
     * @param colors
     */
    fun setColors(colors: IntArray, c: Context) {
        mColors.clear()

        for (color in colors) {
            mColors.add(c.resources.getColor(color))
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
     * Sets a color with a specific alpha value.
     *
     * @param color
     * @param alpha from 0-255
     */
    fun setColor(color: Int, alpha: Int = 1) {
        this.color =
            Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    /**
     * Sets colors with a specific alpha value.
     *
     * @param colors
     * @param alpha
     */
    fun setColors(colors: IntArray, alpha: Int = 1) {
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

    override var valueFormatter: IValueFormatter
        get() {
            if (needsFormatter()) return Utils.getDefaultValueFormatter()
            return mValueFormatter
        }
        set(f) {
            mValueFormatter = f
        }

    override fun needsFormatter(): Boolean {
        return false
    }

    override fun setValueTextColors(colors: MutableList<Int>) {
        valueColors = colors
    }

    override var valueTextColor: Int
        get() = valueColors[0]
        set(color) {
            valueColors.clear()
            valueColors.add(color)
        }

    override fun getValueTextColor(index: Int): Int {
        return valueColors[index % valueColors.size]
    }

    override var valueTextSize: Float
        get() = mValueTextSize
        set(size) {
            mValueTextSize = Utils.convertDpToPixel(size)
        }

    override fun setDrawValues(enabled: Boolean) {
        this.isDrawValuesEnabled = enabled
    }

    override fun setDrawIcons(enabled: Boolean) {
        isDrawIconsEnabled = enabled
    }

    override var iconsOffset: MPPointF?
        get() = mIconsOffset
        set(offsetDp) {
            offsetDp?.let {
                mIconsOffset.x = offsetDp.x
                mIconsOffset.y = offsetDp.y
            } ?: run {
                mIconsOffset.x = 0f
                mIconsOffset.y = 0f
            }
        }


    /**
     * ###### ###### DATA RELATED METHODS ###### ######
     */
    override fun getIndexInEntries(xIndex: Int): Int {
        for (i in 0..<entryCount) {
            if (xIndex.toFloat() == getEntryForIndex(i).x) return i
        }

        return -1
    }

    override fun removeFirst(): Boolean {
        if (entryCount > 0) {
            val entry: T = getEntryForIndex(0)
            return removeEntry(entry)
        } else return false
    }

    override fun removeLast(): Boolean {
        if (entryCount > 0) {
            val e: T = getEntryForIndex(entryCount - 1)
            return removeEntry(e)
        } else return false
    }

    override fun removeEntryByXValue(xValue: Float): Boolean {
        val e: T = getEntryForXValue(xValue, Float.NaN)
        return removeEntry(e)
    }

    override fun removeEntry(index: Int): Boolean {
        val e: T = getEntryForIndex(index)
        return removeEntry(e)
    }

    override fun contains(entry: T): Boolean {
        for (i in 0..<entryCount) {
            if (getEntryForIndex(i) == entry) return true
        }

        return false
    }

    protected fun copy(baseDataSet: BaseDataSet<*>) {
        baseDataSet.axisDependency = axisDependency
        baseDataSet.mColors = mColors
        baseDataSet.isDrawIconsEnabled = isDrawIconsEnabled
        baseDataSet.isDrawValuesEnabled = isDrawValuesEnabled
        baseDataSet.form = form
        baseDataSet.formLineDashEffect = formLineDashEffect
        baseDataSet.formLineWidth = formLineWidth
        baseDataSet.formSize = formSize
        baseDataSet.isHighlightEnabled = isHighlightEnabled
        baseDataSet.mIconsOffset = mIconsOffset
        baseDataSet.valueColors = valueColors
        baseDataSet.mValueFormatter = mValueFormatter
        baseDataSet.mValueTextSize = mValueTextSize
        baseDataSet.isVisible = isVisible
    }
}
