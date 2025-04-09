package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

open class RadarChartRenderer(
    protected var mChart: RadarChart, animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : LineRadarRenderer(animator, viewPortHandler) {
    /**
     * paint for drawing the web
     */
    var webPaint: Paint
        protected set
    protected var mHighlightCirclePaint: Paint

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val previousPath = Path()
    private val innerArea = Path()
    private val temp = Path()


    override fun initBuffers() {
        // TODO Auto-generated method stub
    }

    override fun drawData(c: Canvas) {
        val radarData = mChart.data

        val mostEntries = radarData!!.maxEntryCountSet.entryCount

        for (set in radarData.dataSets) {
            if (set.isVisible) {
                drawDataSet(c, set, mostEntries)
            }
        }
    }

    protected var mDrawDataSetSurfacePathBuffer: Path = Path()

    /**
     * Draws the RadarDataSet
     *
     * @param c
     * @param dataSet
     * @param mostEntries the entry count of the dataset with the most entries
     */
    protected fun drawDataSet(c: Canvas, dataSet: IRadarDataSet, mostEntries: Int) {
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        val sliceangle = mChart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = mChart.factor

        val center = mChart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)
        val surface = mDrawDataSetSurfacePathBuffer
        surface.reset()

        var hasMovedToPoint = false

        for (j in 0..<dataSet.entryCount) {
            paintRender.color = dataSet.getColor(j)

            val e = dataSet.getEntryForIndex(j)

            Utils.getPosition(
                center,
                (e.y - mChart.yChartMin) * factor * phaseY,
                sliceangle * j * phaseX + mChart.rotationAngle, pOut
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
                drawFilledPath(c, surface, drawable)
            } else {
                drawFilledPath(c, surface, dataSet.fillColor, dataSet.fillAlpha)
            }
        }

        paintRender.strokeWidth = dataSet.lineWidth
        paintRender.style = Paint.Style.STROKE

        // draw the line (only if filled is disabled or alpha is below 255)
        if (!dataSet.isDrawFilledEnabled || dataSet.fillAlpha < 255) c.drawPath(surface, paintRender)

        MPPointF.recycleInstance(center)
        MPPointF.recycleInstance(pOut)
    }

    override fun drawValues(c: Canvas) {
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        val sliceangle = mChart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = mChart.factor

        val center = mChart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)
        val pIcon = MPPointF.getInstance(0f, 0f)

        val yoffset = Utils.convertDpToPixel(5f)

        for (i in 0..<mChart.data!!.dataSetCount) {
            val dataSet = mChart.data!!.getDataSetByIndex(i)
            if (dataSet.entryCount == 0) {
                continue
            }
            if (!shouldDrawValues(dataSet)) {
                continue
            }

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet)

            val iconsOffset = MPPointF.getInstance(dataSet.iconsOffset)
            iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x)
            iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y)

            for (j in 0..<dataSet.entryCount) {
                val entry = dataSet.getEntryForIndex(j)

                Utils.getPosition(
                    center,
                    (entry.y - mChart.yChartMin) * factor * phaseY,
                    sliceangle * j * phaseX + mChart.rotationAngle,
                    pOut
                )

                if (dataSet.isDrawValuesEnabled) {
                    drawValue(
                        c,
                        dataSet.valueFormatter,
                        entry.y,
                        entry,
                        i,
                        pOut.x,
                        pOut.y - yoffset,
                        dataSet.getValueTextColor(j)
                    )
                }

                if (entry.icon != null && dataSet.isDrawIconsEnabled) {
                    val icon = entry.icon

                    Utils.getPosition(
                        center,
                        (entry.y) * factor * phaseY + iconsOffset.y,
                        sliceangle * j * phaseX + mChart.rotationAngle,
                        pIcon
                    )

                    pIcon.y += iconsOffset.x

                    Utils.drawImage(
                        c,
                        icon,
                        pIcon.x.toInt(),
                        pIcon.y.toInt(),
                        icon!!.intrinsicWidth,
                        icon.intrinsicHeight
                    )
                }
            }

            MPPointF.recycleInstance(iconsOffset)
        }

        MPPointF.recycleInstance(center)
        MPPointF.recycleInstance(pOut)
        MPPointF.recycleInstance(pIcon)
    }

    override fun drawExtras(c: Canvas) {
        drawWeb(c)
    }

    protected fun drawWeb(c: Canvas) {
        val sliceangle = mChart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = mChart.factor
        val rotationangle = mChart.rotationAngle

        val center = mChart.centerOffsets

        // draw the web lines that come from the center
        webPaint.strokeWidth = mChart.webLineWidth
        webPaint.color = mChart.webColor
        webPaint.alpha = mChart.webAlpha

        val xIncrements = 1 + mChart.skipWebLineCount
        val maxEntryCount = mChart.data!!.maxEntryCountSet.entryCount

        val p = MPPointF.getInstance(0f, 0f)
        var i = 0
        while (i < maxEntryCount) {
            Utils.getPosition(
                center,
                mChart.yRange * factor,
                sliceangle * i + rotationangle,
                p
            )

            c.drawLine(center.x, center.y, p.x, p.y, webPaint)
            i += xIncrements
        }
        MPPointF.recycleInstance(p)

        // draw the inner-web
        webPaint.strokeWidth = mChart.webLineWidthInner
        webPaint.color = mChart.webColorInner
        webPaint.alpha = mChart.webAlpha

        val labelCount = mChart.yAxis.mEntryCount

        val p1out = MPPointF.getInstance(0f, 0f)
        val p2out = MPPointF.getInstance(0f, 0f)
        for (j in 0..<labelCount) {
            if (mChart.isCustomLayerColorEnable) {
                innerArea.rewind()
                paint.color = mChart.layerColorList[j]
            }
            for (i in 0..<mChart.data!!.entryCount) {
                val r = (mChart.yAxis.mEntries[j] - mChart.yChartMin) * factor

                Utils.getPosition(center, r, sliceangle * i + rotationangle, p1out)
                Utils.getPosition(center, r, sliceangle * (i + 1) + rotationangle, p2out)

                c.drawLine(p1out.x, p1out.y, p2out.x, p2out.y, webPaint)
                if (mChart.isCustomLayerColorEnable) {
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
            if (mChart.isCustomLayerColorEnable) {
                temp.set(innerArea)
                if (!innerArea.isEmpty) {
                    val result = innerArea.op(previousPath, Path.Op.DIFFERENCE)
                    if (result) {
                        c.drawPath(innerArea, paint)
                    }
                }
                previousPath.set(temp)
            }
        }
        MPPointF.recycleInstance(p1out)
        MPPointF.recycleInstance(p2out)
    }

    override fun drawHighlighted(c: Canvas, indices: Array<Highlight>) {
        val sliceangle = mChart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = mChart.factor

        val center = mChart.centerOffsets
        val pOut = MPPointF.getInstance(0f, 0f)

        val radarData = mChart.data

        for (high in indices) {
            val set = radarData!!.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled) continue

            val e = set.getEntryForIndex(high.x.toInt())

            if (!isInBoundsX(e, set)) continue

            val y = (e.y - mChart.yChartMin)

            Utils.getPosition(
                center,
                y * factor * mAnimator.phaseY,
                sliceangle * high.x * mAnimator.phaseX + mChart.rotationAngle,
                pOut
            )

            high.setDraw(pOut.x, pOut.y)

            // draw the lines
            drawHighlightLines(c, pOut.x, pOut.y, set)

            if (set.isDrawHighlightCircleEnabled) {
                if (!java.lang.Float.isNaN(pOut.x) && !java.lang.Float.isNaN(pOut.y)) {
                    var strokeColor = set.highlightCircleStrokeColor
                    if (strokeColor == ColorTemplate.COLOR_NONE) {
                        strokeColor = set.getColor(0)
                    }

                    if (set.highlightCircleStrokeAlpha < 255) {
                        strokeColor = ColorTemplate.colorWithAlpha(strokeColor, set.highlightCircleStrokeAlpha)
                    }

                    drawHighlightCircle(
                        c,
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

        mHighlightCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    fun drawHighlightCircle(
        c: Canvas,
        point: MPPointF,
        innerRadius: Float,
        outerRadius: Float,
        fillColor: Int,
        strokeColor: Int,
        strokeWidth: Float
    ) {
        var innerRadiusLocal = innerRadius
        var outerRadiusLocal = outerRadius
        c.save()

        outerRadiusLocal = Utils.convertDpToPixel(outerRadiusLocal)
        innerRadiusLocal = Utils.convertDpToPixel(innerRadiusLocal)

        if (fillColor != ColorTemplate.COLOR_NONE) {
            val p = mDrawHighlightCirclePathBuffer
            p.reset()
            p.addCircle(point.x, point.y, outerRadiusLocal, Path.Direction.CW)
            if (innerRadiusLocal > 0f) {
                p.addCircle(point.x, point.y, innerRadiusLocal, Path.Direction.CCW)
            }
            mHighlightCirclePaint.color = fillColor
            mHighlightCirclePaint.style = Paint.Style.FILL
            c.drawPath(p, mHighlightCirclePaint)
        }

        if (strokeColor != ColorTemplate.COLOR_NONE) {
            mHighlightCirclePaint.color = strokeColor
            mHighlightCirclePaint.style = Paint.Style.STROKE
            mHighlightCirclePaint.strokeWidth = Utils.convertDpToPixel(strokeWidth)
            c.drawCircle(point.x, point.y, outerRadiusLocal, mHighlightCirclePaint)
        }

        c.restore()
    }
}
