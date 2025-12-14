package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.Fill
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.ceil
import kotlin.math.min

open class BarChartRenderer(
    @JvmField var chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarLineScatterCandleBubbleRenderer(animator, viewPortHandler) {
    /**
     * the rect object that is used for drawing the bars
     */
    @JvmField
    protected var barRect: RectF = RectF()

    @JvmField
    protected var barBuffers: MutableList<BarBuffer?> = mutableListOf()

    @JvmField
    protected var shadowPaint: Paint

    @JvmField
    protected var barBorderPaint: Paint

    /**
     * if set to true, the bar chart's bars would be round on all corners instead of rectangular
     */
    private var drawRoundedBars = false

    /**
     * the radius of the rounded bar chart bars
     */
    private var roundedBarRadius = 0f

    constructor(
        chart: BarDataProvider, animator: ChartAnimator,
        viewPortHandler: ViewPortHandler, mDrawRoundedBars: Boolean, mRoundedBarRadius: Float
    ) : this(chart, animator, viewPortHandler) {
        this.drawRoundedBars = mDrawRoundedBars
        this.roundedBarRadius = mRoundedBarRadius
    }

    override fun initBuffers() {
        val barData = chart.barData
        barBuffers = mutableListOf()

        barData.dataSets.forEach {
            barBuffers.add(
                BarBuffer(
                    it.entryCount * 4 * (if (it.isStacked) it.stackSize else 1),
                    barData.dataSetCount, it.isStacked
                )
            )
        }
    }

    override fun drawData(canvas: Canvas) {
        if (barBuffers.isEmpty()) {
            initBuffers()
        }

        val barData = chart.barData

        for (i in 0..<barData.dataSetCount) {
            val set = barData.getDataSetByIndex(i)

            if (set.isVisible) {
                drawDataSet(canvas, set, i)
            }
        }
    }

    private val barShadowRectBuffer = RectF()

    init {
        paintHighlight = Paint(Paint.ANTI_ALIAS_FLAG)
        paintHighlight.style = Paint.Style.FILL
        paintHighlight.color = Color.rgb(0, 0, 0)
        // set alpha after color
        paintHighlight.alpha = 120

        shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowPaint.style = Paint.Style.FILL

        barBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        barBorderPaint.style = Paint.Style.STROKE
    }

    protected open fun drawDataSet(canvas: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = chart.getTransformer(dataSet.axisDependency)

        barBorderPaint.color = dataSet.barBorderColor
        barBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

        val drawBorder = dataSet.barBorderWidth > 0f

        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        // draw the bar shadow before the values
        if (chart.isDrawBarShadowEnabled) {
            shadowPaint.color = dataSet.barShadowColor

            val barData = chart.barData

            val barWidth = barData.barWidth
            val barWidthHalf = barWidth / 2.0f
            var x: Float

            var i = 0
            val count = min((ceil(((dataSet.entryCount).toFloat() * phaseX).toDouble())).toInt().toDouble(), dataSet.entryCount.toDouble()).toInt()
            while (i < count) {
                val e = dataSet.getEntryForIndex(i)

                x = e.x

                barShadowRectBuffer.left = x - barWidthHalf
                barShadowRectBuffer.right = x + barWidthHalf

                trans!!.rectValueToPixel(barShadowRectBuffer)

                if (!viewPortHandler.isInBoundsLeft(barShadowRectBuffer.right)) {
                    i++
                    continue
                }

                if (!viewPortHandler.isInBoundsRight(barShadowRectBuffer.left)) {
                    break
                }

                barShadowRectBuffer.top = viewPortHandler.contentTop()
                barShadowRectBuffer.bottom = viewPortHandler.contentBottom()

                if (drawRoundedBars) {
                    canvas.drawRoundRect(barShadowRectBuffer, roundedBarRadius, roundedBarRadius, shadowPaint)
                } else {
                    canvas.drawRect(barShadowRectBuffer, shadowPaint)
                }
                i++
            }
        }

        // initialize the buffer
        val buffer = barBuffers[index]!!.apply {
            setPhases(phaseX, phaseY)
            setDataSet(index)
            setInverted(chart.isInverted(dataSet.axisDependency))
            setBarWidth(chart.barData.barWidth)
            feed(dataSet)
        }
        trans!!.pointValuesToPixel(buffer.buffer)

        val isCustomFill = dataSet.fills != null && dataSet.fills.isNotEmpty()
        val isSingleColor = dataSet.colors.size == 1
        val isInverted = chart.isInverted(dataSet.axisDependency)

        if (isSingleColor) {
            paintRender.color = dataSet.color
        }

        var j = 0
        var pos = 0
        while (j < buffer.size()) {
            if (!viewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                j += 4
                pos++
                continue
            }

            if (!viewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                break
            }

            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                paintRender.color = dataSet.getColorByIndex(pos)
            }

            if (isCustomFill) {
                dataSet.getFill(pos)
                    .fillRect(
                        canvas, paintRender,
                        buffer.buffer[j],
                        buffer.buffer[j + 1],
                        buffer.buffer[j + 2],
                        buffer.buffer[j + 3],
                        if (isInverted) Fill.Direction.DOWN else Fill.Direction.UP,
                        roundedBarRadius
                    )
            } else {
                if (drawRoundedBars) {
                    canvas.drawRoundRect(
                        RectF(
                            buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3]
                        ), roundedBarRadius, roundedBarRadius, paintRender
                    )
                } else {
                    canvas.drawRect(
                        buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], paintRender
                    )
                }
            }

            if (drawBorder) {
                if (drawRoundedBars) {
                    canvas.drawRoundRect(
                        RectF(
                            buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3]
                        ), roundedBarRadius, roundedBarRadius, barBorderPaint
                    )
                } else {
                    canvas.drawRect(
                        buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                        buffer.buffer[j + 3], barBorderPaint
                    )
                }
            }
            j += 4
            pos++
        }
    }

    protected open fun prepareBarHighlight(x: Float, y1: Float, y2: Float, barWidthHalf: Float, trans: Transformer) {
        val left = x - barWidthHalf
        val right = x + barWidthHalf

        barRect[left, y1, right] = y2

        trans.rectToPixelPhase(barRect, animator.phaseY)
    }

    override fun drawValues(canvas: Canvas) {
        // if values are drawn

        if (isDrawingValuesAllowed(chart)) {
            val dataSets = chart.barData.dataSets

            val valueOffsetPlus = Utils.convertDpToPixel(4.5f)
            var posOffset: Float
            var negOffset: Float
            val drawValueAboveBar = chart.isDrawValueAboveBarEnabled

            for (i in 0..<chart.barData.dataSetCount) {
                val dataSet = dataSets[i]
                if (dataSet.entryCount == 0) {
                    continue
                }
                if (!shouldDrawValues(dataSet)) {
                    continue
                }

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)

                val isInverted = chart.isInverted(dataSet.axisDependency)

                // calculate the correct offset depending on the draw position of
                // the value
                val valueTextHeight = Utils.calcTextHeight(paintValues, "8").toFloat()
                posOffset = (if (drawValueAboveBar) -valueOffsetPlus else valueTextHeight + valueOffsetPlus)
                negOffset = (if (drawValueAboveBar) valueTextHeight + valueOffsetPlus else -valueOffsetPlus)

                if (isInverted) {
                    posOffset = -posOffset - valueTextHeight
                    negOffset = -negOffset - valueTextHeight
                }

                // get the buffer
                val buffer = barBuffers[i]

                val phaseY = animator.phaseY

                val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)

                // if only single values are drawn (sum)
                if (!dataSet.isStacked) {
                    var j = 0
                    while (j < buffer!!.buffer.size * animator.phaseX) {
                        val x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f

                        if (!viewPortHandler.isInBoundsRight(x)) {
                            break
                        }

                        if (!viewPortHandler.isInBoundsY(buffer.buffer[j + 1])
                            || !viewPortHandler.isInBoundsLeft(x)
                        ) {
                            j += 4
                            continue
                        }

                        val entry = dataSet.getEntryForIndex(j / 4)
                        val `val` = entry.y

                        if (dataSet.isDrawValuesEnabled) {
                            drawValue(
                                canvas, dataSet.valueFormatter, `val`, entry, i, x,
                                if (`val` >= 0) (buffer.buffer[j + 1] + posOffset) else (buffer.buffer[j + 3] + negOffset),
                                dataSet.getValueTextColor(j / 4)
                            )
                        }

                        if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                            val icon = entry.icon

                            var px = x
                            var py = if (`val` >= 0) (buffer.buffer[j + 1] + posOffset) else (buffer.buffer[j + 3] + negOffset)

                            px += iconsOffset.x
                            py += iconsOffset.y

                            icon?.let {
                                Utils.drawImage(
                                    canvas,
                                    it,
                                    px.toInt(),
                                    py.toInt()
                                )
                            }
                        }
                        j += 4
                    }

                    // if we have stacks
                } else {
                    val trans = chart.getTransformer(dataSet.axisDependency)

                    var bufferIndex = 0
                    var index = 0

                    while (index < dataSet.entryCount * animator.phaseX) {
                        val entry = dataSet.getEntryForIndex(index)

                        val vals = entry.yVals
                        val x = (buffer!!.buffer[bufferIndex] + buffer.buffer[bufferIndex + 2]) / 2f

                        val color = dataSet.getValueTextColor(index)

                        // we still draw stacked bars, but there is one
                        // non-stacked
                        // in between
                        if (vals == null) {
                            if (!viewPortHandler.isInBoundsRight(x)) {
                                break
                            }

                            if (!viewPortHandler.isInBoundsY(buffer.buffer[bufferIndex + 1])
                                || !viewPortHandler.isInBoundsLeft(x)
                            ) {
                                continue
                            }

                            if (dataSet.isDrawValuesEnabled) {
                                drawValue(
                                    canvas, dataSet.valueFormatter, entry.y, entry, i, x,
                                    buffer.buffer[bufferIndex + 1] +
                                            (if (entry.y >= 0) posOffset else negOffset),
                                    color
                                )
                            }

                            if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                                val icon = entry.icon

                                var px = x
                                var py = buffer.buffer[bufferIndex + 1] +
                                        (if (entry.y >= 0) posOffset else negOffset)

                                px += iconsOffset.x
                                py += iconsOffset.y

                                icon?.let {
                                    Utils.drawImage(
                                        canvas,
                                        it,
                                        px.toInt(),
                                        py.toInt()
                                    )
                                }
                            }

                            // draw stack values
                        } else {
                            val transformed = FloatArray(vals.size * 2)

                            var posY = 0f
                            var negY = -entry.negativeSum

                            run {
                                var k = 0
                                var idx = 0
                                while (k < transformed.size) {
                                    val value = vals[idx]
                                    val y: Float

                                    if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
                                        // Take care of the situation of a 0.0 value, which overlaps a non-zero bar
                                        y = value
                                    } else if (value >= 0.0f) {
                                        posY += value
                                        y = posY
                                    } else {
                                        y = negY
                                        negY -= value
                                    }

                                    transformed[k + 1] = y * phaseY
                                    k += 2
                                    idx++
                                }
                            }

                            trans!!.pointValuesToPixel(transformed)

                            var k = 0
                            while (k < transformed.size) {
                                val `val` = vals[k / 2]
                                val drawBelow =
                                    (`val` == 0.0f && negY == 0.0f && posY > 0.0f) ||
                                            `val` < 0.0f
                                val y = (transformed[k + 1]
                                        + (if (drawBelow) negOffset else posOffset))

                                if (!viewPortHandler.isInBoundsRight(x)) {
                                    break
                                }

                                if (!viewPortHandler.isInBoundsY(y)
                                    || !viewPortHandler.isInBoundsLeft(x)
                                ) {
                                    k += 2
                                    continue
                                }

                                if (dataSet.isDrawValuesEnabled) {
                                    drawValue(
                                        canvas,
                                        dataSet.valueFormatter,
                                        vals[k / 2],
                                        entry,
                                        i,
                                        x,
                                        y,
                                        color
                                    )
                                }

                                if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                                    val icon = entry.icon

                                    icon?.let {
                                        Utils.drawImage(
                                            canvas,
                                            it,
                                            (x + iconsOffset.x).toInt(),
                                            (y + iconsOffset.y).toInt()
                                        )
                                    }
                                }
                                k += 2
                            }
                        }

                        bufferIndex = if (vals == null) bufferIndex + 4 else bufferIndex + 4 * vals.size
                        index++
                    }
                }

                MPPointF.recycleInstance(iconsOffset)
            }
        }
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val barData = chart.barData

        for (high in indices) {
            val set = barData.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled) {
                continue
            }

            val barEntry = set.getEntryForXValue(high.x, high.y)!!

            if (!isInBoundsX(barEntry, set)) {
                continue
            }

            val trans = chart.getTransformer(set.axisDependency)

            paintHighlight.color = set.highLightColor
            paintHighlight.alpha = set.highLightAlpha

            val isStack = high.stackIndex >= 0 && barEntry.isStacked

            val y1: Float
            val y2: Float

            if (isStack) {
                if (chart.isHighlightFullBarEnabled) {
                    y1 = barEntry.positiveSum
                    y2 = -barEntry.negativeSum
                } else {
                    val range = barEntry.ranges[high.stackIndex]

                    y1 = range?.from ?: 0f
                    y2 = range?.to ?: 0f
                }
            } else {
                y1 = barEntry.y
                y2 = 0f
            }

            prepareBarHighlight(barEntry.x, y1, y2, barData.barWidth / 2f, trans!!)

            setHighlightDrawPos(high, barRect)

            if (drawRoundedBars) {
                canvas.drawRoundRect(RectF(barRect), roundedBarRadius, roundedBarRadius, paintHighlight)
            } else {
                canvas.drawRect(barRect, paintHighlight)
            }
        }
    }

    /**
     * Sets the drawing position of the highlight object based on the riven bar-rect.
     *
     * @param high
     */
    protected open fun setHighlightDrawPos(high: Highlight, bar: RectF) {
        high.setDraw(bar.centerX(), bar.top)
    }

    override fun drawExtras(canvas: Canvas) {
    }
}
