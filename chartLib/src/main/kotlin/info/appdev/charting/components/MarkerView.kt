package info.appdev.charting.components

import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.widget.RelativeLayout
import info.appdev.charting.charts.Chart
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.utils.MPPointF
import java.lang.ref.WeakReference

/**
 * View that can be displayed when selecting values in the chart. Extend this class to provide custom layouts for your markers.
 */
open class MarkerView(context: Context?, layoutResource: Int) : RelativeLayout(context), IMarker {
    private var mOffset: MPPointF = MPPointF()
    private val mOffset2 = MPPointF()
    private var mWeakChart: WeakReference<Chart<*>?>? = null

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    init {
        setupLayoutResource(layoutResource)
    }

    /**
     * Sets the layout resource for a custom MarkerView.
     *
     * @param layoutResource
     */
    private fun setupLayoutResource(layoutResource: Int) {
        val inflated = LayoutInflater.from(context).inflate(layoutResource, this)

        inflated.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        inflated.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))

        // measure(getWidth(), getHeight());
        inflated.layout(0, 0, inflated.measuredWidth, inflated.measuredHeight)
    }

    override var offset: MPPointF
        get() = mOffset
        set(offset) {
            mOffset = offset
        }

    var chartView: Chart<*>?
        get() = if (mWeakChart == null) null else mWeakChart!!.get()
        set(chart) {
            mWeakChart = WeakReference(chart)
        }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        val offset = offset
        mOffset2.x = offset.x
        mOffset2.y = offset.y

        val chart = this.chartView

        val width = width.toFloat()
        val height = height.toFloat()

        if (posX + mOffset2.x < 0) {
            mOffset2.x = -posX
        } else if (chart != null && posX + width + mOffset2.x > chart.width) {
            mOffset2.x = chart.width - posX - width
        }

        if (posY + mOffset2.y < 0) {
            mOffset2.y = -posY
        } else if (chart != null && posY + height + mOffset2.y > chart.height) {
            mOffset2.y = chart.height - posY - height
        }

        return mOffset2
    }

    override fun refreshContent(entry: Entry, highlight: Highlight) {
        measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        layout(0, 0, measuredWidth, measuredHeight)
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        val offset: MPPointF = getOffsetForDrawingAtPoint(posX, posY)

        val saveId = canvas.save()
        // translate to the correct position and draw
        canvas.translate(posX + offset.x, posY + offset.y)
        draw(canvas)
        canvas.restoreToCount(saveId)
    }
}
