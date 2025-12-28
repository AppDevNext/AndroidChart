package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel
import kotlin.math.min

class RoundedBarChartRenderer(
    dataProvider: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(dataProvider, animator, viewPortHandler) {
    private val mBarShadowRectBuffer = RectF()
    private val radius = 20f
    var roundedShadowRadius = 0f
    var roundedPositiveDataSetRadius = 0f
    var roundedNegativeDataSetRadius = 0f

    override fun drawDataSet(canvas: Canvas, dataSet: IBarDataSet, index: Int) {
        initBuffers()
        val trans = dataProvider.getTransformer(dataSet.axisDependency)
        barBorderPaint.color = dataSet.barBorderColor
        barBorderPaint.strokeWidth = dataSet.barBorderWidth.convertDpToPixel()
        shadowPaint.color = dataSet.barShadowColor
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        if (dataProvider.isDrawBarShadowEnabled) {
            shadowPaint.color = dataSet.barShadowColor
            dataProvider.barData?.let { barData ->
                val barWidth = barData.barWidth
                val barWidthHalf = barWidth / 2.0f
                var x: Float
                var i = 0
                val count = min((dataSet.entryCount.toFloat() * phaseX).toDouble().toInt().toDouble(), dataSet.entryCount.toDouble())
                while (i < count) {
                    dataSet.getEntryForIndex(i)?.let { barEntry ->
                        x = barEntry.x
                        mBarShadowRectBuffer.left = x - barWidthHalf
                        mBarShadowRectBuffer.right = x + barWidthHalf
                    }
                    trans!!.rectValueToPixel(mBarShadowRectBuffer)
                    if (!viewPortHandler.isInBoundsLeft(mBarShadowRectBuffer.right)) {
                        i++
                        continue
                    }
                    if (!viewPortHandler.isInBoundsRight(mBarShadowRectBuffer.left)) {
                        break
                    }
                    mBarShadowRectBuffer.top = viewPortHandler.contentTop()
                    mBarShadowRectBuffer.bottom = viewPortHandler.contentBottom()


                    if (roundedShadowRadius > 0) {
                        canvas.drawRoundRect(barRect, roundedShadowRadius, roundedShadowRadius, shadowPaint)
                    } else {
                        canvas.drawRect(mBarShadowRectBuffer, shadowPaint)
                    }
                    i++
                }

            }

            val buffer = barBuffers[index]!!
            buffer.setPhases(phaseX, phaseY)
            buffer.setDataSet(index)
            buffer.inverted = dataProvider.isInverted(dataSet.axisDependency)
            dataProvider.barData?.let { buffer.barWidth = it.barWidth }
            buffer.feed(dataSet)
            trans!!.pointValuesToPixel(buffer.buffer)

            // if multiple colors has been assigned to Bar Chart
            dataSet.colors.let {
                if (it.size > 1) {
                    var j = 0
                    while (j < buffer.size()) {
                        if (!viewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                            j += 4
                            continue
                        }

                        if (!viewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                            break
                        }

                        if (dataProvider.isDrawBarShadowEnabled) {
                            if (roundedShadowRadius > 0) {
                                canvas.drawRoundRect(
                                    RectF(
                                        buffer.buffer[j], viewPortHandler.contentTop(),
                                        buffer.buffer[j + 2],
                                        viewPortHandler.contentBottom()
                                    ), roundedShadowRadius, roundedShadowRadius, shadowPaint
                                )
                            } else {
                                canvas.drawRect(
                                    buffer.buffer[j], viewPortHandler.contentTop(),
                                    buffer.buffer[j + 2],
                                    viewPortHandler.contentBottom(), shadowPaint
                                )
                            }
                        }

                        // Set the color for the currently drawn value. If the index
                        paintRender.color = dataSet.getColorByIndex(j / 4)

                        if (roundedPositiveDataSetRadius > 0) {
                            canvas.drawRoundRect(
                                RectF(
                                    buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                    buffer.buffer[j + 3]
                                ), roundedPositiveDataSetRadius, roundedPositiveDataSetRadius, paintRender
                            )
                        } else {
                            canvas.drawRect(
                                buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                buffer.buffer[j + 3], paintRender
                            )
                        }
                        j += 4
                    }
                } else {
                    paintRender.color = dataSet.color

                    var j = 0
                    while (j < buffer.size()) {
                        if (!viewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                            j += 4
                            continue
                        }

                        if (!viewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                            break
                        }

                        if (dataProvider.isDrawBarShadowEnabled) {
                            if (roundedShadowRadius > 0) {
                                canvas.drawRoundRect(
                                    RectF(
                                        buffer.buffer[j], viewPortHandler.contentTop(),
                                        buffer.buffer[j + 2],
                                        viewPortHandler.contentBottom()
                                    ), roundedShadowRadius, roundedShadowRadius, shadowPaint
                                )
                            } else {
                                canvas.drawRect(
                                    buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                    buffer.buffer[j + 3], paintRender
                                )
                            }
                        }

                        if (roundedPositiveDataSetRadius > 0) {
                            canvas.drawRoundRect(
                                RectF(
                                    buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                    buffer.buffer[j + 3]
                                ), roundedPositiveDataSetRadius, roundedPositiveDataSetRadius, paintRender
                            )
                        } else {
                            canvas.drawRect(
                                buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                buffer.buffer[j + 3], paintRender
                            )
                        }
                        j += 4
                    }
                }
            }

            val isSingleColor = dataSet.colors.size == 1
            if (isSingleColor) {
                paintRender.color = dataSet.getColorByIndex(index)
            }

            var j = 0
            while (j < buffer.size()) {
                if (!viewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                    j += 4
                    continue
                }

                if (!viewPortHandler.isInBoundsRight(buffer.buffer[j])) {
                    break
                }

                if (!isSingleColor) {
                    paintRender.color = dataSet.getColorByIndex(j / 4)
                }

                paintRender.shader = LinearGradient(
                    buffer.buffer[j],
                    buffer.buffer[j + 3],
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    dataSet.getColorByIndex(j / 4),
                    dataSet.getColorByIndex(j / 4),
                    Shader.TileMode.MIRROR
                )

                paintRender.shader = LinearGradient(
                    buffer.buffer[j],
                    buffer.buffer[j + 3],
                    buffer.buffer[j],
                    buffer.buffer[j + 1],
                    dataSet.getColorByIndex(j / 4),
                    dataSet.getColorByIndex(j / 4),
                    Shader.TileMode.MIRROR
                )

                dataSet.getEntryForIndex(j / 4)?.let { barEntry ->

                    if ((barEntry.y < 0 && roundedNegativeDataSetRadius > 0)) {
                        val path2 = roundRect(
                            RectF(
                                buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                buffer.buffer[j + 3]
                            ), roundedNegativeDataSetRadius, roundedNegativeDataSetRadius, tl = true, tr = true, br = true, bl = true
                        )
                        canvas.drawPath(path2, paintRender)
                    } else if ((barEntry.y > 0 && roundedPositiveDataSetRadius > 0)) {
                        val path2 = roundRect(
                            RectF(
                                buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                buffer.buffer[j + 3]
                            ), roundedPositiveDataSetRadius, roundedPositiveDataSetRadius, tl = true, tr = true, br = true, bl = true
                        )
                        canvas.drawPath(path2, paintRender)
                    } else {
                        canvas.drawRect(
                            buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                            buffer.buffer[j + 3], paintRender
                        )
                    }
                }
                j += 4
            }
        }
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        dataProvider.barData?.let { barData ->

            for (high in indices) {
                val set = barData.getDataSetByIndex(high.dataSetIndex)

                if (set == null || !set.isHighlightEnabled)
                    continue


                set.getEntryForXValue(high.x, high.y)?.let { barEntry ->

                    if (!isInBoundsX(barEntry, set)) {
                        continue
                    }

                    val trans = dataProvider.getTransformer(set.axisDependency)

                    paintHighlight.color = set.highLightColor
                    paintHighlight.alpha = set.highLightAlpha

                    val isStack = high.stackIndex >= 0 && barEntry.isStacked

                    val y1: Float
                    val y2: Float

                    if (isStack) {
                        if (dataProvider.isHighlightFullBarEnabled) {
                            y1 = barEntry.positiveSum
                            y2 = -barEntry.negativeSum
                        } else {
                            val range = barEntry.ranges[high.stackIndex]

                            y1 = range.from
                            y2 = range.to
                        }
                    } else {
                        y1 = barEntry.y
                        y2 = 0f
                    }

                    prepareBarHighlight(barEntry.x, y1, y2, barData.barWidth / 2f, trans!!)

                    setHighlightDrawPos(high, barRect)

                    val path2 = roundRect(
                        RectF(
                            barRect.left, barRect.top, barRect.right,
                            barRect.bottom
                        ), radius, radius, tl = true, tr = true, br = true, bl = true
                    )

                    canvas.drawPath(path2, paintHighlight)
                }
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun roundRect(rect: RectF, rx: Float, ry: Float, tl: Boolean, tr: Boolean, br: Boolean, bl: Boolean): Path {
        var rx = rx
        var ry = ry
        val top = rect.top
        val left = rect.left
        val right = rect.right
        val bottom = rect.bottom
        val path = Path()
        if (rx < 0) {
            rx = 0f
        }
        if (ry < 0) {
            ry = 0f
        }
        val width = right - left
        val height = bottom - top
        if (rx > width / 2) {
            rx = width / 2
        }
        if (ry > height / 2) {
            ry = height / 2
        }
        val widthMinusCorners = (width - (2 * rx))
        val heightMinusCorners = (height - (2 * ry))

        path.moveTo(right, top + ry)
        if (tr) {
            path.rQuadTo(0f, -ry, -rx, -ry) //top-right corner
        } else {
            path.rLineTo(0f, -ry)
            path.rLineTo(-rx, 0f)
        }
        path.rLineTo(-widthMinusCorners, 0f)
        if (tl) {
            path.rQuadTo(-rx, 0f, -rx, ry) //top-left corner
        } else {
            path.rLineTo(-rx, 0f)
            path.rLineTo(0f, ry)
        }
        path.rLineTo(0f, heightMinusCorners)

        if (bl) {
            path.rQuadTo(0f, ry, rx, ry) //bottom-left corner
        } else {
            path.rLineTo(0f, ry)
            path.rLineTo(rx, 0f)
        }

        path.rLineTo(widthMinusCorners, 0f)
        if (br) path.rQuadTo(rx, 0f, rx, -ry) //bottom-right corner
        else {
            path.rLineTo(rx, 0f)
            path.rLineTo(0f, -ry)
        }

        path.rLineTo(0f, -heightMinusCorners)
        path.close()
        return path
    }
}
