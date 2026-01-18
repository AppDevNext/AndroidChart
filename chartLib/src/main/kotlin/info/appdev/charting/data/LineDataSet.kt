package info.appdev.charting.data

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import info.appdev.charting.formatter.DefaultFillFormatter
import info.appdev.charting.formatter.IFillFormatter
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.convertDpToPixel
import timber.log.Timber

open class LineDataSet<T, N>(yVals: MutableList<T> = mutableListOf(), label: String = "") : LineRadarDataSet<T, N>(yVals, label), ILineDataSet<T, N>
    where T : BaseEntry<N>, N : Number, N : Comparable<N> {
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
    @ColorInt
    var circleColors: MutableList<Int> = mutableListOf()

    /**
     * the color of the inner circles
     */
    @ColorInt
    private var mCircleHoleColor = Color.WHITE

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
    private var mDashPathEffect: DashPathEffect? = null

    /**
     * formatter for customizing the position of the fill-line
     */
    private var mFillFormatter: IFillFormatter? = DefaultFillFormatter()

    /**
     * if true, drawing circles is enabled
     */
    private var mDrawCircles = true

    private var mDrawCircleHole = true

    init {
        // default colors
        // mColors.add(Color.rgb(192, 255, 140));
        // mColors.add(Color.rgb(255, 247, 140));
        circleColors.add(Color.rgb(140, 234, 255))
    }

    override fun copy(): DataSet<T, N>? {
        val entries: MutableList<T> = mutableListOf()
        entries.addAll(mEntries)
        val copied = LineDataSet(entries, label)
        copy(copied)
        return copied
    }

    protected fun copy(lineDataSet: LineDataSet<T, N>) {
        super.copy(lineDataSet)
        lineDataSet.circleColors = this.circleColors
        lineDataSet.mCircleHoleColor = this.mCircleHoleColor
        lineDataSet.mCircleHoleRadius = this.mCircleHoleRadius
        lineDataSet.mCircleRadius = this.mCircleRadius
        lineDataSet.mCubicIntensity = this.mCubicIntensity
        lineDataSet.mDashPathEffect = this.mDashPathEffect
        lineDataSet.mDrawCircleHole = this.mDrawCircleHole
        lineDataSet.mDrawCircles = this.mDrawCircleHole
        lineDataSet.mFillFormatter = this.mFillFormatter
        lineDataSet.mLineDataSetMode = this.mLineDataSetMode
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
        mDashPathEffect = DashPathEffect(
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
                Timber.e("Circle radius cannot be < 1")
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
                Timber.e("Circle radius cannot be < 0.5")
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
    override var dashPathEffect: DashPathEffect?
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
     * Sets a custom IFillFormatter to the chart that handles the position of the
     * filled-line for each DataSet. Set this to null to use the default logic.
     */
    override var fillFormatter: IFillFormatter?
        get() = mFillFormatter
        set(value) {
            mFillFormatter = value ?: DefaultFillFormatter()
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
     * ets the colors that should be used for the circles of this DataSet.
     * Colors are reused as soon as the number of Entries the DataSet represents
     * is higher than the size of the colors array. You can use
     * "new String[] { R.color.red, R.color.green, ... }" to provide colors for
     * this method. Internally, the colors are resolved using
     * getResources().getColor(...)
     */
    fun setCircleColors(@ColorInt colors: IntArray, context: Context) {
        val clrs = this.circleColors
        clrs.clear()

        for (color in colors) {
            clrs.add(ContextCompat.getColor(context, color))
        }

        this.circleColors = clrs
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
