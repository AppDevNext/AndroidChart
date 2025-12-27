package info.appdev.charting.data

import android.graphics.Color
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.Fill
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.Int
import kotlin.String
import kotlin.let

open class BarDataSet(yVals: MutableList<BarEntry>, label: String) : BarLineScatterCandleBubbleDataSet<BarEntry>(yVals, label), IBarDataSet {
    /**
     * the maximum number of bars that are stacked upon each other, this value
     * is calculated from the Entries that are added to the DataSet
     */
    private var mStackSize = 1

    /**
     * the color used for drawing the bar shadows
     */
    private var mBarShadowColor = Color.rgb(215, 215, 215)

    private var mBarBorderWidth = 0.0f

    private var mBarBorderColor = Color.BLACK

    /**
     * the alpha value used to draw the highlight indicator bar
     */
    private var mHighLightAlpha = 120

    /**
     * returns the overall entry count, including counting each stack-value individually
     */
    /**
     * the overall entry count, including counting each stack-value individually
     */
    var entryCountStacks: Int = 0
        private set

    /**
     * array of labels used to describe the different values of the stacked bars
     */
    private var mStackLabels: MutableList<String> = mutableListOf()

    @get:Deprecated("Use getFills() instead")
    var gradients: MutableList<Fill> = mutableListOf()
        protected set

    init {
        highLightColor = Color.rgb(0, 0, 0)

        calcStackSize(yVals)
        calcEntryCountIncludingStacks(yVals)
    }

    override fun copy(): DataSet<BarEntry>? {
        val entries: MutableList<BarEntry> = mutableListOf()
        mEntries?.let {
            for (i in it.indices) {
                entries.add(it[i].copy())
            }
        }
        val copied = BarDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(barDataSet: BarDataSet) {
        super.copy((barDataSet as BaseDataSet<*>?)!!)
        barDataSet.mStackSize = mStackSize
        barDataSet.mBarShadowColor = mBarShadowColor
        barDataSet.mBarBorderWidth = mBarBorderWidth
        barDataSet.mStackLabels = mStackLabels
        barDataSet.mHighLightAlpha = mHighLightAlpha
    }

    override var fills: MutableList<Fill>
        get() = this.gradients
        set(value) {
            this.gradients = value
        }

    override fun getFill(index: Int): Fill? {
        return gradients[index % gradients.size]
    }

    @Deprecated("Use getFill(...) instead")
    fun getGradient(index: Int): Fill? {
        return getFill(index)
    }

    /**
     * Sets the start and end color for gradient color, ONLY color that should be used for this DataSet.
     */
    fun setGradientColor(startColor: Int, endColor: Int) {
        gradients.clear()
        gradients.add(Fill(startColor, endColor))
    }

    @Deprecated("Use setFills(...) instead")
    fun setGradientColors(gradientColors: MutableList<Fill>) {
        this.gradients = gradientColors
    }

    /**
     * Calculates the total number of entries this DataSet represents, including
     * stacks. All values belonging to a stack are calculated separately.
     */
    private fun calcEntryCountIncludingStacks(yVals: MutableList<BarEntry>) {
        this.entryCountStacks = 0

        for (i in yVals.indices) {
            val vals = yVals.get(i).yVals

            if (vals == null) this.entryCountStacks++
            else this.entryCountStacks += vals.size
        }
    }

    /**
     * calculates the maximum stackSize that occurs in the Entries array of this
     * DataSet
     */
    private fun calcStackSize(yVals: MutableList<BarEntry>) {
        for (i in yVals.indices) {
            val vals = yVals[i].yVals

            if (vals != null && vals.size > mStackSize) mStackSize = vals.size
        }
    }

    override fun calcMinMax(entry: BarEntry) {
        if (!entry.y.isNaN()) {
            if (entry.yVals == null) {
                if (entry.y < yMin) yMin = entry.y

                if (entry.y > yMax) yMax = entry.y
            } else {
                if (-entry.negativeSum < yMin) yMin = -entry.negativeSum

                if (entry.positiveSum > yMax) yMax = entry.positiveSum
            }

            calcMinMaxX(entry)
        }
    }

    override var stackSize: Int
        get() = mStackSize
        set(value) {
            mStackSize = value
        }

    override val isStacked: Boolean
        get() = mStackSize > 1

    /**
     * Sets the color used for drawing the bar-shadows. The bar shadows is a
     * surface behind the bar that indicates the maximum value. Don't for get to
     * use getResources().getColor(...) to set this. Or Color.rgb(...).
     */
    override var barShadowColor: Int
        get() = mBarShadowColor
        set(value) {
            mBarShadowColor = value
        }

    override var barBorderColor: Int
        get() = mBarBorderColor
        set(value) {
            mBarBorderColor = value
        }

    /**
     * The width used for drawing borders around the bars.
     * If borderWidth == 0, no border will be drawn.
     */
    override var barBorderWidth: kotlin.Float
        get() = mBarBorderWidth
        set(value) {
            mBarBorderWidth = value
        }

    /**
     * Set the alpha value (transparency) that is used for drawing the highlight
     * indicator bar. min = 0 (fully transparent), max = 255 (fully opaque)
     */
    override var highLightAlpha: Int
        get() = mHighLightAlpha
        set(value) {
            mHighLightAlpha = value
        }

    /**
     * Sets labels for different values of bar-stacks, in case there are one.
     */
    override var stackLabels: MutableList<String>
        get() = mStackLabels
        set(value) {
            mStackLabels = value
        }

    override fun getEntryIndex(entry: BarEntry): Int {
        return this.getEntryIndex(entry)
    }
}
