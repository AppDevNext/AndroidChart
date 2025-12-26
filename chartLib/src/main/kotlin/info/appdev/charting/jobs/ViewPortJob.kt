package info.appdev.charting.jobs

import android.view.View
import info.appdev.charting.utils.ObjectPool.Poolable
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.ViewPortHandler

/**
 * Runnable that is used for viewport modifications since they cannot be
 * executed at any time. This can be used to delay the execution of viewport
 * modifications until the onSizeChanged(...) method of the chart-view is called.
 * This is especially important if viewport modifying methods are called on the chart
 * directly after initialization.
 */
abstract class ViewPortJob<T : ViewPortJob<T>>(
    protected var mViewPortHandler: ViewPortHandler, xValue: Float, yValue: Float,
    trans: Transformer?, v: View?
) : Poolable<T>(), Runnable {
    protected var pts: FloatArray = FloatArray(2)

    var xValue: Float = 0f
        protected set
    var yValue: Float = 0f
        protected set
    protected var mTrans: Transformer?
    protected var view: View?

    init {
        this.xValue = xValue
        this.yValue = yValue
        this.mTrans = trans
        this.view = v
    }

    protected fun recycle() {
        view = null
    }
}
