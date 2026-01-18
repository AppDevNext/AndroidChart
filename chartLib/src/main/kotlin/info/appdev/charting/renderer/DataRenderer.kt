package info.appdev.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import info.appdev.charting.animation.ChartAnimator
import info.appdev.charting.data.BaseEntry
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.dataprovider.base.IBaseProvider
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.charting.utils.convertDpToPixel

/**
 * Superclass of all render classes for the different data types (line, bar, ...).
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class DataRenderer(
    protected var animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : Renderer(viewPortHandler) {

    /**
     * main paint object used for rendering
     */
    var paintRender: Paint
        protected set

    /**
     * paint used for highlighting values
     */
    var paintHighlight: Paint
        protected set

    protected var drawPaint: Paint

    /**
     * paint object for drawing values (text representing values of chart
     * entries)
     */
    var paintValues: Paint
        protected set

    init {
        paintRender = Paint(Paint.ANTI_ALIAS_FLAG)
        paintRender.style = Paint.Style.FILL

        drawPaint = Paint(Paint.DITHER_FLAG)

        paintValues = Paint(Paint.ANTI_ALIAS_FLAG)
        paintValues.color = Color.rgb(63, 63, 63)
        paintValues.textAlign = Align.CENTER
        paintValues.textSize = 9f.convertDpToPixel()

        paintHighlight = Paint(Paint.ANTI_ALIAS_FLAG)
        paintHighlight.style = Paint.Style.STROKE
        paintHighlight.strokeWidth = 2f
        paintHighlight.color = Color.rgb(255, 187, 115)
    }

    protected open fun isDrawingValuesAllowed(baseProvider: IBaseProvider<*>): Boolean {
        return baseProvider.data?.let { data ->
            data.entryCount < baseProvider.maxVisibleCount * viewPortHandler.scaleX
        } ?: run { false }
    }

    /**
     * Applies the required styling (provided by the DataSet) to the value-paint
     * object.
     */
    protected fun applyValueTextStyle(set: IDataSet<*, *>) {
        paintValues.typeface = set.valueTypeface
        paintValues.textSize = set.valueTextSize
    }

    /**
     * Initializes the buffers used for rendering with a new size. Since this
     * method performs memory allocations, it should only be called if
     * necessary.
     */
    abstract fun initBuffers()

    /**
     * Draws the actual data in form of lines, bars, ... depending on Renderer subclass.
     */
    abstract fun drawData(canvas: Canvas)

    /**
     * Loops over all Entries and draws their values.
     */
    abstract fun drawValues(canvas: Canvas)

    /**
     * Draws the value of the given entry by using the provided IValueFormatter.
     *
     * @param canvas            canvas
     * @param formatter    formatter for custom value-formatting
     * @param value        the value to be drawn
     * @param entry        the entry the value belongs to
     * @param dataSetIndex the index of the DataSet the drawn Entry belongs to
     * @param x            position
     * @param y            position
     */
    fun drawValue(canvas: Canvas, formatter: IValueFormatter, value: Float, entry: BaseEntry<Float>?, dataSetIndex: Int, x: Float, y: Float, color: Int) {
        paintValues.color = color
        canvas.drawText(formatter.getFormattedValue(value, entry, dataSetIndex, viewPortHandler)!!, x, y, paintValues)
    }

    /**
     * Draws any kind of additional information (e.g. line-circles).
     */
    abstract fun drawExtras(canvas: Canvas)

    /**
     * Draws all highlight indicators for the values that are currently highlighted.
     * @param indices the highlighted values
     */
    abstract fun drawHighlighted(canvas: Canvas, indices: Array<Highlight>)
}
