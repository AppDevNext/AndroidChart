package com.github.mikephil.charting.jobs

import android.view.View
import com.github.mikephil.charting.utils.ObjectPool
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

open class MoveViewJob(viewPortHandler: ViewPortHandler, xValue: Float, yValue: Float, trans: Transformer?, v: View?) :
    ViewPortJob<MoveViewJob>(viewPortHandler, xValue, yValue, trans, v) {
    override fun run() {
        pts[0] = xValue
        pts[1] = yValue

        mTrans?.pointValuesToPixel(pts)
        mViewPortHandler.centerViewPort(pts, view)

        recycleInstance(this)
    }

    override fun instantiate(): MoveViewJob {
        return MoveViewJob(mViewPortHandler, xValue, yValue, mTrans, view)
    }

    companion object {
        private val pool = ObjectPool.create(2, MoveViewJob(ViewPortHandler(), 0f, 0f, null, null))

        init {
            pool.replenishPercentage = 0.5f
        }

        fun getInstance(viewPortHandler: ViewPortHandler, xValue: Float, yValue: Float, trans: Transformer?, v: View?): MoveViewJob {
            val result: MoveViewJob = pool.get()
            result.mViewPortHandler = viewPortHandler
            result.xValue = xValue
            result.yValue = yValue
            result.mTrans = trans
            result.view = v
            return result
        }

        fun recycleInstance(instance: MoveViewJob) {
            instance.recycle()
            // Clear reference avoid memory leak
            instance.xValue = 0f
            instance.yValue = 0f
            pool.recycle(instance)
        }
    }
}
