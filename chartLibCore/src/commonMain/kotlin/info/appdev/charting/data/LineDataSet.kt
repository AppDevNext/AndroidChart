package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.DashEffect
import info.appdev.charting.utils.convertDpToPixel

/**
 * LineDataSet describes a line-chart-able DataSet.
 *
 * Two Android-only members were removed in favor of side-channels defined in
 * `chartLib` (see `LineDataSetAndroid.kt`), since neither has a portable KMP
 * equivalent:
 * - `fillFormatter` (an [info.appdev.charting.formatter.IFillFormatter]) is now
 *   an extension property. As a result, `.copy()` no longer propagates a
 *   custom `fillFormatter` to the copy (it did before, along with the other
 *   fields in the old `copy(LineDataSet)` helper).
 * - `setCircleColors(colors: IntArray, context: Context)`, the
 *   `ContextCompat.getColor(...)`-resolving overload, is now an extension
 *   function taking a `LineDataSet<*>` receiver.
 */
open class LineDataSet<T : BaseEntry<Float>>(yVals: MutableList<T> = mutableListOf(), label: String = "") : LineRadarDataSet<T>(yVals, label), ILineDataSet<T> {
    /**
     * Drawing mode for this line dataset
     */
    private var mLineDataSetMode: Mode = Mode.LINEAR

    /**
     * Sets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. Make sure that the colors
     * are already prepared (by calling getResources().getColor(...)) before
     * adding them to the DataSet.
     */
    var circleColors: MutableList<Int> = mutableListOf()

    /**
     * the color of the inner circles
     */
    private var mCircleHoleColor = ColorTemplate.WHITE

    /**
     * the radius of the circle-shaped value indicators
     */
    private var mCircleRadius = 8f

    /**
     * the hole radius of the circle-shaped value indicators
     */
    private var mCircleHoleRadius = 4f

    /**
     * sets the intensity of the cubic lines
     */
    private var mCubicIntensity = 0.2f

    /**
     * the path effect of this DataSet that makes dashed lines possible
     */
    private var mDashPathEffect: DashEffect? = null

    /**
     * if true, drawing circles is enabled
     */
    private var mDrawCircles = true

    private var mDrawCircleHole = true

    init {
        // default colors
        // mColors.add(Color.rgb(192, 255, 140));
        // mColors.add(Color.rgb(255, 247, 140));
        circleColors.add(ColorTemplate.argb(140, 234, 255))
    }

    @Suppress("UNCHECKED_CAST")
    override fun copy(): DataSet<T>? {
        val entries: MutableList<EntryFloat> = mutableListOf()
        for (i in entriesInternal.indices) {
            entries.add((entriesInternal[i] as EntryFloat).copy())
        }
        val copied = LineDataSet<EntryFloat>(entries, label)
        copy(copied)
        return copied as DataSet<T>
    }

    protected fun copy(lineDataSet: LineDataSet<*>) {
        super.copy((lineDataSet as BaseDataSet<*>?)!!)
        lineDataSet.circleColors = this.circleColors
        lineDataSet.mCircleHoleColor = mCircleHoleColor
        lineDataSet.mCircleHoleRadius = mCircleHoleRadius
        lineDataSet.mCircleRadius = mCircleRadius
        lineDataSet.mCubicIntensity = mCubicIntensity
        lineDataSet.mDashPathEffect = mDashPathEffect
        lineDataSet.mDrawCircleHole = mDrawCircleHole
        lineDataSet.mDrawCircles = mDrawCircleHole
        lineDataSet.mLineDataSetMode = mLineDataSetMode
    }

    /**
     * Enables the line to be drawn in dashed mode, e.g. like this
     * "- - - - - -". THIS ONLY WORKS IF HARDWARE-ACCELERATION IS TURNED OFF.
     * Keep in mind that hardware acceleration boosts performance.
     *
     * @param lineLength  the length of the line pieces
     * @param spaceLength the length of space in between the pieces
     * @param phase       offset, in degrees (normally, use 0)
     */
    fun enableDashedLine(lineLength: Float, spaceLength: Float, phase: Float) {
        mDashPathEffect = DashEffect(
            floatArrayOf(
                lineLength, spaceLength
            ), phase
        )
    }

    override var lineMode: Mode
        get() = mLineDataSetMode
        set(value) {
            mLineDataSetMode = value
        }

    /**
     * Sets the intensity for cubic lines (if enabled). Max = 1f = very cubic,
     * Min = 0.05f = low cubic effect, Default: 0.2f
     */
    override var cubicIntensity: Float
        get() = mCubicIntensity
        set(value) {
            var intensity = value
            if (intensity > 1f) {
                intensity = 1f
            }
            if (intensity < 0.05f) {
                intensity = 0.05f
            }

            mCubicIntensity = intensity
        }

    override val isDrawCubicEnabled: Boolean
        get() = mLineDataSetMode == Mode.CUBIC_BEZIER

    override val isDrawSteppedEnabled: Boolean
        get() = mLineDataSetMode == Mode.STEPPED

    /**
     * Sets the radius of the drawn circles.
     * Default radius = 4f, Min = 1f
     */
    override var circleRadius: Float
        get() = mCircleRadius
        set(value) {
            if (value >= 1f) {
                mCircleRadius = value.convertDpToPixel()
            } else {
                println("Circle radius cannot be < 1")
            }
        }

    /**
     * Sets the hole radius of the drawn circles.
     * Default radius = 2f, Min = 0.5f
     */
    override var circleHoleRadius: Float
        get() = mCircleHoleRadius
        set(value) {
            if (value >= 0.5f) {
                mCircleHoleRadius = value.convertDpToPixel()
            } else {
                println("Circle radius cannot be < 0.5")
            }
        }

    override fun getCircleColor(index: Int): Int {
        return circleColors[index]
    }

    override val circleColorCount: Int
        get() = circleColors.size

    override var isDrawCircles: Boolean
        get() = mDrawCircles
        set(value) {
            mDrawCircles = value
        }
    override var circleHoleColor: Int
        get() = mCircleHoleColor
        set(value) {
            mCircleHoleColor = value
        }
    override var isDrawCircleHoleEnabled: Boolean
        get() = mDrawCircleHole
        set(value) {
            mDrawCircleHole = value
        }
    override var dashPathEffect: DashEffect?
        get() = mDashPathEffect
        set(value) {
            mDashPathEffect = value
        }

    /**
     * set it with method enableDashedLine(..)
     */
    override var isDashedLineEnabled: Boolean
        get() = mDashPathEffect != null
        set(_) {
            mDashPathEffect = null
        }

    /**
     * Sets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. Make sure that the colors
     * are already prepared (by calling getResources().getColor(...)) before
     * adding them to the DataSet.
     */
    fun setCircleColors(vararg colors: Int) {
        this.circleColors = ColorTemplate.createColors(colors)
    }

    /**
     * Sets the one and ONLY color that should be used for this DataSet.
     * Internally, this recreates the colors array and adds the specified color.
     */
    fun setCircleColor(color: Int) {
        resetCircleColors()
        circleColors.add(color)
    }

    /**
     * resets the circle-colors array and creates a new one
     */
    fun resetCircleColors() {
        circleColors.clear()
    }

    enum class Mode {
        LINEAR,
        STEPPED,
        CUBIC_BEZIER,
        HORIZONTAL_BEZIER
    }
}
