package info.appdev.charting.utils

import info.appdev.charting.utils.ObjectPool.Poolable

/**
 * Point encapsulating two double values.
 */
class PointD private constructor(var x: Double, var y: Double) : Poolable<PointD>() {
    override fun instantiate(): PointD {
        return PointD(0.0, 0.0)
    }

    /**
     * returns a string representation of the object
     */
    override fun toString(): String {
        return "PointD, x: $x, y: $y"
    }

    companion object {
        private val pool: ObjectPool<PointD> = ObjectPool.create(64, PointD(0.0, 0.0))

        init {
            pool.replenishPercentage = 0.5f
        }

        fun getInstance(x: Double, y: Double): PointD {
            val result: PointD = pool.get()
            result.x = x
            result.y = y
            return result
        }

        fun recycleInstance(instance: PointD) {
            pool.recycle(instance)
        }

        fun recycleInstances(instances: MutableList<PointD>) {
            pool.recycle(instances)
        }
    }
}