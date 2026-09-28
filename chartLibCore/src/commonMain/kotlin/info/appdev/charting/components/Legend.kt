package info.appdev.charting.components

import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.DashEffect
import info.appdev.charting.utils.FSize
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel
import kotlin.math.max
import kotlin.math.min

/**
 * Class representing the legend of the chart. The legend will contain one entry
 * per color and DataSet. Multiple colors in one DataSet are grouped together.
 * The legend object is NOT available before setting data to the chart.
 */
class Legend() : ComponentBase() {
    /**
     * Kept as a nested type alias (rather than a nested enum) so that the actual enum can live
     * in the platform-independent `chartLibCore` module while `Legend.LegendForm` keeps
     * working unchanged for existing callers.
     */
    typealias LegendForm = info.appdev.charting.components.LegendForm

    enum class LegendHorizontalAlignment {
        LEFT, CENTER, RIGHT
    }

    enum class LegendVerticalAlignment {
        TOP, CENTER, BOTTOM
    }

    enum class LegendOrientation {
        HORIZONTAL, VERTICAL
    }

    enum class LegendDirection {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    /**
     * The legend entries array
     */
    var entries: Array<LegendEntry> = arrayOf()
        private set

    /**
     * Entries that will be appended to the end of the auto calculated entries after calculating the legend.
     * (if the legend has already been calculated, you will need to call notifyDataSetChanged() to let the changes take effect)
     */
    var extraEntries: Array<LegendEntry> = arrayOf()
        private set

    /**
     * Are the legend labels/colors a custom value or auto calculated? If false,
     * then it's auto, if true, then custom. default false (automatic legend)
     */
    var isLegendCustom: Boolean = false
        private set

    /**
     * sets the horizontal alignment of the legend
     */
    var horizontalAlignment: LegendHorizontalAlignment = LegendHorizontalAlignment.LEFT

    /**
     * sets the vertical alignment of the legend
     */
    var verticalAlignment: LegendVerticalAlignment = LegendVerticalAlignment.BOTTOM

    /**
     * sets the orientation of the legend
     */
    var orientation: LegendOrientation = LegendOrientation.HORIZONTAL

    /**
     * returns whether the legend will draw inside the chart or outside
     */
    var isDrawInsideEnabled: Boolean = false
        private set

    /**
     * the text direction for the legend
     */
    var direction: LegendDirection = LegendDirection.LEFT_TO_RIGHT

    /**
     * the shape/form the legend colors are drawn in
     */
    var form: LegendForm = LegendForm.SQUARE

    /**
     * the size of the legend forms/shapes
     */
    var formSize: Float = 8f

    /**
     * the size of the legend forms/shapes
     */
    var formLineWidth: Float = 3f

    /**
     * Line dash path effect used for shapes that consist of lines.
     */
    var formLineDashEffect: DashEffect? = null

    /**
     * the space between the legend entries on a horizontal axis, default 6f
     */
    var xEntrySpace: Float = 6f

    /**
     * the space between the legend entries on a vertical axis, default 5f
     */
    var yEntrySpace: Float = 0f

    /**
     * the space between the legend entries on a vertical axis, default 2f
     * private float mYEntrySpace = 2f; / ** the space between the form and the
     * actual label/text
     */
    var formToTextSpace: Float = 5f

    /**
     * the space that should be left between stacked forms
     */
    var stackSpace: Float = 3f

    /**
     * The maximum relative size out of the whole chart view. / If the legend is
     * to the right/left of the chart, then this affects the width of the
     * legend. / If the legend is to the top/bottom of the chart, then this
     * affects the height of the legend. / If the legend is the center of the
     * piechart, then this defines the size of the rectangular bounds out of the
     * size of the "hole". / default: 0.95f (95%)
     */
    var maxSizePercent: Float = 0.95f

    /**
     * Constructor. Provide entries for the legend.
     */
    constructor(entries: Array<LegendEntry>) : this() {
        this.entries = entries
    }

    /**
     * This method sets the automatically computed colors for the legend. Use setCustom(...) to set custom colors.
     */
    fun setEntries(entries: MutableList<LegendEntry>) {
        this.entries = entries.toTypedArray<LegendEntry>()
    }

    fun setExtra(entries: MutableList<LegendEntry>) {
        this.extraEntries = entries.toTypedArray<LegendEntry>()
    }

    fun setExtra(entries: Array<LegendEntry>) {
        this.extraEntries = entries
    }

    /**
     * Entries that will be appended to the end of the auto calculated
     * entries after calculating the legend.
     * (if the legend has already been calculated, you will need to call notifyDataSetChanged()
     * to let the changes take effect)
     */
    fun setExtra(colors: IntArray, labels: Array<String>) {
        val entries: MutableList<LegendEntry> = ArrayList()

        for (i in 0..<min(colors.size, labels.size)) {
            val entry = LegendEntry()
            entry.formColor = colors[i]
            entry.label = labels[i]

            if (entry.formColor == ColorTemplate.COLOR_SKIP ||
                entry.formColor == 0
            ) entry.form = LegendForm.NONE
            else if (entry.formColor == ColorTemplate.COLOR_NONE) entry.form = LegendForm.EMPTY

            entries.add(entry)
        }

        this.extraEntries = entries.toTypedArray<LegendEntry>()
    }

    /**
     * Sets a custom legend's entries array.
     * * A null label will start a group.
     * This will disable the feature that automatically calculates the legend
     * entries from the datasets.
     * Call resetCustom() to re-enable automatic calculation (and then
     * notifyDataSetChanged() is needed to auto-calculate the legend again)
     */
    fun setCustom(entries: Array<LegendEntry>) {
        this.entries = entries
        this.isLegendCustom = true
    }

    /**
     * Sets a custom legend's entries array.
     * * A null label will start a group.
     * This will disable the feature that automatically calculates the legend
     * entries from the datasets.
     * Call resetCustom() to re-enable automatic calculation (and then
     * notifyDataSetChanged() is needed to auto-calculate the legend again)
     */
    fun setCustom(entries: MutableList<LegendEntry>) {
        this.entries = entries.toTypedArray<LegendEntry>()
        this.isLegendCustom = true
    }

    /**
     * Calling this will disable the custom legend entries (set by
     * setCustom(...)). Instead, the entries will again be calculated
     * automatically (after notifyDataSetChanged() is called).
     */
    fun resetCustom() {
        this.isLegendCustom = false
    }

    /**
     * sets whether the legend will draw inside the chart or outside
     */
    fun setDrawInside(value: Boolean) {
        this.isDrawInsideEnabled = value
    }

    /**
     * the total width of the legend (needed width space)
     */
    var neededWidth: Float = 0f

    /**
     * the total height of the legend (needed height space)
     */
    var neededHeight: Float = 0f

    var mTextHeightMax: Float = 0f

    var mTextWidthMax: Float = 0f

    /**
     * Should the legend word wrap? / this is currently supported only for:
     * BelowChartLeft, BelowChartRight, BelowChartCenter. / note that word
     * wrapping a legend takes a toll on performance. / you may want to set
     * maxSizePercent when word wrapping, to set the point where the text wraps.
     * / default: false
     */
    var isWordWrapEnabled: Boolean = false

    val calculatedLabelSizes: MutableList<FSize> = mutableListOf()
    val calculatedLabelBreakPoints: MutableList<Boolean> = mutableListOf()
    val calculatedLineSizes: MutableList<FSize> = mutableListOf()

    init {
        this.mTextSize = 10f.convertDpToPixel()
        this.mXOffset = 5f.convertDpToPixel()
        this.mYOffset = 3f.convertDpToPixel() // 2
    }

}
