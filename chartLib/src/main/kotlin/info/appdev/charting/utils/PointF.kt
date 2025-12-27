package info.appdev.charting.utils

import android.os.Parcel
import android.os.Parcelable
import info.appdev.charting.utils.ObjectPool.Poolable
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

    /**
     * Set the point's coordinates from the data stored in the specified
     * parcel. To write a point to a parcel, call writeToParcel().
     * Provided to support older Android devices.
     *
     * @param in The parcel to read the point's coordinates from
     */
    fun my_readFromParcel(`in`: Parcel) {
        x = `in`.readFloat()
        y = `in`.readFloat()
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

        val CREATOR: Parcelable.Creator<PointF?> = object : Parcelable.Creator<PointF?> {
            /**
             * Return a new point from the data in the specified parcel.
             */
            override fun createFromParcel(`in`: Parcel): PointF {
                val r = PointF(0f, 0f)
                r.my_readFromParcel(`in`)
                return r
            }

            /**
             * Return an array of rectangles of the specified size.
             */
            override fun newArray(size: Int): Array<PointF?> {
                return arrayOfNulls(size)
            }
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
    outputPoint.x = (this.x + dist * cos(Math.toRadians(angle.toDouble()))).toFloat()
    outputPoint.y = (this.y + dist * sin(Math.toRadians(angle.toDouble()))).toFloat()
}