package info.appdev.charting.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import info.appdev.charting.charts.Chart
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.utils.FSize
import info.appdev.charting.utils.PointF
import java.lang.ref.WeakReference
import androidx.core.graphics.withTranslation

/**
 * View that can be displayed when selecting values in the chart. Extend this class to provide custom layouts for your markers.
 */
class MarkerImage(private var mContext: Context, drawableResourceId: Int) : IMarker {
    private var drawable: Drawable? = null

    private var mOffset: PointF = PointF()
    private val mOffset2 = PointF()
    private var mWeakChart: WeakReference<Chart<*>?>? = null

    private var mSize: FSize? = FSize()
    private val mDrawableBoundsCache = Rect()

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param mContext
     * @param drawableResourceId the drawable resource to render
     */
    init {
        drawable = mContext.resources.getDrawable(drawableResourceId, null)
    }

    fun setOffset(offsetX: Float, offsetY: Float) {
        mOffset.x = offsetX
        mOffset.y = offsetY
    }

    override var offset: PointF
        get() = mOffset
        set(offset) {
            mOffset = offset
        }

    var size: FSize?
        get() = mSize
        set(size) {
            mSize = size

            if (mSize == null) {
                mSize = FSize()
            }
        }

    var chartView: Chart<*>?
        get() = if (mWeakChart == null) null else mWeakChart!!.get()
        set(chart) {
            mWeakChart = WeakReference<Chart<*>?>(chart)
        }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): PointF {
        val offset = offset
        mOffset2.x = offset.x
        mOffset2.y = offset.y

        val chart = this.chartView

        var width = mSize!!.width
        var height = mSize!!.height

        if (width == 0f && drawable != null) {
            width = drawable!!.intrinsicWidth.toFloat()
        }
        if (height == 0f && drawable != null) {
            height = drawable!!.intrinsicHeight.toFloat()
        }

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

    override fun refreshContent(entry: Entry, highlight: Highlight) = Unit

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        if (drawable == null)
            return

        val offset: PointF = getOffsetForDrawingAtPoint(posX, posY)

        var width = mSize!!.width
        var height = mSize!!.height

        if (width == 0f) {
            width = drawable!!.intrinsicWidth.toFloat()
        }
        if (height == 0f) {
            height = drawable!!.intrinsicHeight.toFloat()
        }

        drawable!!.copyBounds(mDrawableBoundsCache)
        drawable!!.setBounds(
            mDrawableBoundsCache.left,
            mDrawableBoundsCache.top,
            mDrawableBoundsCache.left + width.toInt(),
            mDrawableBoundsCache.top + height.toInt()
        )

        canvas.withTranslation(posX + offset.x, posY + offset.y) {
            // translate to the correct position and draw
            drawable!!.draw(canvas)
        }

        drawable!!.bounds = mDrawableBoundsCache
    }
}
