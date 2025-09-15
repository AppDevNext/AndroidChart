package com.github.mikephil.charting.utils

import com.github.mikephil.charting.utils.ObjectPool.Poolable

/**
 * An object pool for recycling of object instances extending Poolable.
 *
 *
 * Cost/Benefit :
 * Cost - The pool can only contain objects extending Poolable.
 * Benefit - The pool can very quickly determine if an object is elligable for storage without iteration.
 * Benefit - The pool can also know if an instance of Poolable is already stored in a different pool instance.
 * Benefit - The pool can grow as needed, if it is empty
 * Cost - However, refilling the pool when it is empty might incur a time cost with sufficiently large capacity.  Set the replenishPercentage to a lower number if this is a concern.
 *
 * Created by Tony Patino on 6/20/16.
 */
class ObjectPool<T : Poolable<T>> private constructor(withCapacity: Int, `object`: T?) {
    /**
     * Returns the id of the given pool instance.
     *
     * @return an integer ID belonging to this pool instance.
     */
    var poolId: Int = 0
        private set
    private var desiredCapacity: Int
    private val objects = ArrayList<T>(withCapacity)
    private val modelObject: T?
    var replenishPercentage: Float = 1.0f
        set(value) {
            var p = value
            if (p > 1) {
                p = 1f
            } else if (p < 0f) {
                p = 0f
            }
            field = p
        }


    init {
        require(withCapacity > 0) { "Object Pool must be instantiated with a capacity greater than 0!" }
        this.desiredCapacity = withCapacity
        this.modelObject = `object`
        this.refillPool()
    }

    private fun refillPool(percentage: Float = this.replenishPercentage) {
        var portionOfCapacity = (desiredCapacity * percentage).toInt()

        if (portionOfCapacity < 1) {
            portionOfCapacity = 1
        } else if (portionOfCapacity > desiredCapacity) {
            portionOfCapacity = desiredCapacity
        }

        this.objects.clear()

        repeat(portionOfCapacity) {
            this.objects.add(modelObject!!.instantiate())
        }
    }

    /**
     * Returns an instance of Poolable.  If get() is called with an empty pool, the pool will be
     * replenished.  If the pool capacity is sufficiently large, this could come at a performance
     * cost.
     *
     * @return An instance of Poolable object T
     */
    @Synchronized
    fun get(): T {
        if (objects.isEmpty() && this.replenishPercentage > 0.0f) {
            this.refillPool()
        }

        val result = objects.removeAt(objects.lastIndex)
        result.currentOwnerId = Poolable.Companion.NO_OWNER

        return result
    }

    /**
     * Recycle an instance of Poolable that this pool is capable of generating.
     * The T instance passed must not already exist inside this or any other ObjectPool instance.
     *
     * @param object An object of type T to recycle
     */
    @Synchronized
    fun recycle(`object`: T?) {
        if (`object` == null) return

        if (`object`.currentOwnerId != Poolable.Companion.NO_OWNER) {
            require(`object`.currentOwnerId != this.poolId) { "The object passed is already stored in this pool!" }
            throw IllegalArgumentException("The object to recycle already belongs to poolId " + `object`.currentOwnerId + ".  Object cannot belong to two different pool instances simultaneously!")
        }

        `object`.currentOwnerId = this.poolId
        objects.add(`object`)

        if (objects.size > desiredCapacity) {
            resizePool()
        }
    }

    /**
     * Recycle a List of Poolables that this pool is capable of generating.
     * The T instances passed must not already exist inside this or any other ObjectPool instance.
     *
     * @param objects A list of objects of type T to recycle
     */
    @Synchronized
    fun recycle(objects: List<T>) {
        val objectsListSize = objects.size

        while (objectsListSize + this.objects.size > this.desiredCapacity) {
            resizePool()
        }

        // Not relying on recycle(T object) because this is more performant.
        for (i in 0..<objectsListSize) {
            val `object` = objects[i]
            if (`object`.currentOwnerId != Poolable.Companion.NO_OWNER) {
                require(`object`.currentOwnerId != this.poolId) { "The object passed is already stored in this pool!" }
                throw IllegalArgumentException("The object to recycle already belongs to poolId " + `object`.currentOwnerId + ".  Object cannot belong to two different pool instances simultaneously!")
            }
            `object`.currentOwnerId = this.poolId
            this.objects.add(`object`)
        }
    }

    private fun resizePool() {
        this.desiredCapacity *= 2
    }

    val poolCapacity: Int
        /**
         * Returns the capacity of this object pool.  Note : The pool will automatically resize
         * to contain additional objects if the user tries to add more objects than the pool's
         * capacity allows, but this comes at a performance cost.
         *
         * @return The capacity of the pool.
         */
        get() = this.desiredCapacity

    val poolCount: Int
        /**
         * Returns the number of objects remaining in the pool, for diagnostic purposes.
         *
         * @return The number of objects remaining in the pool.
         */
        get() = objects.size


    abstract class Poolable<T : Poolable<T>> {
        var currentOwnerId: Int = NO_OWNER

        abstract fun instantiate(): T

        companion object {
            var NO_OWNER: Int = -1
        }
    }

    companion object {
        private var ids = 0

        /**
         * Returns an ObjectPool instance, of a given starting capacity, that recycles instances of a given Poolable object.
         *
         * @param withCapacity A positive integer value.
         * @param object An instance of the object that the pool should recycle.
         * @return
         */
        @Synchronized
        fun <T : Poolable<T>> create(withCapacity: Int, `object`: T): ObjectPool<T> {
            val result: ObjectPool<T> = ObjectPool(withCapacity, `object`)
            result.poolId = ids
            ids++

            return result
        }
    }
}