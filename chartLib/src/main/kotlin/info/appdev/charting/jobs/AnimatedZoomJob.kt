package info.appdev.charting.jobs

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Matrix
import android.view.View
import info.appdev.charting.charts.BarLineChartBase
import info.appdev.charting.components.YAxis
import info.appdev.charting.utils.ObjectPool
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.ViewPortHandler

@SuppressLint("NewApi")
open class AnimatedZoomJob @SuppressLint("NewApi") constructor(
    viewPortHandler: ViewPortHandler,
    v: View?,
    trans: Transformer?,
    axis: YAxis,
    xAxisRange: Float,
    scaleX: Float,
    scaleY: Float,
    xOrigin: Float,
    yOrigin: Float,
    protected var zoomCenterX: Float,
    protected var zoomCenterY: Float,
    protected var zoomOriginX: Float,
    protected var zoomOriginY: Float,
    duration: Long
) : AnimatedViewPortJob<AnimatedZoomJob>(viewPortHandler, scaleX, scaleY, trans, v, xOrigin, yOrigin, duration), Animator.AnimatorListener {
    protected var yAxis: YAxis

    protected var xAxisRange: Float

    protected var mOnAnimationUpdateMatrixBuffer: Matrix = Matrix()

    init {
        this.animator.addListener(this)
        this.yAxis = axis
        this.xAxisRange = xAxisRange
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val scaleX = xOrigin + (xValue - xOrigin) * phase
        val scaleY = yOrigin + (yValue - yOrigin) * phase

        val save = mOnAnimationUpdateMatrixBuffer
        mViewPortHandler.setZoom(scaleX, scaleY, save)
        mViewPortHandler.refresh(save, view, false)

        val valsInView = yAxis.mAxisRange / mViewPortHandler.scaleY
        val xsInView = xAxisRange / mViewPortHandler.scaleX

        pts[0] = zoomOriginX + ((zoomCenterX - xsInView / 2f) - zoomOriginX) * phase
        pts[1] = zoomOriginY + ((zoomCenterY + valsInView / 2f) - zoomOriginY) * phase

        mTrans?.pointValuesToPixel(pts)

        mViewPortHandler.translate(pts, save)
        mViewPortHandler.refresh(save, view, true)
    }

    override fun onAnimationEnd(animation: Animator) {
        (view as? BarLineChartBase<*>)?.calculateOffsets()
        view?.postInvalidate()
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationRepeat(animation: Animator) {
    }

    override fun recycleSelf() {
    }

    override fun onAnimationStart(animation: Animator) {
    }

    override fun instantiate(): AnimatedZoomJob {
        return AnimatedZoomJob(ViewPortHandler(), null, null, YAxis(), 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0)
    }

    companion object {
        private val pool = ObjectPool.create(8, AnimatedZoomJob(ViewPortHandler(), null, null, YAxis(), 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0))

        fun getInstance(
            viewPortHandler: ViewPortHandler,
            v: View?,
            trans: Transformer,
            axis: YAxis,
            xAxisRange: Float,
            scaleX: Float,
            scaleY: Float,
            xOrigin: Float,
            yOrigin: Float,
            zoomCenterX: Float,
            zoomCenterY: Float,
            zoomOriginX: Float,
            zoomOriginY: Float,
            duration: Long
        ): AnimatedZoomJob {
            val result: AnimatedZoomJob = pool.get()
            result.mViewPortHandler = viewPortHandler
            result.xValue = scaleX
            result.yValue = scaleY
            result.mTrans = trans
            result.view = v
            result.xOrigin = xOrigin
            result.yOrigin = yOrigin
            result.yAxis = axis
            result.xAxisRange = xAxisRange
            result.zoomCenterX = zoomCenterX
            result.zoomCenterY = zoomCenterY
            result.zoomOriginX = zoomOriginX
            result.zoomOriginY = zoomOriginY
            result.resetAnimator()
            result.animator.duration = duration
            return result
        }
    }
}
