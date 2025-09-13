package com.github.mikephil.charting.utils

import com.github.mikephil.charting.utils.ObjectPool.Poolable
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String

/**
 * Class for describing width and height dimensions in some arbitrary
 * unit. Replacement for the android.Util.SizeF which is available only on API >= 21.
 */
open class FSize : Poolable<FSize> {
    // TODO : Encapsulate width & height
    var width: Float = 0f
    var height: Float = 0f

    override fun instantiate(): FSize {
        return FSize(0f, 0f)
    }

    constructor()

    constructor(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (this === obj) {
            return true
        }
        if (obj is FSize) {
            val other = obj
            return width == other.width && height == other.height
        }
        return false
    }

    override fun toString(): String {
        return width.toString() + "x" + height
    }

    /**
     * {@inheritDoc}
     */
    override fun hashCode(): Int {
        return width.toBits() xor height.toBits()
    }

    companion object {
        private val pool: ObjectPool<FSize> = ObjectPool.create(256, FSize(0f, 0f))

        init {
            pool.setReplenishPercentage(0.5f)
        }


        fun getInstance(width: Float, height: Float): FSize {
            val result: FSize = pool.get()
            result.width = width
            result.height = height
            return result
        }

        fun recycleInstance(instance: FSize) {
            pool.recycle(instance)
        }

        fun recycleInstances(instances: MutableList<FSize?>) {
            pool.recycle(instances)
        }
    }
}
