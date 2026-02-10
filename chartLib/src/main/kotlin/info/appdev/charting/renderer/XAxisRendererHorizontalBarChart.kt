package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withSave
import info.appdev.charting.components.LimitLine.LimitLabelPosition
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.utils.FSize
import info.appdev.charting.utils.PointD
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.Utils
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.calcTextSize
import info.appdev.charting.utils.convertDpToPixel
import kotlin.math.roundToInt

@Suppress("MemberVisibilityCanBePrivate")
open class XAxisRendererHorizontalBarChart(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer?
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

    protected var renderLimitLinesPathBuffer: Path = Path()

    override fun computeAxis(min: Float, max: Float, inverted: Boolean) {
        // calculate the starting and entry point of the y-labels (depending on
        // zoom / content rect bounds)

        var minLocal = min
        var maxLocal = max
        if (viewPortHandler.contentWidth() > 10 && !viewPortHandler.isFullyZoomedOutY) {
            val p1 = transformer!!.getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentBottom())
            val p2 = transformer!!.getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop())

            if (inverted) {
                minLocal = p2.y.toFloat()
                maxLocal = p1.y.toFloat()
            } else {
                minLocal = p1.y.toFloat()
                maxLocal = p2.y.toFloat()
            }

            PointD.recycleInstance(p1)
            PointD.recycleInstance(p2)
        }

        computeAxisValues(minLocal, maxLocal)
    }

    override fun computeSize() {
        paintAxisLabels.typeface = xAxis.typeface
        paintAxisLabels.textSize = xAxis.textSize

        val longest = xAxis.longestLabel

        val labelSize = paintAxisLabels.calcTextSize(longest)

        val labelWidth = (labelSize.width + xAxis.xOffset * 3.5f).toInt().toFloat()
        val labelHeight = labelSize.height

        val labelRotatedSize = Utils.getSizeOfRotatedRectangleByDegrees(
            labelWidth,
            labelHeight,
            xAxis.labelRotationAngle
        )

        xAxis.labelWidth = labelRotatedSize.width.roundToInt()
        xAxis.labelHeight = labelRotatedSize.height.roundToInt()

        FSize.recycleInstance(labelRotatedSize)
    }

    override fun renderAxisLabels(canvas: Canvas) {
        if (!xAxis.isEnabled || !xAxis.isDrawLabelsEnabled)
            return

        val xOffset = xAxis.xOffset

        paintAxisLabels.typeface = xAxis.typeface
        paintAxisLabels.textSize = xAxis.textSize
        paintAxisLabels.color = xAxis.textColor

        val pointF = PointF.getInstance(0f, 0f)

        when (xAxis.position) {
            XAxisPosition.TOP -> {
                pointF.x = 0.0f
                pointF.y = 0.5f
                drawLabels(canvas, viewPortHandler.contentRight() + xOffset, pointF)
            }

            XAxisPosition.TOP_INSIDE -> {
                pointF.x = 1.0f
                pointF.y = 0.5f
                drawLabels(canvas, viewPortHandler.contentRight() - xOffset, pointF)
            }

            XAxisPosition.BOTTOM -> {
                pointF.x = 1.0f
                pointF.y = 0.5f
                drawLabels(canvas, viewPortHandler.contentLeft() - xOffset, pointF)
            }

            XAxisPosition.BOTTOM_INSIDE -> {
                pointF.x = 1.0f
                pointF.y = 0.5f
                drawLabels(canvas, viewPortHandler.contentLeft() + xOffset, pointF)
            }

            else -> { // BOTH SIDED
                pointF.x = 0.0f
                pointF.y = 0.5f
                drawLabels(canvas, viewPortHandler.contentRight() + xOffset, pointF)
                pointF.x = 1.0f
                pointF.y = 0.5f
                drawLabels(canvas, viewPortHandler.contentLeft() - xOffset, pointF)
            }
        }

        PointF.recycleInstance(pointF)
    }

    override fun drawLabels(canvas: Canvas, pos: Float, anchor: PointF) {
        val labelRotationAngleDegrees = xAxis.labelRotationAngle
        val centeringEnabled = xAxis.isCenterAxisLabelsEnabled

        val positions = FloatArray(xAxis.entryCount * 2)

        run {
            var i = 0
            while (i < positions.size) {
                // only fill x values
                if (centeringEnabled) {
                    positions[i + 1] = xAxis.centeredEntries[i / 2]
                } else {
                    positions[i + 1] = xAxis.entries[i / 2]
                }
                i += 2
            }
        }

        transformer!!.pointValuesToPixel(positions)

        var i = 0
        while (i < positions.size) {
            val y = positions[i + 1]

            if (viewPortHandler.isInBoundsY(y)) {
                val label = xAxis.valueFormatter?.getFormattedValue(xAxis.entries[i / 2], xAxis)
                drawLabel(canvas, label, pos, y, anchor, labelRotationAngleDegrees)
            }
            i += 2
        }
    }

    override val gridClippingRect: RectF
        get() {
            mGridClippingRect.set(viewPortHandler.contentRect)
            mGridClippingRect.inset(0f, -axis.gridLineWidth)
            return mGridClippingRect
        }

    override fun drawGridLine(canvas: Canvas, x: Float, y: Float, gridLinePath: Path) {
        gridLinePath.moveTo(viewPortHandler.contentRight(), y)
        gridLinePath.lineTo(viewPortHandler.contentLeft(), y)

        // draw a path because lines don't support dashing on lower android versions
        canvas.drawPath(gridLinePath, paintGrid)

        gridLinePath.reset()
    }

    override fun renderAxisLine(canvas: Canvas) {
        if (!xAxis.isDrawAxisLine || !xAxis.isEnabled)
            return

        paintAxisLine.color = xAxis.axisLineColor
        paintAxisLine.strokeWidth = xAxis.axisLineWidth

        if (xAxis.position == XAxisPosition.TOP || xAxis.position == XAxisPosition.TOP_INSIDE || xAxis.position == XAxisPosition.BOTH_SIDED) {
            canvas.drawLine(
                viewPortHandler.contentRight(),
                viewPortHandler.contentTop(), viewPortHandler.contentRight(),
                viewPortHandler.contentBottom(), paintAxisLine
            )
        }

        if (xAxis.position == XAxisPosition.BOTTOM || xAxis.position == XAxisPosition.BOTTOM_INSIDE || xAxis.position == XAxisPosition.BOTH_SIDED) {
            canvas.drawLine(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentTop(), viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), paintAxisLine
            )
        }
    }

    /**
     * Draws the LimitLines associated with this axis to the screen.
     * This is the standard YAxis renderer using the XAxis limit lines.
     *
     * @param canvas
     */
    override fun renderLimitLines(canvas: Canvas) {
        val limitLines = xAxis.limitLines

        if (limitLines.isEmpty())
            return

        val pts = mRenderLimitLinesBuffer
        pts[0] = 0f
        pts[1] = 0f

        val limitLinePath = renderLimitLinesPathBuffer
        limitLinePath.reset()

        for (i in limitLines.indices) {
            val limitLine = limitLines[i]

            if (!limitLine.isEnabled) continue

            canvas.withSave {
                mLimitLineClippingRect.set(viewPortHandler.contentRect)
                mLimitLineClippingRect.inset(0f, -limitLine.lineWidth)
                canvas.clipRect(mLimitLineClippingRect)

                limitLinePaint.style = Paint.Style.STROKE
                limitLinePaint.color = limitLine.lineColor
                limitLinePaint.strokeWidth = limitLine.lineWidth
                limitLinePaint.pathEffect = limitLine.dashPathEffect

                pts[1] = limitLine.limit

                transformer!!.pointValuesToPixel(pts)

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
}
