package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.withSave
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.utils.convertDpToPixel

open class RadarChartRenderer(
    protected var chart: RadarChart, animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineRadarRenderer(animator, viewPortHandler) {
    var webPaint: Paint
        protected set
    protected var highlightCirclePaint: Paint

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val previousPath = Path()
    private val innerArea = Path()
    private val temp = Path()


    override fun initBuffers() = Unit

    override fun drawData(canvas: Canvas) {
        val radarData = chart.data

        val mostEntries = radarData!!.maxEntryCountSet.entryCount

        for (set in radarData.dataSets) {
            if (set.isVisible) {
                drawDataSet(canvas, set, mostEntries)
            }
        }
    }

    protected var drawDataSetSurfacePathBuffer: Path = Path()

    /**
     * Draws the RadarDataSet
     *
     * @param canvas
     * @param dataSet
     * @param mostEntries the entry count of the dataset with the most entries
     */
    protected fun drawDataSet(canvas: Canvas, dataSet: IRadarDataSet, mostEntries: Int) {
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor

        val center = chart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)
        val surface = drawDataSetSurfacePathBuffer
        surface.reset()

        var hasMovedToPoint = false

        for (j in 0..<dataSet.entryCount) {
            paintRender.color = dataSet.getColorByIndex(j)

            val e = dataSet.getEntryForIndex(j)

            Utils.getPosition(
                center,
                (e.y - chart.yChartMin) * factor * phaseY,
                sliceAngle * j * phaseX + chart.rotationAngle, pOut
            )

            if (java.lang.Float.isNaN(pOut.x)) continue

            if (!hasMovedToPoint) {
                surface.moveTo(pOut.x, pOut.y)
                hasMovedToPoint = true
            } else surface.lineTo(pOut.x, pOut.y)
        }

        if (dataSet.entryCount > mostEntries) {
            // if this is not the largest set, draw a line to the center before closing
            surface.lineTo(center.x, center.y)
        }

        surface.close()

        if (dataSet.isDrawFilledEnabled) {
            val drawable = dataSet.fillDrawable
            if (drawable != null) {
                drawFilledPath(canvas, surface, drawable)
            } else {
                drawFilledPath(canvas, surface, dataSet.fillColor, dataSet.fillAlpha)
            }
        }

        paintRender.strokeWidth = dataSet.lineWidth
        paintRender.style = Paint.Style.STROKE

        // draw the line (only if filled is disabled or alpha is below 255)
        if (!dataSet.isDrawFilledEnabled || dataSet.fillAlpha < 255) canvas.drawPath(surface, paintRender)

        MPPointF.recycleInstance(center)
        MPPointF.recycleInstance(pOut)
    }

    override fun drawValues(canvas: Canvas) {
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor

        val center = chart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)
        val pIcon = MPPointF.getInstance(0f, 0f)

        val yOffset = 5f.convertDpToPixel()

        for (i in 0..<chart.data!!.dataSetCount) {
            val dataSet = chart.data!!.getDataSetByIndex(i)
            if (dataSet.entryCount == 0) {
                continue
            }
            if (!shouldDrawValues(dataSet)) {
                continue
            }

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet)

            val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
            iconsOffset.x = iconsOffset.x.convertDpToPixel()
            iconsOffset.y = iconsOffset.y.convertDpToPixel()

            for (j in 0..<dataSet.entryCount) {
                val entry = dataSet.getEntryForIndex(j)

                Utils.getPosition(
                    center,
                    (entry.y - chart.yChartMin) * factor * phaseY,
                    sliceAngle * j * phaseX + chart.rotationAngle,
                    pOut
                )

                if (dataSet.isDrawValues) {
                    drawValue(
                        canvas,
                        dataSet.valueFormatter,
                        entry.y,
                        entry,
                        i,
                        pOut.x,
                        pOut.y - yOffset,
                        dataSet.getValueTextColor(j)
                    )
                }

                if (entry.icon != null && dataSet.isDrawIcons) {
                    val icon = entry.icon

                    Utils.getPosition(
                        center,
                        (entry.y) * factor * phaseY + iconsOffset.y,
                        sliceAngle * j * phaseX + chart.rotationAngle,
                        pIcon
                    )

                    pIcon.y += iconsOffset.x

                    icon?.let {
                        Utils.drawImage(
                            canvas,
                            it,
                            pIcon.x.toInt(),
                            pIcon.y.toInt()
                        )
                    }
                }
            }

            MPPointF.recycleInstance(iconsOffset)
        }

        MPPointF.recycleInstance(center)
        MPPointF.recycleInstance(pOut)
        MPPointF.recycleInstance(pIcon)
    }

    override fun drawExtras(canvas: Canvas) {
        drawWeb(canvas)
    }

    protected fun drawWeb(canvas: Canvas) {
        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor
        val rotationAngle = chart.rotationAngle

        val center = chart.centerOffsets

        // draw the web lines that come from the center
        webPaint.strokeWidth = chart.webLineWidth
        webPaint.color = chart.webColor
        webPaint.alpha = chart.webAlpha

        val xIncrements = 1 + chart.skipWebLineCount
        val maxEntryCount = chart.data!!.maxEntryCountSet.entryCount

        val p = MPPointF.getInstance(0f, 0f)
        var i = 0
        while (i < maxEntryCount) {
            Utils.getPosition(
                center,
                chart.yRange * factor,
                sliceAngle * i + rotationAngle,
                p
            )

            canvas.drawLine(center.x, center.y, p.x, p.y, webPaint)
            i += xIncrements
        }
        MPPointF.recycleInstance(p)

        // draw the inner-web
        webPaint.strokeWidth = chart.webLineWidthInner
        webPaint.color = chart.webColorInner
        webPaint.alpha = chart.webAlpha

        val labelCount = chart.yAxis.mEntryCount

        val p1out = MPPointF.getInstance(0f, 0f)
        val p2out = MPPointF.getInstance(0f, 0f)
        for (j in 0..<labelCount) {
            if (chart.isCustomLayerColorEnable) {
                innerArea.rewind()
                paint.color = chart.layerColorList[j]
            }
            for (i in 0..<chart.data!!.entryCount) {
                val r = (chart.yAxis.mEntries[j] - chart.yChartMin) * factor

                Utils.getPosition(center, r, sliceAngle * i + rotationAngle, p1out)
                Utils.getPosition(center, r, sliceAngle * (i + 1) + rotationAngle, p2out)

                canvas.drawLine(p1out.x, p1out.y, p2out.x, p2out.y, webPaint)
                if (chart.isCustomLayerColorEnable) {
                    if (p1out.x != p2out.x) {
                        if (i == 0) {
                            innerArea.moveTo(p1out.x, p1out.y)
                        } else {
                            innerArea.lineTo(p1out.x, p1out.y)
                        }
                        innerArea.lineTo(p2out.x, p2out.y)
                    }
                }
            }
            if (chart.isCustomLayerColorEnable) {
                temp.set(innerArea)
                if (!innerArea.isEmpty) {
                    val result = innerArea.op(previousPath, Path.Op.DIFFERENCE)
                    if (result) {
                        canvas.drawPath(innerArea, paint)
                    }
                }
                previousPath.set(temp)
            }
        }
        MPPointF.recycleInstance(p1out)
        MPPointF.recycleInstance(p2out)
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor

        val center = chart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)

        val radarData = chart.data

        for (high in indices) {
            val set = radarData!!.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled) continue

            val radarEntry = set.getEntryForIndex(high.x.toInt())

            if (!isInBoundsX(radarEntry, set)) continue

            val y = (radarEntry.y - chart.yChartMin)

            Utils.getPosition(
                center,
                y * factor * animator.phaseY,
                sliceAngle * high.x * animator.phaseX + chart.rotationAngle,
                pOut
            )

            high.setDraw(pOut.x, pOut.y)

            // draw the lines
            drawHighlightLines(canvas, pOut.x, pOut.y, set)

            if (set.isDrawHighlightCircleEnabled) {
                if (!java.lang.Float.isNaN(pOut.x) && !java.lang.Float.isNaN(pOut.y)) {
                    var strokeColor = set.highlightCircleStrokeColor
                    if (strokeColor == ColorTemplate.COLOR_NONE) {
                        strokeColor = set.getColorByIndex(0)
                    }

                    if (set.highlightCircleStrokeAlpha < 255) {
                        strokeColor = ColorTemplate.colorWithAlpha(strokeColor, set.highlightCircleStrokeAlpha)
                    }

                    drawHighlightCircle(
                        canvas,
                        pOut,
                        set.highlightCircleInnerRadius,
                        set.highlightCircleOuterRadius,
                        set.highlightCircleFillColor,
                        strokeColor,
                        set.highlightCircleStrokeWidth
                    )
                }
            }
        }

        MPPointF.recycleInstance(center)
        MPPointF.recycleInstance(pOut)
    }

    protected var mDrawHighlightCirclePathBuffer: Path = Path()

    init {
        paintHighlight = Paint(Paint.ANTI_ALIAS_FLAG)
        paintHighlight.style = Paint.Style.STROKE
        paintHighlight.strokeWidth = 2f
        paintHighlight.color = Color.rgb(255, 187, 115)

        paint.style = Paint.Style.FILL
        paint.strokeWidth = 2f
        paint.color = Color.RED

        webPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        webPaint.style = Paint.Style.STROKE

        highlightCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    fun drawHighlightCircle(
        canvas: Canvas,
        point: MPPointF,
        innerRadius: Float,
        outerRadius: Float,
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Float
    ) {
        var innerRadiusLocal = innerRadius
        var outerRadiusLocal = outerRadius
        canvas.withSave {
            outerRadiusLocal = outerRadiusLocal.convertDpToPixel()
            innerRadiusLocal = innerRadiusLocal.convertDpToPixel()

            if (fillColor != ColorTemplate.COLOR_NONE) {
                val p = mDrawHighlightCirclePathBuffer
                p.reset()
                p.addCircle(point.x, point.y, outerRadiusLocal, Path.Direction.CW)
                if (innerRadiusLocal > 0f) {
                    p.addCircle(point.x, point.y, innerRadiusLocal, Path.Direction.CCW)
                }
                highlightCirclePaint.color = fillColor
                highlightCirclePaint.style = Paint.Style.FILL
                drawPath(p, highlightCirclePaint)
            }

            if (strokeColor != ColorTemplate.COLOR_NONE) {
                highlightCirclePaint.color = strokeColor
                highlightCirclePaint.style = Paint.Style.STROKE
                highlightCirclePaint.strokeWidth = strokeWidth.convertDpToPixel()
                drawCircle(point.x, point.y, outerRadiusLocal, highlightCirclePaint)
            }

        }
    }
}
