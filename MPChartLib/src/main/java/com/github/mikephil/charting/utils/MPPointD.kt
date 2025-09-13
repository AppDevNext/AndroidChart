package com.github.mikephil.charting.utils

import com.github.mikephil.charting.utils.ObjectPool.Poolable

/**
 * Point encapsulating two double values.
 *
 * @author Philipp Jahoda
 */
class MPPointD private constructor(var x: Double, var y: Double) : Poolable<MPPointD>() {
    override fun instantiate(): MPPointD {
        return MPPointD(0.0, 0.0)
    }

    /**
     * returns a string representation of the object
     */
    override fun toString(): String {
        return "MPPointD, x: $x, y: $y"
    }

    companion object {
        private val pool: ObjectPool<MPPointD> = ObjectPool.Companion.create(64, MPPointD(0.0, 0.0))

        init {
            pool.setReplenishPercentage(0.5f)
        }

        fun getInstance(x: Double, y: Double): MPPointD {
            val result: MPPointD = pool.get()
            result.x = x
            result.y = y
            return result
        }

        fun recycleInstance(instance: MPPointD) {
            pool.recycle(instance)
        }

        fun recycleInstances(instances: MutableList<MPPointD>) {
            pool.recycle(instances)
        }
    }
}