package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.PieHighlighter
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * View that represents a pie chart. Draws cake like slices.
 *
 * @author Philipp Jahoda
 */
class PieChart : PieRadarChartBase<PieEntry, IPieDataSet, PieData> {
    /**
     * returns the circlebox, the boundingbox of the pie-chart slices
     *
     * @return
     */
    /**
     * rect object that represents the bounds of the piechart, needed for
     * drawing the circle
     */
    val circleBox: RectF = RectF()

    override val data: PieData?
        get() = super.mData

    /**
     * Returns true if drawing the entry labels is enabled, false if not.
     *
     * @return
     */
    /**
     * flag indicating if entry labels should be drawn or not
     */
    var isDrawEntryLabelsEnabled: Boolean = true
        private set

    /**
     * returns an integer array of all the different angles the chart slices
     * have the angles in the returned array determine how much space (of 360°)
     * each slice takes
     *
     * @return
     */
    /**
     * array that holds the width of each pie-slice in degrees
     */
    var drawAngles: FloatArray = FloatArray(1)
        private set

    /**
     * returns the absolute angles of the different chart slices (where the
     * slices end)
     *
     * @return
     */
    /**
     * array that holds the absolute angle in degrees of each slice
     */
    var absoluteAngles: FloatArray = FloatArray(1)
        private set

    /**
     * returns true if the hole in the center of the pie-chart is set to be
     * visible, false if not
     *
     * @return
     */
    /**
     * set this to true to draw the pie center empty
     *
     * @param enabled
     */
    /**
     * if true, the white hole inside the chart will be drawn
     */
    var isDrawHoleEnabled: Boolean = true

    /**
     * Returns true if the inner tips of the slices are visible behind the hole,
     * false if not.
     *
     * @return true if slices are visible behind the hole.
     */
    /**
     * if true, the hole will see-through to the inner tips of the slices
     */
    var isDrawSlicesUnderHoleEnabled: Boolean = false
        private set

    /**
     * Returns true if using percentage values is enabled for the chart.
     *
     * @return
     */
    /**
     * if true, the values inside the piechart are drawn as percent values
     */
    var isUsePercentValuesEnabled: Boolean = false
        private set

    /**
     * Returns true if the chart is set to draw each end of a pie-slice
     * "rounded".
     *
     * @return
     */
    /**
     * if true, the slices of the piechart are rounded
     */
    var isDrawRoundedSlicesEnabled: Boolean = false
        private set

    /**
     * variable for the text that is drawn in the center of the pie-chart
     */
    private var mCenterText: CharSequence? = ""

    private val mCenterTextOffset: MPPointF = MPPointF.Companion.getInstance(0f, 0f)

    /**
     * Returns the size of the hole radius in percent of the total radius.
     *
     * @return
     */
    /**
     * sets the radius of the hole in the center of the piechart in percent of
     * the maximum radius (max = the radius of the whole chart), default 50%
     *
     * @param percent
     */
    /**
     * indicates the size of the hole in the center of the piechart, default:
     * radius / 2
     */
    var holeRadius: Float = 50f

    /**
     * sets the radius of the transparent circle that is drawn next to the hole
     * in the piechart in percent of the maximum radius (max = the radius of the
     * whole chart), default 55% -> means 5% larger than the center-hole by
     * default
     *
     * @param percent
     */
    /**
     * the radius of the transparent circle next to the chart-hole in the center
     */
    var transparentCircleRadius: Float = 55f

    /**
     * returns true if drawing the center text is enabled
     *
     * @return
     */
    /**
     * if enabled, centertext is drawn
     */
    var isDrawCenterTextEnabled: Boolean = true
        private set

    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie
     * hole
     * default 1.f (100%)
     */
    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie
     * hole
     * default 1.f (100%)
     */
    var centerTextRadiusPercent: Float = 100f

    protected var mMaxAngle: Float = 360f

    /**
     * Minimum angle to draw slices, this only works if there is enough room for all slices to have
     * the minimum angle, default 0f.
     */
    private var mMinAngleForSlices = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mRenderer = PieChartRenderer(this, mAnimator!!, viewPortHandler)
        mXAxis = null

        highlighter = PieHighlighter(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mData == null) return

        mRenderer!!.drawData(canvas)

        if (valuesToHighlight()) mRenderer!!.drawHighlighted(canvas, highlighted!!)

        mRenderer!!.drawExtras(canvas)

        mRenderer!!.drawValues(canvas)

        legendRenderer!!.renderLegend(canvas)

        drawDescription(canvas)

        drawMarkers(canvas)
    }

    override fun calculateOffsets() {
        super.calculateOffsets()

        // prevent nullpointer when no data set
        if (mData == null) return

        val diameter = diameter
        val radius = diameter / 2f

        val c = centerOffsets

        val shift = mData!!.dataSet!!.selectionShift

        // create the circle box that will contain the pie-chart (the bounds of
        // the pie-chart)
        circleBox!!.set(
            c!!.x - radius + shift,
            c.y - radius + shift,
            c.x + radius - shift,
            c.y + radius - shift
        )

        MPPointF.Companion.recycleInstance(c)
    }

    override fun calcMinMax() {
        calcAngles()
    }

    override fun getMarkerPosition(high: Highlight): FloatArray {
        val center = this.centerCircleBox
        var r = radius

        var off = r / 10f * 3.6f

        if (this.isDrawHoleEnabled) {
            off = (r - (r / 100f * this.holeRadius)) / 2f
        }

        r -= off // offset to keep things inside the chart

        val rotationAngle = rotationAngle

        val entryIndex = high.x.toInt()

        // offset needed to center the drawn text in the slice
        val offset = this.drawAngles[entryIndex] / 2

        // calculate the text position
        val x = (r
                * cos(
            Math.toRadians(
                ((rotationAngle + this.absoluteAngles[entryIndex] - offset)
                        * mAnimator!!.phaseY).toDouble()
            )
        ) + center.x).toFloat()
        val y = (r
                * sin(
            Math.toRadians(
                ((rotationAngle + this.absoluteAngles[entryIndex] - offset)
                        * mAnimator!!.phaseY).toDouble()
            )
        ) + center.y).toFloat()

        MPPointF.Companion.recycleInstance(center)
        return floatArrayOf(x, y)
    }

    /**
     * calculates the needed angles for the chart slices
     */
    private fun calcAngles() {
        val entryCount = mData!!.entryCount

        if (drawAngles.size != entryCount) {
            this.drawAngles = FloatArray(entryCount)
        } else {
            for (i in 0..<entryCount) {
                this.drawAngles[i] = 0f
            }
        }
        if (absoluteAngles.size != entryCount) {
            this.absoluteAngles = FloatArray(entryCount)
        } else {
            for (i in 0..<entryCount) {
                this.absoluteAngles[i] = 0f
            }
        }

        val yValueSum = mData!!.yValueSum

        val dataSets = mData!!.dataSets

        val hasMinAngle = mMinAngleForSlices != 0f && entryCount * mMinAngleForSlices <= mMaxAngle
        val minAngles = FloatArray(entryCount)

        var cnt = 0
        var offset = 0f
        var diff = 0f

        for (i in 0..<mData!!.dataSetCount) {
            val set = dataSets[i]

            for (j in 0..<set.entryCount) {
                val drawAngle = calcAngle(abs(set.getEntryForIndex(j)!!.y), yValueSum)

                if (hasMinAngle) {
                    val temp = drawAngle - mMinAngleForSlices
                    if (temp <= 0) {
                        minAngles[cnt] = mMinAngleForSlices
                        offset += -temp
                    } else {
                        minAngles[cnt] = drawAngle
                        diff += temp
                    }
                }

                this.drawAngles[cnt] = drawAngle

                if (cnt == 0) {
                    this.absoluteAngles[cnt] = this.drawAngles[cnt]
                } else {
                    this.absoluteAngles[cnt] = this.absoluteAngles[cnt - 1] + this.drawAngles[cnt]
                }

                cnt++
            }
        }

        if (hasMinAngle) {
            // Correct bigger slices by relatively reducing their angles based on the total angle needed to subtract
            // This requires that `entryCount * mMinAngleForSlices <= mMaxAngle` be true to properly work!
            for (i in 0..<entryCount) {
                minAngles[i] -= (minAngles[i] - mMinAngleForSlices) / diff * offset
                if (i == 0) {
                    this.absoluteAngles[0] = minAngles[0]
                } else {
                    this.absoluteAngles[i] = this.absoluteAngles[i - 1] + minAngles[i]
                }
            }

            this.drawAngles = minAngles
        }
    }

    /**
     * Checks if the given index is set to be highlighted.
     *
     * @param index
     * @return
     */
    fun needsHighlight(index: Int): Boolean {
        // no highlight

        if (!valuesToHighlight()) return false

        // check if the xvalue for the given dataset needs highlight
        for (highlight in highlighted!!) if (highlight.x.toInt() == index) return true

        return false
    }

    /**
     * calculates the needed angle for a given value
     *
     * @param value
     * @param yValueSum
     * @return
     */
    /**
     * calculates the needed angle for a given value
     *
     * @param value
     * @return
     */
    private fun calcAngle(value: Float, yValueSum: Float = mData!!.yValueSum): Float {
        return value / yValueSum * mMaxAngle
    }

    /**
     * This will throw an exception, PieChart has no XAxis object.
     *
     * @return
     */
    @Deprecated("")
    override val xAxis: XAxis
        get() = throw RuntimeException("PieChart has no XAxis")

    override fun getIndexForAngle(angle: Float): Int {
        // take the current angle of the chart into consideration

        val a = Utils.getNormalizedAngle(angle - rotationAngle)

        for (i in absoluteAngles.indices) {
            if (this.absoluteAngles[i] > a) return i
        }

        return -1 // return -1 if no index found
    }

    /**
     * Returns the index of the DataSet this x-index belongs to.
     *
     * @param xIndex
     * @return
     */
    fun getDataSetIndexForIndex(xIndex: Int): Int {
        val dataSets = mData!!.dataSets

        for (i in dataSets.indices) {
            if (dataSets[i].getEntryForXValue(xIndex.toFloat(), Float.Companion.NaN) != null) return i
        }

        return -1
    }

    /**
     * Sets the color for the hole that is drawn in the center of the PieChart
     * (if enabled).
     *
     * @param color
     */
    fun setHoleColor(color: Int) {
        (mRenderer as PieChartRenderer).paintHole.setColor(color)
    }

    /**
     * Enable or disable the visibility of the inner tips of the slices behind the hole
     */
    fun setDrawSlicesUnderHole(enable: Boolean) {
        this.isDrawSlicesUnderHoleEnabled = enable
    }

    var centerText: CharSequence?
        /**
         * returns the text that is drawn in the center of the pie-chart
         *
         * @return
         */
        get() = mCenterText
        /**
         * Sets the text String that is displayed in the center of the PieChart.
         *
         * @param text
         */
        set(text) {
            if (text == null) mCenterText = ""
            else mCenterText = text
        }

    /**
     * set this to true to draw the text that is displayed in the center of the
     * pie chart
     *
     * @param enabled
     */
    fun setDrawCenterText(enabled: Boolean) {
        this.isDrawCenterTextEnabled = enabled
    }

    override val requiredLegendOffset: Float
        get() = legendRenderer!!.labelPaint.textSize * 2f

    override val requiredBaseOffset: Float
        get() = 0f

    override val radius: Float
        get() = if (this.circleBox == null) 0f
        else min(circleBox.width() / 2f, circleBox.height() / 2f)

    val centerCircleBox: MPPointF
        /**
         * returns the center of the circlebox
         *
         * @return
         */
        get() = MPPointF.Companion.getInstance(circleBox!!.centerX(), circleBox.centerY())

    /**
     * sets the typeface for the center-text paint
     *
     * @param t
     */
    fun setCenterTextTypeface(t: Typeface?) {
        (mRenderer as PieChartRenderer).paintCenterText.setTypeface(t)
    }

    /**
     * Sets the size of the center text of the PieChart in dp.
     *
     * @param sizeDp
     */
    fun setCenterTextSize(sizeDp: Float) {
        (mRenderer as PieChartRenderer).paintCenterText.setTextSize(
            Utils.convertDpToPixel(sizeDp)
        )
    }

    /**
     * Sets the size of the center text of the PieChart in pixels.
     *
     * @param sizePixels
     */
    fun setCenterTextSizePixels(sizePixels: Float) {
        (mRenderer as PieChartRenderer).paintCenterText.setTextSize(sizePixels)
    }

    /**
     * Sets the offset the center text should have from it's original position in dp. Default x = 0, y = 0
     *
     * @param x
     * @param y
     */
    fun setCenterTextOffset(x: Float, y: Float) {
        mCenterTextOffset.x = Utils.convertDpToPixel(x)
        mCenterTextOffset.y = Utils.convertDpToPixel(y)
    }

    val centerTextOffset: MPPointF
        /**
         * Returns the offset on the x- and y-axis the center text has in dp.
         *
         * @return
         */
        get() = MPPointF.Companion.getInstance(mCenterTextOffset.x, mCenterTextOffset.y)

    /**
     * Sets the color of the center text of the PieChart.
     *
     * @param color
     */
    fun setCenterTextColor(color: Int) {
        (mRenderer as PieChartRenderer).paintCenterText.setColor(color)
    }

    /**
     * Sets the color the transparent-circle should have.
     *
     * @param color
     */
    fun setTransparentCircleColor(color: Int) {
        val p = (mRenderer as PieChartRenderer).paintTransparentCircle
        val alpha = p.getAlpha()
        p.setColor(color)
        p.setAlpha(alpha)
    }

    /**
     * Sets the amount of transparency the transparent circle should have 0 = fully transparent,
     * 255 = fully opaque.
     * Default value is 100.
     *
     * @param alpha 0-255
     */
    fun setTransparentCircleAlpha(alpha: Int) {
        (mRenderer as PieChartRenderer).paintTransparentCircle.setAlpha(alpha)
    }

    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     * Deprecated -> use setDrawEntryLabels(...) instead.
     *
     * @param enabled
     */
    @Deprecated("")
    fun setDrawSliceText(enabled: Boolean) {
        this.isDrawEntryLabelsEnabled = enabled
    }

    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     *
     * @param enabled
     */
    fun setDrawEntryLabels(enabled: Boolean) {
        this.isDrawEntryLabelsEnabled = enabled
    }

    /**
     * Sets the color the entry labels are drawn with.
     *
     * @param color
     */
    fun setEntryLabelColor(color: Int) {
        (mRenderer as PieChartRenderer).paintEntryLabels.setColor(color)
    }

    /**
     * Sets a custom Typeface for the drawing of the entry labels.
     *
     * @param tf
     */
    fun setEntryLabelTypeface(tf: Typeface?) {
        (mRenderer as PieChartRenderer).paintEntryLabels.setTypeface(tf)
    }

    /**
     * Sets the size of the entry labels in dp. Default: 13dp
     *
     * @param size
     */
    fun setEntryLabelTextSize(size: Float) {
        (mRenderer as PieChartRenderer).paintEntryLabels.setTextSize(Utils.convertDpToPixel(size))
    }

    /**
     * Sets whether to draw slices in a curved fashion, only works if drawing the hole is enabled
     * and if the slices are not drawn under the hole.
     *
     * @param enabled draw curved ends of slices
     */
    fun setDrawRoundedSlices(enabled: Boolean) {
        this.isDrawRoundedSlicesEnabled = enabled
    }

    /**
     * If this is enabled, values inside the PieChart are drawn in percent and
     * not with their original value. Values provided for the IValueFormatter to
     * format are then provided in percent.
     *
     * @param enabled
     */
    fun setUsePercentValues(enabled: Boolean) {
        this.isUsePercentValuesEnabled = enabled
    }

    var maxAngle: Float
        get() = mMaxAngle
        /**
         * Sets the max angle that is used for calculating the pie-circle. 360f means
         * it's a full PieChart, 180f results in a half-pie-chart. Default: 360f
         *
         * @param maxangle min 90, max 360
         */
        set(maxangle) {
            var maxangle = maxangle
            if (maxangle > 360) maxangle = 360f

            if (maxangle < 90) maxangle = 90f

            this.mMaxAngle = maxangle
        }

    var minAngleForSlices: Float
        /**
         * The minimum angle slices on the chart are rendered with, default is 0f.
         *
         * @return minimum angle for slices
         */
        get() = mMinAngleForSlices
        /**
         * Set the angle to set minimum size for slices, you must call [.notifyDataSetChanged]
         * and [.invalidate] when changing this, only works if there is enough room for all
         * slices to have the minimum angle.
         *
         * @param minAngle minimum 0, maximum is half of [.setMaxAngle]
         */
        set(minAngle) {
            var minAngle = minAngle
            if (minAngle > (mMaxAngle / 2f)) minAngle = mMaxAngle / 2f
            else if (minAngle < 0) minAngle = 0f

            this.mMinAngleForSlices = minAngle
        }

    protected override fun onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer is PieChartRenderer) {
            (mRenderer as PieChartRenderer).releaseBitmap()
        }
        super.onDetachedFromWindow()
    }

    override val accessibilityDescription: String?
        get() {
            val pieData = mData

            var entryCount = 0
            if (pieData != null) entryCount = pieData.entryCount

            val builder = StringBuilder()

            builder.append(String.format(Locale.getDefault(), "The pie chart has %d entries.", entryCount))

            for (i in 0..<entryCount) {
                val entry = pieData!!.dataSet!!.getEntryForIndex(i)
                val percentage = (entry!!.value / pieData.yValueSum) * 100
                builder.append(
                    String.format(
                        Locale.getDefault(), "%s has %.2f percent pie taken",
                        (if (TextUtils.isEmpty(entry.label)) "No Label" else entry.label),
                        percentage
                    )
                )
            }

            return builder.toString()
        }
}
