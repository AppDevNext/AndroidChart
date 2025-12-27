package info.appdev.charting.renderer

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.Utils
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

open class LineChartRenderer(
    var dataProvider: LineDataProvider, animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineRadarRenderer(animator, viewPortHandler) {
    /**
     * paint for the inner circle of the value indicators
     */
    protected var circlePaintInner: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lineBuffer = FloatArray(4)

    /**
     * Bitmap object used for drawing the paths (otherwise they are too long if
     * rendered directly on the canvas)
     */
    protected var drawBitmap: WeakReference<Bitmap>? = null

    /**
     * on this canvas, the paths are rendered, it is initialized with the
     * pathBitmap
     */
    protected var bitmapCanvas: Canvas? = null

    /**
     * the bitmap configuration to be used
     */
    protected var mBitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888

    protected var cubicPath: Path = Path()
    protected var cubicFillPath: Path = Path()

    override fun initBuffers() {
    }

    override fun drawData(canvas: Canvas) {
        val width = viewPortHandler.chartWidth.toInt()
        val height = viewPortHandler.chartHeight.toInt()

        var drawBitmapLocal = if (drawBitmap == null) null else drawBitmap!!.get()

        if (drawBitmapLocal == null || (drawBitmapLocal.width != width)
            || (drawBitmapLocal.height != height)
        ) {
            if (width > 0 && height > 0) {
                drawBitmapLocal = createBitmap(width, height, mBitmapConfig)
                this.drawBitmap = WeakReference(drawBitmapLocal)
                bitmapCanvas = Canvas(drawBitmapLocal)
            } else
                return
        }

        drawBitmapLocal.eraseColor(Color.TRANSPARENT)

        dataProvider.lineData?.let { lineData ->
            lineData.dataSets?.forEach { set ->
                if (set.isVisible)
                    drawDataSet(canvas, set)
            }
        }
        canvas.drawBitmap(drawBitmapLocal, 0f, 0f, null)
    }

    protected fun drawDataSet(canvas: Canvas, dataSet: ILineDataSet) {
        if (dataSet.entryCount < 1)
            return

        paintRender.strokeWidth = dataSet.lineWidth
        paintRender.pathEffect = dataSet.dashPathEffect

        when (dataSet.lineMode) {
            LineDataSet.Mode.LINEAR, LineDataSet.Mode.STEPPED -> drawLinear(canvas, dataSet)
            LineDataSet.Mode.CUBIC_BEZIER -> drawCubicBezier(dataSet)
            LineDataSet.Mode.HORIZONTAL_BEZIER -> drawHorizontalBezier(dataSet)
//            else -> drawLinear(canvas, dataSet)
        }

        paintRender.pathEffect = null
    }

    protected fun drawHorizontalBezier(dataSet: ILineDataSet) {
        val phaseY = animator.phaseY

        val trans = dataProvider.getTransformer(dataSet.axisDependency)

        xBounds.set(dataProvider, dataSet)

        cubicPath.reset()

        if (xBounds.range >= 1) {
            var prev = dataSet.getEntryForIndex(xBounds.min)!!
            var cur = prev

            // let the spline start
            cubicPath.moveTo(cur.x, cur.y * phaseY)

            for (j in xBounds.min + 1..xBounds.range + xBounds.min) {
                prev = cur
                cur = dataSet.getEntryForIndex(j)!!

                val cpx = ((prev.x)
                        + (cur.x - prev.x) / 2.0f)

                cubicPath.cubicTo(
                    cpx, prev.y * phaseY,
                    cpx, cur.y * phaseY,
                    cur.x, cur.y * phaseY
                )
            }
        }

        // if filled is enabled, close the path
        if (dataSet.isDrawFilledEnabled) {
            cubicFillPath.reset()
            cubicFillPath.addPath(cubicPath)
            // create a new path, this is bad for performance
            bitmapCanvas?.let { drawCubicFill(it, dataSet, cubicFillPath, trans!!, xBounds) }
        }

        paintRender.color = dataSet.color

        paintRender.style = Paint.Style.STROKE

        trans!!.pathValueToPixel(cubicPath)

        bitmapCanvas!!.drawPath(cubicPath, paintRender)

        paintRender.pathEffect = null
    }

    protected fun drawCubicBezier(dataSet: ILineDataSet) {
        val phaseY = animator.phaseY

        val trans = dataProvider.getTransformer(dataSet.axisDependency)

        xBounds.set(dataProvider, dataSet)

        val intensity = dataSet.cubicIntensity

        cubicPath.reset()

        if (xBounds.range >= 1) {
            var prevDx: Float
            var prevDy: Float
            var curDx: Float
            var curDy: Float

            // Take an extra point from the left, and an extra from the right.
            // That's because we need 4 points for a cubic bezier (cubic=4), otherwise we get lines moving and doing weird stuff on the edges of the chart.
            // So in the starting `prev` and `cur`, go -2, -1
            // And in the `lastIndex`, add +1
            val firstIndex = xBounds.min + 1

            var prevPrev: Entry?
            var prev = dataSet.getEntryForIndex(max((firstIndex - 2).toDouble(), 0.0).toInt())
            var cur = dataSet.getEntryForIndex(max((firstIndex - 1).toDouble(), 0.0).toInt())
            var next = cur
            var nextIndex = -1

            if (cur == null)
                return

            // let the spline start
            cubicPath.moveTo(cur.x, cur.y * phaseY)

            for (j in xBounds.min + 1..xBounds.range + xBounds.min) {
                prevPrev = prev
                prev = cur
                cur = if (nextIndex == j) next else dataSet.getEntryForIndex(j)

                nextIndex = if (j + 1 < dataSet.entryCount) j + 1 else j
                next = dataSet.getEntryForIndex(nextIndex)!!

                prevDx = (cur!!.x - prevPrev!!.x) * intensity
                prevDy = (cur.y - prevPrev.y) * intensity
                curDx = (next.x - prev!!.x) * intensity
                curDy = (next.y - prev.y) * intensity

                cubicPath.cubicTo(
                    prev.x + prevDx, (prev.y + prevDy) * phaseY,
                    cur.x - curDx,
                    (cur.y - curDy) * phaseY, cur.x, cur.y * phaseY
                )
            }
        }

        // if filled is enabled, close the path
        if (dataSet.isDrawFilledEnabled) {
            cubicFillPath.reset()
            cubicFillPath.addPath(cubicPath)

            bitmapCanvas?.let { drawCubicFill(it, dataSet, cubicFillPath, trans!!, xBounds) }
        }

        paintRender.color = dataSet.color

        paintRender.style = Paint.Style.STROKE

        trans!!.pathValueToPixel(cubicPath)

        bitmapCanvas!!.drawPath(cubicPath, paintRender)

        paintRender.pathEffect = null
    }

    protected fun drawCubicFill(canvas: Canvas, dataSet: ILineDataSet, spline: Path, trans: Transformer, bounds: XBounds) {
        val fillMin = dataSet.fillFormatter!!.getFillLinePosition(dataSet, dataProvider)

        dataSet.getEntryForIndex(bounds.min + bounds.range)?.let {
            spline.lineTo(it.x, fillMin)
        }
        dataSet.getEntryForIndex(bounds.min)?.let {
            spline.lineTo(it.x, fillMin)
        }
        spline.close()

        trans.pathValueToPixel(spline)

        val drawable = dataSet.fillDrawable
        if (drawable != null) {
            drawFilledPath(canvas, spline, drawable)
        } else {
            drawFilledPath(canvas, spline, dataSet.fillColor, dataSet.fillAlpha)
        }
    }

    /**
     * Draws a normal line.
     */
    protected fun drawLinear(c: Canvas, dataSet: ILineDataSet) {
        val entryCount = dataSet.entryCount

        val pointsPerEntryPair = if (dataSet.isDrawSteppedEnabled) 4 else 2

        val trans = dataProvider.getTransformer(dataSet.axisDependency)

        val phaseY = animator.phaseY

        paintRender.style = Paint.Style.STROKE

        // if the data-set is dashed, draw on bitmap-canvas
        val canvas: Canvas? = if (dataSet.isDashedLineEnabled) {
            bitmapCanvas
        } else {
            c
        }

        xBounds.set(dataProvider, dataSet)

        // if drawing filled is enabled
        if (dataSet.isDrawFilledEnabled && entryCount > 0) {
            drawLinearFill(c, dataSet, trans!!, xBounds)
        }

        // more than 1 color
        if (dataSet.colors.size > 1) {
            val numberOfFloats = pointsPerEntryPair * 2

            if (lineBuffer.size <= numberOfFloats)
                lineBuffer = FloatArray(numberOfFloats * 2)

            val max = xBounds.min + xBounds.range

            for (j in xBounds.min..<max) {
                var entry: Entry = dataSet.getEntryForIndex(j) ?: continue

                lineBuffer[0] = entry.x
                lineBuffer[1] = entry.y * phaseY

                if (j < xBounds.max) {
                    entry = dataSet.getEntryForIndex(j + 1)!!

                    if (dataSet.isDrawSteppedEnabled) {
                        lineBuffer[2] = entry.x
                        lineBuffer[3] = lineBuffer[1]
                        lineBuffer[4] = lineBuffer[2]
                        lineBuffer[5] = lineBuffer[3]
                        lineBuffer[6] = entry.x
                        lineBuffer[7] = entry.y * phaseY
                    } else {
                        lineBuffer[2] = entry.x
                        lineBuffer[3] = entry.y * phaseY
                    }
                } else {
                    lineBuffer[2] = lineBuffer[0]
                    lineBuffer[3] = lineBuffer[1]
                }

                // Determine the start and end coordinates of the line, and make sure they differ.
                val firstCoordinateX = lineBuffer[0]
                val firstCoordinateY = lineBuffer[1]
                val lastCoordinateX = lineBuffer[numberOfFloats - 2]
                val lastCoordinateY = lineBuffer[numberOfFloats - 1]

                if (firstCoordinateX == lastCoordinateX &&
                    firstCoordinateY == lastCoordinateY
                ) continue

                trans!!.pointValuesToPixel(lineBuffer)

                if (!viewPortHandler.isInBoundsRight(firstCoordinateX)) break

                // make sure the lines don't do shitty things outside bounds
                if (!viewPortHandler.isInBoundsLeft(lastCoordinateX) || !viewPortHandler.isInBoundsTop(
                        max(
                            firstCoordinateY.toDouble(),
                            lastCoordinateY.toDouble()
                        ).toFloat()
                    ) || !viewPortHandler.isInBoundsBottom(
                        min(firstCoordinateY.toDouble(), lastCoordinateY.toDouble()).toFloat()
                    )
                ) continue

                // get the color that is set for this line-segment
                paintRender.color = dataSet.getColorByIndex(j)

                canvas!!.drawLines(lineBuffer, 0, pointsPerEntryPair * 2, paintRender)
            }
        } else { // only one color per dataset

            if (lineBuffer.size < max(((entryCount) * pointsPerEntryPair).toDouble(), pointsPerEntryPair.toDouble()) * 2) lineBuffer = FloatArray(
                (max(((entryCount) * pointsPerEntryPair).toDouble(), pointsPerEntryPair.toDouble()) * 4).toInt()
            )

            var e1: Entry?
            var e2: Entry?

            e1 = dataSet.getEntryForIndex(xBounds.min)

            if (e1 != null) {
                var j = 0
                for (x in xBounds.min..xBounds.range + xBounds.min) {
                    e1 = dataSet.getEntryForIndex(if (x == 0) 0 else (x - 1))
                    e2 = dataSet.getEntryForIndex(x)

                    if (e1 == null || e2 == null) continue

                    lineBuffer[j++] = e1.x
                    lineBuffer[j++] = e1.y * phaseY

                    if (dataSet.isDrawSteppedEnabled) {
                        lineBuffer[j++] = e2.x
                        lineBuffer[j++] = e1.y * phaseY
                        lineBuffer[j++] = e2.x
                        lineBuffer[j++] = e1.y * phaseY
                    }

                    lineBuffer[j++] = e2.x
                    lineBuffer[j++] = e2.y * phaseY
                }

                if (j > 0) {
                    trans!!.pointValuesToPixel(lineBuffer)

                    val size = (max(((xBounds.range + 1) * pointsPerEntryPair).toDouble(), pointsPerEntryPair.toDouble()) * 2).toInt()

                    paintRender.color = dataSet.color

                    canvas!!.drawLines(lineBuffer, 0, size, paintRender)
                }
            }
        }

        paintRender.pathEffect = null
    }

    protected var mGenerateFilledPathBuffer: Path = Path()

    /**
     * Draws a filled linear path on the canvas.
     */
    protected fun drawLinearFill(canvas: Canvas, dataSet: ILineDataSet, trans: Transformer, bounds: XBounds) {
        val filled = mGenerateFilledPathBuffer

        val startingIndex = bounds.min
        val endingIndex = bounds.range + bounds.min
        val indexInterval = 128

        var currentStartIndex: Int
        var currentEndIndex: Int
        var iterations = 0

        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.
        do {
            currentStartIndex = startingIndex + (iterations * indexInterval)
            currentEndIndex = currentStartIndex + indexInterval
            currentEndIndex = min(currentEndIndex.toDouble(), endingIndex.toDouble()).toInt()

            if (currentStartIndex <= currentEndIndex) {
                val drawable = dataSet.fillDrawable

                generateFilledPath(dataSet, currentStartIndex, currentEndIndex, filled)
                trans.pathValueToPixel(filled)

                if (drawable != null) {
                    drawFilledPath(canvas, filled, drawable)
                } else {
                    drawFilledPath(canvas, filled, dataSet.fillColor, dataSet.fillAlpha)
                }
            }

            iterations++
        } while (currentStartIndex <= currentEndIndex)
    }

    /**
     * Generates a path that is used for filled drawing.
     *
     * @param dataSet    The dataset from which to read the entries.
     * @param startIndex The index from which to start reading the dataset
     * @param endIndex   The index from which to stop reading the dataset
     * @param outputPath The path object that will be assigned the chart data.
     */
    private fun generateFilledPath(dataSet: ILineDataSet, startIndex: Int, endIndex: Int, outputPath: Path) {
        val fillMin = dataSet.fillFormatter!!.getFillLinePosition(dataSet, dataProvider)
        val phaseY = animator.phaseY
        val isDrawSteppedEnabled = dataSet.lineMode == LineDataSet.Mode.STEPPED

        outputPath.reset()

        dataSet.getEntryForIndex(startIndex)?.let { entry ->

            outputPath.moveTo(entry.x, fillMin)
            outputPath.lineTo(entry.x, entry.y * phaseY)

            // create a new path
            var currentEntry: Entry? = null
            var previousEntry = entry
            for (x in startIndex + 1..endIndex) {
                currentEntry = dataSet.getEntryForIndex(x)

                if (currentEntry != null) {
                    if (isDrawSteppedEnabled) {
                        outputPath.lineTo(currentEntry.x, previousEntry.y * phaseY)
                    }

                    outputPath.lineTo(currentEntry.x, currentEntry.y * phaseY)

                    previousEntry = currentEntry
                }
            }

            // close up
            if (currentEntry != null) {
                outputPath.lineTo(currentEntry.x, fillMin)
            }
        }
        outputPath.close()
    }

    override fun drawValues(canvas: Canvas) {
        if (isDrawingValuesAllowed(dataProvider)) {
            val dataSets = dataProvider.lineData?.dataSets

            dataSets?.let {
                for (i in it.indices) {
                    val dataSet = dataSets[i]
                    if (dataSet.entryCount == 0) {
                        continue
                    }
                    if (!shouldDrawValues(dataSet) || dataSet.entryCount < 1) {
                        continue
                    }

                    // apply the text-styling defined by the DataSet
                    applyValueTextStyle(dataSet)

                    val trans = dataProvider.getTransformer(dataSet.axisDependency)

                    // make sure the values do not interfear with the circles
                    var valOffset = (dataSet.circleRadius * 1.75f).toInt()

                    if (!dataSet.isDrawCirclesEnabled) valOffset = valOffset / 2

                    xBounds.set(dataProvider, dataSet)

                    val positions = trans!!.generateTransformedValuesLine(
                        dataSet, animator.phaseX, animator
                            .phaseY, xBounds.min, xBounds.max
                    )

                    val iconsOffset = PointF.getInstance(dataSet.iconsOffset)
                    iconsOffset.x = iconsOffset.x.convertDpToPixel()
                    iconsOffset.y = iconsOffset.y.convertDpToPixel()

                    var j = 0
                    while (j < positions.size) {
                        val x = positions[j]
                        val y = positions[j + 1]

                        if (!viewPortHandler.isInBoundsRight(x)) break

                        if (!viewPortHandler.isInBoundsLeft(x) || !viewPortHandler.isInBoundsY(y)) {
                            j += 2
                            continue
                        }

                        val entry = dataSet.getEntryForIndex(j / 2 + xBounds.min)

                        if (entry != null) {
                            if (dataSet.isDrawValues) {
                                drawValue(
                                    canvas, dataSet.valueFormatter, entry.y, entry, i, x,
                                    y - valOffset, dataSet.getValueTextColor(j / 2)
                                )
                            }

                            if (entry.icon != null && dataSet.isDrawIcons) {
                                val icon = entry.icon

                                icon?.let {
                                    Utils.drawImage(
                                        canvas,
                                        it,
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

    override fun drawExtras(canvas: Canvas) {
        drawCircles(canvas)
    }

    /**
     * cache for the circle bitmaps of all datasets
     */
    private val mImageCaches = HashMap<IDataSet<*>, DataSetImageCache>()

    /**
     * buffer for drawing the circles
     */
    private val mCirclesBuffer = FloatArray(2)

    init {
        circlePaintInner.style = Paint.Style.FILL
        circlePaintInner.color = Color.WHITE
    }

    protected fun drawCircles(canvas: Canvas) {
        paintRender.style = Paint.Style.FILL

        val phaseY = animator.phaseY

        mCirclesBuffer[0] = 0f
        mCirclesBuffer[1] = 0f

        val dataSets = dataProvider.lineData?.dataSets

        dataSets?.let {
            for (i in it.indices) {
                val dataSet = dataSets[i]
                if (!dataSet.isVisible || !dataSet.isDrawCirclesEnabled || dataSet.entryCount == 0) continue

                circlePaintInner.color = dataSet.circleHoleColor

                val trans = dataProvider.getTransformer(dataSet.axisDependency)

                xBounds.set(dataProvider, dataSet)

                val circleRadius = dataSet.circleRadius
                val circleHoleRadius = dataSet.circleHoleRadius
                val drawCircleHole = dataSet.isDrawCircleHoleEnabled && circleHoleRadius < circleRadius && circleHoleRadius > 0f
                val drawTransparentCircleHole = drawCircleHole &&
                        dataSet.circleHoleColor == ColorTemplate.COLOR_NONE

                val imageCache: DataSetImageCache?

                if (mImageCaches.containsKey(dataSet)) {
                    imageCache = mImageCaches[dataSet]
                } else {
                    imageCache = DataSetImageCache()
                    mImageCaches[dataSet] = imageCache
                }

                val changeRequired = imageCache!!.init(dataSet)

                // only fill the cache with new bitmaps if a change is required
                if (changeRequired) {
                    imageCache.fill(dataSet, drawCircleHole, drawTransparentCircleHole)
                }

                val boundsRangeCount = xBounds.range + xBounds.min

                for (j in xBounds.min..boundsRangeCount) {
                    val e = dataSet.getEntryForIndex(j) ?: break

                    mCirclesBuffer[0] = e.x
                    mCirclesBuffer[1] = e.y * phaseY

                    trans!!.pointValuesToPixel(mCirclesBuffer)

                    if (!viewPortHandler.isInBoundsRight(mCirclesBuffer[0])) break

                    if (!viewPortHandler.isInBoundsLeft(mCirclesBuffer[0]) ||
                        !viewPortHandler.isInBoundsY(mCirclesBuffer[1])
                    ) continue

                    val circleBitmap = imageCache.getBitmap(j)

                    if (circleBitmap != null) {
                        canvas.drawBitmap(circleBitmap, mCirclesBuffer[0] - circleRadius, mCirclesBuffer[1] - circleRadius, null)
                    }
                }
            }
        }
    }

    override fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>) {
        val lineData = dataProvider.lineData

        for (high in indices) {
            val set = lineData?.getDataSetByIndex(high.dataSetIndex)

            if (set == null || !set.isHighlightEnabled)
                continue

            set.getEntryForXValue(high.x, high.y)?.let { entry ->

                if (!isInBoundsX(entry, set))
                    continue

                val pix = dataProvider.getTransformer(set.axisDependency)!!.getPixelForValues(
                    entry.x, entry.y * animator.phaseY
                )

                high.setDraw(pix.x.toFloat(), pix.y.toFloat())
                // draw the lines
                drawHighlightLines(canvas, pix.x.toFloat(), pix.y.toFloat(), set)
            }
        }
    }

    /**
     * Releases the drawing bitmap. This should be called when [info.appdev.charting.charts.LineChart.onDetachedFromWindow].
     */
    fun releaseBitmap() {
        bitmapCanvas?.setBitmap(null)
        bitmapCanvas = null
        if (drawBitmap != null) {
            val drawBitmap = drawBitmap?.get()
            drawBitmap?.recycle()
            this.drawBitmap?.clear()
            this.drawBitmap = null
        }
    }

    private inner class DataSetImageCache {
        private val mCirclePathBuffer = Path()

        private var circleBitmaps: Array<Bitmap?>? = null

        /**
         * Sets up the cache, returns true if a change of cache was required.
         */
        fun init(set: ILineDataSet): Boolean {
            val size = set.circleColorCount
            var changeRequired = false

            if (circleBitmaps == null) {
                circleBitmaps = arrayOfNulls(size)
                changeRequired = true
            } else if (circleBitmaps!!.size != size) {
                circleBitmaps = arrayOfNulls(size)
                changeRequired = true
            }

            return changeRequired
        }

        /**
         * Fills the cache with bitmaps for the given dataset.
         *
         * @param set
         * @param drawCircleHole
         * @param drawTransparentCircleHole
         */
        fun fill(set: ILineDataSet, drawCircleHole: Boolean, drawTransparentCircleHole: Boolean) {
            val colorCount = set.circleColorCount
            val circleRadius = set.circleRadius
            val circleHoleRadius = set.circleHoleRadius

            for (i in 0..<colorCount) {
                val conf = Bitmap.Config.ARGB_4444
                val circleBitmap = createBitmap((circleRadius * 2.1).toInt(), (circleRadius * 2.1).toInt(), conf)

                val canvas = Canvas(circleBitmap)
                circleBitmaps!![i] = circleBitmap
                paintRender.color = set.getCircleColor(i)

                if (drawTransparentCircleHole) {
                    // Begin path for circle with hole
                    mCirclePathBuffer.reset()

                    mCirclePathBuffer.addCircle(
                        circleRadius,
                        circleRadius,
                        circleRadius,
                        Path.Direction.CW
                    )

                    // Cut hole in path
                    mCirclePathBuffer.addCircle(
                        circleRadius,
                        circleRadius,
                        circleHoleRadius,
                        Path.Direction.CCW
                    )

                    // Fill in-between
                    canvas.drawPath(mCirclePathBuffer, paintRender)
                } else {
                    canvas.drawCircle(
                        circleRadius,
                        circleRadius,
                        circleRadius,
                        paintRender
                    )

                    if (drawCircleHole) {
                        canvas.drawCircle(
                            circleRadius,
                            circleRadius,
                            circleHoleRadius,
                            circlePaintInner
                        )
                    }
                }
            }
        }

        /**
         * Returns the cached Bitmap at the given index.
         */
        fun getBitmap(index: Int): Bitmap? {
            return circleBitmaps!![index % circleBitmaps!!.size]
        }
    }
}
