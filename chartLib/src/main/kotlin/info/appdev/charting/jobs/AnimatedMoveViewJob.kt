package info.appdev.charting.jobs

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import info.appdev.charting.utils.ObjectPool
import info.appdev.charting.utils.Transformer
import info.appdev.charting.utils.ViewPortHandler

@SuppressLint("NewApi")
class AnimatedMoveViewJob(
    viewPortHandler: ViewPortHandler,
    xValue: Float,
    yValue: Float,
    trans: Transformer?,
    v: View?,
    xOrigin: Float,
    yOrigin: Float,
    duration: Long
) : AnimatedViewPortJob<AnimatedMoveViewJob>(viewPortHandler, xValue, yValue, trans, v, xOrigin, yOrigin, duration) {
    override fun onAnimationUpdate(animation: ValueAnimator) {
        pts[0] = xOrigin + (xValue - xOrigin) * phase
        pts[1] = yOrigin + (yValue - yOrigin) * phase

        mTrans?.pointValuesToPixel(pts)
        mViewPortHandler.centerViewPort(pts, view)
    }

    override fun recycleSelf() {
        recycleInstance(this)
    }

    override fun instantiate(): AnimatedMoveViewJob {
        return AnimatedMoveViewJob(ViewPortHandler(), 0f, 0f, null, null, 0f, 0f, 0)
    }

    companion object {
        private val pool = ObjectPool.create(4, AnimatedMoveViewJob(ViewPortHandler(), 0f, 0f, null, null, 0f, 0f, 0))

        init {
            pool.replenishPercentage = 0.5f
        }

        fun getInstance(
            viewPortHandler: ViewPortHandler,
            xValue: Float,
            yValue: Float,
            trans: Transformer?,
            v: View?,
            xOrigin: Float,
            yOrigin: Float,
            duration: Long
        ): AnimatedMoveViewJob {
            val result: AnimatedMoveViewJob = pool.get()
            result.mViewPortHandler = viewPortHandler
            result.xValue = xValue
            result.yValue = yValue
            result.mTrans = trans
            result.view = v
            result.xOrigin = xOrigin
            result.yOrigin = yOrigin
            //result.resetAnimator();
            result.animator.duration = duration
            return result
        }

        fun recycleInstance(instance: AnimatedMoveViewJob) {
            // Clear reference avoid memory leak
            instance.xValue = 0f
            instance.yValue = 0f
            instance.xOrigin = 0f
            instance.yOrigin = 0f
            instance.animator.duration = 0
            instance.recycle()
            pool.recycle(instance)
        }
    }
}
