package info.appdev.charting.components

import android.graphics.Paint
import info.appdev.charting.utils.FSize
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.calcTextSize
import info.appdev.charting.utils.calcTextWidth
import info.appdev.charting.utils.convertDpToPixel
import info.appdev.charting.utils.getLineHeight
import info.appdev.charting.utils.getLineSpacing
import kotlin.math.max

/**
 * Returns the maximum length in pixels across all legend labels + formsize
 * + formtotextspace.
 *
 * Android-only extension since text measurement requires `android.graphics.Paint`.
 *
 * @param p the paint object used for rendering the text
 */
fun Legend.getMaximumEntryWidth(p: Paint): Float {
    var max = 0f
    var maxFormSize = 0f
    val formToTextSpace = formToTextSpace.convertDpToPixel()

    for (entry in this.entries) {
        if (entry == null) continue
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
 * Returns the maximum height in pixels across all legend labels.
 *
 * Android-only extension since text measurement requires `android.graphics.Paint`.
 *
 * @param p the paint object used for rendering the text
 */
fun Legend.getMaximumEntryHeight(p: Paint): Float {
    var max = 0f

    for (entry in this.entries) {
        if (entry == null) continue
        val label = entry.label ?: continue

        val length = p.calcTextHeight(label).toFloat()

        if (length > max) max = length
    }

    return max
}

/**
 * Calculates the dimensions of the Legend. This includes the maximum width
 * and height of a single entry, as well as the total width and height of
 * the Legend.
 *
 * Android-only extension since text measurement requires `android.graphics.Paint`.
 */
fun Legend.calculateDimensions(labelPaint: Paint, viewPortHandler: ViewPortHandler) {
    val defaultFormSize = formSize.convertDpToPixel()
    val stackSpace = stackSpace.convertDpToPixel()
    val formToTextSpace = formToTextSpace.convertDpToPixel()
    val xEntrySpace = xEntrySpace.convertDpToPixel()
    val yEntrySpace = yEntrySpace.convertDpToPixel()
    val wordWrapEnabled = this.isWordWrapEnabled
    val entries = this.entries
    val entryCount = entries.size

    mTextWidthMax = getMaximumEntryWidth(labelPaint)
    mTextHeightMax = getMaximumEntryHeight(labelPaint)

    when (this.orientation) {
        Legend.LegendOrientation.VERTICAL -> {
            var maxWidth = 0f
            var maxHeight = 0f
            var width = 0f
            val labelLineHeight = labelPaint.getLineHeight()
            var wasStacked = false

            var i = 0
            while (i < entryCount) {
                val e = entries[i]
                if (e == null) {
                    i++; continue
                }
                val drawingForm = e.form != Legend.LegendForm.NONE
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

                    width += labelPaint.calcTextWidth(label).toFloat()

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

        Legend.LegendOrientation.HORIZONTAL -> {
            val labelLineHeight = labelPaint.getLineHeight()
            val labelLineSpacing = labelPaint.getLineSpacing() + yEntrySpace
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
                val legendEntry = entries[i]
                if (legendEntry == null) {
                    i++; continue
                }
                val drawingForm = legendEntry.form != Legend.LegendForm.NONE
                val formSize = if (legendEntry.formSize.isNaN())
                    defaultFormSize
                else
                    legendEntry.formSize.convertDpToPixel()
                val label = legendEntry.label

                calculatedLabelBreakPoints.add(false)

                if (stackedStartIndex == -1) {
                    // we are not stacking, so required width is for this label only
                    requiredWidth = 0f
                } else {
                    // add the spacing appropriate for stacked labels/forms
                    requiredWidth += stackSpace
                }

                // grouped forms have null labels
                if (label != null) {
                    val fSize = labelPaint.calcTextSize(label)
                    calculatedLabelSizes.add(fSize)
                    requiredWidth += if (drawingForm) formToTextSpace + formSize else 0f
                    requiredWidth += fSize.width
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
                        calculatedLabelBreakPoints[if (stackedStartIndex > -1)
                            stackedStartIndex
                        else
                            i] = true
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

    neededHeight += yOffset
    neededWidth += xOffset
}
