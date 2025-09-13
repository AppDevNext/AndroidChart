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
class ObjectPool<T : Poolable<T>?> private constructor(withCapacity: Int, `object`: T?) {
    /**
     * Returns the id of the given pool instance.
     *
     * @return an integer ID belonging to this pool instance.
     */
    var poolId: Int = 0
        private set
    private var desiredCapacity: Int
    private var objects: Array<T?>
    private var objectsPointer: Int
    private val modelObject: T?
    private var replenishPercentage: Float


    init {
        require(withCapacity > 0) { "Object Pool must be instantiated with a capacity greater than 0!" }
        this.desiredCapacity = withCapacity
        this.objects = arrayOfNulls(this.desiredCapacity)
        this.objectsPointer = 0
        this.modelObject = `object`
        this.replenishPercentage = 1.0f
        this.refillPool()
    }

    /**
     * Set the percentage of the pool to replenish on empty.  Valid values are between
     * 0.00f and 1.00f
     *
     * @param percentage a value between 0 and 1, representing the percentage of the pool to replenish.
     */
    fun setReplenishPercentage(percentage: Float) {
        var p = percentage
        if (p > 1) {
            p = 1f
        } else if (p < 0f) {
            p = 0f
        }
        this.replenishPercentage = p
    }

    fun getReplenishPercentage(): Float {
        return replenishPercentage
    }

    private fun refillPool(percentage: Float = this.replenishPercentage) {
        var portionOfCapacity = (desiredCapacity * percentage).toInt()

        if (portionOfCapacity < 1) {
            portionOfCapacity = 1
        } else if (portionOfCapacity > desiredCapacity) {
            portionOfCapacity = desiredCapacity
        }

        for (i in 0..<portionOfCapacity) {
            this.objects[i] = modelObject!!.instantiate()
        }
        objectsPointer = portionOfCapacity - 1
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
        if (this.objectsPointer == -1 && this.replenishPercentage > 0.0f) {
            this.refillPool()
        }

        val result = objects[this.objectsPointer]
        objects[this.objectsPointer] = null
        result!!.currentOwnerId = Poolable.Companion.NO_OWNER
        this.objectsPointer--

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
        if (`object`!!.currentOwnerId != Poolable.Companion.NO_OWNER) {
            require(`object`.currentOwnerId != this.poolId) { "The object passed is already stored in this pool!" }
            throw IllegalArgumentException("The object to recycle already belongs to poolId " + `object`.currentOwnerId + ".  Object cannot belong to two different pool instances simultaneously!")
        }

        this.objectsPointer++
        if (this.objectsPointer >= objects.size) {
            this.resizePool()
        }

        `object`.currentOwnerId = this.poolId
        objects[this.objectsPointer] = `object`
    }

    /**
     * Recycle a List of Poolables that this pool is capable of generating.
     * The T instances passed must not already exist inside this or any other ObjectPool instance.
     *
     * @param objects A list of objects of type T to recycle
     */
    @Synchronized
    fun recycle(objects: MutableList<T?>) {
        while (objects.size + this.objectsPointer + 1 > this.desiredCapacity) {
            this.resizePool()
        }
        val objectsListSize = objects.size

        // Not relying on recycle(T object) because this is more performant.
        for (i in 0..<objectsListSize) {
            val `object` = objects.get(i)
            if (`object`!!.currentOwnerId != Poolable.Companion.NO_OWNER) {
                require(`object`.currentOwnerId != this.poolId) { "The object passed is already stored in this pool!" }
                throw IllegalArgumentException("The object to recycle already belongs to poolId " + `object`.currentOwnerId + ".  Object cannot belong to two different pool instances simultaneously!")
            }
            `object`.currentOwnerId = this.poolId
            this.objects[this.objectsPointer + 1 + i] = `object`
        }
        this.objectsPointer += objectsListSize
    }

    private fun resizePool() {
        val oldCapacity = this.desiredCapacity
        this.desiredCapacity *= 2
        val temp: Array<T?> = arrayOfNulls(this.desiredCapacity)
        for (i in 0..<oldCapacity) {
            temp[i] = this.objects[i]
        }
        this.objects = temp
    }

    val poolCapacity: Int
        /**
         * Returns the capacity of this object pool.  Note : The pool will automatically resize
         * to contain additional objects if the user tries to add more objects than the pool's
         * capacity allows, but this comes at a performance cost.
         *
         * @return The capacity of the pool.
         */
        get() = this.objects.size

    val poolCount: Int
        /**
         * Returns the number of objects remaining in the pool, for diagnostic purposes.
         *
         * @return The number of objects remaining in the pool.
         */
        get() = this.objectsPointer + 1


    abstract class Poolable<T : Poolable<T>?> {
        var currentOwnerId: Int = NO_OWNER

        abstract fun instantiate(): T?

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