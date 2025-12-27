package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint.Align
import android.graphics.RectF
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.buffer.HorizontalBarBuffer
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.dataprovider.base.IBaseProvider
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.Fill
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.calcTextWidth
import info.appdev.charting.utils.convertDpToPixel
import info.appdev.charting.utils.drawImage
import kotlin.math.ceil
import kotlin.math.min

@Suppress("MemberVisibilityCanBePrivate")
open class HorizontalBarChartRenderer(
    dataProvider: BarDataProvider, animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(dataProvider, animator, viewPortHandler) {
    override fun initBuffers() {
        this@HorizontalBarChartRenderer.dataProvider.barData?.let { barData ->
            barBuffers = arrayOfNulls<HorizontalBarBuffer>(barData.dataSetCount).toMutableList()

            for (i in barBuffers.indices) {
                val set = barData.getDataSetByIndex(i)
                set?.let {
                    barBuffers[i] = HorizontalBarBuffer(
                        it.entryCount * 4 * (if (set.isStacked) set.stackSize else 1),
                        barData.dataSetCount, set.isStacked
                    )
                }
            }
        }
    }

    private val mBarShadowRectBuffer = RectF()

    init {
        paintValues.textAlign = Align.LEFT
    }

    override fun drawDataSet(canvas: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = this@HorizontalBarChartRenderer.dataProvider.getTransformer(dataSet.axisDependency)

        barBorderPaint.color = dataSet.barBorderColor
        barBorderPaint.strokeWidth = dataSet.barBorderWidth.convertDpToPixel()

        val drawBorder = dataSet.barBorderWidth > 0f

        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        // draw the bar shadow before the values
        if (this@HorizontalBarChartRenderer.dataProvider.isDrawBarShadowEnabled) {
            shadowPaint.color = dataSet.barShadowColor

            this@HorizontalBarChartRenderer.dataProvider.barData?.let { barData ->

                val barWidth = barData.barWidth
                val barWidthHalf = barWidth / 2.0f
                var x: Float

                var i = 0
                val count = min((ceil(((dataSet.entryCount).toFloat() * phaseX).toDouble())).toInt().toDouble(), dataSet.entryCount.toDouble()).toInt()
                while (i < count) {
                    val barEntry = dataSet.getEntryForIndex(i)
                    barEntry?.let {
                        x = it.x

                        mBarShadowRectBuffer.top = x - barWidthHalf
                        mBarShadowRectBuffer.bottom = x + barWidthHalf

                        trans!!.rectValueToPixel(mBarShadowRectBuffer)

                        if (!viewPortHandler.isInBoundsTop(mBarShadowRectBuffer.bottom)) {
                            i++
                            continue
                        }

                        if (!viewPortHandler.isInBoundsBottom(mBarShadowRectBuffer.top)) {
                            break
                        }

                        mBarShadowRectBuffer.left = viewPortHandler.contentLeft()
                        mBarShadowRectBuffer.right = viewPortHandler.contentRight()

                        canvas.drawRect(mBarShadowRectBuffer, shadowPaint)
                    }
                    i++
                }
            }
        }

        // initialize the buffer
        val buffer = barBuffers[index]!!
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.inverted = this@HorizontalBarChartRenderer.dataProvider.isInverted(dataSet.axisDependency)
        this@HorizontalBarChartRenderer.dataProvider.barData?.let { buffer.barWidth = it.barWidth }

        buffer.feed(dataSet)

        trans!!.pointValuesToPixel(buffer.buffer)

        val isCustomFill = dataSet.fills.isNotEmpty()
        val isSingleColor = dataSet.colors.size == 1
        val isInverted = this@HorizontalBarChartRenderer.dataProvider.isInverted(dataSet.axisDependency)

        if (isSingleColor) {
            paintRender.color = dataSet.color
        }

        var j = 0
        var pos = 0
        while (j < buffer.size()) {
            if (!viewPortHandler.isInBoundsTop(buffer.buffer[j + 3])) {
                break
            }

            if (!viewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
                j += 4
                pos++
                continue
            }

            if (!isSingleColor) {
                // Set the color for the currently drawn value. If the index
                // is out of bounds, reuse colors.
                paintRender.color = dataSet.getColorByIndex(j / 4)
            }

            if (isCustomFill) {
                dataSet.getFill(pos)?.fillRect(
                    canvas, paintRender,
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    buffer.buffer[j + 2],
                    buffer.buffer[j + 3],
                    if (isInverted) Fill.Direction.LEFT else Fill.Direction.RIGHT,
                    0f
                )
            } else {
                canvas.drawRect(
                    buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                    buffer.buffer[j + 3], paintRender
                )
            }

            if (drawBorder) {
                canvas.drawRect(
                    buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                    buffer.buffer[j + 3], barBorderPaint
                )
            }
            j += 4
            pos++
        }
    }

    override fun drawValues(canvas: Canvas) {
        if (isDrawingValuesAllowed(this@HorizontalBarChartRenderer.dataProvider)) {
            val dataSets = this@HorizontalBarChartRenderer.dataProvider.barData?.dataSets

            val valueOffsetPlus = 5f.convertDpToPixel()
            var posOffset: Float
            var negOffset: Float
            val drawValueAboveBar = this@HorizontalBarChartRenderer.dataProvider.isDrawValueAboveBarEnabled

            this@HorizontalBarChartRenderer.dataProvider.barData?.let { barData ->
                for (i in 0..<barData.dataSetCount) {
                    val dataSet = dataSets!![i]
                    if (dataSet.entryCount == 0) {
                        continue
                    }
                    if (!shouldDrawValues(dataSet)) {
                        continue
                    }

                    val isInverted = this@HorizontalBarChartRenderer.dataProvider.isInverted(dataSet.axisDependency)

                    // apply the text-styling defined by the DataSet
                    applyValueTextStyle(dataSet)
                    val halfTextHeight = paintValues.calcTextHeight("10") / 2f

                    val formatter = dataSet.valueFormatter

                    // get the buffer
                    val buffer = barBuffers[i]!!

                    val phaseY = animator.phaseY

                    val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                    iconsOffset.x = iconsOffset.x.convertDpToPixel()
                    iconsOffset.y = iconsOffset.y.convertDpToPixel()

                    // if only single values are drawn (sum)
                    if (!dataSet.isStacked) {
                        var j = 0
                        while (j < buffer.buffer.size * animator.phaseX) {
                            val y = (buffer.buffer[j + 1] + buffer.buffer[j + 3]) / 2f

                            if (!viewPortHandler.isInBoundsTop(buffer.buffer[j + 1])) {
                                break
                            }

                            if (!viewPortHandler.isInBoundsX(buffer.buffer[j])) {
                                j += 4
                                continue
                            }

                            if (!viewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
                                j += 4
                                continue
                            }

                            val barEntry = dataSet.getEntryForIndex(j / 4)
                            barEntry?.let {
                                val valueY = barEntry.y
                                val formattedValue = formatter.getFormattedValue(valueY, barEntry, i, viewPortHandler)
                                // calculate the correct offset depending on the draw position of the value
                                val valueTextWidth = paintValues.calcTextWidth(formattedValue).toFloat()
                                posOffset = (if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus))
                                negOffset = ((if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus)
                                        - (buffer.buffer[j + 2] - buffer.buffer[j]))

                                if (isInverted) {
                                    posOffset = -posOffset - valueTextWidth
                                    negOffset = -negOffset - valueTextWidth
                                }

                                if (dataSet.isDrawValues) {
                                    drawValue(
                                        canvas,
                                        formattedValue!!,
                                        buffer.buffer[j + 2] + (if (valueY >= 0) posOffset else negOffset),
                                        y + halfTextHeight,
                                        dataSet.getValueTextColor(j / 2)
                                    )
                                }

                                if (barEntry.icon != null && dataSet.isDrawIcons) {
                                    val icon = barEntry.icon

                                    var px = buffer.buffer[j + 2] + (if (valueY >= 0) posOffset else negOffset)
                                    var py = y

                                    px += iconsOffset.x
                                    py += iconsOffset.y

                                    icon?.let {
                                        canvas.drawImage(
                                            it,
                                            px.toInt(),
                                            py.toInt()
                                        )
                                    }
                                }
                            }
                            j += 4
                        }

                        // if each value of a potential stack should be drawn
                    } else {
                        val trans = this@HorizontalBarChartRenderer.dataProvider.getTransformer(dataSet.axisDependency)

                        var bufferIndex = 0
                        var index = 0

                        while (index < dataSet.entryCount * animator.phaseX) {
                            val barEntry = dataSet.getEntryForIndex(index)
                            barEntry?.let {
                                val color = dataSet.getValueTextColor(index)
                                val vals = it.yVals

                                // we still draw stacked bars, but there is one
                                // non-stacked
                                // in between
                                if (vals == null) {
                                    if (!viewPortHandler.isInBoundsTop(buffer.buffer[bufferIndex + 1])) {
                                        break
                                    }

                                    if (!viewPortHandler.isInBoundsX(buffer.buffer[bufferIndex])) {
                                        continue
                                    }

                                    if (!viewPortHandler.isInBoundsBottom(buffer.buffer[bufferIndex + 1])) {
                                        continue
                                    }

                                    val formattedValue = formatter.getFormattedValue(
                                        it.y,
                                        it, i, viewPortHandler
                                    )

                                    // calculate the correct offset depending on the draw position of the value
                                    val valueTextWidth = paintValues.calcTextWidth(formattedValue).toFloat()
                                    posOffset = (if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus))
                                    negOffset = (if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus)

                                    if (isInverted) {
                                        posOffset = -posOffset - valueTextWidth
                                        negOffset = -negOffset - valueTextWidth
                                    }

                                    if (dataSet.isDrawValues) {
                                        drawValue(
                                            canvas, formattedValue!!,
                                            buffer.buffer[bufferIndex + 2]
                                                    + (if (it.y >= 0) posOffset else negOffset),
                                            buffer.buffer[bufferIndex + 1] + halfTextHeight, color
                                        )
                                    }

                                    if (it.icon != null && dataSet.isDrawIcons) {
                                        val icon = it.icon

                                        var px = (buffer.buffer[bufferIndex + 2]
                                                + (if (it.y >= 0) posOffset else negOffset))
                                        var py = buffer.buffer[bufferIndex + 1]

                                        px += iconsOffset.x
                                        py += iconsOffset.y

                                        icon?.let { myIcon ->
                                            canvas.drawImage(
                                                myIcon,
                                                px.toInt(),
                                                py.toInt()
                                            )
                                        }
                                    }
                                } else {
                                    val transformed = FloatArray(vals.size * 2)

                                    var posY = 0f
                                    var negY = -it.negativeSum

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

                                            transformed[k] = y * phaseY
                                            k += 2
                                            idx++
                                        }
                                    }

                                    trans!!.pointValuesToPixel(transformed)

                                    var k = 0
                                    while (k < transformed.size) {
                                        val valueY = vals[k / 2]
                                        val formattedValue = formatter.getFormattedValue(
                                            valueY,
                                            it, i, viewPortHandler
                                        )

                                        // calculate the correct offset depending on the draw position of the value
                                        val valueTextWidth = paintValues.calcTextWidth(formattedValue).toFloat()
                                        posOffset = (if (drawValueAboveBar) valueOffsetPlus else -(valueTextWidth + valueOffsetPlus))
                                        negOffset = (if (drawValueAboveBar) -(valueTextWidth + valueOffsetPlus) else valueOffsetPlus)

                                        if (isInverted) {
                                            posOffset = -posOffset - valueTextWidth
                                            negOffset = -negOffset - valueTextWidth
                                        }

                                        val drawBelow = (valueY == 0.0f && negY == 0.0f && posY > 0.0f) || valueY < 0.0f

                                        val x = (transformed[k] + (if (drawBelow) negOffset else posOffset))
                                        val y = (buffer.buffer[bufferIndex + 1] + buffer.buffer[bufferIndex + 3]) / 2f

                                        if (!viewPortHandler.isInBoundsTop(y)) {
                                            break
                                        }

                                        if (!viewPortHandler.isInBoundsX(x)) {
                                            k += 2
                                            continue
                                        }

                                        if (!viewPortHandler.isInBoundsBottom(y)) {
                                            k += 2
                                            continue
                                        }

                                        if (dataSet.isDrawValues) {
                                            drawValue(canvas, formattedValue!!, x, y + halfTextHeight, color)
                                        }

                                        if (it.icon != null && dataSet.isDrawIcons) {
                                            val icon = it.icon

                                            icon?.let { myIcon ->
                                                canvas.drawImage(
                                                    myIcon,
                                                    (x + iconsOffset.x).toInt(),
                                                    (y + iconsOffset.y).toInt()
                                                )
                                            }
                                        }
                                        k += 2
                                    }
                                }

                                bufferIndex = if (vals == null) bufferIndex + 4 else bufferIndex + 4 * vals.size
                            }
                            index++
                        }
                    }

                    PointF.recycleInstance(iconsOffset)
                }
            }
        }
    }

    protected fun drawValue(canvas: Canvas, valueText: String, x: Float, y: Float, color: Int) {
        paintValues.color = color
        canvas.drawText(valueText, x, y, paintValues)
    }

    override fun prepareBarHighlight(x: Float, y1: Float, y2: Float, barWidthHalf: Float, trans: Transformer) {
        val top = x - barWidthHalf
        val bottom = x + barWidthHalf

        barRect[y1, top, y2] = bottom

        trans.rectToPixelPhaseHorizontal(barRect, animator.phaseY)
    }

    override fun setHighlightDrawPos(high: Highlight, bar: RectF) {
        high.setDraw(bar.centerY(), bar.right)
    }

    override fun isDrawingValuesAllowed(baseProvider: IBaseProvider): Boolean {
        return baseProvider.getData()!!.entryCount < baseProvider.maxVisibleCount * viewPortHandler.scaleY
    }
}
