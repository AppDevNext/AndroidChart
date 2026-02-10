package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import info.appdev.charting.components.LimitLine
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
import info.appdev.charting.utils.calcTextWidth
import info.appdev.charting.utils.convertDpToPixel
import info.appdev.charting.utils.drawXAxisValue
import kotlin.math.roundToInt

open class XAxisRenderer(
    viewPortHandler: ViewPortHandler,
    protected var xAxis: XAxis,
    transformer: Transformer?
) : AxisRenderer(viewPortHandler, transformer, xAxis) {
    protected fun setupGridPaint() {
        paintGrid.color = xAxis.gridColor
        paintGrid.strokeWidth = xAxis.gridLineWidth
        paintGrid.pathEffect = xAxis.gridDashPathEffect
    }

    override fun computeAxis(min: Float, max: Float, inverted: Boolean) {
        // calculate the starting and entry point of the y-labels (depending on
        // zoom / content rect bounds)

        var minLocal = min
        var maxLocal = max
        if (viewPortHandler.contentWidth() > 10 && !viewPortHandler.isFullyZoomedOutX) {
            val p1 = transformer!!.getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop())
            val p2 = transformer!!.getValuesByTouchPoint(viewPortHandler.contentRight(), viewPortHandler.contentTop())

            if (inverted) {
                minLocal = p2.x.toFloat()
                maxLocal = p1.x.toFloat()
            } else {
                minLocal = p1.x.toFloat()
                maxLocal = p2.x.toFloat()
            }

            PointD.recycleInstance(p1)
            PointD.recycleInstance(p2)
        }

        computeAxisValues(minLocal, maxLocal)
    }

    override fun computeAxisValues(min: Float, max: Float) {
        super.computeAxisValues(min, max)

        computeSize()
    }

    protected open fun computeSize() {
        val longest = xAxis.longestLabel

        paintAxisLabels.typeface = xAxis.typeface
        paintAxisLabels.textSize = xAxis.textSize

        val labelSize = paintAxisLabels.calcTextSize(longest)

        val labelWidth = labelSize.width
        val labelHeight = paintAxisLabels.calcTextHeight("Q").toFloat()

        val labelRotatedSize = Utils.getSizeOfRotatedRectangleByDegrees(
            labelWidth,
            labelHeight,
            xAxis.labelRotationAngle
        )

        xAxis.labelWidth = labelRotatedSize.width.roundToInt()
        xAxis.labelHeight = labelRotatedSize.height.roundToInt()

        FSize.recycleInstance(labelRotatedSize)
        FSize.recycleInstance(labelSize)
    }

    override fun renderAxisLabels(canvas: Canvas) {
        if (!xAxis.isEnabled || !xAxis.isDrawLabelsEnabled)
            return

        val yOffset = xAxis.yOffset

        paintAxisLabels.color = xAxis.textColor

        val pointF = PointF.getInstance(0f, 0f)
        when (xAxis.position) {
            XAxisPosition.TOP -> {
                pointF.x = 0.5f
                pointF.y = 1.0f
                drawLabels(canvas, viewPortHandler.contentTop() - yOffset, pointF)
            }

            XAxisPosition.TOP_INSIDE -> {
                pointF.x = 0.5f
                pointF.y = 1.0f
                drawLabels(canvas, viewPortHandler.contentTop() + yOffset + xAxis.labelHeight, pointF)
            }

            XAxisPosition.BOTTOM -> {
                pointF.x = 0.5f
                pointF.y = 0.0f
                drawLabels(canvas, viewPortHandler.contentBottom() + yOffset, pointF)
            }

            XAxisPosition.BOTTOM_INSIDE -> {
                pointF.x = 0.5f
                pointF.y = 0.0f
                drawLabels(canvas, viewPortHandler.contentBottom() - yOffset - xAxis.labelHeight, pointF)
            }

            else -> { // BOTH SIDED
                pointF.x = 0.5f
                pointF.y = 1.0f
                drawLabels(canvas, viewPortHandler.contentTop() - yOffset, pointF)
                pointF.x = 0.5f
                pointF.y = 0.0f
                drawLabels(canvas, viewPortHandler.contentBottom() + yOffset, pointF)
            }
        }
        PointF.recycleInstance(pointF)
    }

    override fun renderAxisLine(canvas: Canvas) {
        if (!xAxis.isDrawAxisLine || !xAxis.isEnabled)
            return

        paintAxisLine.color = xAxis.axisLineColor
        paintAxisLine.strokeWidth = xAxis.axisLineWidth
        paintAxisLine.pathEffect = xAxis.axisLineDashPathEffect

        if (xAxis.position == XAxisPosition.TOP || xAxis.position == XAxisPosition.TOP_INSIDE || xAxis.position == XAxisPosition.BOTH_SIDED) {
            canvas.drawLine(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentTop(), viewPortHandler.contentRight(),
                viewPortHandler.contentTop(), paintAxisLine
            )
        }

        if (xAxis.position == XAxisPosition.BOTTOM || xAxis.position == XAxisPosition.BOTTOM_INSIDE || xAxis.position == XAxisPosition.BOTH_SIDED) {
            canvas.drawLine(
                viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), viewPortHandler.contentRight(),
                viewPortHandler.contentBottom(), paintAxisLine
            )
        }
    }

    /**
     * draws the x-labels on the specified y-position
     *
     * @param pos
     */
    protected open fun drawLabels(canvas: Canvas, pos: Float, anchor: PointF) {
        val labelRotationAngleDegrees = xAxis.labelRotationAngle
        val centeringEnabled = xAxis.isCenterAxisLabelsEnabled

        var positions: FloatArray

        if (xAxis.isShowSpecificPositions) {
            positions = FloatArray(xAxis.specificPositions.size * 2)
            var i = 0
            while (i < positions.size) {
                positions[i] = xAxis.specificPositions[i / 2]
                i += 2
            }
        } else {
            positions = FloatArray(xAxis.entryCount * 2)
            var i = 0
            while (i < positions.size) {
                // only fill x values
                if (centeringEnabled) {
                    positions[i] = xAxis.centeredEntries[i / 2]
                } else {
                    positions[i] = xAxis.entries[i / 2]
                }
                i += 2
            }
        }

        transformer!!.pointValuesToPixel(positions)

        var i = 0
        while (i < positions.size) {
            var x = positions[i]

            if (viewPortHandler.isInBoundsX(x)) {
                val label = if (xAxis.isShowSpecificPositions)
                    xAxis.valueFormatter?.getFormattedValue(xAxis.specificPositions[i / 2], xAxis)
                else
                    xAxis.valueFormatter?.getFormattedValue(xAxis.entries[i / 2], xAxis)

                if (xAxis.isAvoidFirstLastClipping) {
                    // avoid clipping of the last

                    if (i / 2 == xAxis.entryCount - 1 && xAxis.entryCount > 1) {
                        val width = paintAxisLabels.calcTextWidth(label).toFloat()

                        if (width > viewPortHandler.offsetRight() * 2
                            && x + width > viewPortHandler.chartWidth
                        ) x -= width / 2

                        // avoid clipping of the first
                    } else if (i == 0) {
                        val width = paintAxisLabels.calcTextWidth(label).toFloat()
                        x += width / 2
                    }
                }

                drawLabel(canvas, label, x, pos, anchor, labelRotationAngleDegrees)
            }
            i += 2
        }
    }

    protected fun drawLabel(canvas: Canvas, formattedLabel: String?, x: Float, y: Float, anchor: PointF, angleDegrees: Float) {
        formattedLabel?.let { canvas.drawXAxisValue(it, x, y, paintAxisLabels, anchor, angleDegrees) }
    }

    protected open var renderGridLinesPath: Path = Path()
    protected open var renderGridLinesBuffer: FloatArray = FloatArray(2)
    override fun renderGridLines(canvas: Canvas) {
        if (!xAxis.isDrawGridLines || !xAxis.isEnabled)
            return

        canvas.withClip(gridClippingRect!!) {
            if (axis.isShowSpecificPositions) {
                if (renderGridLinesBuffer.size != axis.specificPositions.size * 2) {
                    renderGridLinesBuffer = FloatArray(xAxis.specificPositions.size * 2)
                }
            } else {
                if (renderGridLinesBuffer.size != axis.entryCount * 2) {
                    renderGridLinesBuffer = FloatArray(xAxis.entryCount * 2)
                }
            }
            val positions = renderGridLinesBuffer

            run {
                var i = 0
                while (i < positions.size) {
                    if (axis.isShowSpecificPositions) {
                        positions[i] = xAxis.specificPositions[i / 2]
                        positions[i + 1] = xAxis.specificPositions[i / 2]
                    } else {
                        positions[i] = xAxis.entries[i / 2]
                        positions[i + 1] = xAxis.entries[i / 2]
                    }
                    i += 2
                }
            }

            transformer!!.pointValuesToPixel(positions)

            setupGridPaint()

            val gridLinePath = renderGridLinesPath
            gridLinePath.reset()

            var i = 0
            while (i < positions.size) {
                drawGridLine(canvas, positions[i], positions[i + 1], gridLinePath)
                i += 2
            }

        }
    }

    protected var mGridClippingRect: RectF = RectF()

    open val gridClippingRect: RectF?
        get() {
            mGridClippingRect.set(viewPortHandler.contentRect)
            mGridClippingRect.inset(-axis.gridLineWidth, 0f)
            return mGridClippingRect
        }

    protected var mRenderLimitLinesBuffer: FloatArray = FloatArray(2)

    protected var mLimitLineClippingRect: RectF = RectF()

    var limitLineSegmentsBuffer: FloatArray = FloatArray(4)
    private val mLimitLinePath = Path()

    init {
        paintAxisLabels.color = Color.BLACK
        paintAxisLabels.textAlign = Align.CENTER
        paintAxisLabels.textSize = 10f.convertDpToPixel()
    }

    /**
     * Draws the grid line at the specified position using the provided path.
     *
     * @param canvas
     * @param x
     * @param y
     * @param gridLinePath
     */
    protected open fun drawGridLine(canvas: Canvas, x: Float, y: Float, gridLinePath: Path) {
        gridLinePath.moveTo(x, viewPortHandler.contentBottom())
        gridLinePath.lineTo(x, viewPortHandler.contentTop())

        // draw a path because lines don't support dashing on lower android versions
        canvas.drawPath(gridLinePath, paintGrid)

        gridLinePath.reset()
    }

    fun renderLimitLineLine(canvas: Canvas, limitLine: LimitLine, position: FloatArray) {
        limitLineSegmentsBuffer[0] = position[0]
        limitLineSegmentsBuffer[1] = viewPortHandler.contentTop()
        limitLineSegmentsBuffer[2] = position[0]
        limitLineSegmentsBuffer[3] = viewPortHandler.contentBottom()

        mLimitLinePath.reset()
        mLimitLinePath.moveTo(limitLineSegmentsBuffer[0], limitLineSegmentsBuffer[1])
        mLimitLinePath.lineTo(limitLineSegmentsBuffer[2], limitLineSegmentsBuffer[3])

        limitLinePaint.style = Paint.Style.STROKE
        limitLinePaint.color = limitLine.lineColor
        limitLinePaint.strokeWidth = limitLine.lineWidth
        limitLinePaint.pathEffect = limitLine.dashPathEffect

        canvas.drawPath(mLimitLinePath, limitLinePaint)
    }

    /**
     * Draws the LimitLines associated with this axis to the screen.
     *
     * @param canvas
     */
    override fun renderLimitLines(canvas: Canvas) {
        val limitLines = xAxis.limitLines

        if (limitLines.isEmpty())
            return

        val position = mRenderLimitLinesBuffer
        position[0] = 0f
        position[1] = 0f

        limitLines.forEach { limitLine ->
            if (limitLine.isEnabled) {
                canvas.withSave {
                    mLimitLineClippingRect.set(viewPortHandler.contentRect)
                    mLimitLineClippingRect.inset(-limitLine.lineWidth, 0f)
                    canvas.clipRect(mLimitLineClippingRect)

                    position[0] = limitLine.limit
                    position[1] = 0f

                    transformer?.pointValuesToPixel(position)

                    renderLimitLineLine(canvas, limitLine, position)
                    renderLimitLineLabel(canvas, limitLine, position, 2f + limitLine.yOffset)
                }
            }
        }
    }

    fun renderLimitLineLabel(canvas: Canvas, limitLine: LimitLine, position: FloatArray, yOffset: Float) {
        // if drawing the limit-value label is enabled
        limitLine.label?.let { label ->
            if (label.isNotEmpty() && limitLine.isEnabled) {
                limitLinePaint.style = limitLine.textStyle
                limitLinePaint.pathEffect = null
                limitLinePaint.color = limitLine.textColor
                limitLinePaint.strokeWidth = 0.5f
                limitLinePaint.textSize = limitLine.textSize

                val xOffset = limitLine.lineWidth + limitLine.xOffset

                val labelPosition = limitLine.labelPosition

                when (labelPosition) {
                    LimitLabelPosition.RIGHT_TOP -> {
                        val labelLineHeight = limitLinePaint.calcTextHeight(label).toFloat()
                        limitLinePaint.textAlign = Align.LEFT
                        canvas.drawText(
                            label, position[0] + xOffset, viewPortHandler.contentTop() + yOffset + labelLineHeight,
                            limitLinePaint
                        )
                    }

                    LimitLabelPosition.RIGHT_BOTTOM -> {
                        limitLinePaint.textAlign = Align.LEFT
                        canvas.drawText(label, position[0] + xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
                    }

                    LimitLabelPosition.LEFT_TOP -> {
                        limitLinePaint.textAlign = Align.RIGHT
                        val labelLineHeight = limitLinePaint.calcTextHeight(label).toFloat()
                        canvas.drawText(
                            label, position[0] - xOffset, viewPortHandler.contentTop() + yOffset + labelLineHeight,
                            limitLinePaint
                        )
                    }

                    else -> {
                        limitLinePaint.textAlign = Align.RIGHT
                        canvas.drawText(label, position[0] - xOffset, viewPortHandler.contentBottom() - yOffset, limitLinePaint)
                    }
                }
            }
        }
    }
}
