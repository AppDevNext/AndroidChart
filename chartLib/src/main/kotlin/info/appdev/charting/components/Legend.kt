package info.appdev.charting.components

import android.graphics.DashPathEffect
import android.graphics.Paint
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.FSize
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.calcTextWidth
import info.appdev.charting.utils.convertDpToPixel
import info.appdev.charting.utils.calcTextSize
import info.appdev.charting.utils.getLineHeight
import info.appdev.charting.utils.getLineSpacing
import kotlin.Array
import kotlin.Boolean
import kotlin.IntArray
import kotlin.String
import kotlin.arrayOf
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.toTypedArray
import kotlin.math.max
import kotlin.math.min

/**
 * Class representing the legend of the chart. The legend will contain one entry
 * per color and DataSet. Multiple colors in one DataSet are grouped together.
 * The legend object is NOT available before setting data to the chart.
 */
class Legend() : ComponentBase() {
    enum class LegendForm {
        /**
         * Avoid drawing a form
         */
        NONE,

        /**
         * Do not draw the a form, but leave space for it
         */
        EMPTY,

        /**
         * Use default (default dataset's form to the legend's form)
         */
        DEFAULT,

        /**
         * Draw a square
         */
        SQUARE,

        /**
         * Draw a circle
         */
        CIRCLE,

        /**
         * Draw a horizontal line
         */
        LINE
    }

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
    var entries: Array<LegendEntry> = arrayOf<LegendEntry>()
        private set

    /**
     * Entries that will be appended to the end of the auto calculated entries after calculating the legend.
     * (if the legend has already been calculated, you will need to call notifyDataSetChanged() to let the changes take effect)
     */
    var extraEntries: Array<LegendEntry> = arrayOf<LegendEntry>()
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
    var formLineDashEffect: DashPathEffect? = null

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

    /**
     * returns the maximum length in pixels across all legend labels + formsize
     * + formtotextspace
     *
     * @param p the paint object used for rendering the text
     */
    fun getMaximumEntryWidth(p: Paint): Float {
        var max = 0f
        var maxFormSize = 0f
        val formToTextSpace = formToTextSpace.convertDpToPixel()

        for (entry in this.entries) {
            val formSize = (if (entry.formSize.isNaN())
                this.formSize
            else
                entry.formSize).convertDpToPixel()
            if (formSize > maxFormSize) maxFormSize = formSize

            val label = entry.label ?: continue

            val length = p.calcTextWidth(label).toFloat()

            if (length > max) max = length
        }

        return max + maxFormSize + formToTextSpace
    }

    /**
     * returns the maximum height in pixels across all legend labels
     *
     * @param p the paint object used for rendering the text
     */
    fun getMaximumEntryHeight(p: Paint): Float {
        var max = 0f

        for (entry in this.entries) {
            val label = entry.label
            if (label == null) continue

            val length = p.calcTextHeight(label).toFloat()

            if (length > max) max = length
        }

        return max
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

    val calculatedLabelSizes: MutableList<FSize> = ArrayList(16)
    val calculatedLabelBreakPoints: MutableList<Boolean> = ArrayList(16)
    val calculatedLineSizes: MutableList<FSize> = ArrayList(16)

    init {
        this.mTextSize = 10f.convertDpToPixel()
        this.mXOffset = 5f.convertDpToPixel()
        this.mYOffset = 3f.convertDpToPixel() // 2
    }

    /**
     * Calculates the dimensions of the Legend. This includes the maximum width
     * and height of a single entry, as well as the total width and height of
     * the Legend.
     */
    fun calculateDimensions(labelpaint: Paint, viewPortHandler: ViewPortHandler) {
        val defaultFormSize = formSize.convertDpToPixel()
        val stackSpace = stackSpace.convertDpToPixel()
        val formToTextSpace = formToTextSpace.convertDpToPixel()
        val xEntrySpace = xEntrySpace.convertDpToPixel()
        val yEntrySpace = yEntrySpace.convertDpToPixel()
        val wordWrapEnabled = this.isWordWrapEnabled
        val entries = this.entries
        val entryCount = entries.size

        mTextWidthMax = getMaximumEntryWidth(labelpaint)
        mTextHeightMax = getMaximumEntryHeight(labelpaint)

        when (this.orientation) {
            LegendOrientation.VERTICAL -> {
                var maxWidth = 0f
                var maxHeight = 0f
                var width = 0f
                val labelLineHeight = labelpaint.getLineHeight()
                var wasStacked = false

                var i = 0
                while (i < entryCount) {
                    val e = entries[i]
                    val drawingForm = e.form != LegendForm.NONE
                    val formSize = if (e.formSize.isNaN())
                        defaultFormSize
                    else
                        e.formSize.convertDpToPixel()
                    val label = e.label

                    if (!wasStacked) width = 0f

                    if (drawingForm) {
                        if (wasStacked) width += stackSpace
                        width += formSize
                    }

                    // grouped forms have null labels
                    if (label != null) {
                        // make a step to the left

                        if (drawingForm && !wasStacked) width += formToTextSpace
                        else if (wasStacked) {
                            maxWidth = max(maxWidth, width)
                            maxHeight += labelLineHeight + yEntrySpace
                            width = 0f
                            wasStacked = false
                        }

                        width += labelpaint.calcTextWidth(label).toFloat()

                        maxHeight += labelLineHeight + yEntrySpace
                    } else {
                        wasStacked = true
                        width += formSize
                        if (i < entryCount - 1) width += stackSpace
                    }

                    maxWidth = max(maxWidth, width)
                    i++
                }

                neededWidth = maxWidth
                neededHeight = maxHeight
            }

            LegendOrientation.HORIZONTAL -> {
                val labelLineHeight = labelpaint.getLineHeight()
                val labelLineSpacing = labelpaint.getLineSpacing() + yEntrySpace
                val contentWidth = viewPortHandler.contentWidth() * this.maxSizePercent

                // Start calculating layout
                var maxLineWidth = 0f
                var currentLineWidth = 0f
                var requiredWidth = 0f
                var stackedStartIndex = -1

                calculatedLabelBreakPoints.clear()
                calculatedLabelSizes.clear()
                calculatedLineSizes.clear()

                var i = 0
                while (i < entryCount) {
                    val e = entries[i]
                    val drawingForm = e.form != LegendForm.NONE
                    val formSize = if (e.formSize.isNaN())
                        defaultFormSize
                    else
                        e.formSize.convertDpToPixel()
                    val label = e.label

                    calculatedLabelBreakPoints.add(false)

                    if (stackedStartIndex == -1) {
                        // we are not stacking, so required width is for this label
                        // only
                        requiredWidth = 0f
                    } else {
                        // add the spacing appropriate for stacked labels/forms
                        requiredWidth += stackSpace
                    }

                    // grouped forms have null labels
                    if (label != null) {
                        calculatedLabelSizes.add(labelpaint.calcTextSize(label))
                        requiredWidth += if (drawingForm) formToTextSpace + formSize else 0f
                        requiredWidth += calculatedLabelSizes.get(i).width
                    } else {
                        calculatedLabelSizes.add(FSize.getInstance(0f, 0f))
                        requiredWidth += if (drawingForm) formSize else 0f

                        if (stackedStartIndex == -1) {
                            // mark this index as we might want to break here later
                            stackedStartIndex = i
                        }
                    }

                    if (label != null || i == entryCount - 1) {
                        val requiredSpacing = if (currentLineWidth == 0f) 0f else xEntrySpace

                        if (!wordWrapEnabled // No word wrapping, it must fit.
                            // The line is empty, it must fit
                            || currentLineWidth == 0f // It simply fits
                            || (contentWidth - currentLineWidth >=
                                    requiredSpacing + requiredWidth)
                        ) {
                            // Expand current line
                            currentLineWidth += requiredSpacing + requiredWidth
                        } else { // It doesn't fit, we need to wrap a line

                            // Add current line size to array

                            calculatedLineSizes.add(FSize.getInstance(currentLineWidth, labelLineHeight))
                            maxLineWidth = max(maxLineWidth, currentLineWidth)

                            // Start a new line
                            calculatedLabelBreakPoints.set(
                                if (stackedStartIndex > -1)
                                    stackedStartIndex
                                else
                                    i, true
                            )
                            currentLineWidth = requiredWidth
                        }

                        if (i == entryCount - 1) {
                            // Add last line size to array
                            calculatedLineSizes.add(FSize.getInstance(currentLineWidth, labelLineHeight))
                            maxLineWidth = max(maxLineWidth, currentLineWidth)
                        }
                    }

                    stackedStartIndex = if (label != null) -1 else stackedStartIndex
                    i++
                }

                neededWidth = maxLineWidth
                neededHeight = (labelLineHeight
                        * (calculatedLineSizes.size).toFloat()
                        + labelLineSpacing * (if (calculatedLineSizes.isEmpty())
                    0
                else
                    (calculatedLineSizes.size - 1)).toFloat())
            }
        }

        neededHeight += mYOffset
        neededWidth += mXOffset
    }
}
