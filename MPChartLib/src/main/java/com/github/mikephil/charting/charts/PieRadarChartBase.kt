package com.github.mikephil.charting.charts

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.animation.Easing.EasingFunction
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.PieRadarChartTouchListener
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.MPPointF.Companion.getInstance
import com.github.mikephil.charting.utils.MPPointF.Companion.recycleInstance
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.convertDpToPixel
import timber.log.Timber
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Baseclass of PieChart and RadarChart.
 */
abstract class PieRadarChartBase<T : ChartData<out IDataSet<out Entry>>>
    : Chart<T> {
    /**
     * holds the normalized version of the current rotation angle of the chart
     */
    private var mRotationAngle = 270f

    /**
     * gets the raw version of the current rotation angle of the pie chart the
     * returned value could be any value, negative or positive, outside of the
     * 360 degrees. this is used when working with rotation direction, mainly by
     * gestures and animations.
     */
    /**
     * holds the raw version of the current rotation angle of the chart
     */
    var rawRotationAngle: Float = 270f
        private set

    /**
     * Returns true if rotation of the chart by touch is enabled, false if not.
     */
    /**
     * Set this to true to enable the rotation / spinning of the chart by touch.
     * Set it to false to disable it. Default: true
     */
    /**
     * flag that indicates if rotation is enabled or not
     */
    var isRotationEnabled: Boolean = true

    /**
     * Gets the minimum offset (padding) around the chart, defaults to 0.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 0.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 0.f
     */
    var minOffset: Float = 0f

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        chartTouchListener = PieRadarChartTouchListener(this)
    }

    override fun calcMinMax() {
        //mXAxis.mAxisRange = mData.getXVals().size() - 1;
    }

    override val maxVisibleCount: Int
        get() = mData!!.entryCount

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // use the Pie- and RadarChart listener own listener
        return if (mTouchEnabled && chartTouchListener != null)
            chartTouchListener!!.onTouch(this, event)
        else
            super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (chartTouchListener is PieRadarChartTouchListener) (chartTouchListener as PieRadarChartTouchListener).computeScroll()
    }

    override fun notifyDataSetChanged() {
        if (mData == null)
            return

        calcMinMax()

        if (legend != null) legendRenderer?.computeLegend(mData!!)

        calculateOffsets()
    }

    public override fun calculateOffsets() {
        var legendLeft = 0f
        var legendRight = 0f
        var legendBottom = 0f
        var legendTop = 0f

        legend?.let { legend ->
            if (legend.isEnabled && !legend.isDrawInsideEnabled) {
                val fullLegendWidth = min(
                    legend.neededWidth,
                    viewPortHandler.chartWidth * legend.maxSizePercent
                )
                val fullLegendHeight = min(
                    legend.neededHeight,
                    viewPortHandler.chartHeight * legend.maxSizePercent
                )

                when (legend.orientation) {
                    LegendOrientation.VERTICAL -> {
                        when (legend.horizontalAlignment) {
                            LegendHorizontalAlignment.LEFT -> legendLeft = fullLegendWidth
                            LegendHorizontalAlignment.RIGHT -> legendRight = fullLegendWidth
                            LegendHorizontalAlignment.CENTER -> {
                                // do nothing for center
                            }
                        }
                    }

                    LegendOrientation.HORIZONTAL -> {
                        when (legend.verticalAlignment) {
                            LegendVerticalAlignment.TOP -> legendTop = fullLegendHeight
                            LegendVerticalAlignment.BOTTOM -> legendBottom = fullLegendHeight
                            LegendVerticalAlignment.CENTER -> {
                                // do nothing for center
                            }
                        }
                    }
                }
            }

            if (legend.isEnabled && !legend.isDrawInsideEnabled) {
                val fullLegendWidth = min(
                    legend.neededWidth,
                    viewPortHandler.chartWidth * legend.maxSizePercent
                )

                when (legend.orientation) {
                    LegendOrientation.VERTICAL -> {
                        var xLegendOffset = 0f

                        if (legend.horizontalAlignment == LegendHorizontalAlignment.LEFT
                            || legend.horizontalAlignment == LegendHorizontalAlignment.RIGHT
                        ) {
                            if (legend.verticalAlignment == LegendVerticalAlignment.CENTER) {
                                // this is the space between the legend and the chart
                                val spacing = 13f.convertDpToPixel()

                                xLegendOffset = fullLegendWidth + spacing
                            } else {
                                // this is the space between the legend and the chart
                                val spacing = 8f.convertDpToPixel()

                                val legendWidth = fullLegendWidth + spacing
                                val legendHeight = legend.neededHeight + legend.mTextHeightMax

                                val bottomX = if (legend.horizontalAlignment ==
                                    LegendHorizontalAlignment.RIGHT
                                )
                                    width - legendWidth + 15f
                                else
                                    legendWidth - 15f
                                val bottomY = legendHeight + 15f
                                val distLegend = distanceToCenter(bottomX, bottomY)

                                val reference = getPosition(
                                    center, this.radius,
                                    getAngleForPoint(bottomX, bottomY)
                                )

                                val distReference = distanceToCenter(reference.x, reference.y)
                                val minOffset = 5f.convertDpToPixel()

                                if (bottomY >= center.y && height - legendWidth > width) {
                                    xLegendOffset = legendWidth
                                } else if (distLegend < distReference) {
                                    val diff = distReference - distLegend
                                    xLegendOffset = minOffset + diff
                                }

                                recycleInstance(center)
                                recycleInstance(reference)
                            }
                        }

                        when (legend.horizontalAlignment) {
                            LegendHorizontalAlignment.LEFT -> legendLeft = xLegendOffset
                            LegendHorizontalAlignment.RIGHT -> legendRight = xLegendOffset
                            LegendHorizontalAlignment.CENTER -> when (legend.verticalAlignment) {
                                LegendVerticalAlignment.TOP -> legendTop = min(
                                    legend.neededHeight,
                                    viewPortHandler.chartHeight * legend.maxSizePercent
                                )

                                LegendVerticalAlignment.BOTTOM -> legendBottom = min(
                                    legend.neededHeight,
                                    viewPortHandler.chartHeight * legend.maxSizePercent
                                )

                                LegendVerticalAlignment.CENTER -> Timber.e("LegendCenter/VerticalCenter not supported for PieRadarChart")
                            }
                        }
                    }

                    LegendOrientation.HORIZONTAL -> {
                        val yLegendOffset: Float

                        if (legend.verticalAlignment == LegendVerticalAlignment.TOP ||
                            legend.verticalAlignment == LegendVerticalAlignment.BOTTOM
                        ) {
                            // It's possible that we do not need this offset anymore as it
                            //   is available through the extraOffsets, but changing it can mean
                            //   changing default visibility for existing apps.

                            val yOffset = this.requiredLegendOffset

                            yLegendOffset = min(
                                legend.neededHeight + yOffset,
                                viewPortHandler.chartHeight * legend.maxSizePercent
                            )

                            when (legend.verticalAlignment) {
                                LegendVerticalAlignment.TOP -> legendTop = yLegendOffset
                                LegendVerticalAlignment.BOTTOM -> legendBottom = yLegendOffset
                                LegendVerticalAlignment.CENTER -> Timber.e("LegendCenter/HorizontalCenter not supported for PieRadarChart")
                            }
                        }
                    }
                }

                legendLeft += this.requiredBaseOffset
                legendRight += this.requiredBaseOffset
                legendTop += this.requiredBaseOffset
                legendBottom += this.requiredBaseOffset
            }
        }
        var minOffset = minOffset.convertDpToPixel()

        if (this is RadarChart) {
            val x = this.xAxis

            if (x.isEnabled && x.isDrawLabelsEnabled) {
                minOffset = max(minOffset, x.mLabelWidth.toFloat())
            }
        }

        legendTop += extraTopOffset
        legendRight += extraRightOffset
        legendBottom += extraBottomOffset
        legendLeft += extraLeftOffset

        val offsetLeft = max(minOffset, legendLeft)
        val offsetTop = max(minOffset, legendTop)
        val offsetRight = max(minOffset, legendRight)
        val offsetBottom = max(minOffset, max(this.requiredBaseOffset, legendBottom))

        viewPortHandler.restrainViewPort(offsetLeft, offsetTop, offsetRight, offsetBottom)

        if (isLogEnabled)
            Timber.i("offsetLeft: $offsetLeft, offsetTop: $offsetTop, offsetRight: $offsetRight, offsetBottom: $offsetBottom")
    }

    /**
     * returns the angle relative to the chart center for the given point on the
     * chart in degrees. The angle is always between 0 and 360°, 0° is NORTH,
     * 90° is EAST, ...
     */
    fun getAngleForPoint(x: Float, y: Float): Float {
        centerOffsets.let { c ->

            val tx = (x - c.x).toDouble()
            val ty = (y - c.y).toDouble()
            val length = sqrt(tx * tx + ty * ty)
            val r = acos(ty / length)

            var angle = Math.toDegrees(r).toFloat()

            if (x > c.x) angle = 360f - angle

            // add 90° because chart starts EAST
            angle += 90f

            // neutralize overflow
            if (angle > 360f)
                angle -= 360f

            recycleInstance(c)
            return angle
        }
    }

    /**
     * Returns a recyclable MPPointF instance.
     * Calculates the position around a center point, depending on the distance
     * from the center, and the angle of the position around the center.
     * @param angle  in degrees, converted to radians internally
     */
    fun getPosition(center: MPPointF, dist: Float, angle: Float): MPPointF {
        val p = getInstance(0f, 0f)
        getPosition(center, dist, angle, p)
        return p
    }

    fun getPosition(center: MPPointF, dist: Float, angle: Float, outputPoint: MPPointF) {
        outputPoint.x = (center.x + dist * cos(Math.toRadians(angle.toDouble()))).toFloat()
        outputPoint.y = (center.y + dist * sin(Math.toRadians(angle.toDouble()))).toFloat()
    }

    /**
     * Returns the distance of a certain point on the chart to the center of the chart.
     */
    fun distanceToCenter(x: Float, y: Float): Float {
        centerOffsets.let { c ->

            val dist: Float

            val xDist: Float = if (x > c.x) {
                x - c.x
            } else {
                c.x - x
            }

            val yDist: Float = if (y > c.y) {
                y - c.y
            } else {
                c.y - y
            }

            // pythagoras
            dist = sqrt(xDist.toDouble().pow(2.0) + yDist.toDouble().pow(2.0)).toFloat()

            recycleInstance(c)

            return dist
        }
    }

    /**
     * Returns the xIndex for the given angle around the center of the chart.
     * Returns -1 if not found / out of bounds.
     */
    abstract fun getIndexForAngle(angle: Float): Int

    var rotationAngle: Float
        /**
         * gets a normalized version of the current rotation angle of the pie chart,
         * which will always be between 0.0 < 360.0
         */
        get() = mRotationAngle
        /**
         * Set an offset for the rotation of the RadarChart in degrees. Default 270f
         * --> top (NORTH)
         */
        set(angle) {
            this.rawRotationAngle = angle
            mRotationAngle = Utils.getNormalizedAngle(this.rawRotationAngle)
        }

    val diameter: Float
        /**
         * returns the diameter of the pie- or radar-chart
         */
        get() {
            val content = viewPortHandler.contentRect
            content.left += extraLeftOffset
            content.top += extraTopOffset
            content.right -= extraRightOffset
            content.bottom -= extraBottomOffset
            return min(content.width(), content.height())
        }

    /**
     * Returns the radius of the chart in pixels.
     */
    abstract val radius: Float

    /**
     * Returns the required offset for the chart legend.
     */
    protected abstract val requiredLegendOffset: Float

    /**
     * Returns the base offset needed for the chart without calculating the
     * legend size.
     */
    protected abstract val requiredBaseOffset: Float

    override val yChartMax: Float
        get() = 0f

    override val yChartMin: Float
        get() = 0f

    /**
     * Apply a spin animation to the Chart.
     */
    @SuppressLint("NewApi")
    fun spin(durationMillis: Int, fromAngle: Float, toAngle: Float, easing: EasingFunction?) {
        this.rotationAngle = fromAngle

        val spinAnimator = ObjectAnimator.ofFloat(
            this, "rotationAngle", fromAngle,
            toAngle
        )
        spinAnimator.duration = durationMillis.toLong()
        spinAnimator.interpolator = easing

        spinAnimator.addUpdateListener { postInvalidate() }
        spinAnimator.start()
    }
}
