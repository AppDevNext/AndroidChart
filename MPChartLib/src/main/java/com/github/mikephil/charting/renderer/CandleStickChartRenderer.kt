package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.CandleDataProvider
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.utils.convertDpToPixel

open class CandleStickChartRenderer(
    @JvmField
    var dataProvider: CandleDataProvider, animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineScatterCandleRadarRenderer(animator, viewPortHandler) {
    private val shadowBuffers = FloatArray(8)
    private val bodyBuffers = FloatArray(4)
    private val rangeBuffers = FloatArray(4)
    private val openBuffers = FloatArray(4)
    private val closeBuffers = FloatArray(4)

    override fun initBuffers() = Unit

    override fun drawData(canvas: Canvas) {
        val candleData = dataProvider.candleData

        for (set in candleData.dataSets) {
            if (set.isVisible) drawDataSet(canvas, set)
        }
    }

    protected fun drawDataSet(canvas: Canvas, dataSet: ICandleDataSet) {
        val trans = dataProvider.getTransformer(dataSet.axisDependency)

        val phaseY = animator.phaseY
        val barSpace = dataSet.barSpace
        val showCandleBar = dataSet.showCandleBar

        this.xBounds
        xBounds.set(dataProvider, dataSet)

        paintRender.strokeWidth = dataSet.shadowWidth

        // draw the body
        for (j in xBounds.min..xBounds.range + xBounds.min) {
            // get the entry

            val e = dataSet.getEntryForIndex(j) ?: continue

            val xPos = e.x

            val open = e.open
            val close = e.close
            val high = e.high
            val low = e.low

            if (showCandleBar) {
                // calculate the shadow
                shadowBuffers[0] = xPos
                shadowBuffers[2] = xPos
                shadowBuffers[4] = xPos
                shadowBuffers[6] = xPos

                if (open > close) {
                    shadowBuffers[1] = high * phaseY
                    shadowBuffers[3] = open * phaseY
                    shadowBuffers[5] = low * phaseY
                    shadowBuffers[7] = close * phaseY
                } else if (open < close) {
                    shadowBuffers[1] = high * phaseY
                    shadowBuffers[3] = close * phaseY
                    shadowBuffers[5] = low * phaseY
                    shadowBuffers[7] = open * phaseY
                } else {
                    shadowBuffers[1] = high * phaseY
                    shadowBuffers[3] = open * phaseY
                    shadowBuffers[5] = low * phaseY
                    shadowBuffers[7] = shadowBuffers[3]
                }

                trans!!.pointValuesToPixel(shadowBuffers)

                // draw the shadows
                if (dataSet.shadowColorSameAsCandle) {
                    if (open > close) paintRender.color =
                        if (dataSet.decreasingColor == ColorTemplate.COLOR_NONE) dataSet.getColorByIndex(j) else dataSet.decreasingColor
                    else if (open < close) paintRender.color =
                        if (dataSet.increasingColor == ColorTemplate.COLOR_NONE) dataSet.getColorByIndex(j) else dataSet.increasingColor
                    else paintRender.color = if (dataSet.neutralColor == ColorTemplate.COLOR_NONE) dataSet.getColorByIndex(j) else dataSet.neutralColor
                } else {
                    paintRender.color = if (dataSet.shadowColor == ColorTemplate.COLOR_NONE) dataSet.getColorByIndex(j) else dataSet.shadowColor
                }

                paintRender.style = Paint.Style.STROKE

                canvas.drawLines(shadowBuffers, paintRender)

                // calculate the body
                bodyBuffers[0] = xPos - 0.5f + barSpace
                bodyBuffers[1] = close * phaseY
                bodyBuffers[2] = (xPos + 0.5f - barSpace)
                bodyBuffers[3] = open * phaseY

                trans.pointValuesToPixel(bodyBuffers)

                // draw body differently for increasing and decreasing entry
                if (open > close) { // decreasing

                    if (dataSet.decreasingColor == ColorTemplate.COLOR_NONE) {
                        paintRender.color = dataSet.getColorByIndex(j)
                    } else {
                        paintRender.color = dataSet.decreasingColor
                    }

                    paintRender.style = dataSet.decreasingPaintStyle

                    canvas.drawRect(
                        bodyBuffers[0], bodyBuffers[3],
                        bodyBuffers[2], bodyBuffers[1],
                        paintRender
                    )
                } else if (open < close) {
                    if (dataSet.increasingColor == ColorTemplate.COLOR_NONE) {
                        paintRender.color = dataSet.getColorByIndex(j)
                    } else {
                        paintRender.color = dataSet.increasingColor
                    }

                    paintRender.style = dataSet.increasingPaintStyle

                    canvas.drawRect(
                        bodyBuffers[0], bodyBuffers[1],
                        bodyBuffers[2], bodyBuffers[3],
                        paintRender
                    )
                } else { // equal values

                    if (dataSet.neutralColor == ColorTemplate.COLOR_NONE) {
                        paintRender.color = dataSet.getColorByIndex(j)
                    } else {
                        paintRender.color = dataSet.neutralColor
                    }

                    canvas.drawLine(
                        bodyBuffers[0], bodyBuffers[1],
                        bodyBuffers[2], bodyBuffers[3],
                        paintRender
                    )
                }
            } else {
                rangeBuffers[0] = xPos
                rangeBuffers[1] = high * phaseY
                rangeBuffers[2] = xPos
                rangeBuffers[3] = low * phaseY

                openBuffers[0] = xPos - 0.5f + barSpace
                openBuffers[1] = open * phaseY
                openBuffers[2] = xPos
                openBuffers[3] = open * phaseY

                closeBuffers[0] = xPos + 0.5f - barSpace
                closeBuffers[1] = close * phaseY
                closeBuffers[2] = xPos
                closeBuffers[3] = close * phaseY

                trans!!.pointValuesToPixel(rangeBuffers)
                trans.pointValuesToPixel(openBuffers)
                trans.pointValuesToPixel(closeBuffers)

                // draw the ranges
                val barColor = if (open > close) if (dataSet.decreasingColor == ColorTemplate.COLOR_NONE)
                    dataSet.getColorByIndex(j)
                else
                    dataSet.decreasingColor
                else if (open < close) if (dataSet.increasingColor == ColorTemplate.COLOR_NONE)
                    dataSet.getColorByIndex(j)
                else
                    dataSet.increasingColor
                else if (dataSet.neutralColor == ColorTemplate.COLOR_NONE)
                    dataSet.getColorByIndex(j)
                else
                    dataSet.neutralColor

                paintRender.color = barColor
                canvas.drawLine(
                    rangeBuffers[0], rangeBuffers[1],
                    rangeBuffers[2], rangeBuffers[3],
                    paintRender
                )
                canvas.drawLine(
                    openBuffers[0], openBuffers[1],
                    openBuffers[2], openBuffers[3],
                    paintRender
                )
                canvas.drawLine(
                    closeBuffers[0], closeBuffers[1],
                    closeBuffers[2], closeBuffers[3],
                    paintRender
                )
            }
        }
    }

    override fun drawValues(canvas: Canvas) {
        // if values are drawn
        if (isDrawingValuesAllowed(dataProvider)) {
            val dataSets = dataProvider.candleData.dataSets

            for (i in dataSets.indices) {
                val dataSet = dataSets[i]
                if (dataSet.entryCount == 0) {
                    continue
                }
                if (!shouldDrawValues(dataSet) || dataSet.entryCount < 1) {
                    continue
                }

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)

                val trans = dataProvider.getTransformer(dataSet.axisDependency)

                xBounds.set(dataProvider, dataSet)

                val positions = trans!!.generateTransformedValuesCandle(
                    dataSet, animator.phaseX, animator.phaseY, xBounds.min, xBounds.max
                )

                val yOffset = 5f.convertDpToPixel()

                val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = iconsOffset.x.convertDpToPixel()
                iconsOffset.y = iconsOffset.y.convertDpToPixel()

                var j = 0
                while (j < positions.size) {
                    val x = positions[j]
                    val y = positions[j + 1]

                    if (!viewPortHandler.isInBoundsRight(x)) break

                    if (!viewPortHandler.isInBoundsLeft(x) || !viewPortHandler.isInBoundsY(y)) {
                        j += 2
                        continue
                    }

                    val entry = dataSet.getEntryForIndex(j / 2 + xBounds.min)

                    if (dataSet.isDrawValues) {
                        drawValue(
                            canvas,
                            dataSet.valueFormatter,
                            entry.high,
                            entry,
                            i,
                            x,
                            y - yOffset,
                            dataSet
                                .getValueTextColor(j / 2)
                        )
                    }

                    if (entry.icon != null && dataSet.isDrawIcons) {
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
                    j += 2
                }

                MPPointF.recycleInstance(iconsOffset)
            }
        }
    }

    override fun drawExtras(canvas: Canvas) = Unit

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val candleData = dataProvider.candleData

        for (high in indices) {
            val set = candleData.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled) continue

            val e = set.getEntryForXValue(high.x, high.y)

            if (!isInBoundsX(e, set)) continue

            val lowValue = e.low * animator.phaseY
            val highValue = e.high * animator.phaseY
            val y = (lowValue + highValue) / 2f

            val pix = dataProvider.getTransformer(set.axisDependency)!!.getPixelForValues(e.x, y)

            high.setDraw(pix.x.toFloat(), pix.y.toFloat())

            // draw the lines
            drawHighlightLines(canvas, pix.x.toFloat(), pix.y.toFloat(), set)
        }
    }
}
