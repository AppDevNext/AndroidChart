package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import info.appdev.charting.components.LimitLine.LimitLabelPosition
import info.appdev.charting.components.YAxis
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.components.YAxis.YAxisLabelPosition
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.convertDpToPixel

open class YAxisRenderer(
    viewPortHandler: ViewPortHandler,
    protected var yAxis: YAxis,
    trans: Transformer?
) : AxisRenderer(viewPortHandler, trans, yAxis) {

    protected var zeroLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    /**
     * Return the axis label x position based on axis dependency and label position
     */
    private fun calculateAxisLabelsXPosition(dependency: AxisDependency, labelPosition: YAxisLabelPosition): Float {
        val viewPortBase = if (dependency == AxisDependency.LEFT) viewPortHandler.offsetLeft() else viewPortHandler.contentRight()
        val xOffset = yAxis.xOffset * (if (labelPosition == YAxisLabelPosition.OUTSIDE_CHART) -1 else 1)

        return viewPortBase + xOffset
    }

    /**
     * Return the text align based on axis dependency and label position
     */
    private fun getAxisLabelTextAlign(dependency: AxisDependency, labelPosition: YAxisLabelPosition): Align {
        if ((dependency == AxisDependency.LEFT) xor (labelPosition == YAxisLabelPosition.OUTSIDE_CHART)) {
            return Align.LEFT
        }

        return Align.RIGHT
    }

    /**
     * draws the y-axis labels to the screen
     */
    override fun renderAxisLabels(canvas: Canvas) {
        if (!yAxis.isEnabled || !yAxis.isDrawLabelsEnabled)
            return

        val positions = transformedPositions

        paintAxisLabels.typeface = yAxis.typeface
        paintAxisLabels.textSize = yAxis.textSize
        paintAxisLabels.color = yAxis.textColor

        val yOffset = paintAxisLabels.calcTextHeight("A") / 2.5f + yAxis.yOffset

        val axisDependency = yAxis.axisDependency
        val labelPosition = yAxis.labelPosition

        if (axisDependency != null && labelPosition != null) {
            val xPos = calculateAxisLabelsXPosition(axisDependency, labelPosition)
            paintAxisLabels.textAlign = getAxisLabelTextAlign(axisDependency, labelPosition)

            drawYLabels(canvas, xPos, positions, yOffset)
        }
    }

    override fun renderAxisLine(canvas: Canvas) {
        if (!yAxis.isEnabled || !yAxis.isDrawAxisLineEnabled)
            return

        paintAxisLine.color = yAxis.axisLineColor
        paintAxisLine.strokeWidth = yAxis.axisLineWidth

        if (yAxis.axisDependency == AxisDependency.LEFT) {
            canvas.drawLine(
                viewPortHandler.contentLeft(), viewPortHandler.contentTop(), viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), paintAxisLine
            )
        } else {
            canvas.drawLine(
                viewPortHandler.contentRight(), viewPortHandler.contentTop(), viewPortHandler.contentRight(),
                viewPortHandler.contentBottom(), paintAxisLine
            )
        }
    }

    /**
     * draws the y-labels on the specified x-position
     */
    protected open fun drawYLabels(canvas: Canvas, fixedPosition: Float, positions: FloatArray, offset: Float) {
        val from: Int
        val to: Int

        if (yAxis.isShowSpecificPositions) {
            from = 0
            to = if (yAxis.isDrawTopYLabelEntryEnabled)
                yAxis.specificPositions.size
            else
                (yAxis.specificPositions.size - 1)
        } else {
            from = if (yAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
            to = if (yAxis.isDrawTopYLabelEntryEnabled)
                yAxis.entryCount
            else
                (yAxis.entryCount - 1)
        }

        val xOffset = yAxis.labelXOffset

        // draw
        for (i in from..<to) {
            val text = if (yAxis.isShowSpecificPositions) {
                yAxis.valueFormatter?.getFormattedValue(yAxis.specificPositions[i], yAxis)
            } else {
                yAxis.getFormattedLabel(i)
            }

            canvas.drawText(
                text!!,
                fixedPosition + xOffset,
                positions[i * 2 + 1] + offset,
                paintAxisLabels
            )
        }
    }

    protected var renderGridLinesPath: Path = Path()
    override fun renderGridLines(canvas: Canvas) {
        if (!yAxis.isEnabled)
            return

        if (yAxis.isDrawGridLinesEnabled) {
            canvas.withClip(gridClippingRect!!) {
                val positions = transformedPositions

                paintGrid.color = yAxis.gridColor
                paintGrid.strokeWidth = yAxis.gridLineWidth
                paintGrid.pathEffect = yAxis.gridDashPathEffect

                val gridLinePath = renderGridLinesPath
                gridLinePath.reset()

                // draw the grid
                var i = 0
                while (i < positions.size) {
                    // draw a path because lines don't support dashing on lower android versions
                    canvas.drawPath(linePath(gridLinePath, i, positions)!!, paintGrid)
                    gridLinePath.reset()
                    i += 2
                }
            }
        }

        if (yAxis.isDrawZeroLineEnabled) {
            drawZeroLine(canvas)
        }
    }

    protected var mGridClippingRect: RectF = RectF()

    open val gridClippingRect: RectF?
        get() {
            mGridClippingRect.set(viewPortHandler.contentRect)
            mGridClippingRect.inset(0f, -axis.gridLineWidth)
            return mGridClippingRect
        }

    /**
     * Calculates the path for a grid line.
     */
    protected open fun linePath(p: Path, i: Int, positions: FloatArray): Path? {
        p.moveTo(viewPortHandler.offsetLeft(), positions[i + 1])
        p.lineTo(viewPortHandler.contentRight(), positions[i + 1])

        return p
    }

    protected var mGetTransformedPositionsBuffer: FloatArray = FloatArray(2)
    protected open val transformedPositions: FloatArray
        /**
         * Transforms the values contained in the axis entries to screen pixels and returns them in form of a float array
         * of x- and y-coordinates.
         */
        get() {
            if (yAxis.isShowSpecificPositions) {
                if (mGetTransformedPositionsBuffer.size != yAxis.specificPositions.size * 2) {
                    mGetTransformedPositionsBuffer = FloatArray(yAxis.specificPositions.size * 2)
                }
            } else {
                if (mGetTransformedPositionsBuffer.size != yAxis.entryCount * 2) {
                    mGetTransformedPositionsBuffer = FloatArray(yAxis.entryCount * 2)
                }
            }
            val positions = mGetTransformedPositionsBuffer

            var i = 0
            while (i < positions.size) {
                // only fill y values, x values are not needed for y-labels
                if (yAxis.isShowSpecificPositions) {
                    positions[i + 1] = yAxis.specificPositions[i / 2]
                } else {
                    positions[i + 1] = yAxis.entries[i / 2]
                }
                i += 2
            }

            transformer?.pointValuesToPixel(positions)
            return positions
        }

    protected var drawZeroLinePath: Path = Path()

    protected var zeroLineClippingRect: RectF = RectF()

    /**
     * Draws the zero line.
     */
    protected open fun drawZeroLine(canvas: Canvas) {
        val clipRestoreCount = canvas.save()
        zeroLineClippingRect.set(viewPortHandler.contentRect)
        zeroLineClippingRect.inset(0f, -yAxis.zeroLineWidth)
        canvas.clipRect(zeroLineClippingRect)

        // draw zero line
        val pos = transformer?.getPixelForValues(0f, 0f)
        pos?.let {
            zeroLinePaint.color = yAxis.zeroLineColor
            zeroLinePaint.strokeWidth = yAxis.zeroLineWidth

            val zeroLinePath = drawZeroLinePath
            zeroLinePath.reset()

            zeroLinePath.moveTo(viewPortHandler.contentLeft(), it.y.toFloat())
            zeroLinePath.lineTo(viewPortHandler.contentRight(), it.y.toFloat())

            // draw a path because lines don't support dashing on lower android versions
            canvas.drawPath(zeroLinePath, zeroLinePaint)
        }

        canvas.restoreToCount(clipRestoreCount)
    }

    protected var renderLimitRanges: Path = Path()
    protected var renderLimitLines: Path = Path()
    protected open var renderLimitLinesBuffer: FloatArray = FloatArray(2)

    protected var limitLineClippingRect: RectF = RectF()

    init {
        paintAxisLabels.apply {
            color = Color.BLACK
            textSize = 10f.convertDpToPixel()
        }
    }

    /**
     * Draws the LimitLines associated with this axis to the screen.
     */
    override fun renderLimitLines(canvas: Canvas) {
        val limitLines = yAxis.limitLines
        if (limitLines.isNotEmpty()) {
            val pts = renderLimitLinesBuffer
            pts[0] = 0f
            pts[1] = 0f
            val limitLinePath = renderLimitLines
            limitLinePath.reset()

            for (i in limitLines.indices) {
                val limitLine = limitLines[i]

                if (!limitLine.isEnabled)
                    continue

                canvas.withSave {
                    limitLineClippingRect.set(viewPortHandler.contentRect)
                    limitLineClippingRect.inset(0f, -limitLine.lineWidth)
                    canvas.clipRect(limitLineClippingRect)

                    limitLinePaint.style = Paint.Style.STROKE
                    limitLinePaint.color = limitLine.lineColor
                    limitLinePaint.strokeWidth = limitLine.lineWidth
                    limitLinePaint.pathEffect = limitLine.dashPathEffect

                    pts[1] = limitLine.limit

                    transformer?.pointValuesToPixel(pts)

                    limitLinePath.moveTo(viewPortHandler.contentLeft(), pts[1])
                    limitLinePath.lineTo(viewPortHandler.contentRight(), pts[1])

                    canvas.drawPath(limitLinePath, limitLinePaint)
                    limitLinePath.reset()

                    // c.drawLines(pts, mLimitLinePaint);
                    val label = limitLine.label

                    // if drawing the limit-value label is enabled
                    if (label != null && label != "") {
                        limitLinePaint.style = limitLine.textStyle
                        limitLinePaint.pathEffect = null
                        limitLinePaint.color = limitLine.textColor
                        limitLinePaint.typeface = limitLine.typeface
                        limitLinePaint.strokeWidth = 0.5f
                        limitLinePaint.textSize = limitLine.textSize

                        val labelLineHeight = limitLinePaint.calcTextHeight(label).toFloat()
                        val xOffset = 4f.convertDpToPixel() + limitLine.xOffset
                        val yOffset = limitLine.lineWidth + labelLineHeight + limitLine.yOffset

                        val position = limitLine.labelPosition

                        when (position) {
                            LimitLabelPosition.RIGHT_TOP -> {
                                limitLinePaint.textAlign = Align.RIGHT
                                canvas.drawText(
                                    label,
                                    viewPortHandler.contentRight() - xOffset,
                                    pts[1] - yOffset + labelLineHeight, limitLinePaint
                                )
                            }

                            LimitLabelPosition.RIGHT_BOTTOM -> {
                                limitLinePaint.textAlign = Align.RIGHT
                                canvas.drawText(
                                    label,
                                    viewPortHandler.contentRight() - xOffset,
                                    pts[1] + yOffset, limitLinePaint
                                )
                            }

                            LimitLabelPosition.LEFT_TOP -> {
                                limitLinePaint.textAlign = Align.LEFT
                                canvas.drawText(
                                    label,
                                    viewPortHandler.contentLeft() + xOffset,
                                    pts[1] - yOffset + labelLineHeight, limitLinePaint
                                )
                            }

                            else -> {
                                limitLinePaint.textAlign = Align.LEFT
                                canvas.drawText(
                                    label,
                                    viewPortHandler.offsetLeft() + xOffset,
                                    pts[1] + yOffset, limitLinePaint
                                )
                            }
                        }
                    }

                }
            }
        }

        // Now the ranges
        val limitRanges = yAxis.limitRanges
        if (limitRanges.isNotEmpty()) {
            val ptsr = FloatArray(2)
            ptsr[0] = 0f
            ptsr[1] = 0f
            val ptsr2 = FloatArray(2)
            ptsr2[0] = 0f
            ptsr2[1] = 0f
            val limitRangePath = renderLimitRanges
            val limitRangePathFill = renderLimitRanges
            limitRangePath.reset()
            limitRangePathFill.reset()

            for (limitRangeIndex in limitRanges.indices) {
                val limitRange = limitRanges[limitRangeIndex]

                if (!limitRange.isEnabled)
                    continue

                val clipRestoreCount = canvas.save()
                limitLineClippingRect.set(viewPortHandler.contentRect)
                limitLineClippingRect.inset(0f, -limitRange.lineWidth)
                canvas.clipRect(limitLineClippingRect)

                limitRangePaint.style = Paint.Style.STROKE
                limitRangePaint.color = limitRange.lineColor
                limitRangePaint.strokeWidth = limitRange.lineWidth
                limitRangePaint.pathEffect = limitRange.dashPathEffect

                limitRangePaintFill.style = Paint.Style.FILL
                limitRangePaintFill.color = limitRange.rangeColor

                ptsr[1] = limitRange.limit.high
                ptsr2[1] = limitRange.limit.low

                transformer?.pointValuesToPixel(ptsr)
                transformer?.pointValuesToPixel(ptsr2)

                limitRangePathFill.moveTo(viewPortHandler.contentLeft(), ptsr[1])
                limitRangePathFill.addRect(
                    viewPortHandler.contentLeft(),
                    ptsr[1],
                    viewPortHandler.contentRight(),
                    ptsr2[1],
                    Path.Direction.CW
                )
                canvas.drawPath(limitRangePathFill, limitRangePaintFill)
                limitRangePathFill.reset()

                if (limitRange.lineWidth > 0) {
                    limitRangePath.moveTo(viewPortHandler.contentLeft(), ptsr[1])
                    limitRangePath.lineTo(viewPortHandler.contentRight(), ptsr[1])
                    canvas.drawPath(limitRangePath, limitRangePaint)

                    limitRangePath.moveTo(viewPortHandler.contentLeft(), ptsr2[1])
                    limitRangePath.lineTo(viewPortHandler.contentRight(), ptsr2[1])
                    canvas.drawPath(limitRangePath, limitRangePaint)
                }

                limitRangePath.reset()

                val label = limitRange.label

                // if drawing the limit-value label is enabled
                if (label != null && label != "") {
                    limitRangePaint.style = limitRange.textStyle
                    limitRangePaint.pathEffect = null
                    limitRangePaint.color = limitRange.textColor
                    limitRangePaint.typeface = limitRange.typeface
                    limitRangePaint.strokeWidth = 0.5f
                    limitRangePaint.textSize = limitRange.textSize

                    val labelLineHeight = limitRangePaint.calcTextHeight(label).toFloat()
                    val xOffset = 4f.convertDpToPixel() + limitRange.xOffset
                    val yOffset = limitRange.lineWidth + labelLineHeight + limitRange.yOffset

                    val position = limitRange.labelPosition

                    when (position) {
                        LimitLabelPosition.RIGHT_TOP -> {
                            limitRangePaint.textAlign = Align.RIGHT
                            canvas.drawText(
                                label,
                                viewPortHandler.contentRight() - xOffset,
                                ptsr[1] - yOffset + labelLineHeight, limitRangePaint
                            )
                        }

                        LimitLabelPosition.RIGHT_BOTTOM -> {
                            limitRangePaint.textAlign = Align.RIGHT
                            canvas.drawText(
                                label,
                                viewPortHandler.contentRight() - xOffset,
                                ptsr[1] + yOffset, limitRangePaint
                            )
                        }

                        LimitLabelPosition.LEFT_TOP -> {
                            limitRangePaint.textAlign = Align.LEFT
                            canvas.drawText(
                                label,
                                viewPortHandler.contentLeft() + xOffset,
                                ptsr[1] - yOffset + labelLineHeight, limitRangePaint
                            )
                        }

                        else -> {
                            limitRangePaint.textAlign = Align.LEFT
                            canvas.drawText(
                                label,
                                viewPortHandler.offsetLeft() + xOffset,
                                ptsr[1] + yOffset, limitRangePaint
                            )
                        }
                    }
                }

                canvas.restoreToCount(clipRestoreCount)
            }
        }
    }
}
