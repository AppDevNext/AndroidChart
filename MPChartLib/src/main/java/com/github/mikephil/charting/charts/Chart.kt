package com.github.mikephil.charting.charts

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.graphics.createBitmap
import androidx.core.view.size
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.animation.Easing.EasingFunction
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.ChartHighlighter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.IHighlighter
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.DataRenderer
import com.github.mikephil.charting.renderer.LegendRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.max

/**
 * Baseclass of all Chart-Views.
 *
 * @author Philipp Jahoda
 */
@Suppress("unused")
abstract class Chart<E: Entry, D: IDataSet<E>, T : ChartData<E, D>> : ViewGroup, ChartInterface {
    /**
     * Returns true if log-output is enabled for the chart, fals if not.
     */
    /**
     * Set this to true to enable logcat outputs for the chart. Beware that
     * logcat output decreases rendering performance. Default: disabled.
     */
    /**
     * flag that indicates if logging is enabled or not
     */
    var isLogEnabled: Boolean = false

    /**
     * object that holds all data that was originally set for the chart, before
     * it was modified or any filtering algorithms had been applied
     */
    protected var mData: T? = null

    /**
     * Returns true if values can be highlighted via tap gesture, false if not.
     */
    /**
     * Set this to false to prevent values from being highlighted by tap gesture.
     * Values can still be highlighted via drag or programmatically. Default: true
     */
    /**
     * Flag that indicates if highlighting per tap (touch) is enabled
     */
    var isHighlightPerTapEnabled: Boolean = true

    /**
     * If set to true, chart continues to scroll after touch up default: true
     */
    /**
     * If set to true, chart continues to scroll after touch up. Default: true.
     */
    /**
     * If set to true, chart continues to scroll after touch up
     */
    var isDragDecelerationEnabled: Boolean = true

    /**
     * Deceleration friction coefficient in [0 ; 1] interval, higher values
     * indicate that speed will decrease slowly, for example if it set to 0, it
     * will stop immediately. 1 is an invalid value, and will be converted to
     * 0.999f automatically.
     */
    private var mDragDecelerationFrictionCoef = 0.9f

    /**
     * default value-formatter, number of digits depends on provided chart-data
     */
    protected var mDefaultValueFormatter: DefaultValueFormatter = DefaultValueFormatter(0)

    /**
     * paint object used for drawing the description text in the bottom right
     * corner of the chart
     */
    protected var mDescPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * paint object for drawing the information text when there are no values in
     * the chart
     */
    protected var mInfoPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * the object representing the labels on the x-axis
     */
    protected val mXAxis: XAxis = XAxis()

    /**
     * if true, touch gestures are enabled on the chart
     */
    protected var mTouchEnabled: Boolean = true

    /**
     * Returns the Description object of the chart that is responsible for holding all information related
     * to the description text that is displayed in the bottom right corner of the chart (by default).
     */
    /**
     * Sets a new Description object for the chart.
     */
    /**
     * the object responsible for representing the description text
     */
    var description: Description = Description()

    /**
     * Returns the Legend object of the chart. This method can be used to get an
     * instance of the legend in order to customize the automatically generated
     * Legend.
     */
    /**
     * the legend object containing all data associated with the legend
     */
    val legend: Legend = Legend()

    /**
     * listener that is called when a value on the chart is selected
     */
    protected var mSelectionListener: OnChartValueSelectedListener? = null

    protected var mChartTouchListener: ChartTouchListener<*>? = null

    /**
     * text that is displayed when the chart is empty
     */
    private var mNoDataText = "No chart data available."

    /**
     * Returns the custom gesture listener.
     */
    /**
     * Sets a gesture-listener for the chart for custom callbacks when executing
     * gestures on the chart surface.
     */
    /**
     * Gesture listener for custom callbacks when making gestures on the chart.
     */
    var onChartGestureListener: OnChartGestureListener? = null

    /**
     * Returns the ViewPortHandler of the chart that is responsible for the
     * content area of the chart and its offsets and dimensions.
     */
    /**
     * object that manages the bounds and drawing constraints of the chart
     */
    var viewPortHandler: ViewPortHandler = ViewPortHandler()
        protected set

    /**
     * Returns the renderer object responsible for rendering / drawing the
     * Legend.
     */
    val legendRenderer: LegendRenderer = LegendRenderer(this.viewPortHandler, this.legend)

    /**
     * object responsible for rendering the data
     */
    protected var mRenderer: DataRenderer? = null

    var highlighter: IHighlighter? = null
        protected set

    /**
     * object responsible for animations
     */
    protected val mAnimator = ChartAnimator { // ViewCompat.postInvalidateOnAnimation(Chart.this);
        postInvalidate()
    }

    /**
     * Extra offsets to be appended to the viewport
     */
    private var mExtraTopOffset = 0f
    private var mExtraRightOffset = 0f
    private var mExtraBottomOffset = 0f
    private var mExtraLeftOffset = 0f

    /**
     * Additional data on top of dynamically generated description. This can be set by the user.
     */
    var accessibilitySummaryDescription: String? = ""

    /**
     * default constructor for initialization in code
     */
    constructor(context: Context) : super(context) {
    }

    /**
     * constructor for initialization in xml
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    /**
     * even more awesome constructor
     */
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
    }

    /**
     * The maximum distance in dp away from an entry causing it to highlight.
     */
    protected var mMaxHighlightDistance: Float = 0f

    /**
     * initialize all paints and stuff
     */
    init {
        setWillNotDraw(false)

        // initialize the utils
        Utils.init(context)
        mMaxHighlightDistance = Utils.convertDpToPixel(500f)

        mInfoPaint.setColor(Color.rgb(247, 189, 51)) // orange
        mInfoPaint.textAlign = Align.CENTER
        mInfoPaint.textSize = Utils.convertDpToPixel(12f)

        if (this.isLogEnabled) {
            Log.i("", "Chart.init()")

            // enable being detected by ScreenReader
            setFocusable(true)
        }
    }

    // public void initWithDummyData() {
    // ColorTemplate template = new ColorTemplate();
    // template.addColorsForDataSets(ColorTemplate.COLORFUL_COLORS,
    // getContext());
    //
    // setColorTemplate(template);
    // setDrawYValues(false);
    //
    // ArrayList<String> xVals = new ArrayList<String>();
    // Calendar calendar = Calendar.getInstance();
    // for (int i = 0; i < 12; i++) {
    // xVals.add(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
    // Locale.getDefault()));
    // }
    //
    // ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
    // for (int i = 0; i < 3; i++) {
    //
    // ArrayList<Entry> yVals = new ArrayList<Entry>();
    //
    // for (int j = 0; j < 12; j++) {
    // float val = (float) (Math.random() * 100);
    // yVals.add(new Entry(val, j));
    // }
    //
    // DataSet set = new DataSet(yVals, "DataSet " + i);
    // dataSets.add(set); // add the datasets
    // }
    // // create a data object with the datasets
    // ChartData data = new ChartData(xVals, dataSets);
    // setData(data);
    // invalidate();
    // }
    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     */
    open fun setData(data: T?) {
        mData = data
        mOffsetsCalculated = false

        if (data == null) {
            return
        }

        // calculate how many digits are needed
        setupDefaultFormatter(data.yMin, data.yMax)

        // let the chart know there is new data
        notifyDataSetChanged()

        if (this.isLogEnabled) {
            Log.i(LOG_TAG, "Data is set.")
        }
    }

    /**
     * Clears the chart from all data (sets it to null) and refreshes it (by
     * calling invalidate()).
     */
    fun clear() {
        mData = null
        mOffsetsCalculated = false
        this.highlighted = null
        mChartTouchListener?.setLastHighlighted(null)
        invalidate()
    }

    /**
     * Removes all DataSets (and thereby Entries) from the chart. Does not set the data object to null. Also refreshes the
     * chart by calling invalidate().
     */
    fun clearValues() {
        mData?.clearValues()
        invalidate()
    }

    val isEmpty: Boolean
        /**
         * Returns true if the chart is empty (meaning it's data object is either
         * null or contains no entries).
         */
        get() {
            return mData?.let { it.entryCount <= 0 } ?: true
        }

    /**
     * Lets the chart know its underlying data has changed and performs all
     * necessary recalculations. It is crucial that this method is called
     * everytime data is changed dynamically. Not calling this method can lead
     * to crashes or unexpected behaviour.
     */
    abstract fun notifyDataSetChanged()

    /**
     * Calculates the offsets of the chart to the border depending on the
     * position of an eventual legend or depending on the length of the y-axis
     * and x-axis labels and their position
     */
    protected abstract fun calculateOffsets()

    /**
     * Calculates the y-min and y-max value and the y-delta and x-delta value
     */
    protected abstract fun calcMinMax()

    /**
     * Calculates the required number of digits for the values that might be
     * drawn in the chart (if enabled), and creates the default-value-formatter
     */
    protected fun setupDefaultFormatter(min: Float, max: Float) {
        val reference = if (mData.let { it != null && it.entryCount < 2 }) {
            max(abs(min), abs(max))
        } else {
            abs(max - min)
        }

        val digits = Utils.getDecimals(reference)

        // setup the formatter with a new number of digits
        mDefaultValueFormatter.setup(digits)
    }

    /**
     * flag that indicates if offsets calculation has already been done or not
     */
    private var mOffsetsCalculated = false

    override fun onDraw(canvas: Canvas) {
        // super.onDraw(canvas);

        if (mData == null) {
            val hasText = !TextUtils.isEmpty(mNoDataText)

            if (hasText) {
                val pt = this.center

                when (mInfoPaint.textAlign) {
                    Align.LEFT -> {
                        pt.x = 0f
                        canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint)
                    }

                    Align.RIGHT -> {
                        pt.x *= 2.0.toFloat()
                        canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint)
                    }

                    else -> canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint)
                }
            }

            return
        }

        if (!mOffsetsCalculated) {
            calculateOffsets()
            mOffsetsCalculated = true
        }
    }

    /**
     * Draws the description text in the bottom right corner of the chart (per default)
     */
    protected fun drawDescription(c: Canvas) {
        // check if description should be drawn

        if (description.isEnabled) {
            val position = description.position

            mDescPaint.setTypeface(description.typeface)
            mDescPaint.textSize = description.textSize
            mDescPaint.setColor(description.textColor)
            mDescPaint.textAlign = description.textAlign

            val x: Float
            val y: Float

            // if no position specified, draw on default position
            if (position == null) {
                x = width - viewPortHandler.offsetRight() - description.xOffset
                y = height - viewPortHandler.offsetBottom() - description.yOffset
            } else {
                x = position.x
                y = position.y
            }

            c.drawText(description.text, x, y, mDescPaint)
        }
    }

    /**
     * Returns the array of currently highlighted values. This might a null or
     * empty array if nothing is highlighted.
     */
    /**
     * array of Highlight objects that reference the highlighted slices in the
     * chart
     */
    var highlighted: Array<Highlight>? = null
        protected set

    override val maxHighlightDistance: Float
        get() = mMaxHighlightDistance

    /**
     * Sets the maximum distance in screen dp a touch can be away from an entry to cause it to get highlighted.
     * Default: 500dp
     */
    fun setMaxHighlightDistance(distDp: Float) {
        mMaxHighlightDistance = Utils.convertDpToPixel(distDp)
    }

    /**
     * Returns true if there are values to highlight, false if there are no
     * values to highlight. Checks if the highlight array is null, has a length
     * of zero or if the first object is null.
     */
    fun valuesToHighlight(): Boolean {
        return !highlighted.isNullOrEmpty()
    }

    /**
     * Sets the last highlighted value for the touchlistener.
     */
    protected fun setLastHighlighted(highs: Array<Highlight>?) {
        if (highs == null || highs.isEmpty()) {
            mChartTouchListener?.setLastHighlighted(null)
        } else {
            mChartTouchListener?.setLastHighlighted(highs[0])
        }
    }

    /**
     * Highlights the values at the given indices in the given DataSets. Provide
     * null or an empty array to undo all highlighting. This should be used to
     * programmatically highlight values.
     * This method *will not* call the listener.
     */
    fun highlightValues(highs: Array<Highlight>?) {
        // set the indices to highlight

        this.highlighted = highs

        setLastHighlighted(highs)

        // redraw the chart
        invalidate()
    }

    fun highlightValues(highs: MutableList<Highlight>, markers: MutableList<IMarker>) {
        require(highs.size == markers.size) { "Markers and highs must be mutually corresponding. High size = " + highs.size + " Markers size = " + markers.size }
        setMarkers(markers)
        highlightValues(highs.toTypedArray<Highlight>())
    }

    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     *
     * @param x            The x-value to highlight
     * @param y            The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     * @param dataIndex    The data index to search in (only used in CombinedChartView currently)
     * @param callListener Should the listener be called for this change
     */
    /**
     * Highlights the value at the given x-value and y-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * This method will call the listener.
     *
     * @param x            The x-value to highlight
     * @param y            The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     */
    /**
     * Highlights the value at the given x-value and y-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * This method will call the listener.
     *
     * @param x            The x-value to highlight
     * @param y            The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     * @param dataIndex    The data index to search in (only used in CombinedChartView currently)
     */
    @JvmOverloads
    open fun highlightValue(x: Float, y: Float = Float.NaN, dataSetIndex: Int = -1, dataIndex: Int = -1, callListener: Boolean = true) {
        if (dataSetIndex < 0 || mData.let { it != null && dataSetIndex >= it.dataSetCount }) {
            highlightValue(null, callListener)
        } else {
            highlightValue(Highlight(x, y, dataSetIndex, dataIndex), callListener)
        }
    }

    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     *
     * @param x            The x-value to highlight
     * @param y            The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     * @param callListener Should the listener be called for this change
     */
    fun highlightValue(x: Float, y: Float, dataSetIndex: Int, callListener: Boolean) {
        highlightValue(x, y, dataSetIndex, -1, callListener)
    }

    /**
     * Highlights the values represented by the provided Highlight object
     * This method *will not* call the listener.
     *
     * @param highlight contains information about which entry should be highlighted
     */
    fun highlightValue(highlight: Highlight?) {
        highlightValue(highlight, false)
    }

    /**
     * Highlights the value selected by touch gesture. Unlike
     * highlightValues(...), this generates a callback to the
     * OnChartValueSelectedListener.
     *
     * @param high         - the highlight object
     * @param callListener - call the listener
     */
    fun highlightValue(high: Highlight?, callListener: Boolean) {
        var high = high
        var e: E? = null

        if (high == null) {
            this.highlighted = null
        } else {
            if (this.isLogEnabled) {
                Log.i(LOG_TAG, "Highlighted: $high")
            }

            e = mData?.getEntryForHighlight(high)
            if (e == null) {
                this.highlighted = null
                high = null
            } else {
                // set the indices to highlight

                this.highlighted = arrayOf(high)
            }
        }

        setLastHighlighted(this.highlighted)

        if (callListener && mSelectionListener != null) {
            if (!valuesToHighlight()) {
                mSelectionListener?.onNothingSelected()
            } else {
                // notify the listener
                mSelectionListener?.onValueSelected(e, high)
            }
        }

        // redraw the chart
        invalidate()
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the
     * selected value at the given touch point inside the Line-, Scatter-, or
     * CandleStick-Chart.
     */
    open fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.")
            return null
        } else {
            return this.highlighter?.getHighlight(x, y)
        }
    }

    var onTouchListener: ChartTouchListener<*>?
        /**
         * Returns an instance of the currently active touch listener.
         */
        get() = mChartTouchListener
        /**
         * Set a new (e.g. custom) ChartTouchListener NOTE: make sure to
         * setTouchEnabled(true); if you need touch gestures on the chart
         */
        set(l) {
            this.mChartTouchListener = l
        }

    /**
     * returns true if drawing the marker is enabled when tapping on values
     * (use the setMarker(IMarker marker) method to specify a marker)
     */
    /**
     * if set to true, the marker view is drawn when a value is clicked
     */
    var isDrawMarkersEnabled: Boolean = true
        protected set

    /**
     * returns the marker that is set as a marker view for the chart
     *
     */
    /**
     * the view that represents the marker
     */
    var marker: MutableList<IMarker> = ArrayList()
        protected set

    /**
     * draws all MarkerViews on the highlighted positions
     */
    protected open fun drawMarkers(canvas: Canvas?) {
        // if there is no marker view or drawing marker is disabled

        if (!this.isDrawMarkersEnabled || !valuesToHighlight()) {
            return
        }

        val highlighted = highlighted ?: return
        val data = mData ?: return

        for (i in highlighted.indices) {
            val highlight = highlighted[i]

            // When changing data sets and calling animation functions, sometimes an erroneous highlight is generated
            // on the dataset that is removed. Null check to prevent crash
            val set = data.getDataSetByIndex(highlight.dataSetIndex)
            if (!set.isVisible) {
                continue
            }

            val e = data.getEntryForHighlight(highlight)

            // make sure entry not null before using it
            if (e == null) {
                continue
            }
            val entryIndex = set.getEntryIndex(e)

            if (entryIndex > set.entryCount * mAnimator.phaseX) {
                continue
            }

            val pos = getMarkerPosition(highlight)

            // check bounds
            if (!viewPortHandler.isInBounds(pos[0], pos[1])) {
                continue
            }

            // callbacks to update the content
            if (!marker.isEmpty()) {
                val markerIndex = i % marker.size
                val markerItem = marker[markerIndex]
                markerItem.refreshContent(e, highlight)

                // draw the marker
                markerItem.draw(canvas, pos[0], pos[1])
            }
        }
    }

    /**
     * Returns the actual position in pixels of the MarkerView for the given
     * Highlight object.
     */
    protected open fun getMarkerPosition(high: Highlight): FloatArray {
        return floatArrayOf(high.drawX, high.drawY)
    }

    val animator: ChartAnimator
        /**
         * Returns the animator responsible for animating chart values.
         */
        get() = mAnimator

    var dragDecelerationFrictionCoef: Float
        /**
         * Returns drag deceleration friction coefficient
         */
        get() = mDragDecelerationFrictionCoef
        /**
         * Deceleration friction coefficient in [0 ; 1] interval, higher values
         * indicate that speed will decrease slowly, for example if it set to 0, it
         * will stop immediately. 1 is an invalid value, and will be converted to
         * 0.999f automatically.
         */
        set(newValue) {
            var newValue = newValue
            if (newValue < 0f) {
                newValue = 0f
            }

            if (newValue >= 1f) {
                newValue = 0.999f
            }

            mDragDecelerationFrictionCoef = newValue
        }

    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart. ANIMATIONS
     * ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param easingX a custom easing function to be used on the animation phase
     * @param easingY a custom easing function to be used on the animation phase
     */
    fun animateXY(durationMillisX: Int, durationMillisY: Int, easingX: EasingFunction?, easingY: EasingFunction?) {
        mAnimator.animateXY(durationMillisX, durationMillisY, easingX, easingY)
    }

    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart. ANIMATIONS
     * ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param easing a custom easing function to be used on the animation phase
     */
    fun animateXY(durationMillisX: Int, durationMillisY: Int, easing: EasingFunction?) {
        mAnimator.animateXY(durationMillisX, durationMillisY, easing)
    }

    /**
     * Animates the rendering of the chart on the x-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param easing a custom easing function to be used on the animation phase
     */
    fun animateX(durationMillis: Int, easing: EasingFunction?) {
        mAnimator.animateX(durationMillis, easing)
    }

    /**
     * Animates the rendering of the chart on the y-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param easing a custom easing function to be used on the animation phase
     */
    fun animateY(durationMillis: Int, easing: EasingFunction?) {
        mAnimator.animateY(durationMillis, easing)
    }

    /**
     * Animates the rendering of the chart on the x-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    fun animateX(durationMillis: Int) {
        mAnimator.animateX(durationMillis)
    }

    /**
     * Animates the rendering of the chart on the y-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    fun animateY(durationMillis: Int) {
        mAnimator.animateY(durationMillis)
    }

    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart. ANIMATIONS
     * ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    fun animateXY(durationMillisX: Int, durationMillisY: Int) {
        mAnimator.animateXY(durationMillisX, durationMillisY)
    }


    open val xAxis: XAxis
        /**
         * Returns the object representing all x-labels, this method can be used to
         * acquire the XAxis object and modify it (e.g. change the position of the
         * labels, styling, etc.)
         */
        get() = mXAxis

    /**
     * Returns the default IValueFormatter that has been determined by the chart
     * considering the provided minimum and maximum values.
     */
    override val defaultValueFormatter: IValueFormatter?
        get() = mDefaultValueFormatter

    /**
     * set a selection listener for the chart
     */
    fun setOnChartValueSelectedListener(l: OnChartValueSelectedListener?) {
        this.mSelectionListener = l
    }

    val yMax: Float?
        /**
         * returns the current y-max value across all DataSets
         */
        get() = mData?.yMax

    val yMin: Float?
        /**
         * returns the current y-min value across all DataSets
         */
        get() = mData?.yMin

    override val xChartMax: Float
        get() = mXAxis.mAxisMaximum

    override val xChartMin: Float
        get() = mXAxis.axisMinimum

    override val xRange: Float
        get() = mXAxis.mAxisRange

    val center: MPPointF
        /**
         * Returns a recyclable MPPointF instance.
         * Returns the center point of the chart (the whole View) in pixels.
         */
        get() = MPPointF.Companion.getInstance(width / 2f, height / 2f)

    /**
     * Returns a recyclable MPPointF instance.
     * Returns the center of the chart taking offsets under consideration.
     * (returns the center of the content rectangle)
     */
    override val centerOffsets: MPPointF
        get() = viewPortHandler.contentCenter

    /**
     * Sets extra offsets (around the chart view) to be appended to the
     * auto-calculated offsets.
     */
    fun setExtraOffsets(left: Float, top: Float, right: Float, bottom: Float) {
        this.extraLeftOffset = left
        this.extraTopOffset = top
        this.extraRightOffset = right
        this.extraBottomOffset = bottom
    }

    var extraTopOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's top
         */
        get() = mExtraTopOffset
        /**
         * Set an extra offset to be appended to the viewport's top
         */
        set(offset) {
            mExtraTopOffset = Utils.convertDpToPixel(offset)
        }

    var extraRightOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's right
         */
        get() = mExtraRightOffset
        /**
         * Set an extra offset to be appended to the viewport's right
         */
        set(offset) {
            mExtraRightOffset = Utils.convertDpToPixel(offset)
        }

    var extraBottomOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's bottom
         */
        get() = mExtraBottomOffset
        /**
         * Set an extra offset to be appended to the viewport's bottom
         */
        set(offset) {
            mExtraBottomOffset = Utils.convertDpToPixel(offset)
        }

    var extraLeftOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's left
         */
        get() = mExtraLeftOffset
        /**
         * Set an extra offset to be appended to the viewport's left
         */
        set(offset) {
            mExtraLeftOffset = Utils.convertDpToPixel(offset)
        }

    /**
     * Sets the text that informs the user that there is no data available with
     * which to draw the chart.
     */
    fun setNoDataText(text: String) {
        mNoDataText = text
    }

    /**
     * Sets the color of the no data text.
     */
    fun setNoDataTextColor(color: Int) {
        mInfoPaint.setColor(color)
    }

    /**
     * Sets the typeface to be used for the no data text.
     */
    fun setNoDataTextTypeface(tf: Typeface?) {
        mInfoPaint.setTypeface(tf)
    }

    /**
     * alignment of the no data text
     */
    fun setNoDataTextAlignment(align: Align?) {
        mInfoPaint.textAlign = align
    }

    /**
     * Set this to false to disable all gestures and touches on the chart,
     * default: true
     */
    fun setTouchEnabled(enabled: Boolean) {
        this.mTouchEnabled = enabled
    }

    fun setMarkers(marker: MutableList<IMarker>) {
        this.marker = marker
    }

    /**
     * sets the marker that is displayed when a value is clicked on the chart
     */
    fun setMarker(marker: IMarker?) {
        setMarkers(marker?.let { mutableListOf(it) } ?: mutableListOf())
    }

    @Deprecated("")
    fun setMarkerView(v: IMarker?) {
        v?.let { setMarker(it) } ?: setMarkers(mutableListOf())
    }

    @get:Deprecated("")
    val markerView: MutableList<IMarker>?
        get() = this.marker

    /**
     * Returns the rectangle that defines the borders of the chart-value surface
     * (into which the actual values are drawn).
     */
    override val contentRect: RectF?
        get() = viewPortHandler.contentRect

    /**
     * disables intercept touchevents
     */
    fun disableScroll() {
        val parent = getParent()
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    /**
     * enables intercept touchevents
     */
    fun enableScroll() {
        val parent = getParent()
        parent?.requestDisallowInterceptTouchEvent(false)
    }

    /**
     * set a new paint object for the specified parameter in the chart e.g.
     * Chart.PAINT_VALUES
     *
     * @param p     the new paint object
     * @param which Chart.PAINT_VALUES, Chart.PAINT_GRID, Chart.PAINT_VALUES,
     * ...
     */
    open fun setPaint(p: Paint, which: Int) {
        when (which) {
            PAINT_INFO -> mInfoPaint = p
            PAINT_DESCRIPTION -> mDescPaint = p
        }
    }

    /**
     * Returns the paint object associated with the provided constant.
     *
     * @param which e.g. Chart.PAINT_LEGEND_LABEL
     */
    open fun getPaint(which: Int): Paint? {
        when (which) {
            PAINT_INFO -> return mInfoPaint
            PAINT_DESCRIPTION -> return mDescPaint
        }

        return null
    }

    @get:Deprecated("")
    val isDrawMarkerViewsEnabled: Boolean
        get() = this.isDrawMarkersEnabled

    @Deprecated("")
    fun setDrawMarkerViews(enabled: Boolean) {
        setDrawMarkers(enabled)
    }

    /**
     * Set this to true to draw a user specified marker when tapping on
     * chart values (use the setMarker(IMarker marker) method to specify a
     * marker). Default: true
     */
    fun setDrawMarkers(enabled: Boolean) {
        this.isDrawMarkersEnabled = enabled
    }

    /**
     * Returns the ChartData object that has been set for the chart.
     */
    override val data: T?
        get() = mData

    var renderer: DataRenderer?
        /**
         * Returns the Renderer object the chart uses for drawing data.
         */
        get() = mRenderer
        /**
         * Sets a new DataRenderer object for the chart.
         */
        set(renderer) {
            if (renderer != null) {
                mRenderer = renderer
            }
        }

    /**
     * Sets a custom highligher object for the chart that handles / processes
     * all highlight touch events performed on the chart-view.
     */
    fun setHighlighter(highlighter: ChartHighlighter<*>?) {
        this.highlighter = highlighter
    }

    /**
     * Returns a recyclable MPPointF instance.
     */
    override val centerOfView: MPPointF?
        get() = this.center

    val chartBitmap: Bitmap
        /**
         * Returns the bitmap that represents the chart.
         */
        get() {
            // Define a bitmap with the same size as the view
            val returnedBitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
            // Bind a canvas to it
            val canvas = Canvas(returnedBitmap)
            // Get the view's background
            val bgDrawable = background
            if (bgDrawable != null)  // has background drawable, then draw it on the canvas
            {
                bgDrawable.draw(canvas)
            } else  // does not have background drawable, then draw white background on
            // the canvas
            {
                canvas.drawColor(Color.WHITE)
            }
            // draw the view on the canvas
            draw(canvas)
            // return the bitmap
            return returnedBitmap
        }

    /**
     * Saves the current chart state with the given name to the given path on
     * the sdcard leaving the path empty "" will put the saved file directly on
     * the SD card chart is saved as a PNG image, example:
     * saveToPath("myfilename", "foldername1/foldername2");
     *
     * @param pathOnSD e.g. "folder1/folder2/folder3"
     * @return returns true on success, false on error
     */
    fun saveToPath(title: String?, pathOnSD: String?): Boolean {
        val b = this.chartBitmap

        val stream: OutputStream?
        try {
            stream = FileOutputStream(Environment.getExternalStorageDirectory().path + pathOnSD + "/" + title + ".png")

            /*
			 * Write bitmap to file using JPEG or PNG and 40% quality hint for
			 * JPEG.
			 */
            b.compress(CompressFormat.PNG, 40, stream)

            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    /**
     * Saves the current state of the chart to the gallery as an image type. The
     * compression must be set for JPEG only. 0 == maximum compression, 100 = low
     * compression (high quality). NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName        e.g. "my_image"
     * @param subFolderPath   e.g. "ChartPics"
     * @param fileDescription e.g. "Chart details"
     * @param format          e.g. Bitmap.CompressFormat.PNG
     * @param quality         e.g. 50, min = 0, max = 100
     * @return returns true if saving was successful, false if not
     */
    /**
     * Saves the current state of the chart to the gallery as a PNG image.
     * NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName e.g. "my_image"
     * @return returns true if saving was successful, false if not
     */
    @JvmOverloads
    fun saveToGallery(
        fileName: String,
        subFolderPath: String? = "",
        fileDescription: String? = "MPAndroidChart-Library Save",
        format: CompressFormat = CompressFormat.PNG,
        quality: Int = 40
    ): Boolean {
        // restrain quality
        var fileName = fileName
        var quality = quality
        if (quality < 0 || quality > 100) {
            quality = 50
        }

        val currentTime = System.currentTimeMillis()

        val extBaseDir = Environment.getExternalStorageDirectory()
        val file = File(extBaseDir.absolutePath + "/DCIM/" + subFolderPath)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false
            }
        }

        val mimeType: String?
        when (format) {
            CompressFormat.PNG -> {
                mimeType = "image/png"
                if (!fileName.endsWith(".png")) {
                    fileName += ".png"
                }
            }

            CompressFormat.WEBP -> {
                mimeType = "image/webp"
                if (!fileName.endsWith(".webp")) {
                    fileName += ".webp"
                }
            }

            CompressFormat.JPEG -> {
                mimeType = "image/jpeg"
                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) {
                    fileName += ".jpg"
                }
            }

            else -> {
                mimeType = "image/jpeg"
                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) {
                    fileName += ".jpg"
                }
            }
        }

        val filePath = file.absolutePath + "/" + fileName
        val out: FileOutputStream?
        try {
            out = FileOutputStream(filePath)

            val b = this.chartBitmap
            b.compress(format, quality, out)

            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()

            return false
        }

        val size = File(filePath).length()

        val values = ContentValues(8)

        // store the details
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.DATE_ADDED, currentTime)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        values.put(MediaStore.Images.Media.DESCRIPTION, fileDescription)
        values.put(MediaStore.Images.Media.ORIENTATION, 0)
        values.put(MediaStore.Images.Media.DATA, filePath)
        values.put(MediaStore.Images.Media.SIZE, size)

        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) != null
    }

    /**
     * Saves the current state of the chart to the gallery as a JPEG image. The
     * filename and compression can be set. 0 == maximum compression, 100 = low
     * compression (high quality). NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName e.g. "my_image"
     * @param quality  e.g. 50, min = 0, max = 100
     * @return returns true if saving was successful, false if not
     */
    fun saveToGallery(fileName: String, quality: Int): Boolean {
        return saveToGallery(fileName, "", "MPAndroidChart-Library Save", CompressFormat.PNG, quality)
    }

    /**
     * Returns all jobs that are scheduled to be executed after
     * onSizeChanged(...).
     */
    /**
     * tasks to be done after the view is setup
     */
    var jobs: ArrayList<Runnable?> = ArrayList()
        protected set

    fun removeViewportJob(job: Runnable?) {
        jobs.remove(job)
    }

    fun clearAllViewportJobs() {
        jobs.clear()
    }

    /**
     * Either posts a job immediately if the chart has already setup it's
     * dimensions or adds the job to the execution queue.
     */
    fun addViewportJob(job: Runnable?) {
        if (viewPortHandler.hasChartDimens()) {
            post(job)
        } else {
            jobs.add(job)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0..<size) {
            getChildAt(i).layout(left, top, right, bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Utils.convertDpToPixel(50f).toInt()
        setMeasuredDimension(
            max(suggestedMinimumWidth, resolveSize(size, widthMeasureSpec)),
            max(suggestedMinimumHeight, resolveSize(size, heightMeasureSpec))
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (this.isLogEnabled) {
            Log.i(LOG_TAG, "OnSizeChanged()")
        }

        if (w > 0 && h > 0 && w < 10000 && h < 10000) {
            if (this.isLogEnabled) {
                Log.i(LOG_TAG, "Setting chart dimens, width: $w, height: $h")
            }
            viewPortHandler.setChartDimens(w.toFloat(), h.toFloat())
        } else {
            if (this.isLogEnabled) {
                Log.w(LOG_TAG, "*Avoiding* setting chart dimens! width: $w, height: $h")
            }
        }

        // This may cause the chart view to mutate properties affecting the view port --
        //   lets do this before we try to run any pending jobs on the view port itself
        notifyDataSetChanged()

        for (r in this.jobs) {
            post(r)
        }

        jobs.clear()

        super.onSizeChanged(w, h, oldw, oldh)
    }

    /**
     * Setting this to true will set the layer-type HARDWARE for the view, false
     * will set layer-type SOFTWARE.
     */
    fun setHardwareAccelerationEnabled(enabled: Boolean) {
        if (enabled) {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        //Log.i(LOG_TAG, "Detaching...");
        if (mUnbind) {
            unbindDrawables(this)
        }
    }

    /**
     * unbind flag
     */
    private var mUnbind = false

    /**
     * Unbind all drawables to avoid memory leaks.
     * Link: http://stackoverflow.com/a/6779164/1590502
     */
    private fun unbindDrawables(view: View) {
        if (view.background != null) {
            view.background.callback = null
        }
        if (view is ViewGroup) {
            for (i in 0..<view.size) {
                unbindDrawables(view.getChildAt(i))
            }
            view.removeAllViews()
        }
    }

    /**
     * Set this to true to enable "unbinding" of drawables. When a View is detached
     * from a window. This helps avoid memory leaks.
     * Default: false
     * Link: http://stackoverflow.com/a/6779164/1590502
     */
    fun setUnbindEnabled(enabled: Boolean) {
        this.mUnbind = enabled
    }

    // region accessibility
    /**
     *
     * @return accessibility description must be created for each chart
     */
    abstract val accessibilityDescription: String?

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        val completed = super.dispatchPopulateAccessibilityEvent(event)
        Log.d(LOG_TAG, "Dispatch called for Chart <View> and completed as $completed")

        event.text.add(this.accessibilityDescription)

        // Add the user generated summary after the dynamic summary is complete.
        if (!TextUtils.isEmpty(this.accessibilitySummaryDescription)) {
            event.text.add(this.accessibilitySummaryDescription)
        }

        return true
    } // endregion

    companion object {
        const val LOG_TAG: String = "MPAndroidChart"

        /**
         * paint for the grid background (only line and barchart)
         */
        const val PAINT_GRID_BACKGROUND: Int = 4

        /**
         * paint for the info text that is displayed when there are no values in the
         * chart
         */
        const val PAINT_INFO: Int = 7

        /**
         * paint for the description text in the bottom right corner
         */
        const val PAINT_DESCRIPTION: Int = 11

        /**
         * paint for the hole in the middle of the pie chart
         */
        const val PAINT_HOLE: Int = 13

        /**
         * paint for the text in the middle of the pie chart
         */
        const val PAINT_CENTER_TEXT: Int = 14

        /**
         * paint used for the legend
         */
        const val PAINT_LEGEND_LABEL: Int = 18
    }
}
