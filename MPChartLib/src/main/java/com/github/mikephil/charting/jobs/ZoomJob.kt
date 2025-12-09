package com.github.mikephil.charting.jobs

import android.graphics.Matrix
import android.view.View
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.utils.ObjectPool
import com.github.mikephil.charting.utils.ObjectPool.Poolable
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

class ZoomJob(
    viewPortHandler: ViewPortHandler?, protected var scaleX: Float, protected var scaleY: Float, xValue: Float, yValue: Float, trans: Transformer?,
    protected var axisDependency: AxisDependency?, v: View?
) : ViewPortJob(viewPortHandler, xValue, yValue, trans, v) {
    protected var mRunMatrixBuffer: Matrix = Matrix()

    override fun run() {
        val save = mRunMatrixBuffer
        viewPortHandler!!.zoom(scaleX, scaleY, save)
        viewPortHandler!!.refresh(save, view!!, false)

        val yValsInView = (view as BarLineChartBase<*>).getAxis(axisDependency).mAxisRange / viewPortHandler!!.scaleY
        val xValsInView = (view as BarLineChartBase<*>).getXAxis().mAxisRange / viewPortHandler!!.scaleX

        pts[0] = xValue - xValsInView / 2f
        pts[1] = yValue + yValsInView / 2f

        transformer!!.pointValuesToPixel(pts)

        viewPortHandler!!.translate(pts, save)
        viewPortHandler!!.refresh(save, view!!, false)

        (view as BarLineChartBase<*>).calculateOffsets()
        view!!.postInvalidate()

        recycleInstance(this)
    }

    protected override fun instantiate(): Poolable {
        return ZoomJob(null, 0f, 0f, 0f, 0f, null, null, null)
    }

    companion object {
        private val pool: ObjectPool<ZoomJob> = ObjectPool.create(1, ZoomJob(null, 0f, 0f, 0f, 0f, null, null, null)) as ObjectPool<ZoomJob>

        init {
            pool.setReplenishPercentage(0.5f)
        }

        @JvmStatic
        fun getInstance(
            viewPortHandler: ViewPortHandler?, scaleX: Float, scaleY: Float, xValue: Float, yValue: Float,
            trans: Transformer?, axis: AxisDependency?, v: View?
        ): ZoomJob {
            val result: ZoomJob = pool.get()
            result.xValue = xValue
            result.yValue = yValue
            result.scaleX = scaleX
            result.scaleY = scaleY
            result.viewPortHandler = viewPortHandler
            result.transformer = trans
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
