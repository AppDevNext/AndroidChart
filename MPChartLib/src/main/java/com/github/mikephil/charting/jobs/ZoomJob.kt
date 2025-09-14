package com.github.mikephil.charting.jobs

import android.graphics.Matrix
import android.view.View
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.utils.ObjectPool
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * Created by Philipp Jahoda on 19/02/16.
 */
open class ZoomJob(
    viewPortHandler: ViewPortHandler, protected var scaleX: Float, protected var scaleY: Float, xValue: Float, yValue: Float, trans: Transformer?,
    protected var axisDependency: AxisDependency?, v: View?
) : ViewPortJob<ZoomJob>(viewPortHandler, xValue, yValue, trans, v) {
    protected var mRunMatrixBuffer: Matrix = Matrix()

    override fun run() {
        val save = mRunMatrixBuffer
        mViewPortHandler.zoom(scaleX, scaleY, save)
        mViewPortHandler.refresh(save, view, false)

        (view as? BarLineChartBase<*, *, *>)?.let { view ->
            val yValsInView = view.getAxis(axisDependency).mAxisRange / mViewPortHandler.scaleY
            val xValsInView = view.xAxis.mAxisRange / mViewPortHandler.scaleX

            pts[0] = xValue - xValsInView / 2f
            pts[1] = yValue + yValsInView / 2f

            mTrans?.pointValuesToPixel(pts)

            mViewPortHandler.translate(pts, save)
            mViewPortHandler.refresh(save, view, false)

            view.calculateOffsets()
            view.postInvalidate()
        }

        recycleInstance(this)
    }

    override fun instantiate(): ZoomJob {
        return ZoomJob(ViewPortHandler(), 0f, 0f, 0f, 0f, null, null, null)
    }

    companion object {
        private val pool = ObjectPool.Companion.create(1, ZoomJob(ViewPortHandler(), 0f, 0f, 0f, 0f, null, null, null))

        init {
            pool.replenishPercentage = 0.5f
        }

        fun getInstance(
            viewPortHandler: ViewPortHandler, scaleX: Float, scaleY: Float, xValue: Float, yValue: Float,
            trans: Transformer?, axis: AxisDependency?, v: View?
        ): ZoomJob {
            val result: ZoomJob = pool.get()
            result.xValue = xValue
            result.yValue = yValue
            result.scaleX = scaleX
            result.scaleY = scaleY
            result.mViewPortHandler = viewPortHandler
            result.mTrans = trans
            result.axisDependency = axis
            result.view = v
            return result
        }

        fun recycleInstance(instance: ZoomJob) {
            // Clear reference avoid memory leak
            instance.xValue = 0f
            instance.yValue = 0f
            instance.scaleX = 0f
            instance.scaleY = 0f
            instance.axisDependency = null
            instance.recycle()
            pool.recycle(instance)
        }
    }
}
