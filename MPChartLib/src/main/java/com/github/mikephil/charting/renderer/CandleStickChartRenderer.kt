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

open class CandleStickChartRenderer(
    @JvmField var mChart: CandleDataProvider, animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : LineScatterCandleRadarRenderer(animator, viewPortHandler) {
    private val mShadowBuffers = FloatArray(8)
    private val mBodyBuffers = FloatArray(4)
    private val mRangeBuffers = FloatArray(4)
    private val mOpenBuffers = FloatArray(4)
    private val mCloseBuffers = FloatArray(4)

    override fun initBuffers() {
    }

    override fun drawData(c: Canvas) {
        val candleData = mChart.candleData

        for (set in candleData.dataSets) {
            if (set.isVisible) drawDataSet(c, set)
        }
    }

    protected fun drawDataSet(c: Canvas, dataSet: ICandleDataSet) {
        val trans = mChart.getTransformer(dataSet.axisDependency)

        val phaseY = mAnimator.phaseY
        val barSpace = dataSet.barSpace
        val showCandleBar = dataSet.showCandleBar

        mXBounds[mChart] = dataSet

        paintRender.strokeWidth = dataSet.shadowWidth

        // draw the body
        for (j in mXBounds.min..mXBounds.range + mXBounds.min) {
            // get the entry

            val e = dataSet.getEntryForIndex(j) ?: continue

            val xPos = e.x

            val open = e.open
            val close = e.close
            val high = e.high
            val low = e.low

            if (showCandleBar) {
                // calculate the shadow

                mShadowBuffers[0] = xPos
                mShadowBuffers[2] = xPos
                mShadowBuffers[4] = xPos
                mShadowBuffers[6] = xPos

                if (open > close) {
                    mShadowBuffers[1] = high * phaseY
                    mShadowBuffers[3] = open * phaseY
                    mShadowBuffers[5] = low * phaseY
                    mShadowBuffers[7] = close * phaseY
                } else if (open < close) {
                    mShadowBuffers[1] = high * phaseY
                    mShadowBuffers[3] = close * phaseY
                    mShadowBuffers[5] = low * phaseY
                    mShadowBuffers[7] = open * phaseY
                } else {
                    mShadowBuffers[1] = high * phaseY
                    mShadowBuffers[3] = open * phaseY
                    mShadowBuffers[5] = low * phaseY
                    mShadowBuffers[7] = mShadowBuffers[3]
                }

                trans!!.pointValuesToPixel(mShadowBuffers)

                // draw the shadows
                if (dataSet.shadowColorSameAsCandle) {
                    if (open > close) paintRender.color =
                        if (dataSet.decreasingColor == ColorTemplate.COLOR_NONE) dataSet.getColor(j) else dataSet.decreasingColor
                    else if (open < close) paintRender.color =
                        if (dataSet.increasingColor == ColorTemplate.COLOR_NONE) dataSet.getColor(j) else dataSet.increasingColor
                    else paintRender.color = if (dataSet.neutralColor == ColorTemplate.COLOR_NONE) dataSet.getColor(j) else dataSet.neutralColor
                } else {
                    paintRender.color = if (dataSet.shadowColor == ColorTemplate.COLOR_NONE) dataSet.getColor(j) else dataSet.shadowColor
                }

                paintRender.style = Paint.Style.STROKE

                c.drawLines(mShadowBuffers, paintRender)

                // calculate the body
                mBodyBuffers[0] = xPos - 0.5f + barSpace
                mBodyBuffers[1] = close * phaseY
                mBodyBuffers[2] = (xPos + 0.5f - barSpace)
                mBodyBuffers[3] = open * phaseY

                trans.pointValuesToPixel(mBodyBuffers)

                // draw body differently for increasing and decreasing entry
                if (open > close) { // decreasing

                    if (dataSet.decreasingColor == ColorTemplate.COLOR_NONE) {
                        paintRender.color = dataSet.getColor(j)
                    } else {
                        paintRender.color = dataSet.decreasingColor
                    }

                    paintRender.style = dataSet.decreasingPaintStyle

                    c.drawRect(
                        mBodyBuffers[0], mBodyBuffers[3],
                        mBodyBuffers[2], mBodyBuffers[1],
                        paintRender
                    )
                } else if (open < close) {
                    if (dataSet.increasingColor == ColorTemplate.COLOR_NONE) {
                        paintRender.color = dataSet.getColor(j)
                    } else {
                        paintRender.color = dataSet.increasingColor
                    }

                    paintRender.style = dataSet.increasingPaintStyle

                    c.drawRect(
                        mBodyBuffers[0], mBodyBuffers[1],
                        mBodyBuffers[2], mBodyBuffers[3],
                        paintRender
                    )
                } else { // equal values

                    if (dataSet.neutralColor == ColorTemplate.COLOR_NONE) {
                        paintRender.color = dataSet.getColor(j)
                    } else {
                        paintRender.color = dataSet.neutralColor
                    }

                    c.drawLine(
                        mBodyBuffers[0], mBodyBuffers[1],
                        mBodyBuffers[2], mBodyBuffers[3],
                        paintRender
                    )
                }
            } else {
                mRangeBuffers[0] = xPos
                mRangeBuffers[1] = high * phaseY
                mRangeBuffers[2] = xPos
                mRangeBuffers[3] = low * phaseY

                mOpenBuffers[0] = xPos - 0.5f + barSpace
                mOpenBuffers[1] = open * phaseY
                mOpenBuffers[2] = xPos
                mOpenBuffers[3] = open * phaseY

                mCloseBuffers[0] = xPos + 0.5f - barSpace
                mCloseBuffers[1] = close * phaseY
                mCloseBuffers[2] = xPos
                mCloseBuffers[3] = close * phaseY

                trans!!.pointValuesToPixel(mRangeBuffers)
                trans.pointValuesToPixel(mOpenBuffers)
                trans.pointValuesToPixel(mCloseBuffers)

                // draw the ranges
                val barColor = if (open > close) if (dataSet.decreasingColor == ColorTemplate.COLOR_NONE)
                    dataSet.getColor(j)
                else
                    dataSet.decreasingColor
                else if (open < close) if (dataSet.increasingColor == ColorTemplate.COLOR_NONE)
                    dataSet.getColor(j)
                else
                    dataSet.increasingColor
                else if (dataSet.neutralColor == ColorTemplate.COLOR_NONE)
                    dataSet.getColor(j)
                else
                    dataSet.neutralColor

                paintRender.color = barColor
                c.drawLine(
                    mRangeBuffers[0], mRangeBuffers[1],
                    mRangeBuffers[2], mRangeBuffers[3],
                    paintRender
                )
                c.drawLine(
                    mOpenBuffers[0], mOpenBuffers[1],
                    mOpenBuffers[2], mOpenBuffers[3],
                    paintRender
                )
                c.drawLine(
                    mCloseBuffers[0], mCloseBuffers[1],
                    mCloseBuffers[2], mCloseBuffers[3],
                    paintRender
                )
            }
        }
    }

    override fun drawValues(c: Canvas) {
        // if values are drawn

        if (isDrawingValuesAllowed(mChart)) {
            val dataSets = mChart.candleData.dataSets

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

                val trans = mChart.getTransformer(dataSet.axisDependency)

                mXBounds[mChart] = dataSet

                val positions = trans!!.generateTransformedValuesCandle(
                    dataSet, mAnimator.phaseX, mAnimator.phaseY, mXBounds.min, mXBounds.max
                )

                val yOffset = Utils.convertDpToPixel(5f)

                val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)

                var j = 0
                while (j < positions.size) {
                    val x = positions[j]
                    val y = positions[j + 1]

                    if (!viewPortHandler.isInBoundsRight(x)) break

                    if (!viewPortHandler.isInBoundsLeft(x) || !viewPortHandler.isInBoundsY(y)) {
                        j += 2
                        continue
                    }

                    val entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min)

                    if (dataSet.isDrawValuesEnabled) {
                        drawValue(
                            c,
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

                    if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                        val icon = entry.icon

                        Utils.drawImage(
                            c,
                            icon,
                            (x + iconsOffset.x).toInt(),
                            (y + iconsOffset.y).toInt(),
                            icon!!.intrinsicWidth,
                            icon.intrinsicHeight
                        )
                    }
                    j += 2
                }

                MPPointF.recycleInstance(iconsOffset)
            }
        }
    }

    override fun drawExtras(c: Canvas) {
    }

    override fun drawHighlighted(c: Canvas, indices: Array<Highlight>) {
        val candleData = mChart.candleData

        for (high in indices) {
            val set = candleData.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled) continue

            val e = set.getEntryForXValue(high.x, high.y)

            if (!isInBoundsX(e, set)) continue

            val lowValue = e.low * mAnimator.phaseY
            val highValue = e.high * mAnimator.phaseY
            val y = (lowValue + highValue) / 2f

            val pix = mChart.getTransformer(set.axisDependency)!!.getPixelForValues(e.x, y)

            high.setDraw(pix.x.toFloat(), pix.y.toFloat())

            // draw the lines
            drawHighlightLines(c, pix.x.toFloat(), pix.y.toFloat(), set)
        }
    }
}
