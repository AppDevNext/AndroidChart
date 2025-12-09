package com.github.mikephil.charting.jobs

import android.view.View
import com.github.mikephil.charting.utils.ObjectPool
import com.github.mikephil.charting.utils.ObjectPool.Poolable
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

class MoveViewJob(viewPortHandler: ViewPortHandler?, xValue: Float, yValue: Float, trans: Transformer?, v: View?) :
    ViewPortJob(viewPortHandler, xValue, yValue, trans, v) {
    override fun run() {
        pts[0] = xValue
        pts[1] = yValue

        transformer!!.pointValuesToPixel(pts)
        viewPortHandler!!.centerViewPort(pts, view!!)

        recycleInstance(this)
    }

    protected override fun instantiate(): Poolable {
        return MoveViewJob(viewPortHandler, xValue, yValue, transformer, view)
    }

    companion object {
        private val pool: ObjectPool<MoveViewJob> = ObjectPool.create(2, MoveViewJob(null, 0f, 0f, null, null)) as ObjectPool<MoveViewJob>

        init {
            pool.setReplenishPercentage(0.5f)
        }

        @JvmStatic
        fun getInstance(viewPortHandler: ViewPortHandler?, xValue: Float, yValue: Float, trans: Transformer?, v: View?): MoveViewJob {
            val result: MoveViewJob = pool.get()
            result.viewPortHandler = viewPortHandler
            result.xValue = xValue
            result.yValue = yValue
            result.transformer = trans
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
