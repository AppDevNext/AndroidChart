package info.appdev.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import info.appdev.charting.components.XAxis
import info.appdev.charting.data.PieData
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.highlight.PieHighlighter
import info.appdev.charting.renderer.PieChartRenderer
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.PointF.Companion.getInstance
import info.appdev.charting.utils.PointF.Companion.recycleInstance
import info.appdev.charting.utils.Utils
import info.appdev.charting.utils.convertDpToPixel
import java.util.Locale
import java.util.Objects
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * View that represents a pie chart. Draws cake like slices.
 */
@Suppress("unused")
class PieChart : PieRadarChartBase<PieData> {
    /**
     * rect object that represents the bounds of the piechart, needed for
     * drawing the circle
     */
    val circleBox: RectF = RectF()

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
     */
    var drawAngles: FloatArray = FloatArray(1)
        private set

    /**
     * returns the absolute angles of the different chart slices (where the
     * slices end)
     */
    var absoluteAngles: FloatArray = FloatArray(1)
        private set

    /**
     * returns true if the hole in the center of the pie-chart is set to be
     * visible, false if not
     */
    var isDrawHoleEnabled: Boolean = true

    /**
     * Returns true if the inner tips of the slices are visible behind the hole,
     * false if not.
     *
     * @return true if slices are visible behind the hole.
     */
    var isDrawSlicesUnderHoleEnabled: Boolean = false
        private set

    /**
     * Returns true if using percentage values is enabled for the chart.
     */
    var isUsePercentValuesEnabled: Boolean = false
        private set

    /**
     * Returns true if the chart is set to draw each end of a pie-slice
     * "rounded".
     */
    var isDrawRoundedSlicesEnabled: Boolean = false
        private set

    /**
     * variable for the text that is drawn in the center of the pie-chart
     */
    private var mCenterText: CharSequence = ""

    private val mCenterTextOffset = getInstance(0f, 0f)

    /**
     * sets the radius of the hole in the center of the piechart in percent of
     * the maximum radius (max = the radius of the whole chart), default 50%
     *
     * indicates the size of the hole in the center of the piechart,
     * default: radius / 2
     */
    var holeRadius: Float = 50f

    /**
     * sets the radius of the transparent circle that is drawn next to the hole
     * in the piechart in percent of the maximum radius (max = the radius of the
     * whole chart), default 55% -> means 5% larger than the center-hole by
     * default
     */
    var transparentCircleRadius: Float = 55f

    /**
     * returns true if drawing the center text is enabled
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

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mRenderer = PieChartRenderer(this, mAnimator, viewPortHandler)
        highlighter = PieHighlighter(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mData == null)
            return

        mRenderer?.drawData(canvas)

        if (valuesToHighlight())
            mRenderer?.drawHighlighted(canvas, highlighted!!)

        mRenderer?.drawExtras(canvas)

        mRenderer?.drawValues(canvas)

        legendRenderer?.renderLegend(canvas)

        drawDescription(canvas)

        drawMarkers(canvas)
    }

    override fun calculateOffsets() {
        super.calculateOffsets()

        mData?.let { data ->
            val diameter = diameter
            val radius = diameter / 2f

            val shift = data.dataSet.selectionShift

            // create the circle box that will contain the pie-chart (the bounds of
            // the pie-chart)
            circleBox.set(
                centerOffsets.x - radius + shift,
                centerOffsets.y - radius + shift,
                centerOffsets.x + radius - shift,
                centerOffsets.y + radius - shift
            )

            recycleInstance(centerOffsets)
        }
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
                        * mAnimator.phaseY).toDouble()
            )
        ) + center.x).toFloat()
        val y = (r
                * sin(
            Math.toRadians(
                ((rotationAngle + this.absoluteAngles[entryIndex] - offset)
                        * mAnimator.phaseY).toDouble()
            )
        ) + center.y).toFloat()

        recycleInstance(center)
        return floatArrayOf(x, y)
    }

    /**
     * calculates the needed angles for the chart slices
     */
    private fun calcAngles() {
        mData?.let { data ->
            val entryCount = data.entryCount

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

            val yValueSum = data.yValueSum

            val dataSets = data.dataSets

            val hasMinAngle = mMinAngleForSlices != 0f && entryCount * mMinAngleForSlices <= mMaxAngle
            val minAngles = FloatArray(entryCount)

            var cnt = 0
            var offset = 0f
            var diff = 0f

            for (i in 0..<data.dataSetCount) {
                val set = dataSets!!.get(i)

                for (j in 0..<set.entryCount) {
                    val drawAngle = calcAngle(abs(set.getEntryForIndex(j)!!.y), yValueSum)

                    if (hasMinAngle) {
                        val temp = drawAngle - mMinAngleForSlices
                        if (temp <= 0) {
                            minAngles[cnt] = mMinAngleForSlices
                            offset -= temp
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
    }

    /**
     * Checks if the given index is set to be highlighted.
     */
    fun needsHighlight(index: Int): Boolean {
        if (!valuesToHighlight())
            return false

        // check if the xvalue for the given dataset needs highlight
        highlighted?.let {
            for (highlight in it)
                if (highlight.x.toInt() == index)
                    return true
        }
        return false
    }

    /**
     * calculates the needed angle for a given value
     */
    private fun calcAngle(value: Float, yValueSum: Float): Float {
        return value / yValueSum * mMaxAngle
    }

    /**
     * This will throw an exception, PieChart has no XAxis object.
     */
    @Deprecated("")
    override val xAxis: XAxis
        get() = throw RuntimeException("PieChart has no XAxis")

    override fun getIndexForAngle(angle: Float): Int {
        // take the current angle of the chart into consideration

        val a = Utils.getNormalizedAngle(angle - rotationAngle)

        for (i in absoluteAngles.indices) {
            if (this.absoluteAngles[i] > a)
                return i
        }

        return -1 // return -1 if no index found
    }

    /**
     * Returns the index of the DataSet this x-index belongs to.
     */
    fun getDataSetIndexForIndex(xIndex: Int): Int {
        val dataSets = mData?.dataSets

        for (i in dataSets!!.indices) {
            if (dataSets[i].getEntryForXValue(xIndex.toFloat(), Float.NaN) != null)
                return i
        }

        return -1
    }

    /**
     * Sets the color for the hole that is drawn in the center of the PieChart
     * (if enabled).
     */
    fun setHoleColor(color: Int) {
        (mRenderer as PieChartRenderer).paintHole.color = color
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
         */
        get() = mCenterText
        /**
         * Sets the text String that is displayed in the center of the PieChart.
         */
        set(text) {
            mCenterText = Objects.requireNonNullElse<CharSequence>(text, "")
        }

    /**
     * set this to true to draw the text that is displayed in the center of the
     * pie chart
     */
    fun setDrawCenterText(enabled: Boolean) {
        this.isDrawCenterTextEnabled = enabled
    }

    override val requiredLegendOffset: Float
        get() {
            legendRenderer?.let {
                return it.labelPaint.textSize * 2f
            }
            return 0f
        }

    override val requiredBaseOffset: Float
        get() = 0f

    override val radius: Float
        get() {
            return min(circleBox.width() / 2f, circleBox.height() / 2f)
        }

    val centerCircleBox: PointF
        get() = getInstance(circleBox.centerX(), circleBox.centerY())

    fun setCenterTextTypeface(t: Typeface?) {
        (mRenderer as PieChartRenderer).paintCenterText.typeface = t
    }

    /**
     * Sets the size of the center text of the PieChart in dp.
     */
    fun setCenterTextSize(sizeDp: Float) {
        (mRenderer as PieChartRenderer).paintCenterText.textSize = sizeDp.convertDpToPixel()
    }

    /**
     * Sets the size of the center text of the PieChart in pixels.
     */
    fun setCenterTextSizePixels(sizePixels: Float) {
        (mRenderer as PieChartRenderer).paintCenterText.textSize = sizePixels
    }

    /**
     * Sets the offset the center text should have from it's original position in dp. Default x = 0, y = 0
     */
    fun setCenterTextOffset(x: Float, y: Float) {
        mCenterTextOffset.x = x.convertDpToPixel()
        mCenterTextOffset.y = y.convertDpToPixel()
    }

    val centerTextOffset: PointF
        /**
         * Returns the offset on the x- and y-axis the center text has in dp.
         */
        get() = getInstance(mCenterTextOffset.x, mCenterTextOffset.y)

    /**
     * Sets the color of the center text of the PieChart.
     */
    fun setCenterTextColor(color: Int) {
        (mRenderer as PieChartRenderer).paintCenterText.color = color
    }

    /**
     * Sets the color the transparent-circle should have.
     */
    fun setTransparentCircleColor(color: Int) {
        val p = (mRenderer as PieChartRenderer).paintTransparentCircle
        val alpha = p.alpha
        p.color = color
        p.alpha = alpha
    }

    /**
     * Sets the amount of transparency the transparent circle should have 0 = fully transparent,
     * 255 = fully opaque.
     * Default value is 100.
     *
     * @param alpha 0-255
     */
    fun setTransparentCircleAlpha(alpha: Int) {
        (mRenderer as PieChartRenderer).paintTransparentCircle.alpha = alpha
    }

    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     * Deprecated -> use setDrawEntryLabels(...) instead.
     *
     */
    @Deprecated("")
    fun setDrawSliceText(enabled: Boolean) {
        this.isDrawEntryLabelsEnabled = enabled
    }

    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     *
     */
    fun setDrawEntryLabels(enabled: Boolean) {
        this.isDrawEntryLabelsEnabled = enabled
    }

    /**
     * Sets the color the entry labels are drawn with.
     *
     */
    fun setEntryLabelColor(color: Int) {
        (mRenderer as PieChartRenderer).paintEntryLabels.color = color
    }

    /**
     * Sets a custom Typeface for the drawing of the entry labels.
     *
     */
    fun setEntryLabelTypeface(tf: Typeface?) {
        (mRenderer as PieChartRenderer).paintEntryLabels.typeface = tf
    }

    /**
     * Sets the size of the entry labels in dp. Default: 13dp
     *
     */
    fun setEntryLabelTextSize(size: Float) {
        (mRenderer as PieChartRenderer).paintEntryLabels.textSize = size.convertDpToPixel()
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
         * @param maxAngle min 90, max 360
         */
        set(value) {
            var maxangle = value
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

    override fun onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer is PieChartRenderer) {
            (mRenderer as PieChartRenderer).releaseBitmap()
        }
        super.onDetachedFromWindow()
    }

    override val accessibilityDescription: String
        get() {
            val pieData = getData()

            var entryCount = 0
            if (pieData != null) entryCount = pieData.entryCount

            val builder = StringBuilder()

            builder.append(String.format(Locale.getDefault(), "The pie chart has %d entries.", entryCount))

            for (i in 0..<entryCount) {
                val entry = pieData!!.dataSet.getEntryForIndex(i)
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
