package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.utils.convertDpToPixel

@Suppress("MemberVisibilityCanBePrivate")
open class YAxisRendererHorizontalBarChart(
    viewPortHandler: ViewPortHandler, yAxis: YAxis,
    trans: Transformer?
) : YAxisRenderer(viewPortHandler, yAxis, trans) {

    protected var drawZeroLinePathBuffer: Path = Path()

    /**
     * Computes the axis values.
     *
     * @param min - the minimum y-value in the data object for this axis
     * @param max - the maximum y-value in the data object for this axis
     */
    override fun computeAxis(min: Float, max: Float, inverted: Boolean) {
        // calculate the starting and entry point of the y-labels (depending on
        // zoom / content rect bounds)

        var yMin = min
        var yMax = max
        if (viewPortHandler.contentHeight() > 10 && !viewPortHandler.isFullyZoomedOutX) {
            val p1 = transformer!!.getValuesByTouchPoint(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentTop()
            )
            val p2 = transformer!!.getValuesByTouchPoint(
                viewPortHandler.contentRight(),
                viewPortHandler.contentTop()
            )

            if (!inverted) {
                yMin = p1.x.toFloat()
                yMax = p2.x.toFloat()
            } else {
                yMin = p2.x.toFloat()
                yMax = p1.x.toFloat()
            }

            MPPointD.recycleInstance(p1)
            MPPointD.recycleInstance(p2)
        }

        computeAxisValues(yMin, yMax)
    }

    /**
     * draws the y-axis labels to the screen
     */
    override fun renderAxisLabels(canvas: Canvas) {
        if (!yAxis.isEnabled || !yAxis.isDrawLabelsEnabled) return

        val positions = transformedPositions

        paintAxisLabels.typeface = yAxis.typeface
        paintAxisLabels.textSize = yAxis.textSize
        paintAxisLabels.color = yAxis.textColor
        paintAxisLabels.textAlign = Align.CENTER

        val baseYOffset = 2.5f.convertDpToPixel()
        val textHeight = Utils.calcTextHeight(paintAxisLabels, "Q").toFloat()

        val dependency = yAxis.axisDependency
        val labelPosition = yAxis.labelPosition

        val yPos: Float = if (dependency == AxisDependency.LEFT) {
            if (labelPosition == YAxisLabelPosition.OUTSIDE_CHART) {
                viewPortHandler.contentTop() - baseYOffset
            } else {
                viewPortHandler.contentTop() - baseYOffset
            }
        } else {
            if (labelPosition == YAxisLabelPosition.OUTSIDE_CHART) {
                viewPortHandler.contentBottom() + textHeight + baseYOffset
            } else {
                viewPortHandler.contentBottom() + textHeight + baseYOffset
            }
        }

        drawYLabels(canvas, yPos, positions, yAxis.yOffset)
    }

    override fun renderAxisLine(canvas: Canvas) {
        if (!yAxis.isEnabled || !yAxis.isDrawAxisLineEnabled) return

        paintAxisLine.color = yAxis.axisLineColor
        paintAxisLine.strokeWidth = yAxis.axisLineWidth

        if (yAxis.axisDependency == AxisDependency.LEFT) {
            canvas.drawLine(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentTop(), viewPortHandler.contentRight(),
                viewPortHandler.contentTop(), paintAxisLine
            )
        } else {
            canvas.drawLine(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), viewPortHandler.contentRight(),
                viewPortHandler.contentBottom(), paintAxisLine
            )
        }
    }

    /**
     * draws the y-labels on the specified x-position
     *
     * @param fixedPosition
     * @param positions
     */
    override fun drawYLabels(canvas: Canvas, fixedPosition: Float, positions: FloatArray, offset: Float) {
        paintAxisLabels.typeface = yAxis.typeface
        paintAxisLabels.textSize = yAxis.textSize
        paintAxisLabels.color = yAxis.textColor

        val from = if (yAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
        val to = if (yAxis.isDrawTopYLabelEntryEnabled)
            yAxis.mEntryCount
        else
            (yAxis.mEntryCount - 1)

        val xOffset = yAxis.labelXOffset

        for (i in from..<to) {
            val text = yAxis.getFormattedLabel(i)

            text?.let {
                canvas.drawText(
                    it,
                    positions[i * 2],
                    fixedPosition - offset + xOffset,
                    paintAxisLabels
                )
            }
        }
    }

    override val transformedPositions: FloatArray
        get() {
            if (mGetTransformedPositionsBuffer.size != yAxis.mEntryCount * 2) {
                mGetTransformedPositionsBuffer = FloatArray(yAxis.mEntryCount * 2)
            }
            val positions = mGetTransformedPositionsBuffer

            var i = 0
            while (i < positions.size) {
                // only fill x values, y values are not needed for x-labels
                positions[i] = yAxis.mEntries[i / 2]
                i += 2
            }

            transformer!!.pointValuesToPixel(positions)
            return positions
        }

    override val gridClippingRect: RectF
        get() {
            mGridClippingRect.set(viewPortHandler.contentRect)
            mGridClippingRect.inset(-axis.gridLineWidth, 0f)
            return mGridClippingRect
        }

    override fun linePath(p: Path, i: Int, positions: FloatArray): Path {
        p.moveTo(positions[i], viewPortHandler.contentTop())
        p.lineTo(positions[i], viewPortHandler.contentBottom())

        return p
    }

    override fun drawZeroLine(canvas: Canvas) {
        canvas.withSave {
            zeroLineClippingRect.set(viewPortHandler.contentRect)
            zeroLineClippingRect.inset(-yAxis.zeroLineWidth, 0f)
            canvas.clipRect(limitLineClippingRect)

            // draw zero line
            val pos = transformer!!.getPixelForValues(0f, 0f)

            zeroLinePaint.color = yAxis.zeroLineColor
            zeroLinePaint.strokeWidth = yAxis.zeroLineWidth

            val zeroLinePath = drawZeroLinePathBuffer
            zeroLinePath.reset()

            zeroLinePath.moveTo(pos.x.toFloat() - 1, viewPortHandler.contentTop())
            zeroLinePath.lineTo(pos.x.toFloat() - 1, viewPortHandler.contentBottom())

            // draw a path because lines don't support dashing on lower android versions
            canvas.drawPath(zeroLinePath, zeroLinePaint)

        }
    }

    protected var mRenderLimitLinesPathBuffer: Path = Path()
    override var renderLimitLinesBuffer: FloatArray = FloatArray(4)

    init {
        limitLinePaint.textAlign = Align.LEFT
    }

    /**
     * Draws the LimitLines associated with this axis to the screen.
     * This is the standard XAxis renderer using the YAxis limit lines.
     *
     * @param canvas
     */
    override fun renderLimitLines(canvas: Canvas) {
        val limitLines = yAxis.limitLines

        if (limitLines.isEmpty()) return

        val pts = renderLimitLinesBuffer
        pts[0] = 0f
        pts[1] = 0f
        pts[2] = 0f
        pts[3] = 0f
        val limitLinePath = mRenderLimitLinesPathBuffer
        limitLinePath.reset()

        for (i in limitLines.indices) {
            val limitLine = limitLines[i]

            if (!limitLine.isEnabled) continue

            canvas.withSave {
                limitLineClippingRect.set(viewPortHandler.contentRect)
                limitLineClippingRect.inset(-limitLine.lineWidth, 0f)
                canvas.clipRect(limitLineClippingRect)

                pts[0] = limitLine.limit
                pts[2] = limitLine.limit

                transformer!!.pointValuesToPixel(pts)

                pts[1] = viewPortHandler.contentTop()
                pts[3] = viewPortHandler.contentBottom()

                limitLinePath.moveTo(pts[0], pts[1])
                limitLinePath.lineTo(pts[2], pts[3])

                limitLinePaint.style = Paint.Style.STROKE
                limitLinePaint.color = limitLine.lineColor
                limitLinePaint.pathEffect = limitLine.dashPathEffect
                limitLinePaint.strokeWidth = limitLine.lineWidth

                canvas.drawPath(limitLinePath, limitLinePaint)
                limitLinePath.reset()

                val label = limitLine.label

                // if drawing the limit-value label is enabled
                if (label != null && label != "") {
                    limitLinePaint.style = limitLine.textStyle
                    limitLinePaint.pathEffect = null
                    limitLinePaint.color = limitLine.textColor
                    limitLinePaint.typeface = limitLine.typeface
                    limitLinePaint.strokeWidth = 0.5f
                    limitLinePaint.textSize = limitLine.textSize

                    val xOffset = limitLine.lineWidth + limitLine.xOffset
                    val yOffset = 2f.convertDpToPixel() + limitLine.yOffset

                    val position = limitLine.labelPosition

                    when (position) {
                        LimitLabelPosition.RIGHT_TOP -> {
                            val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
                            limitLinePaint.textAlign = Align.LEFT
                            canvas.drawText(label, pts[0] + xOffset, viewPortHandler.contentTop() + yOffset + labelLineHeight, limitLinePaint)
                        }
                        LimitLabelPosition.RIGHT_BOTTOM -> {
                            limitLinePaint.textAlign = Align.LEFT
                            canvas.drawText(label, pts[0] + xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
                        }
                        LimitLabelPosition.LEFT_TOP -> {
                            limitLinePaint.textAlign = Align.RIGHT
                            val labelLineHeight = Utils.calcTextHeight(limitLinePaint, label).toFloat()
                            canvas.drawText(label, pts[0] - xOffset, viewPortHandler.contentTop() + yOffset + labelLineHeight, limitLinePaint)
                        }
                        else -> {
                            limitLinePaint.textAlign = Align.RIGHT
                            canvas.drawText(label, pts[0] - xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
                        }
                    }
                }

            }
        }
    }
}
