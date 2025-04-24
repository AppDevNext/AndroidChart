package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.min

class RoundedBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val shadowRect = RectF()
    private val tmpPts     = FloatArray(4)
    private val ovalPath   = Path()

    private val defaultRadius = 20f

    private var roundedShadowRadius          = 0f
    private var roundedPositiveDataSetRadius = 0f
    private var roundedNegativeDataSetRadius = 0f
    private var useAutoFullRadius            = false

    /** If true, corner radii = half the bar’s screen‐pixel width at draw‐time. */
    fun setUseAutoFullRadius(useAuto: Boolean) {
        useAutoFullRadius = useAuto
    }

    fun setRoundedShadowRadius(r: Float) {
        roundedShadowRadius = r
    }

    fun setRoundedPositiveDataSetRadius(r: Float) {
        roundedPositiveDataSetRadius = r
    }

    fun setRoundedNegativeDataSetRadius(r: Float) {
        roundedNegativeDataSetRadius = r
    }

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        initBuffers()
        val trans    = chart.getTransformer(dataSet.axisDependency) ?: return
        val handler  = viewPortHandler

        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        // 1) auto‐radius?
        if (useAutoFullRadius) {
            val halfVal = chart.barData.barWidth / 2f
            tmpPts[0] = 0f; tmpPts[1] = 0f
            tmpPts[2] = halfVal; tmpPts[3] = 0f
            trans.pointValuesToPixel(tmpPts)
            val pxHalf = abs(tmpPts[2] - tmpPts[0])
            roundedShadowRadius          = pxHalf
            roundedPositiveDataSetRadius = pxHalf
            roundedNegativeDataSetRadius = pxHalf
        }

        // 2) prep paints
        barBorderPaint.color       = dataSet.barBorderColor
        barBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)
        shadowPaint.color          = dataSet.barShadowColor

        // 3) draw shadows
        if (chart.isDrawBarShadowEnabled) {
            val barWidth = chart.barData.barWidth
            val half     = barWidth / 2f
            val count    = min((dataSet.entryCount * phaseX).toInt(), dataSet.entryCount)
            for (i in 0 until count) {
                dataSet.getEntryForIndex(i)?.let { e ->
                    val x = e.x
                    shadowRect.left  = x - half
                    shadowRect.right = x + half
                    trans.rectValueToPixel(shadowRect)

                    if (!handler.isInBoundsLeft(shadowRect.right) ||
                        !handler.isInBoundsRight(shadowRect.left)) return@let

                    shadowRect.top    = handler.contentTop()
                    shadowRect.bottom = handler.contentBottom()

                    if (roundedShadowRadius > 0f) {
                        c.drawRoundRect(shadowRect, roundedShadowRadius, roundedShadowRadius, shadowPaint)
                    } else {
                        c.drawRect(shadowRect, shadowPaint)
                    }
                }
            }
        }

        // 4) feed & transform
        val buffer = barBuffers[index] ?: return
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(chart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(chart.barData.barWidth)
        buffer.feed(dataSet)
        trans.pointValuesToPixel(buffer.buffer)

        val singleColor = dataSet.colors.size == 1

        // 5a) multi‐color bars
        if (!singleColor) {
            var j = 0
            while (j < buffer.size()) {
                val left   = buffer.buffer[j]
                val top    = buffer.buffer[j + 1]
                val right  = buffer.buffer[j + 2]
                val bottom = buffer.buffer[j + 3]

                if (!handler.isInBoundsLeft(right)) { j += 4; continue }
                // if bar is off‐right, we're past visible, so stop
                if (!handler.isInBoundsRight(left)) break

                // shadow
                if (chart.isDrawBarShadowEnabled && roundedShadowRadius > 0f) {
                    ovalPath.reset()
                    ovalPath.addRoundRect(
                        RectF(left, handler.contentTop(), right, handler.contentBottom()),
                        roundedShadowRadius, roundedShadowRadius, Path.Direction.CW
                    )
                    c.drawPath(ovalPath, shadowPaint)
                }

                paintRender.color = dataSet.getColor(j / 4)
                if (roundedPositiveDataSetRadius > 0f) {
                    ovalPath.reset()
                    ovalPath.addRoundRect(
                        RectF(left, top, right, bottom),
                        roundedPositiveDataSetRadius,
                        roundedPositiveDataSetRadius,
                        Path.Direction.CW
                    )
                    c.drawPath(ovalPath, paintRender)
                } else {
                    c.drawRect(left, top, right, bottom, paintRender)
                }
                j += 4
            }
        }
        // 5b) single‐color bars
        else {
            paintRender.color = dataSet.color
            var j = 0
            while (j < buffer.size()) {
                val left   = buffer.buffer[j]
                val top    = buffer.buffer[j + 1]
                val right  = buffer.buffer[j + 2]
                val bottom = buffer.buffer[j + 3]

                if (!handler.isInBoundsLeft(right)) { j += 4; continue }
                if (!handler.isInBoundsRight(left)) break

                if (chart.isDrawBarShadowEnabled && roundedShadowRadius > 0f) {
                    ovalPath.reset()
                    ovalPath.addRoundRect(
                        RectF(left, handler.contentTop(), right, handler.contentBottom()),
                        roundedShadowRadius, roundedShadowRadius, Path.Direction.CW
                    )
                    c.drawPath(ovalPath, shadowPaint)
                }

                if (roundedPositiveDataSetRadius > 0f) {
                    ovalPath.reset()
                    ovalPath.addRoundRect(
                        RectF(left, top, right, bottom),
                        roundedPositiveDataSetRadius,
                        roundedPositiveDataSetRadius,
                        Path.Direction.CW
                    )
                    c.drawPath(ovalPath, paintRender)
                } else {
                    c.drawRect(left, top, right, bottom, paintRender)
                }
                j += 4
            }
        }

        // 6) gradient overlay
        var j = 0
        if (singleColor) paintRender.color = dataSet.getColor(index)
        while (j < buffer.size()) {
            val left   = buffer.buffer[j]
            val top    = buffer.buffer[j + 1]
            val right  = buffer.buffer[j + 2]
            val bottom = buffer.buffer[j + 3]

            if (!handler.isInBoundsLeft(right)) { j += 4; continue }
            if (!handler.isInBoundsRight(left)) break

            if (!singleColor) paintRender.color = dataSet.getColor(j / 4)
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
                c.drawPath(ovalPath, paintRender)
            } else {
                c.drawRect(left, top, right, bottom, paintRender)
            }
            j += 4
        }
        paintRender.shader = null
    }

    override fun drawHighlighted(c: Canvas, indices: Array<Highlight>) {
        // 1) early exits
        val handler = viewPortHandler
        val barData = chart.barData

        for (h in indices) {
            // 2) only highlight enabled sets
            val set = barData.getDataSetByIndex(h.dataSetIndex) ?: continue
            if (!set.isHighlightEnabled) continue

            // 3) find the matching Entry
            val e = set.getEntryForXValue(h.x, h.y) ?: continue
            if (!isInBoundsX(e, set)) continue

            // 4) compute the y‐range of the highlight (stack vs. normal)
            val isStack = h.stackIndex >= 0 && e.isStacked
            val (y1, y2) = if (isStack) {
                if (chart.isHighlightFullBarEnabled) {
                    e.positiveSum to -e.negativeSum
                } else {
                    val range = e.ranges[h.stackIndex]
                    range.from to range.to
                }
            } else {
                e.y to 0f
            }

            // 5) transform values to pixel‐rect
            val trans = chart.getTransformer(set.axisDependency) ?: continue
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
            c.drawRoundRect(barRect, radius, radius, paintHighlight)
        }
    }
}
