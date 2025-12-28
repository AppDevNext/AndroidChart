package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.BubbleDataProvider
import info.appdev.charting.interfaces.datasets.IBubbleDataSet
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.Utils
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.calcTextHeight
import info.appdev.charting.utils.convertDpToPixel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Suppress("MemberVisibilityCanBePrivate")
open class BubbleChartRenderer(
    var dataProvider: BubbleDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarLineScatterCandleBubbleRenderer(animator, viewPortHandler) {

    override fun initBuffers() = Unit

    override fun drawData(canvas: Canvas) {
        val bubbleData = dataProvider.bubbleData

        bubbleData?.dataSets?.forEach { set ->
            if (set.isVisible)
                drawDataSet(canvas, set)
        }
    }

    private val sizeBuffer = FloatArray(4)
    private val pointBuffer = FloatArray(2)

    protected fun getShapeSize(entrySize: Float, maxSize: Float, reference: Float, normalizeSize: Boolean): Float {
        val factor = if (normalizeSize) (if (maxSize == 0f) 1f else sqrt((entrySize / maxSize).toDouble()).toFloat()) else entrySize
        val shapeSize = reference * factor
        return shapeSize
    }

    protected fun drawDataSet(canvas: Canvas, dataSet: IBubbleDataSet) {
        if (dataSet.entryCount < 1)
            return

        val trans = dataProvider.getTransformer(dataSet.axisDependency)

        val phaseY = animator.phaseY

        xBounds.set(dataProvider, dataSet)

        sizeBuffer[0] = 0f
        sizeBuffer[2] = 1f

        trans!!.pointValuesToPixel(sizeBuffer)

        val normalizeSize = dataSet.isNormalizeSizeEnabled

        // calculate the full width of 1 step on the x-axis
        val maxBubbleWidth = abs((sizeBuffer[2] - sizeBuffer[0]).toDouble()).toFloat()
        val maxBubbleHeight = abs((viewPortHandler.contentBottom() - viewPortHandler.contentTop()).toDouble()).toFloat()
        val referenceSize = min(maxBubbleHeight.toDouble(), maxBubbleWidth.toDouble()).toFloat()

        for (j in xBounds.min..xBounds.range + xBounds.min) {
            val bubbleEntry = dataSet.getEntryForIndex(j)
            bubbleEntry?.let {
                pointBuffer[0] = it.x
                pointBuffer[1] = (it.y) * phaseY
                trans.pointValuesToPixel(pointBuffer)

                val shapeHalf = getShapeSize(it.size, dataSet.maxSize, referenceSize, normalizeSize) / 2f

                if (!viewPortHandler.isInBoundsTop(pointBuffer[1] + shapeHalf)
                    || !viewPortHandler.isInBoundsBottom(pointBuffer[1] - shapeHalf)
                ) continue

                if (!viewPortHandler.isInBoundsLeft(pointBuffer[0] + shapeHalf)) continue

                if (!viewPortHandler.isInBoundsRight(pointBuffer[0] - shapeHalf)) break

                val color = dataSet.getColorByIndex(j)

                paintRender.color = color
                canvas.drawCircle(pointBuffer[0], pointBuffer[1], shapeHalf, paintRender)
            }
        }
    }

    override fun drawValues(canvas: Canvas) {
        val bubbleData = dataProvider.bubbleData ?: return

        // if values are drawn
        if (isDrawingValuesAllowed(dataProvider)) {
            val dataSets = bubbleData.dataSets

            val lineHeight = paintValues.calcTextHeight("1").toFloat()

            dataSets?.let {
                for (i in it.indices) {
                    val dataSet = it[i]
                    if (dataSet.entryCount == 0) {
                        continue
                    }
                    if (!shouldDrawValues(dataSet) || dataSet.entryCount < 1) {
                        continue
                    }

                    // apply the text-styling defined by the DataSet
                    applyValueTextStyle(dataSet)

                    val phaseX = max(0.0, min(1.0, animator.phaseX.toDouble())).toFloat()
                    val phaseY = animator.phaseY

                    xBounds.set(dataProvider, dataSet)

                    dataProvider.getTransformer(dataSet.axisDependency)?.let { transformer ->
                        val positions = transformer.generateTransformedValuesBubble(dataSet, phaseY, xBounds.min, xBounds.max)

                        val alpha = if (phaseX == 1f)
                            phaseY
                        else
                            phaseX

                        val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                        iconsOffset.x = iconsOffset.x.convertDpToPixel()
                        iconsOffset.y = iconsOffset.y.convertDpToPixel()

                        var j = 0
                        while (j < positions.size) {
                            var valueTextColor = dataSet.getValueTextColor(j / 2 + xBounds.min)
                            valueTextColor = Color.argb(
                                (255f * alpha).roundToInt(), Color.red(valueTextColor),
                                Color.green(valueTextColor), Color.blue(valueTextColor)
                            )

                            val x = positions[j]
                            val y = positions[j + 1]

                            if (!viewPortHandler.isInBoundsRight(x)) break

                            if ((!viewPortHandler.isInBoundsLeft(x) || !viewPortHandler.isInBoundsY(y))) {
                                j += 2
                                continue
                            }

                            val bubbleEntry = dataSet.getEntryForIndex(j / 2 + xBounds.min)
                            bubbleEntry?.let {
                                if (dataSet.isDrawValues) {
                                    drawValue(
                                        canvas, dataSet.valueFormatter, bubbleEntry.size, bubbleEntry, i, x,
                                        y + (0.5f * lineHeight), valueTextColor
                                    )
                                }

                                if (bubbleEntry.icon != null && dataSet.isDrawIcons) {
                                    val icon = bubbleEntry.icon

                                    icon?.let { ico ->
                                        Utils.drawImage(
                                            canvas,
                                            ico,
                                            (x + iconsOffset.x).toInt(),
                                            (y + iconsOffset.y).toInt()
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
    }

    override fun drawExtras(canvas: Canvas) {
    }

    private val _hsvBuffer = FloatArray(3)

    init {
        paintRender.style = Paint.Style.FILL

        paintHighlight.style = Paint.Style.STROKE
        paintHighlight.strokeWidth = 1.5f.convertDpToPixel()
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val bubbleData = dataProvider.bubbleData

        val phaseY = animator.phaseY

        for (high in indices) {
            val set = bubbleData?.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled)
                continue

            val bubbleEntry = set.getEntryForXValue(high.x, high.y)!!

            if (bubbleEntry.y != high.y)
                continue

            if (!isInBoundsX(bubbleEntry, set))
                continue

            val trans = dataProvider.getTransformer(set.axisDependency)

            sizeBuffer[0] = 0f
            sizeBuffer[2] = 1f

            trans!!.pointValuesToPixel(sizeBuffer)

            val normalizeSize = set.isNormalizeSizeEnabled

            // calculate the full width of 1 step on the x-axis
            val maxBubbleWidth = abs((sizeBuffer[2] - sizeBuffer[0]).toDouble()).toFloat()
            val maxBubbleHeight = abs((viewPortHandler.contentBottom() - viewPortHandler.contentTop()).toDouble()).toFloat()
            val referenceSize = min(maxBubbleHeight.toDouble(), maxBubbleWidth.toDouble()).toFloat()

            pointBuffer[0] = bubbleEntry.x
            pointBuffer[1] = (bubbleEntry.y) * phaseY
            trans.pointValuesToPixel(pointBuffer)

            high.setDraw(pointBuffer[0], pointBuffer[1])

            val shapeHalf = getShapeSize(
                bubbleEntry.size,
                set.maxSize,
                referenceSize,
                normalizeSize
            ) / 2f

            if (!viewPortHandler.isInBoundsTop(pointBuffer[1] + shapeHalf)
                || !viewPortHandler.isInBoundsBottom(pointBuffer[1] - shapeHalf)
            ) continue

            if (!viewPortHandler.isInBoundsLeft(pointBuffer[0] + shapeHalf))
                continue

            if (!viewPortHandler.isInBoundsRight(pointBuffer[0] - shapeHalf))
                break

            val originalColor = set.getColorByIndex(bubbleEntry.x.toInt())

            Color.RGBToHSV(
                Color.red(originalColor), Color.green(originalColor),
                Color.blue(originalColor), _hsvBuffer
            )
            _hsvBuffer[2] *= 0.5f
            val color = Color.HSVToColor(Color.alpha(originalColor), _hsvBuffer)

            paintHighlight.color = color
            paintHighlight.strokeWidth = set.highlightCircleWidth
            canvas.drawCircle(pointBuffer[0], pointBuffer[1], shapeHalf, paintHighlight)
        }
    }
}
