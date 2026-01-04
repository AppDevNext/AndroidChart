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
import kotlin.math.abs
import kotlin.math.min

@Suppress("unused")
class RoundedBarChartRenderer(
    dataProvider: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(dataProvider, animator, viewPortHandler) {

    private val shadowRect = RectF()
    private val tmpPts = FloatArray(4)
    private val ovalPath = Path()

    private val defaultRadius = 20f

    private var roundedShadowRadius = 0f
    var roundedPositiveDataSetRadius = 0f
    var roundedNegativeDataSetRadius = 0f
    /** If true, corner radii = half the bar’s screen‐pixel width at draw‐time. */
    var useAutoFullRadius = false

    override fun drawDataSet(canvas: Canvas, dataSet: IBarDataSet, index: Int) {
        initBuffers()
        val transformer = dataProvider.getTransformer(dataSet.axisDependency) ?: return
        val handler = viewPortHandler

        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        dataProvider.barData?.let { barData ->
            // 1) auto‐radius?
            if (useAutoFullRadius) {
                val halfVal = barData.barWidth / 2f
                tmpPts[0] = 0f; tmpPts[1] = 0f
                tmpPts[2] = halfVal; tmpPts[3] = 0f
                transformer.pointValuesToPixel(tmpPts)
                val pxHalf = abs(tmpPts[2] - tmpPts[0])
                roundedShadowRadius = pxHalf
                roundedPositiveDataSetRadius = pxHalf
                roundedNegativeDataSetRadius = pxHalf
            }

            // 2) prep paints
            barBorderPaint.color = dataSet.barBorderColor
            barBorderPaint.strokeWidth = dataSet.barBorderWidth.convertDpToPixel()
            shadowPaint.color = dataSet.barShadowColor

            // 3) draw shadows
            if (dataProvider.isDrawBarShadowEnabled) {
                val barWidth = barData.barWidth
                val half = barWidth / 2f
                val count = min((dataSet.entryCount * phaseX).toInt(), dataSet.entryCount)
                for (i in 0 until count) {
                    dataSet.getEntryForIndex(i)?.let { e ->
                        val x = e.x
                        shadowRect.left = x - half
                        shadowRect.right = x + half
                        transformer.rectValueToPixel(shadowRect)

                        if (!handler.isInBoundsLeft(shadowRect.right) ||
                            !handler.isInBoundsRight(shadowRect.left)
                        ) return@let

                        shadowRect.top = handler.contentTop()
                        shadowRect.bottom = handler.contentBottom()

                        if (roundedShadowRadius > 0f) {
                            canvas.drawRoundRect(shadowRect, roundedShadowRadius, roundedShadowRadius, shadowPaint)
                        } else {
                            canvas.drawRect(shadowRect, shadowPaint)
                        }
                    }
                }
            }

            // 4) feed & transform
            val buffer = barBuffers[index] ?: return
            buffer.setPhases(phaseX, phaseY)
            buffer.setDataSet(index)
            buffer.inverted = dataProvider.isInverted(dataSet.axisDependency)
            buffer.barWidth = barData.barWidth
            buffer.feed(dataSet)
            transformer.pointValuesToPixel(buffer.buffer)

            val singleColor = dataSet.colors.size == 1

            // 5a) multi‐color bars
            if (!singleColor) {
                var j = 0
                while (j < buffer.size()) {
                    val left = buffer.buffer[j]
                    val top = buffer.buffer[j + 1]
                    val right = buffer.buffer[j + 2]
                    val bottom = buffer.buffer[j + 3]

                    if (!handler.isInBoundsLeft(right)) {
                        j += 4; continue
                    }
                    // if bar is off‐right, we're past visible, so stop
                    if (!handler.isInBoundsRight(left)) break

                    // shadow
                    if (dataProvider.isDrawBarShadowEnabled && roundedShadowRadius > 0f) {
                        ovalPath.reset()
                        ovalPath.addRoundRect(
                            RectF(left, handler.contentTop(), right, handler.contentBottom()),
                            roundedShadowRadius, roundedShadowRadius, Path.Direction.CW
                        )
                        canvas.drawPath(ovalPath, shadowPaint)
                    }

                    paintRender.color = dataSet.getColorByIndex(j / 4)
                    if (roundedPositiveDataSetRadius > 0f) {
                        ovalPath.reset()
                        ovalPath.addRoundRect(
                            RectF(left, top, right, bottom),
                            roundedPositiveDataSetRadius,
                            roundedPositiveDataSetRadius,
                            Path.Direction.CW
                        )
                        canvas.drawPath(ovalPath, paintRender)
                    } else {
                        canvas.drawRect(left, top, right, bottom, paintRender)
                    }
                    j += 4
                }
            }
            // 5b) single‐color bars
            else {
                paintRender.color = dataSet.color
                var j = 0
                while (j < buffer.size()) {
                    val left = buffer.buffer[j]
                    val top = buffer.buffer[j + 1]
                    val right = buffer.buffer[j + 2]
                    val bottom = buffer.buffer[j + 3]

                    if (!handler.isInBoundsLeft(right)) {
                        j += 4; continue
                    }
                    if (!handler.isInBoundsRight(left)) break

                    if (dataProvider.isDrawBarShadowEnabled && roundedShadowRadius > 0f) {
                        ovalPath.reset()
                        ovalPath.addRoundRect(
                            RectF(left, handler.contentTop(), right, handler.contentBottom()),
                            roundedShadowRadius, roundedShadowRadius, Path.Direction.CW
                        )
                        canvas.drawPath(ovalPath, shadowPaint)
                    }

                    if (roundedPositiveDataSetRadius > 0f) {
                        ovalPath.reset()
                        ovalPath.addRoundRect(
                            RectF(left, top, right, bottom),
                            roundedPositiveDataSetRadius,
                            roundedPositiveDataSetRadius,
                            Path.Direction.CW
                        )
                        canvas.drawPath(ovalPath, paintRender)
                    } else {
                        canvas.drawRect(left, top, right, bottom, paintRender)
                    }
                    j += 4
                }
            }

            // 6) gradient overlay
            var j = 0
            if (singleColor) paintRender.color = dataSet.getColorByIndex(index)
            while (j < buffer.size()) {
                val left = buffer.buffer[j]
                val top = buffer.buffer[j + 1]
                val right = buffer.buffer[j + 2]
                val bottom = buffer.buffer[j + 3]

                if (!handler.isInBoundsLeft(right)) {
                    j += 4; continue
                }
                if (!handler.isInBoundsRight(left)) break

                if (!singleColor) paintRender.color = dataSet.getColorByIndex(j / 4)
                paintRender.shader = LinearGradient(
                    left, bottom, left, top,
                    paintRender.color, paintRender.color,
                    Shader.TileMode.MIRROR
                )

                val entryY = dataSet.getEntryForIndex(j / 4)?.y ?: 0f
                val radius = if (entryY < 0f) roundedNegativeDataSetRadius else roundedPositiveDataSetRadius
                if (radius > 0f) {
                    ovalPath.reset()
                    ovalPath.addRoundRect(
                        RectF(left, top, right, bottom),
                        radius, radius, Path.Direction.CW
                    )
                    canvas.drawPath(ovalPath, paintRender)
                } else {
                    canvas.drawRect(left, top, right, bottom, paintRender)
                }
                j += 4
            }
        }
        paintRender.shader = null
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        // 1) early exits
        val handler = viewPortHandler
        val barData = dataProvider.barData

        for (h in indices) {
            // 2) only highlight enabled sets
            val set = barData?.getDataSetByIndex(h.dataSetIndex) ?: continue
            if (!set.isHighlightEnabled) continue

            // 3) find the matching Entry
            val e = set.getEntryForXValue(h.x, h.y) ?: continue
            if (!isInBoundsX(e, set)) continue

            // 4) compute the y‐range of the highlight (stack vs. normal)
            val isStack = h.stackIndex >= 0 && e.isStacked
            val (y1, y2) = if (isStack) {
                if (dataProvider.isHighlightFullBarEnabled) {
                    e.positiveSum to -e.negativeSum
                } else {
                    val range = e.ranges[h.stackIndex]
                    range.from to range.to
                }
            } else {
                e.y to 0f
            }

            // 5) transform values to pixel‐rect
            val trans = dataProvider.getTransformer(set.axisDependency) ?: continue
            prepareBarHighlight(
                e.x,
                y1,
                y2,
                barData.barWidth / 2f,
                trans
            )

            // 5b) record the center/top into the Highlight object so markers can be drawn
            setHighlightDrawPos(h, barRect)

            // 6) clip any highlights that are fully off‐screen
            if (!handler.isInBoundsLeft(barRect.right) ||
                !handler.isInBoundsRight(barRect.left) ||
                !handler.isInBoundsTop(barRect.bottom) ||
                !handler.isInBoundsBottom(barRect.top)
            ) {
                continue
            }

            // 7) choose corner radius
            val radius = if (useAutoFullRadius) {
                abs((barRect.right - barRect.left) / 2f)
            } else {
                defaultRadius
            }

            // 8) draw your rounded highlight
            paintHighlight.color = set.highLightColor
            paintHighlight.alpha = set.highLightAlpha
            canvas.drawRoundRect(barRect, radius, radius, paintHighlight)
        }
    }
}
