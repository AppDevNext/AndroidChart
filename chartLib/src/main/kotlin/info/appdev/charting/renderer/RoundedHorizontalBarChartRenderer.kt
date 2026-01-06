package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel
import kotlin.math.min

class RoundedHorizontalBarChartRenderer(
    dataProvider: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : HorizontalBarChartRenderer(dataProvider, animator, viewPortHandler) {
    private val mBarShadowRectBuffer = RectF()
    var roundedShadowRadius = 0f
    var roundedPositiveDataSetRadius = 0f
    var roundedNegativeDataSetRadius = 0f

    init {
        paintValues.textAlign = Paint.Align.LEFT
    }

    override fun drawDataSet(canvas: Canvas, dataSet: IBarDataSet, index: Int) {
        initBuffers()
        val trans = dataProvider.getTransformer(dataSet.axisDependency)
        barBorderPaint.color = dataSet.barBorderColor
        barBorderPaint.strokeWidth = dataSet.barBorderWidth.convertDpToPixel()
        shadowPaint.color = dataSet.barShadowColor
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        if (dataProvider.isDrawBarShadow) {
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
                        mBarShadowRectBuffer.top = x - barWidthHalf
                        mBarShadowRectBuffer.bottom = x + barWidthHalf
                    }
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
            if (dataSet.colors.size > 1) {
                var j = 0
                while (j < buffer.size()) {
                    if (!viewPortHandler.isInBoundsTop(buffer.buffer[j + 3])) {
                        j += 4
                        continue
                    }

                    if (!viewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
                        break
                    }

                    if (dataProvider.isDrawBarShadow) {
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
                    if (!viewPortHandler.isInBoundsTop(buffer.buffer[j + 3])) {
                        j += 4
                        continue
                    }

                    if (!viewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
                        break
                    }

                    if (dataProvider.isDrawBarShadow) {
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

            val isSingleColor = dataSet.colors.size == 1
            if (isSingleColor) {
                paintRender.color = dataSet.getColorByIndex(index)
            }

            var j = 0
            while (j < buffer.size()) {
                if (!viewPortHandler.isInBoundsTop(buffer.buffer[j + 3])) {
                    j += 4
                    continue
                }

                if (!viewPortHandler.isInBoundsBottom(buffer.buffer[j + 1])) {
                    break
                }

                if (!isSingleColor) {
                    paintRender.color = dataSet.getColorByIndex(j / 4)
                }

                dataSet.getEntryForIndex(j / 4)?.let { barEntry ->
                    if ((barEntry.y < 0 && roundedNegativeDataSetRadius > 0)) {
                        val path2 = roundRect(
                            RectF(
                                buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
                                buffer.buffer[j + 3]
                            ), roundedNegativeDataSetRadius, roundedNegativeDataSetRadius, true, tr = true, br = true, bl = true
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
        if (br) {
            path.rQuadTo(rx, 0f, rx, -ry) //bottom-right corner
        } else {
            path.rLineTo(rx, 0f)
            path.rLineTo(0f, -ry)
        }

        path.rLineTo(0f, -heightMinusCorners)
        path.close()
        return path
    }
}
