package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.charts.PieChart
import info.appdev.charting.data.PieDataSet.ValuePosition
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.Utils
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.convertDpToPixel
import info.appdev.charting.utils.drawImage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class PieChartRendererFixCover(chart: PieChart, animator: ChartAnimator, viewPortHandler: ViewPortHandler) :
    PieChartRenderer(chart, animator, viewPortHandler) {
    var text = "2.0%"
    var mode: String? = null

    override fun drawValues(canvas: Canvas) {
        if (TextUtils.isEmpty(mode) || TextUtils.equals(mode, "1")) {
            drawValuesWithAVG(canvas)
        } else if (TextUtils.equals(mode, "2")) {
            drawValuesTopAlign(canvas)
        } else if (TextUtils.equals(mode, "3")) {
            drawValuesNotTopAlign(canvas)
        } else {
            drawValuesWithAVG(canvas)
        }
    }

    private fun drawValuesWithAVG(canvas: Canvas) {
        val rect = Rect()
        paintEntryLabels.getTextBounds(text, 0, text.length, rect)
        val center = chart.centerCircleBox

        // get whole the radius
        val radius = chart.radius
        val rotationAngle = chart.rotationAngle
        val drawAngles = chart.drawAngles
        val absoluteAngles = chart.absoluteAngles
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY
        val holeRadiusPercent = chart.holeRadius / 100f
        var labelRadiusOffset = radius / 10f * 3.6f
        if (chart.isDrawHoleEnabled) {
            labelRadiusOffset = radius - radius * holeRadiusPercent / 2f
        }
        val labelRadius = radius - labelRadiusOffset
        val data = chart.getData()
        val dataSets = data?.dataSets
        val yValueSum = data?.yValueSum ?: 0F
        val drawEntryLabels = chart.isDrawEntryLabelsEnabled
        var angle: Float
        var xIndex = 0
        canvas.save()
        val offset = 5f.convertDpToPixel()
        dataSets?.let {
            for (i in it.indices) {
                val dataSet = dataSets[i]
                val drawValues = dataSet.isDrawValues
                if (!drawValues && !drawEntryLabels) continue
                val xValuePosition = dataSet.xValuePosition
                val yValuePosition = dataSet.yValuePosition

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)
                val lineHeight = (paintValues.calcTextHeight("Q")
                        + 4f.convertDpToPixel())
                val formatter = dataSet.valueFormatter
                val entryCount = dataSet.entryCount
                paintValues.color = dataSet.valueLineColor
                paintValues.strokeWidth = dataSet.valueLineWidth.convertDpToPixel()
                val sliceSpace = getSliceSpace(dataSet)
                val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = iconsOffset.x.convertDpToPixel()
                iconsOffset.y = iconsOffset.y.convertDpToPixel()
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
                val measuredHeight = chart.measuredHeight
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
                    dataSet.getEntryForIndex(j)?.let { entry ->
                        angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                        val sliceAngle = drawAngles[xIndex]
                        val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                        // offset needed to center the drawn text in the slice
                        val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                        angle += angleOffset
                        val transformedAngle = rotationAngle + angle * phaseY
                        val value: Float = if (chart.isUsePercentValuesEnabled)
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
                            val line1Radius: Float = if (chart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                                    * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage
                            if (dataSet.isValueLineVariableLength) labelRadius * valueLineLength2 *
                                    abs(sin((transformedAngle * Utils.FDEG2RAD).toDouble())).toFloat()
                            else
                                labelRadius * valueLineLength2
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
                                paintValues.textAlign = Paint.Align.RIGHT
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
                                paintValues.textAlign = Paint.Align.LEFT
                                if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.LEFT
                                labelPtx = pt2x + offset
                                labelPty = pt2y
                            }
                            if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                                canvas.drawLine(pt0x, pt0y, pt1x, pt1y, valueLinePaint)
                                canvas.drawLine(pt1x, pt1y, pt2x, pt2y, valueLinePaint)
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
                                    drawEntryLabel(canvas, entry.label!!, labelPtx, labelPty + lineHeight)
                                }
                            } else if (drawXOutside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(canvas, entry.label!!, labelPtx, labelPty + lineHeight / 2f)
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
                            paintValues.textAlign = Paint.Align.CENTER

                            // draw everything, depending on settings
                            if (drawXInside && drawYInside) {
                                drawValue(canvas, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(canvas, entry.label!!, x, y + lineHeight)
                                }
                            } else if (drawXInside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(canvas, entry.label!!, x, y + lineHeight / 2f)
                                }
                            } else if (drawYInside) {
                                drawValue(canvas, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                            }
                        }
                        if (entry.icon != null && dataSet.isDrawIcons) {
                            val icon = entry.icon
                            val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                            var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                            y += iconsOffset.x
                            icon?.let { ic ->
                                canvas.drawImage(
                                    ic, x.toInt(), y.toInt(),
                                )
                            }
                        }
                    }
                    xIndex++
                }
                PointF.recycleInstance(iconsOffset)
            }
        }
        PointF.recycleInstance(center)
        canvas.restore()
    }

    private fun drawValuesTopAlign(c: Canvas) {
        val rect = Rect()
        paintEntryLabels.getTextBounds(text, 0, text.length, rect)
        val textHeight = rect.height()
        val center = chart.centerCircleBox

        // get whole the radius
        val radius = chart.radius
        val rotationAngle = chart.rotationAngle
        val drawAngles = chart.drawAngles
        val absoluteAngles = chart.absoluteAngles
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY
        val holeRadiusPercent = chart.holeRadius / 100f
        var labelRadiusOffset = radius / 10f * 3.6f
        if (chart.isDrawHoleEnabled) {
            labelRadiusOffset = radius - radius * holeRadiusPercent / 2f
        }
        val labelRadius = radius - labelRadiusOffset
        val data = chart.getData()
        val dataSets = data?.dataSets
        val yValueSum = data?.yValueSum ?: 0F
        val drawEntryLabels = chart.isDrawEntryLabelsEnabled
        var angle: Float
        var xIndex = 0
        c.save()
        val offset = 5f.convertDpToPixel()
        dataSets?.let {
            for (i in it.indices) {
                val dataSet = dataSets[i]
                val drawValues = dataSet.isDrawValues
                if (!drawValues && !drawEntryLabels) continue
                val xValuePosition = dataSet.xValuePosition
                val yValuePosition = dataSet.yValuePosition

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)
                val lineHeight = (paintValues.calcTextHeight("Q")
                        + 4f.convertDpToPixel())
                val formatter = dataSet.valueFormatter
                val entryCount = dataSet.entryCount
                valueLinePaint.color = dataSet.valueLineColor
                valueLinePaint.strokeWidth = dataSet.valueLineWidth.convertDpToPixel()
                val sliceSpace = getSliceSpace(dataSet)
                val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = iconsOffset.x.convertDpToPixel()
                iconsOffset.y = iconsOffset.y.convertDpToPixel()
                var lastPositionOfLeft = 0f
                var lastPositionOfRight = 0f
                for (j in 0 until entryCount) {
                    dataSet.getEntryForIndex(j)?.let { entry ->
                        angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                        val sliceAngle = drawAngles[xIndex]
                        val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                        // offset needed to center the drawn text in the slice
                        val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                        angle += angleOffset
                        val transformedAngle = rotationAngle + angle * phaseY
                        val value: Float = if (chart.isUsePercentValuesEnabled)
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
                            val line1Radius: Float = if (chart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                                    * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage
                            if (dataSet.isValueLineVariableLength) labelRadius * valueLineLength2 *
                                    abs(sin((transformedAngle * Utils.FDEG2RAD).toDouble())).toFloat()
                            else
                                labelRadius * valueLineLength2
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
                                paintValues.textAlign = Paint.Align.LEFT
                                if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.LEFT
                                labelPtx = pt2x + offset
                                labelPty = pt2y
                            }
                            if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                                c.drawLine(pt0x, pt0y, pt1x, pt1y, valueLinePaint)
                                c.drawLine(pt1x, pt1y, pt2x, pt2y, valueLinePaint)
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
                                    drawEntryLabel(c, entry.label!!, labelPtx, labelPty + lineHeight)
                                }
                            } else if (drawXOutside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, labelPtx, labelPty + lineHeight / 2f)
                                }
                            } else if (drawYOutside) {
                                drawValue(c, formatter, value, entry, 0, labelPtx, labelPty + lineHeight / 2f, dataSet.getValueTextColor(j))
                            }
                        }
                        if (drawXInside || drawYInside) {
                            // calculate the text position
                            val x = labelRadius * sliceXBase + center.x
                            val y = labelRadius * sliceYBase + center.y
                            paintValues.textAlign = Paint.Align.CENTER

                            // draw everything, depending on settings
                            if (drawXInside && drawYInside) {
                                drawValue(c, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, x, y + lineHeight)
                                }
                            } else if (drawXInside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, x, y + lineHeight / 2f)
                                }
                            } else if (drawYInside) {
                                drawValue(c, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                            }
                        }
                        if (entry.icon != null && dataSet.isDrawIcons) {
                            val icon = entry.icon
                            val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                            var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                            y += iconsOffset.x
                            icon?.let { ic ->
                                c.drawImage(
                                    ic, x.toInt(), y.toInt()
                                )
                            }
                        }
                    }
                    xIndex++
                }

                //画左边
                xIndex = entryCount - 1
                for (j in entryCount - 1 downTo 0) {
                    dataSet.getEntryForIndex(j)?.let { entry ->
                        angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                        val sliceAngle = drawAngles[xIndex]
                        val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                        // offset needed to center the drawn text in the slice
                        val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                        angle += angleOffset
                        val transformedAngle = rotationAngle + angle * phaseY
                        val value: Float = if (chart.isUsePercentValuesEnabled)
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
                            val line1Radius: Float = if (chart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                                    * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage
                            if (dataSet.isValueLineVariableLength) labelRadius * valueLineLength2 *
                                    abs(sin((transformedAngle * Utils.FDEG2RAD).toDouble())).toFloat()
                            else
                                labelRadius * valueLineLength2
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
                                paintValues.textAlign = Paint.Align.RIGHT
                                if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.RIGHT
                                labelPtx = pt2x - offset
                                labelPty = pt2y
                            } else {
                                continue
                            }
                            if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                                c.drawLine(pt0x, pt0y, pt1x, pt1y, valueLinePaint)
                                c.drawLine(pt1x, pt1y, pt2x, pt2y, valueLinePaint)
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
                                    drawEntryLabel(c, entry.label!!, labelPtx, labelPty + lineHeight)
                                }
                            } else if (drawXOutside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, labelPtx, labelPty + lineHeight / 2f)
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
                            paintValues.textAlign = Paint.Align.CENTER

                            // draw everything, depending on settings
                            if (drawXInside && drawYInside) {
                                drawValue(c, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, x, y + lineHeight)
                                }
                            } else if (drawXInside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, x, y + lineHeight / 2f)
                                }
                            } else if (drawYInside) {
                                drawValue(c, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                            }
                        }
                        if (entry.icon != null && dataSet.isDrawIcons) {
                            val icon = entry.icon
                            val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                            var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                            y += iconsOffset.x
                            icon?.let { ic ->
                                c.drawImage(
                                    ic, x.toInt(), y.toInt()
                                )
                            }
                        }
                    }
                    xIndex--
                }
                PointF.recycleInstance(iconsOffset)
            }
        }
        PointF.recycleInstance(center)
        c.restore()
    }

    private fun drawValuesNotTopAlign(c: Canvas) {
        val rect = Rect()
        paintEntryLabels.getTextBounds(text, 0, text.length, rect)
        val textHeight = rect.height()
        val center = chart.centerCircleBox

        // get whole the radius
        val radius = chart.radius
        val rotationAngle = chart.rotationAngle
        val drawAngles = chart.drawAngles
        val absoluteAngles = chart.absoluteAngles
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY
        val holeRadiusPercent = chart.holeRadius / 100f
        var labelRadiusOffset = radius / 10f * 3.6f
        if (chart.isDrawHoleEnabled) {
            labelRadiusOffset = radius - radius * holeRadiusPercent / 2f
        }
        val labelRadius = radius - labelRadiusOffset
        val data = chart.getData()
        val dataSets = data?.dataSets
        val yValueSum = data?.yValueSum ?: 0F
        val drawEntryLabels = chart.isDrawEntryLabelsEnabled
        var angle: Float
        var xIndex = 0
        c.save()
        val offset = 5f.convertDpToPixel()
        dataSets?.let {
            for (i in it.indices) {
                val dataSet = dataSets[i]
                val drawValues = dataSet.isDrawValues
                if (!drawValues && !drawEntryLabels) continue
                val xValuePosition = dataSet.xValuePosition
                val yValuePosition = dataSet.yValuePosition

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)
                val lineHeight = (paintValues.calcTextHeight("Q")
                        + 4f.convertDpToPixel())
                val formatter = dataSet.valueFormatter
                val entryCount = dataSet.entryCount
                valueLinePaint.color = dataSet.valueLineColor
                valueLinePaint.strokeWidth = dataSet.valueLineWidth.convertDpToPixel()
                val sliceSpace = getSliceSpace(dataSet)
                val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = iconsOffset.x.convertDpToPixel()
                iconsOffset.y = iconsOffset.y.convertDpToPixel()
                var lastPositionOfLeft = 0f
                var lastPositionOfRight = 0f
                for (j in 0 until entryCount) {
                    dataSet.getEntryForIndex(j)?.let { entry ->
                        angle = if (xIndex == 0) 0f else absoluteAngles[xIndex - 1] * phaseX
                        val sliceAngle = drawAngles[xIndex]
                        val sliceSpaceMiddleAngle = sliceSpace / (Utils.FDEG2RAD * labelRadius)

                        // offset needed to center the drawn text in the slice
                        val angleOffset = (sliceAngle - sliceSpaceMiddleAngle / 2f) / 2f
                        angle += angleOffset
                        val transformedAngle = rotationAngle + angle * phaseY
                        val value: Float = if (chart.isUsePercentValuesEnabled)
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
                            dataSet.valueLinePart2Length
                            val valueLinePart1OffsetPercentage = dataSet.valueLinePart1OffsetPercentage / 100f
                            var pt2x: Float
                            var pt2y: Float
                            var labelPtx: Float
                            var labelPty: Float
                            val line1Radius: Float = if (chart.isDrawHoleEnabled) (radius - radius * holeRadiusPercent
                                    * valueLinePart1OffsetPercentage) + radius * holeRadiusPercent else radius * valueLinePart1OffsetPercentage

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
                                paintValues.textAlign = Paint.Align.RIGHT
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
                                paintValues.textAlign = Paint.Align.LEFT
                                if (drawXOutside) paintEntryLabels.textAlign = Paint.Align.LEFT
                                labelPtx = pt2x + offset
                                labelPty = pt2y
                            }
                            if (dataSet.valueLineColor != ColorTemplate.COLOR_NONE) {
                                c.drawLine(pt0x, pt0y, pt1x, pt1y, valueLinePaint)
                                c.drawLine(pt1x, pt1y, pt2x, pt2y, valueLinePaint)
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
                                    drawEntryLabel(c, entry.label!!, labelPtx, labelPty + lineHeight)
                                }
                            } else if (drawXOutside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, labelPtx, labelPty + lineHeight / 2f)
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
                            paintValues.textAlign = Paint.Align.CENTER

                            // draw everything, depending on settings
                            if (drawXInside && drawYInside) {
                                drawValue(c, formatter, value, entry, 0, x, y, dataSet.getValueTextColor(j))
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, x, y + lineHeight)
                                }
                            } else if (drawXInside) {
                                if (j < data.entryCount && entry.label != null) {
                                    drawEntryLabel(c, entry.label!!, x, y + lineHeight / 2f)
                                }
                            } else if (drawYInside) {
                                drawValue(c, formatter, value, entry, 0, x, y + lineHeight / 2f, dataSet.getValueTextColor(j))
                            }
                        }
                        if (entry.icon != null && dataSet.isDrawIcons) {
                            val icon = entry.icon
                            val x = (labelRadius + iconsOffset.y) * sliceXBase + center.x
                            var y = (labelRadius + iconsOffset.y) * sliceYBase + center.y
                            y += iconsOffset.x
                            icon?.let { ic ->
                                c.drawImage(
                                    ic, x.toInt(), y.toInt()
                                )
                            }
                        }
                    }
                    xIndex++
                }
                PointF.recycleInstance(iconsOffset)
            }
        }
        PointF.recycleInstance(center)
        c.restore()
    }
}