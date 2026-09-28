package info.appdev.charting.utils

import info.appdev.charting.utils.ObjectPool.Poolable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PointF : Poolable<PointF> {
    var x: Float = 0f
    var y: Float = 0f

    constructor()

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    override fun instantiate(): PointF {
        return PointF(0f, 0f)
    }

    override fun toString(): String {
        return "x=$x y=$y"
    }

    companion object {
        private var pool: ObjectPool<PointF> = ObjectPool.create(32, PointF(0f, 0f))

        init {
            pool.replenishPercentage = 0.5f
        }

        fun getInstance(x: Float, y: Float): PointF {
            val result: PointF = pool.get()
            result.x = x
            result.y = y
            return result
        }

        val instance: PointF
            get() = pool.get()

        fun getInstance(copy: PointF): PointF {
            val result: PointF = pool.get()
            result.x = copy.x
            result.y = copy.y
            return result
        }

        fun recycleInstance(instance: PointF?) {
            pool.recycle(instance)
        }

        fun recycleInstances(instances: MutableList<PointF>) {
            pool.recycle(instances)
        }
    }
}

/**
 * Returns a recyclable PointF instance.
 * Calculates the position around a center point, depending on the distance
 * from the center, and the angle of the position around the center.
 *
 * @param angle  in degrees, converted to radians internally
 */
fun PointF.getPosition(dist: Float, angle: Float): PointF {
    val pointF = PointF.getInstance(0f, 0f)
    changePosition(dist, angle, pointF)
    return pointF
}

fun PointF.changePosition(dist: Float, angle: Float, outputPoint: PointF) {
    val radians = angle.toDouble() * PI / 180.0
    outputPoint.x = (this.x + dist * cos(radians)).toFloat()
    outputPoint.y = (this.y + dist * sin(radians)).toFloat()
}