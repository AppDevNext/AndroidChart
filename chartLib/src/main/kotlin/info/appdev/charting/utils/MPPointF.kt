package info.appdev.charting.utils

import android.os.Parcel
import android.os.Parcelable
import info.appdev.charting.utils.ObjectPool.Poolable

class MPPointF : Poolable<MPPointF> {
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

    override fun instantiate(): MPPointF {
        return MPPointF(0f, 0f)
    }

    override fun toString(): String {
        return "x=$x y=$y"
    }

    companion object {
        private var pool: ObjectPool<MPPointF> = ObjectPool.create(32, MPPointF(0f, 0f))

        init {
            pool.replenishPercentage = 0.5f
        }

        fun getInstance(x: Float, y: Float): MPPointF {
            val result: MPPointF = pool.get()
            result.x = x
            result.y = y
            return result
        }

        val instance: MPPointF
            get() = pool.get()

        fun getInstance(copy: MPPointF): MPPointF {
            val result: MPPointF = pool.get()
            result.x = copy.x
            result.y = copy.y
            return result
        }

        @JvmStatic
        fun recycleInstance(instance: MPPointF?) {
            pool.recycle(instance)
        }

        fun recycleInstances(instances: MutableList<MPPointF>) {
            pool.recycle(instances)
        }

        val CREATOR: Parcelable.Creator<MPPointF?> = object : Parcelable.Creator<MPPointF?> {
            /**
             * Return a new point from the data in the specified parcel.
             */
            override fun createFromParcel(`in`: Parcel): MPPointF {
                val r = MPPointF(0f, 0f)
                r.my_readFromParcel(`in`)
                return r
            }

            /**
             * Return an array of rectangles of the specified size.
             */
            override fun newArray(size: Int): Array<MPPointF?> {
                return arrayOfNulls(size)
            }
        }
    }
}