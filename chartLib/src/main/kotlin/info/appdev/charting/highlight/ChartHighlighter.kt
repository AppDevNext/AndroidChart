package info.appdev.charting.highlight

import android.annotation.SuppressLint
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.data.ChartData
import info.appdev.charting.data.DataSet
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.interfaces.dataprovider.base.BarLineScatterCandleBubbleDataProvider
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.utils.PointD
import kotlin.math.abs
import kotlin.math.hypot

open class ChartHighlighter<T : BarLineScatterCandleBubbleDataProvider<*>>(protected var provider: T) : IHighlighter {

    /**
     * Buffer for storing previously highlighted values.
     */
    protected var highlightBuffer: MutableList<Highlight> = ArrayList()

    override fun getHighlight(x: Float, y: Float): Highlight? {
        val pos = getValsForTouch(x, y)
        val xVal = pos.x.toFloat()
        PointD.recycleInstance(pos)

        val high = getHighlightForX(xVal, x, y)
        return high
    }

    /**
     * Returns a recyclable PointD instance matching the corresponding xPos for a given touch-position in pixels.
     */
    protected fun getValsForTouch(x: Float, y: Float): PointD {
        return provider.getTransformer(AxisDependency.LEFT)!!.getValuesByTouchPoint(x, y)
    }

    /**
     * Returns the corresponding Highlight for a given xVal and x- and y-touch position in pixels.
     */
    protected fun getHighlightForX(xVal: Float, x: Float, y: Float): Highlight? {
        val closestValues = getHighlightsAtXValue(xVal, x, y)

        if (closestValues.isNullOrEmpty()) {
            return null
        }

        val leftAxisMinDist = getMinimumDistance(closestValues, y, AxisDependency.LEFT)
        val rightAxisMinDist = getMinimumDistance(closestValues, y, AxisDependency.RIGHT)

        val axis = if (leftAxisMinDist < rightAxisMinDist) AxisDependency.LEFT else AxisDependency.RIGHT

        // Match original architecture by passing the custom display metric density scaling directly
        val density = (provider as? android.view.View)?.context?.resources?.displayMetrics?.density ?: 1f
        val maxSelectionDistance = 40f * density

        return getClosestHighlightByPixel(closestValues, x, y, axis, maxSelectionDistance)
    }

    /**
     * Returns the minimum distance from a touch value (in pixels) to the closest visible chart value.
     */
    protected fun getMinimumDistance(closestValues: MutableList<Highlight>, pos: Float, axis: AxisDependency?): Float {
        var distance = Float.MAX_VALUE

        for (i in closestValues.indices) {
            val high = closestValues[i]

            if (high.axis == axis) {
                val tempDistance = abs(getHighlightPos(high) - pos)
                if (tempDistance < distance) {
                    distance = tempDistance
                }
            }
        }

        return distance
    }

    protected fun getHighlightPos(h: Highlight): Float {
        return h.yPx
    }

    /**
     * Returns a list of Highlight objects representing the entries closest to the given xVal.
     */
    override fun getHighlightsAtXValue(xVal: Float, x: Float, y: Float): MutableList<Highlight>? {
        highlightBuffer.clear()

        val myData = provider.data ?: return highlightBuffer
        var i = 0
        val dataSetCount = myData.dataSetCount
        while (i < dataSetCount) {
            val dataSet = myData.getDataSetByIndex(i)

            dataSet?.let {
                if (it.isHighlight) {
                    highlightBuffer.addAll(buildHighlights(it, i, xVal, DataSet.Rounding.CLOSEST))
                }
            }
            i++
        }
        return highlightBuffer
    }

    /**
     * An array of `Highlight` objects corresponding to the selected xValue and dataSetIndex.
     */
    @Suppress("SameParameterValue")
    protected open fun buildHighlights(
        @SuppressLint("RawTypeDataSet") set: IDataSet<*>,
        dataSetIndex: Int,
        xVal: Float,
        rounding: DataSet.Rounding?
    ): MutableList<Highlight> {
        val highlights = ArrayList<Highlight>()
        var entries = set.getEntriesForXValue(xVal)?.map { it as EntryFloat }?.toMutableList()

        if (entries != null && entries.isEmpty()) {
            val closest = set.getEntryForXValue(xVal, Float.NaN, rounding) as? EntryFloat
            if (closest != null) {
                entries = set.getEntriesForXValue(closest.x)?.map { it as EntryFloat }?.toMutableList()
            }
        }

        if (entries == null || entries.isEmpty()) return highlights

        for (e in entries) {
            val pixels = provider.getTransformer(set.axisDependency)!!.getPixelForValues(e.x, e.y)
            highlights.add(
                Highlight(
                    x = e.x,
                    y = e.y,
                    xPx = pixels.x.toFloat(),
                    yPx = pixels.y.toFloat(),
                    dataSetIndex = dataSetIndex,
                    axis = set.axisDependency
                )
            )
        }

        return highlights
    }

    /**
     * Selects the closest highlight point based on precise geometric collision bounding checks.
     */
    fun getClosestHighlightByPixel(
        closestValues: MutableList<Highlight>, x: Float, y: Float,
        axis: AxisDependency?, minSelectionDistance: Float
    ): Highlight? {
        var closest: Highlight? = null
        var shortestDistance = Float.MAX_VALUE

        // Reflection package verification matching your specific Bubble engine implementation strategy
        val isBubble = provider.javaClass.name.contains("BubbleDataProvider", ignoreCase = true)

        for (i in closestValues.indices) {
            val high = closestValues[i]

            if (axis == null || high.axis == axis) {
                val cDistance = getDistance(x, y, high.xPx, high.yPx)

                if (isBubble) {
                    val dataSet = provider.data?.getDataSetByIndex(high.dataSetIndex) as? info.appdev.charting.interfaces.datasets.IBubbleDataSet
                    if (dataSet == null) continue

                    val entry = dataSet.getEntryForXValue(high.x, Float.NaN, DataSet.Rounding.CLOSEST)
                    val bubbleClass = try { Class.forName("info.appdev.charting.data.BubbleEntry") } catch (e: Exception) { null }

                    if (entry != null && bubbleClass?.isInstance(entry) == true) {
                        val density = (provider as? android.view.View)?.context?.resources?.displayMetrics?.density ?: 1f

                        // 1. SAFE ZOOM SCALE TRACKING VIA BASE CHART VIEW
                        var currentZoomScale = 1.0f
                        try {
                            val viewPortHandlerMethod = provider.javaClass.getMethod("getViewPortHandler")
                            val viewPortHandler = viewPortHandlerMethod.invoke(provider)
                            val getScaleXMethod = viewPortHandler?.javaClass?.getMethod("getScaleX")
                            currentZoomScale = (getScaleXMethod?.invoke(viewPortHandler) as? Float) ?: 1.0f
                        } catch (e: Exception) {
                            // Secondary safety fallback if wrapped inside specific view contexts
                        }

                        // Calculate raw size radius safely dynamically
                        val getSizeMethod = entry.javaClass.getMethod("getSize")
                        val rawSize = (getSizeMethod.invoke(entry) as? Float) ?: 0f
                        val baseRadius = rawSize / 2f
                        var physicalBubbleRadius = baseRadius * density * currentZoomScale

                        // 2. THE TINY-BUBBLE ACCESSIBILITY BOOST (15dp touch floor target tracking)
                        val minimumTouchFloor = 15f * density
                        if (physicalBubbleRadius < minimumTouchFloor) {
                            physicalBubbleRadius = minimumTouchFloor
                        }

                        // 3. SELECTION RESOLUTION
                        if (cDistance <= physicalBubbleRadius) {
                            if (cDistance < shortestDistance) {
                                shortestDistance = cDistance
                                closest = high
                            }
                        }
                    }
                } else {
                    // Standard target mapping for Line/Scatter plots
                    if (cDistance < minSelectionDistance && cDistance < shortestDistance) {
                        shortestDistance = cDistance
                        closest = high
                    }
                }
            }
        }

        return closest
    }

    /**
     * Calculates the distance between two distinct points on the viewport canvas grid.
     */
    protected open fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()).toFloat()
    }
}