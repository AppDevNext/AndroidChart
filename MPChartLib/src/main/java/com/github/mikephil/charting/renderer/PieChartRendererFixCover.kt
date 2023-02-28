package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet.ValuePosition
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class PieChartRendererFixCover(chart: PieChart?, animator: ChartAnimator?, viewPortHandler: ViewPortHandler?) :
    PieChartRenderer(chart, animator, viewPortHandler) {
    var text = "2.0%"
    private var mode: String? = null
    fun setMode(mode: String?): PieChartRendererFixCover {
        this.mode = mode
        return this
    }

    override fun drawValues(c: Canvas) {
        if (TextUtils.isEmpty(mode) || TextUtils.equals(mode, "1")) {
            drawValuesWithAVG(c)
        } else if (TextUtils.equals(mode, "2")) {
            drawValuesTopAlign(c)
        } else if (TextUtils.equals(mode, "3")) {
            drawValuesNotTopAlign(c)
        } else {
            drawValuesWithAVG(c)
        }
    }

    private fun drawValuesWithAVG(canvas: Canvas) {
        val rect = Rect()
        paintEntryLabels.getTextBounds(text, 0, text.length, rect)
        val center = mChart.centerCircleBox

        // get whole the radius
        val radius = mChart.radius
        val rotationAngle = mChart.rotationAngle
        val drawAngles = mChart.drawAngles
        val absoluteAngles = mChart.absoluteAngles
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        val holeRadiusPercent = mChart.holeRadius / 100f
        var labelRadiusOffset = radius / 10f * 3.6f
        if (mChart.isDrawHoleEnabled) {
            labelRadiusOffset = radius - radius * holeRadiusPercent / 2f
        }
        val labelRadius = radius - labelRadiusOffset
        val data = mChart.data
        val dataSets = data.dataSets
        val yValueSum = data.yValueSum
        val drawEntryLabels = mChart.isDrawEntryLabelsEnabled
        var angle: Float
        var xIndex = 0
        canvas.save()
        val offset = Utils.convertDpToPixel(5f)
        for (i in dataSets.indices) {
            val dataSet = dataSets[i]
            val drawValues = dataSet.isDrawValuesEnabled
            if (!drawValues && !drawEntryLabels) continue
            val xValuePosition = dataSet.xValuePosition
            val yValuePosition = dataSet.yValuePosition

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet)
            val lineHeight = (Utils.calcTextHeight(mValuePaint, "Q")
                    + Utils.convertDpToPixel(4f))
            val formatter = dataSet.valueFormatter
            val entryCount = dataSet.entryCount
            mValueLinePaint.color = dataSet.valueLineColor
            mValueLinePaint.strokeWidth = Utils.convertDpToPixel(dataSet.valueLineWidth)
            val sliceSpace = getSliceSpace(dataSet)
            val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)
            var rightCount = 0
            var leftCount = 0
            var leftToRightCount = 0
            var rightToRightCount = 0
            var rightToLeftCount = 0
            var leftToLeftCount = 0
            for (j in 0 until entryCount) {
                angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                val sliceAngle = drawAngles[xIndex]
                val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                // offset needed to center the drawn text in the slice
                val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                angle += angleOffset
                val transformedAngle = rotationAngle + angle * phaseY
                val drawXOutside = drawEntryLabels &&
                        xValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawYOutside = drawValues &&
                        yValuePosition == ValuePosition.OUTSIDE_SLICE
                if (drawXOutside || drawYOutside) {
                    if (transformedAngle % 360.0 in 90.0..270.0) {
                        leftCount++
                        if (rotationAngle != 270f && angle * phaseY % 360.0 <= 180.0 && angle * phaseY % 360.0 >= 0) {
                            rightToLeftCount++
                        } else if (rotationAngle % 360 in 90.0..270.0 && rotationAngle != 270f && angle * phaseY % 360.0 > 180.0 && angle * phaseY % 360.0 < 360.0) {
                            leftToLeftCount++
                        }
                    } else {
                        rightCount++
                        if (rotationAngle != 270f && angle * phaseY % 360.0 > 180.0 && angle * phaseY % 360.0 < 360.0) {
                            leftToRightCount++
                        } else if (rotationAngle % 360 in 90.0..270.0 && rotationAngle != 270f && angle * phaseY % 360.0 <= 180.0 && angle * phaseY % 360.0 >= 0) {
                            rightToRightCount++
                        }
                    }
                }
                xIndex++
            }
            xIndex = 0
            val measuredHeight = mChart.measuredHeight
            val topAndBottomSpace = measuredHeight - radius * 2
            val rightSpace = radius * 2 / (rightCount - 1)
            val leftSpace = radius * 2 / (leftCount - 1)
            var tempRightIndex = 0
            var tempLeftIndex = 0
            var tempLeftToRightIndex = 0
            var tempRightToRightIndex = 0
            var tempRightToLeftIndex = 0
            var tempLeftToLeftIndex = 0
            for (j in 0 until entryCount) {
                val entry = dataSet.getEntryForIndex(j)
                angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                val sliceAngle = drawAngles[xIndex]
                val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                // offset needed to center the drawn text in the slice
                val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                angle += angleOffset
                val transformedAngle = rotationAngle + angle * phaseY
                val value: Float = if (mChart.isUsePercentValuesEnabled)
                    entry.y / yValueSum * 100f
                else
                    entry.y
                val sliceXBase = cos((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val sliceYBase = sin((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val drawXOutside = drawEntryLabels &&
                        xValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawYOutside = drawValues &&
                        yValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawXInside = drawEntryLabels &&
                        xValuePosition == ValuePosition.INSIDE_SLICE
                val drawYInside = drawValues &&
                        yValuePosition == ValuePosition.INSIDE_SLICE
                if (drawXOutside || drawYOutside) {
                    val valueLineLength1 = dataSet.valueLinePart1Length
                    val valueLineLength2 = dataSet.valueLinePart2Length
                    val valueLinePart1OffsetPercentage = dataSet.valueLinePart1OffsetPercentage / 100f
                    var pt2x: Float
                    var pt2y: Float
                    var labelPtx: Float
                    var labelPty: Float
                    val line1Radius: Float = if (mChart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                            * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage
                    val polyline2Width = if (dataSet.isValueLineVariableLength) labelRadius * valueLineLength2 * abs(
                        sin(
                            (
                                    transformedAngle * Utils.FDEG2RAD).toDouble()
                        )
                    ).toFloat() else labelRadius * valueLineLength2
                    val pt0x = line1Radius * sliceXBase + center.x
                    val pt0y = line1Radius * sliceYBase + center.y
                    val pt1x = labelRadius * (1 + valueLineLength1) * sliceXBase + center.x
                    val pt1y = labelRadius * (1 + valueLineLength1) * sliceYBase + center.y
                    if (transformedAngle % 360.0 in 90.0..270.0) {
                        pt2x = center.x - radius - 5
                        if (rotationAngle != 270f && angle * phaseY % 360.0 <= 180.0 && angle * phaseY % 360.0 >= 0) {
                            pt2y = measuredHeight - topAndBottomSpace / 2 - leftSpace * (tempRightToLeftIndex + leftToLeftCount)
                            tempRightToLeftIndex++
                            tempLeftIndex++
                        } else if (rotationAngle % 360 in 90.0..270.0 && rotationAngle != 270f && angle * phaseY % 360.0 > 180.0 && angle * phaseY % 360.0 < 360.0) {
                            pt2y = measuredHeight - topAndBottomSpace / 2 - leftSpace * tempLeftToLeftIndex
                            tempLeftToLeftIndex++
                        } else {
                            pt2y = measuredHeight - topAndBottomSpace / 2 - leftSpace * (tempLeftIndex + leftToLeftCount)
                            tempLeftIndex++
                        }
                        mValuePaint.textAlign = Paint.Align.RIGHT
                        if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.RIGHT
                        labelPtx = pt2x - offset
                        labelPty = pt2y
                    } else {
                        pt2x = center.x + radius + 5
                        if (rotationAngle != 270f && angle * phaseY % 360.0 > 180.0 && angle * phaseY % 360.0 < 360.0) {
                            pt2y = topAndBottomSpace / 2 + rightSpace * (tempLeftToRightIndex + rightToRightCount)
                            tempLeftToRightIndex++
                            tempRightIndex++
                        } else if (rotationAngle % 360 in 90.0..270.0 && rotationAngle != 270f && angle * phaseY % 360.0 <= 180.0 && angle * phaseY % 360.0 >= 0) {
                            pt2y = topAndBottomSpace / 2 + rightSpace * tempRightToRightIndex
                            tempRightIndex++
                            tempRightToRightIndex++
                        } else {
                            pt2y = topAndBottomSpace / 2 + rightSpace * (tempRightIndex + leftToRightCount + rightToRightCount)
                            tempRightIndex++
                        }
                        mValuePaint.textAlign = Paint.Align.LEFT
                        if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.LEFT
                        labelPtx = pt2x + offset
                        labelPty = pt2y
                    }
                    if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                        canvas.drawLine(pt0x, pt0y, pt1x, pt1y, mValueLinePaint)
                        canvas.drawLine(pt1x, pt1y, pt2x, pt2y, mValueLinePaint)
                    }

                    // draw everything, depending on settings
                    if (drawXOutside && drawYOutside) {
                        drawValue(
                            canvas,
                            formatter,
                            value,
                            entry,
                            0,
                            labelPtx,
                            labelPty,
                            dataSet.getValueTextColor(j)
                        )
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(canvas, entry.label, labelPtx, labelPty + lineHeight)
                        }
                    } else if (drawXOutside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(canvas, entry.label, labelPtx, labelPty + lineHeight / 2f)
                        }
                    } else if (drawYOutside) {
                        drawValue(
                            canvas, formatter, value, entry, 0, labelPtx, labelPty + lineHeight / 2f, dataSet
                                .getValueTextColor(j)
                        )
                    }
                }
                if (drawXInside || drawYInside) {
                    // calculate the text position
                    val x = labelRadius * sliceXBase + center.x
                    val y = labelRadius * sliceYBase + center.y
                    mValuePaint.textAlign = Paint.Align.CENTER

                    // draw everything, depending on settings
                    if (drawXInside && drawYInside) {
                        drawValue(canvas, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(canvas, entry.label, x, y + lineHeight)
                        }
                    } else if (drawXInside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(canvas, entry.label, x, y + lineHeight / 2f)
                        }
                    } else if (drawYInside) {
                        drawValue(canvas, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                    }
                }
                if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                    val icon = entry.icon
                    val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                    var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                    y += iconsOffset.x
                    Utils.drawImage(
                        canvas,
                        icon, x.toInt(), y.toInt(),
                        icon.intrinsicWidth,
                        icon.intrinsicHeight
                    )
                }
                xIndex++
            }
            MPPointF.recycleInstance(iconsOffset)
        }
        MPPointF.recycleInstance(center)
        canvas.restore()
    }

    private fun drawValuesTopAlign(c: Canvas) {
        val rect = Rect()
        paintEntryLabels.getTextBounds(text, 0, text.length, rect)
        val textHeight = rect.height()
        val center = mChart.centerCircleBox

        // get whole the radius
        val radius = mChart.radius
        val rotationAngle = mChart.rotationAngle
        val drawAngles = mChart.drawAngles
        val absoluteAngles = mChart.absoluteAngles
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        val holeRadiusPercent = mChart.holeRadius / 100f
        var labelRadiusOffset = radius / 10f * 3.6f
        if (mChart.isDrawHoleEnabled) {
            labelRadiusOffset = radius - radius * holeRadiusPercent / 2f
        }
        val labelRadius = radius - labelRadiusOffset
        val data = mChart.data
        val dataSets = data.dataSets
        val yValueSum = data.yValueSum
        val drawEntryLabels = mChart.isDrawEntryLabelsEnabled
        var angle: Float
        var xIndex = 0
        c.save()
        val offset = Utils.convertDpToPixel(5f)
        for (i in dataSets.indices) {
            val dataSet = dataSets[i]
            val drawValues = dataSet.isDrawValuesEnabled
            if (!drawValues && !drawEntryLabels) continue
            val xValuePosition = dataSet.xValuePosition
            val yValuePosition = dataSet.yValuePosition

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet)
            val lineHeight = (Utils.calcTextHeight(mValuePaint, "Q")
                    + Utils.convertDpToPixel(4f))
            val formatter = dataSet.valueFormatter
            val entryCount = dataSet.entryCount
            mValueLinePaint.color = dataSet.valueLineColor
            mValueLinePaint.strokeWidth = Utils.convertDpToPixel(dataSet.valueLineWidth)
            val sliceSpace = getSliceSpace(dataSet)
            val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)
            var lastPositionOfLeft = 0f
            var lastPositionOfRight = 0f
            for (j in 0 until entryCount) {
                val entry = dataSet.getEntryForIndex(j)
                angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                val sliceAngle = drawAngles[xIndex]
                val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                // offset needed to center the drawn text in the slice
                val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                angle += angleOffset
                val transformedAngle = rotationAngle + angle * phaseY
                val value: Float = if (mChart.isUsePercentValuesEnabled)
                    entry.y / yValueSum * 100f
                else
                    entry.y
                val sliceXBase = cos((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val sliceYBase = sin((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val drawXOutside = drawEntryLabels &&
                        xValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawYOutside = drawValues &&
                        yValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawXInside = drawEntryLabels &&
                        xValuePosition == ValuePosition.INSIDE_SLICE
                val drawYInside = drawValues &&
                        yValuePosition == ValuePosition.INSIDE_SLICE
                if (drawXOutside || drawYOutside) {
                    val valueLineLength1 = dataSet.valueLinePart1Length
                    val valueLineLength2 = dataSet.valueLinePart2Length
                    val valueLinePart1OffsetPercentage = dataSet.valueLinePart1OffsetPercentage / 100f
                    var pt2x: Float
                    var pt2y: Float
                    var labelPtx: Float
                    var labelPty: Float
                    val line1Radius: Float = if (mChart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                            * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage
                    val polyline2Width = if (dataSet.isValueLineVariableLength) labelRadius * valueLineLength2 * abs(
                        sin(
                            (
                                    transformedAngle * Utils.FDEG2RAD).toDouble()
                        )
                    ).toFloat() else labelRadius * valueLineLength2
                    val pt0x = line1Radius * sliceXBase + center.x
                    val pt0y = line1Radius * sliceYBase + center.y
                    val pt1x = labelRadius * (1 + valueLineLength1) * sliceXBase + center.x
                    val pt1y = labelRadius * (1 + valueLineLength1) * sliceYBase + center.y
                    if (transformedAngle % 360.0 in 90.0..270.0) {
                        break
                    } else {
                        pt2x = center.x + radius + 5
                        pt2y = if (lastPositionOfRight == 0f) {
                            pt1y
                        } else {
                            if (pt1y - lastPositionOfRight < textHeight) {
                                pt1y + (textHeight - (pt1y - lastPositionOfRight))
                            } else {
                                pt1y
                            }
                        }
                        lastPositionOfRight = pt2y
                        mValuePaint.textAlign = Paint.Align.LEFT
                        if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.LEFT
                        labelPtx = pt2x + offset
                        labelPty = pt2y
                    }
                    if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                        c.drawLine(pt0x, pt0y, pt1x, pt1y, mValueLinePaint)
                        c.drawLine(pt1x, pt1y, pt2x, pt2y, mValueLinePaint)
                    }

                    // draw everything, depending on settings
                    if (drawXOutside && drawYOutside) {
                        drawValue(
                            c,
                            formatter,
                            value,
                            entry,
                            0,
                            labelPtx,
                            labelPty,
                            dataSet.getValueTextColor(j)
                        )
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, labelPtx, labelPty + lineHeight)
                        }
                    } else if (drawXOutside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, labelPtx, labelPty + lineHeight / 2f)
                        }
                    } else if (drawYOutside) {
                        drawValue(
                            c, formatter, value, entry, 0, labelPtx, labelPty + lineHeight / 2f, dataSet
                                .getValueTextColor(j)
                        )
                    }
                }
                if (drawXInside || drawYInside) {
                    // calculate the text position
                    val x = labelRadius * sliceXBase + center.x
                    val y = labelRadius * sliceYBase + center.y
                    mValuePaint.textAlign = Paint.Align.CENTER

                    // draw everything, depending on settings
                    if (drawXInside && drawYInside) {
                        drawValue(c, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, x, y + lineHeight)
                        }
                    } else if (drawXInside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, x, y + lineHeight / 2f)
                        }
                    } else if (drawYInside) {
                        drawValue(c, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                    }
                }
                if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                    val icon = entry.icon
                    val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                    var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                    y += iconsOffset.x
                    Utils.drawImage(
                        c,
                        icon, x.toInt(), y.toInt(),
                        icon.intrinsicWidth,
                        icon.intrinsicHeight
                    )
                }
                xIndex++
            }

            //画左边
            xIndex = entryCount - 1
            for (j in entryCount - 1 downTo 0) {
                val entry = dataSet.getEntryForIndex(j)
                angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                val sliceAngle = drawAngles[xIndex]
                val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                // offset needed to center the drawn text in the slice
                val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                angle += angleOffset
                val transformedAngle = rotationAngle + angle * phaseY
                val value: Float = if (mChart.isUsePercentValuesEnabled)
                    entry.y / yValueSum * 100f
                else
                    entry.y
                val sliceXBase = cos((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val sliceYBase = sin((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val drawXOutside = drawEntryLabels &&
                        xValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawYOutside = drawValues &&
                        yValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawXInside = drawEntryLabels &&
                        xValuePosition == ValuePosition.INSIDE_SLICE
                val drawYInside = drawValues &&
                        yValuePosition == ValuePosition.INSIDE_SLICE
                if (drawXOutside || drawYOutside) {
                    val valueLineLength1 = dataSet.valueLinePart1Length
                    val valueLineLength2 = dataSet.valueLinePart2Length
                    val valueLinePart1OffsetPercentage = dataSet.valueLinePart1OffsetPercentage / 100f
                    var pt2x: Float
                    var pt2y: Float
                    var labelPtx: Float
                    var labelPty: Float
                    val line1Radius: Float = if (mChart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                            * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage
                    val polyline2Width = if (dataSet.isValueLineVariableLength) labelRadius * valueLineLength2 * abs(
                        sin(
                            (
                                    transformedAngle * Utils.FDEG2RAD).toDouble()
                        )
                    ).toFloat() else labelRadius * valueLineLength2
                    val pt0x = line1Radius * sliceXBase + center.x
                    val pt0y = line1Radius * sliceYBase + center.y
                    val pt1x = labelRadius * (1 + valueLineLength1) * sliceXBase + center.x
                    val pt1y = labelRadius * (1 + valueLineLength1) * sliceYBase + center.y
                    if (transformedAngle % 360.0 in 90.0..270.0) {
                        pt2x = center.x - radius - 5
                        pt2y = if (lastPositionOfLeft == 0f) {
                            pt1y
                        } else {
                            if (pt1y - lastPositionOfLeft < textHeight) {
                                pt1y + (textHeight - (pt1y - lastPositionOfLeft))
                            } else {
                                pt1y
                            }
                        }
                        lastPositionOfLeft = pt2y
                        mValuePaint.textAlign = Paint.Align.RIGHT
                        if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.RIGHT
                        labelPtx = pt2x - offset
                        labelPty = pt2y
                    } else {
                        continue
                    }
                    if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                        c.drawLine(pt0x, pt0y, pt1x, pt1y, mValueLinePaint)
                        c.drawLine(pt1x, pt1y, pt2x, pt2y, mValueLinePaint)
                    }

                    // draw everything, depending on settings
                    if (drawXOutside && drawYOutside) {
                        drawValue(
                            c,
                            formatter,
                            value,
                            entry,
                            0,
                            labelPtx,
                            labelPty,
                            dataSet.getValueTextColor(j)
                        )
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, labelPtx, labelPty + lineHeight)
                        }
                    } else if (drawXOutside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, labelPtx, labelPty + lineHeight / 2f)
                        }
                    } else if (drawYOutside) {
                        drawValue(
                            c, formatter, value, entry, 0, labelPtx, labelPty + lineHeight / 2f, dataSet
                                .getValueTextColor(j)
                        )
                    }
                }
                if (drawXInside || drawYInside) {
                    // calculate the text position
                    val x = labelRadius * sliceXBase + center.x
                    val y = labelRadius * sliceYBase + center.y
                    mValuePaint.textAlign = Paint.Align.CENTER

                    // draw everything, depending on settings
                    if (drawXInside && drawYInside) {
                        drawValue(c, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, x, y + lineHeight)
                        }
                    } else if (drawXInside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, x, y + lineHeight / 2f)
                        }
                    } else if (drawYInside) {
                        drawValue(c, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                    }
                }
                if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                    val icon = entry.icon
                    val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                    var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                    y += iconsOffset.x
                    Utils.drawImage(
                        c,
                        icon, x.toInt(), y.toInt(),
                        icon.intrinsicWidth,
                        icon.intrinsicHeight
                    )
                }
                xIndex--
            }
            MPPointF.recycleInstance(iconsOffset)
        }
        MPPointF.recycleInstance(center)
        c.restore()
    }

    private fun drawValuesNotTopAlign(c: Canvas) {
        val rect = Rect()
        paintEntryLabels.getTextBounds(text, 0, text.length, rect)
        val textHeight = rect.height()
        val center = mChart.centerCircleBox

        // get whole the radius
        val radius = mChart.radius
        val rotationAngle = mChart.rotationAngle
        val drawAngles = mChart.drawAngles
        val absoluteAngles = mChart.absoluteAngles
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        val holeRadiusPercent = mChart.holeRadius / 100f
        var labelRadiusOffset = radius / 10f * 3.6f
        if (mChart.isDrawHoleEnabled) {
            labelRadiusOffset = radius - radius * holeRadiusPercent / 2f
        }
        val labelRadius = radius - labelRadiusOffset
        val data = mChart.data
        val dataSets = data.dataSets
        val yValueSum = data.yValueSum
        val drawEntryLabels = mChart.isDrawEntryLabelsEnabled
        var angle: Float
        var xIndex = 0
        c.save()
        val offset = Utils.convertDpToPixel(5f)
        for (i in dataSets.indices) {
            val dataSet = dataSets[i]
            val drawValues = dataSet.isDrawValuesEnabled
            if (!drawValues && !drawEntryLabels) continue
            val xValuePosition = dataSet.xValuePosition
            val yValuePosition = dataSet.yValuePosition

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet)
            val lineHeight = (Utils.calcTextHeight(mValuePaint, "Q")
                    + Utils.convertDpToPixel(4f))
            val formatter = dataSet.valueFormatter
            val entryCount = dataSet.entryCount
            mValueLinePaint.color = dataSet.valueLineColor
            mValueLinePaint.strokeWidth = Utils.convertDpToPixel(dataSet.valueLineWidth)
            val sliceSpace = getSliceSpace(dataSet)
            val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)
            var lastPositionOfLeft = 0f
            var lastPositionOfRight = 0f
            for (j in 0 until entryCount) {
                val entry = dataSet.getEntryForIndex(j)
                angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                val sliceAngle = drawAngles[xIndex]
                val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                // offset needed to center the drawn text in the slice
                val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                angle += angleOffset
                val transformedAngle = rotationAngle + angle * phaseY
                val value: Float = if (mChart.isUsePercentValuesEnabled)
                    entry.y / yValueSum * 100f
                else
                    entry.y
                val sliceXBase = cos((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val sliceYBase = sin((transformedAngle * Utils.FDEG2RAD).toDouble()).toFloat()
                val drawXOutside = drawEntryLabels &&
                        xValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawYOutside = drawValues &&
                        yValuePosition == ValuePosition.OUTSIDE_SLICE
                val drawXInside = drawEntryLabels &&
                        xValuePosition == ValuePosition.INSIDE_SLICE
                val drawYInside = drawValues &&
                        yValuePosition == ValuePosition.INSIDE_SLICE
                if (drawXOutside || drawYOutside) {
                    val valueLineLength1 = dataSet.valueLinePart1Length
                    val valueLineLength2 = dataSet.valueLinePart2Length
                    val valueLinePart1OffsetPercentage = dataSet.valueLinePart1OffsetPercentage / 100f
                    var pt2x: Float
                    var pt2y: Float
                    var labelPtx: Float
                    var labelPty: Float
                    val line1Radius: Float = if (mChart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                            * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage
                    val polyline2Width = if (dataSet.isValueLineVariableLength) labelRadius * valueLineLength2 * abs(
                        sin(
                            (
                                    transformedAngle * Utils.FDEG2RAD).toDouble()
                        )
                    ).toFloat() else labelRadius * valueLineLength2
                    val pt0x = line1Radius * sliceXBase + center.x
                    val pt0y = line1Radius * sliceYBase + center.y
                    val pt1x = labelRadius * (1 + valueLineLength1) * sliceXBase + center.x
                    val pt1y = labelRadius * (1 + valueLineLength1) * sliceYBase + center.y
                    if (transformedAngle % 360.0 in 90.0..270.0) {
                        pt2x = center.x - radius - 5
                        pt2y = if (lastPositionOfLeft == 0f) {
                            pt1y
                        } else {
                            if (lastPositionOfLeft - pt1y < textHeight) {
                                pt1y - (textHeight - (lastPositionOfLeft - pt1y))
                            } else {
                                pt1y
                            }
                        }
                        lastPositionOfLeft = pt2y
                        mValuePaint.textAlign = Paint.Align.RIGHT
                        if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.RIGHT
                        labelPtx = pt2x - offset
                        labelPty = pt2y
                    } else {
                        pt2x = center.x + radius + 5
                        pt2y = if (lastPositionOfRight == 0f) {
                            pt1y
                        } else {
                            if (pt1y - lastPositionOfRight < textHeight) {
                                pt1y + (textHeight - (pt1y - lastPositionOfRight))
                            } else {
                                pt1y
                            }
                        }
                        lastPositionOfRight = pt2y
                        mValuePaint.textAlign = Paint.Align.LEFT
                        if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.LEFT
                        labelPtx = pt2x + offset
                        labelPty = pt2y
                    }
                    if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                        c.drawLine(pt0x, pt0y, pt1x, pt1y, mValueLinePaint)
                        c.drawLine(pt1x, pt1y, pt2x, pt2y, mValueLinePaint)
                    }

                    // draw everything, depending on settings
                    if (drawXOutside && drawYOutside) {
                        drawValue(
                            c,
                            formatter,
                            value,
                            entry,
                            0,
                            labelPtx,
                            labelPty,
                            dataSet.getValueTextColor(j)
                        )
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, labelPtx, labelPty + lineHeight)
                        }
                    } else if (drawXOutside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, labelPtx, labelPty + lineHeight / 2f)
                        }
                    } else if (drawYOutside) {
                        drawValue(
                            c, formatter, value, entry, 0, labelPtx, labelPty + lineHeight / 2f, dataSet
                                .getValueTextColor(j)
                        )
                    }
                }
                if (drawXInside || drawYInside) {
                    // calculate the text position
                    val x = labelRadius * sliceXBase + center.x
                    val y = labelRadius * sliceYBase + center.y
                    mValuePaint.textAlign = Paint.Align.CENTER

                    // draw everything, depending on settings
                    if (drawXInside && drawYInside) {
                        drawValue(c, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, x, y + lineHeight)
                        }
                    } else if (drawXInside) {
                        if (j < data.entryCount && entry.label != null) {
                            drawEntryLabel(c, entry.label, x, y + lineHeight / 2f)
                        }
                    } else if (drawYInside) {
                        drawValue(c, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                    }
                }
                if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                    val icon = entry.icon
                    val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                    var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                    y += iconsOffset.x
                    Utils.drawImage(
                        c,
                        icon, x.toInt(), y.toInt(),
                        icon.intrinsicWidth,
                        icon.intrinsicHeight
                    )
                }
                xIndex++
            }
            MPPointF.recycleInstance(iconsOffset)
        }
        MPPointF.recycleInstance(center)
        c.restore()
    }
}