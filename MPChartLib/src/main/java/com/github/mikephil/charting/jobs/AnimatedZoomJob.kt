package com.github.mikephil.charting.jobs

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Matrix
import android.view.View
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.utils.ObjectPool
import com.github.mikephil.charting.utils.ObjectPool.Poolable
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

@SuppressLint("NewApi")
open class AnimatedZoomJob @SuppressLint("NewApi") constructor(
    viewPortHandler: ViewPortHandler?,
    v: View?,
    trans: Transformer?,
    axis: YAxis?,
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
) : AnimatedViewPortJob(viewPortHandler, scaleX, scaleY, trans, v, xOrigin, yOrigin, duration), Animator.AnimatorListener {
    protected var yAxis: YAxis? = null

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
        viewPortHandler!!.setZoom(scaleX, scaleY, save)
        viewPortHandler!!.refresh(save, view!!, false)

        val valsInView = yAxis?.let { it.mAxisRange / viewPortHandler!!.scaleY }
        val xsInView = xAxisRange / viewPortHandler!!.scaleX

        pts[0] = zoomOriginX + ((zoomCenterX - xsInView / 2f) - zoomOriginX) * phase
        valsInView?.let { pts[1] = zoomOriginY + ((zoomCenterY + it / 2f) - zoomOriginY) * phase }

        transformer!!.pointValuesToPixel(pts)

        viewPortHandler!!.translate(pts, save)
        viewPortHandler!!.refresh(save, view!!, true)
    }

    override fun onAnimationEnd(animation: Animator) {
        (view as BarLineChartBase<*>).calculateOffsets()
        view!!.postInvalidate()
    }

    override fun onAnimationCancel(animation: Animator) = Unit

    override fun onAnimationRepeat(animation: Animator) = Unit

    override fun recycleSelf() = Unit

    override fun onAnimationStart(animation: Animator) = Unit
    protected override fun instantiate(): Poolable {
        return AnimatedZoomJob(null, null, null, null, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0)
    }

    companion object {
        private val pool: ObjectPool<AnimatedZoomJob> =
            ObjectPool.create(8, AnimatedZoomJob(null, null, null, null, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0)) as ObjectPool<AnimatedZoomJob>

        @JvmStatic
        fun getInstance(
            viewPortHandler: ViewPortHandler?,
            v: View?,
            trans: Transformer?,
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
            result.viewPortHandler = viewPortHandler
            result.xValue = scaleX
            result.yValue = scaleY
            result.transformer = trans
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
            result.animator.setDuration(duration)
            return result
        }
    }
}
