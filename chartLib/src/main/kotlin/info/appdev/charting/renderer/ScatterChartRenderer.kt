package info.appdev.charting.renderer

import android.graphics.Canvas
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.ScatterDataProvider
import info.appdev.charting.interfaces.datasets.IScatterDataSet
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.Utils
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel
import timber.log.Timber
import kotlin.math.ceil
import kotlin.math.min

open class ScatterChartRenderer(
    var dataProvider: ScatterDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineScatterCandleRadarRenderer(animator, viewPortHandler) {
    override fun initBuffers() = Unit

    override fun drawData(canvas: Canvas) {
        val scatterData = dataProvider.scatterData

        scatterData?.let {
            for (set in it.dataSets!!) {
                if (set.isVisible)
                    drawDataSet(canvas, set)
            }
        }
    }

    var pixelBuffer: FloatArray = FloatArray(2)

    protected fun drawDataSet(canvas: Canvas, dataSet: IScatterDataSet) {
        if (dataSet.entryCount < 1)
            return

        val viewPortHandler = this.viewPortHandler

        val trans = dataProvider.getTransformer(dataSet.axisDependency)

        val phaseY = animator.phaseY

        val renderer = dataSet.shapeRenderer
        if (renderer == null) {
            Timber.i("There's no IShapeRenderer specified for ScatterDataSet")
            return
        }

        val max = min(
            ceil((dataSet.entryCount.toFloat() * animator.phaseX).toDouble()),
            dataSet.entryCount.toFloat().toDouble()
        ).toInt()

        for (i in 0..<max) {
            dataSet.getEntryForIndex(i)?.let { entry ->

                pixelBuffer[0] = entry.x
                pixelBuffer[1] = entry.y * phaseY

                trans!!.pointValuesToPixel(pixelBuffer)

                if (!viewPortHandler.isInBoundsRight(pixelBuffer[0]))
                    break

                if (!viewPortHandler.isInBoundsLeft(pixelBuffer[0]) || !viewPortHandler.isInBoundsY(pixelBuffer[1]))
                    continue

                paintRender.color = dataSet.getColorByIndex(i / 2)
                renderer.renderShape(
                    canvas, dataSet, this.viewPortHandler,
                    pixelBuffer[0], pixelBuffer[1],
                    paintRender
                )
            }
        }
    }

    override fun drawValues(canvas: Canvas) {
        if (isDrawingValuesAllowed(dataProvider)) {
            dataProvider.scatterData?.let { scatterData ->
                for (i in 0..<scatterData.dataSetCount) {
                    val dataSet = scatterData.dataSets!![i]

                    if (dataSet.entryCount == 0) {
                        continue
                    }
                    if (!shouldDrawValues(dataSet) || dataSet.entryCount < 1) {
                        continue
                    }

                    // apply the text-styling defined by the DataSet
                    applyValueTextStyle(dataSet)

                    xBounds.set(dataProvider, dataSet)

                    val positions = dataProvider.getTransformer(dataSet.axisDependency)!!.generateTransformedValuesScatter(
                        dataSet,
                        animator.phaseX, animator.phaseY, xBounds.min, xBounds.max
                    )

                    val shapeSize = dataSet.scatterShapeSize.convertDpToPixel()

                    val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                    iconsOffset.x = iconsOffset.x.convertDpToPixel()
                    iconsOffset.y = iconsOffset.y.convertDpToPixel()

                    var j = 0
                    while (j < positions.size) {
                        if (!viewPortHandler.isInBoundsRight(positions[j]))
                            break

                        // make sure the lines don't do shitty things outside bounds
                        if ((!viewPortHandler.isInBoundsLeft(positions[j])
                                    || !viewPortHandler.isInBoundsY(positions[j + 1]))
                        ) {
                            j += 2
                            continue
                        }

                        dataSet.getEntryForIndex(j / 2 + xBounds.min)?.let { entry ->

                            if (dataSet.isDrawValues) {
                                drawValue(
                                    canvas,
                                    dataSet.valueFormatter,
                                    entry.y,
                                    entry,
                                    i,
                                    positions[j],
                                    positions[j + 1] - shapeSize,
                                    dataSet.getValueTextColor(j / 2 + xBounds.min)
                                )
                            }

                            if (entry.icon != null && dataSet.isDrawIcons) {
                                val icon = entry.icon

                                icon?.let {
                                    Utils.drawImage(
                                        canvas,
                                        it,
                                        (positions[j] + iconsOffset.x).toInt(),
                                        (positions[j + 1] + iconsOffset.y).toInt()
                                    )
                                }
                            }
                        }
                        j += 2
                    }

                    PointF.recycleInstance(iconsOffset)
                }
            }
        }
    }

    override fun drawExtras(canvas: Canvas) = Unit

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val scatterData = dataProvider.scatterData

        for (high in indices) {
            val set = scatterData?.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled)
                continue

            set.getEntryForXValue(high.x, high.y)?.let { entry ->

                if (!isInBoundsX(entry, set)) continue

                val pix = dataProvider.getTransformer(set.axisDependency)!!.getPixelForValues(
                    entry.x, entry.y * animator
                        .phaseY
                )

                high.setDraw(pix.x.toFloat(), pix.y.toFloat())

                // draw the lines
                drawHighlightLines(canvas, pix.x.toFloat(), pix.y.toFloat(), set)
            }
        }
    }
}
