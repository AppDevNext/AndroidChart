package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.withSave
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.charts.RadarChart
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IRadarDataSet
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel
import info.appdev.charting.utils.getPosition
import info.appdev.charting.utils.drawImage

open class RadarChartRenderer(
    protected var chart: RadarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineRadarRenderer(animator, viewPortHandler) {
    var webPaint: Paint
        protected set
    protected var highlightCirclePaint: Paint

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val previousPath = Path()
    private val innerAreaPath = Path()
    private val tempPath = Path()


    override fun initBuffers() = Unit

    override fun drawData(canvas: Canvas) {
        chart.getData()?.let { radarData ->

            val mostEntries = radarData.maxEntryCountSet?.entryCount ?: 0

            radarData.dataSets?.forEach { set ->
                if (set.isVisible) {
                    drawDataSet(canvas, set, mostEntries)
                }
            }
        }
    }

    protected var drawDataSetSurfacePathBuffer: Path = Path()

    /**
     * Draws the RadarDataSet
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
        var pOut = PointF.getInstance(0f, 0f)
        val surface = drawDataSetSurfacePathBuffer
        surface.reset()

        var hasMovedToPoint = false

        for (j in 0..<dataSet.entryCount) {
            paintRender.color = dataSet.getColorByIndex(j)

            dataSet.getEntryForIndex(j)?.let { e ->

                pOut = center.getPosition(
                    (e.y - chart.yChartMin) * factor * phaseY,
                    sliceAngle * j * phaseX + chart.rotationAngle
                )
            }
            if (pOut.x.isNaN())
                continue

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

        PointF.recycleInstance(center)
        PointF.recycleInstance(pOut)
    }

    override fun drawValues(canvas: Canvas) {
        val phaseX = animator.phaseX
        val phaseY = animator.phaseY

        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor

        val center = chart.centerOffsets
        var pOut = PointF.getInstance(0f, 0f)
        var pIcon = PointF.getInstance(0f, 0f)

        val yOffset = 5f.convertDpToPixel()

        for (i in 0..<chart.getData()!!.dataSetCount) {
            chart.getData()!!.getDataSetByIndex(i)?.let { dataSet ->

                chart.getData()!!.getDataSetByIndex(i)
                if (dataSet.entryCount == 0) {
                    continue
                }
                if (!shouldDrawValues(dataSet)) {
                    continue
                }

                // apply the text-styling defined by the DataSet
                applyValueTextStyle(dataSet)

                val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                iconsOffset.x = iconsOffset.x.convertDpToPixel()
                iconsOffset.y = iconsOffset.y.convertDpToPixel()

                for (j in 0..<dataSet.entryCount) {
                    dataSet.getEntryForIndex(j)?.let { entry ->

                        pOut = center.getPosition(
                            (entry.y - chart.yChartMin) * factor * phaseY,
                            sliceAngle * j * phaseX + chart.rotationAngle
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

                            pIcon = center.getPosition(
                                (entry.y) * factor * phaseY + iconsOffset.y,
                                sliceAngle * j * phaseX + chart.rotationAngle
                            )

                            pIcon.y += iconsOffset.x

                            icon?.let {
                                canvas.drawImage(
                                    it,
                                    pIcon.x.toInt(),
                                    pIcon.y.toInt()
                                )
                            }
                        }
                    }
                }

                PointF.recycleInstance(iconsOffset)
            }
        }

        PointF.recycleInstance(center)
        PointF.recycleInstance(pOut)
        PointF.recycleInstance(pIcon)
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
        val maxEntryCount = chart.getData()!!.maxEntryCountSet?.entryCount ?: 0

        var p = PointF.getInstance(0f, 0f)
        var i = 0
        while (i < maxEntryCount) {
            p = center.getPosition(
                chart.yRange * factor,
                sliceAngle * i + rotationAngle
            )

            canvas.drawLine(center.x, center.y, p.x, p.y, webPaint)
            i += xIncrements
        }
        PointF.recycleInstance(p)

        // draw the inner-web
        webPaint.strokeWidth = chart.webLineWidthInner
        webPaint.color = chart.webColorInner
        webPaint.alpha = chart.webAlpha

        val labelCount = chart.yAxis.entryCount

        var p1out = PointF.getInstance(0f, 0f)
        var p2out = PointF.getInstance(0f, 0f)
        for (j in 0..<labelCount) {
            if (chart.isCustomLayerColorEnable) {
                innerAreaPath.rewind()
                paint.color = chart.layerColorList[j]
            }
            for (i in 0..<chart.getData()!!.entryCount) {
                val r = (chart.yAxis.entries[j] - chart.yChartMin) * factor

                p1out = center.getPosition(r, sliceAngle * i + rotationAngle)
                p2out = center.getPosition(r, sliceAngle * (i + 1) + rotationAngle)

                canvas.drawLine(p1out.x, p1out.y, p2out.x, p2out.y, webPaint)
                if (chart.isCustomLayerColorEnable) {
                    if (p1out.x != p2out.x) {
                        if (i == 0) {
                            innerAreaPath.moveTo(p1out.x, p1out.y)
                        } else {
                            innerAreaPath.lineTo(p1out.x, p1out.y)
                        }
                        innerAreaPath.lineTo(p2out.x, p2out.y)
                    }
                }
            }
            if (chart.isCustomLayerColorEnable) {
                tempPath.set(innerAreaPath)
                if (!innerAreaPath.isEmpty) {
                    val result = innerAreaPath.op(previousPath, Path.Op.DIFFERENCE)
                    if (result) {
                        canvas.drawPath(innerAreaPath, paint)
                    }
                }
                previousPath.set(tempPath)
            }
        }
        PointF.recycleInstance(p1out)
        PointF.recycleInstance(p2out)
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val sliceAngle = chart.sliceAngle

        // calculate the factor that is needed for transforming the value to
        // pixels
        val factor = chart.factor

        val center = chart.centerOffsets
        var pOut = PointF.getInstance(0f, 0f)

        val radarData = chart.getData()

        for (high in indices) {
            val set = radarData!!.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled)
                continue

            set.getEntryForIndex(high.x.toInt())?.let { radarEntry ->
                high.y = radarEntry.y

                if (!isInBoundsX(radarEntry, set)) continue

                val y = (radarEntry.y - chart.yChartMin)

                pOut = center.getPosition(
                    y * factor * animator.phaseY,
                    sliceAngle * high.x * animator.phaseX + chart.rotationAngle
                )
            }
            high.setDraw(pOut.x, pOut.y)

            // draw the lines
            drawHighlightLines(canvas, pOut.x, pOut.y, set)

            if (set.isDrawHighlightCircleEnabled) {
                if (!pOut.x.isNaN() && !pOut.y.isNaN()) {
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

        PointF.recycleInstance(center)
        PointF.recycleInstance(pOut)
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
        point: PointF,
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
