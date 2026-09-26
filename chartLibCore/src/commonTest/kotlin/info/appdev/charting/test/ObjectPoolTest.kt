package info.appdev.charting.test

import info.appdev.charting.utils.ObjectPool
import info.appdev.charting.utils.ObjectPool.Poolable
import kotlin.test.assertEquals
import kotlin.test.Test

class ObjectPoolTest {
    internal class TestPoolable private constructor(var foo: Int, var bar: Int) : Poolable<TestPoolable>() {
        override fun instantiate(): TestPoolable {
            return TestPoolable(0, 0)
        }

        companion object {
            private val pool: ObjectPool<TestPoolable> = ObjectPool.create(4, TestPoolable(0, 0))

            fun getInstance(foo: Int, bar: Int): TestPoolable {
                val result = pool.get()
                result.foo = foo
                result.bar = bar
                return result
            }

            fun recycleInstance(instance: TestPoolable) {
                pool.recycle(instance)
            }

            fun recycleInstances(instances: List<TestPoolable>) {
                pool.recycle(instances)
            }

            fun getPool(): ObjectPool<*> {
                return pool
            }
        }
    }

    @Test
    fun testObjectPool() {
        var poolCapacity = TestPoolable.getPool().poolCapacity
        var poolCount = TestPoolable.getPool().poolCount
        val testPoolables = ArrayList<TestPoolable>()

        assertEquals(4, poolCapacity)
        assertEquals(4, poolCount)

        var testPoolable = TestPoolable.getInstance(6, 7)
        assertEquals(6, testPoolable.foo)
        assertEquals(7, testPoolable.bar)

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount

        assertEquals(4, poolCapacity)
        assertEquals(3, poolCount)

        TestPoolable.recycleInstance(testPoolable)

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(4, poolCapacity)
        assertEquals(4, poolCount)


        testPoolable = TestPoolable.getInstance(20, 30)
        assertEquals(20, testPoolable.foo)
        assertEquals(30, testPoolable.bar)

        TestPoolable.recycleInstance(testPoolable)

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(4, poolCapacity)
        assertEquals(4, poolCount)

        testPoolables.add(TestPoolable.getInstance(12, 24))
        testPoolables.add(TestPoolable.getInstance(1, 2))
        testPoolables.add(TestPoolable.getInstance(3, 5))
        testPoolables.add(TestPoolable.getInstance(6, 8))

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(4, poolCapacity)
        assertEquals(0, poolCount)


        TestPoolable.recycleInstances(testPoolables)
        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(4, poolCapacity)
        assertEquals(4, poolCount)

        testPoolables.clear()


        testPoolables.add(TestPoolable.getInstance(12, 24))
        testPoolables.add(TestPoolable.getInstance(1, 2))
        testPoolables.add(TestPoolable.getInstance(3, 5))
        testPoolables.add(TestPoolable.getInstance(6, 8))
        testPoolables.add(TestPoolable.getInstance(8, 9))
        assertEquals(12, testPoolables[0].foo)
        assertEquals(24, testPoolables[0].bar)
        assertEquals(1, testPoolables[1].foo)
        assertEquals(2, testPoolables[1].bar)
        assertEquals(3, testPoolables[2].foo)
        assertEquals(5, testPoolables[2].bar)
        assertEquals(6, testPoolables[3].foo)
        assertEquals(8, testPoolables[3].bar)
        assertEquals(8, testPoolables[4].foo)
        assertEquals(9, testPoolables[4].bar)


        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(4, poolCapacity)
        assertEquals(3, poolCount)

        TestPoolable.recycleInstances(testPoolables)
        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(8, poolCapacity)
        assertEquals(8, poolCount)

        testPoolables.clear()


        testPoolables.add(TestPoolable.getInstance(0, 0))
        testPoolables.add(TestPoolable.getInstance(6, 8))
        testPoolables.add(TestPoolable.getInstance(1, 2))
        testPoolables.add(TestPoolable.getInstance(3, 5))
        testPoolables.add(TestPoolable.getInstance(8, 9))
        testPoolables.add(TestPoolable.getInstance(12, 24))
        testPoolables.add(TestPoolable.getInstance(12, 24))
        testPoolables.add(TestPoolable.getInstance(12, 24))
        testPoolables.add(TestPoolable.getInstance(6, 8))
        testPoolables.add(TestPoolable.getInstance(6, 8))
        assertEquals(0, testPoolables[0].foo)
        assertEquals(0, testPoolables[0].bar)
        assertEquals(6, testPoolables[1].foo)
        assertEquals(8, testPoolables[1].bar)
        assertEquals(1, testPoolables[2].foo)
        assertEquals(2, testPoolables[2].bar)
        assertEquals(3, testPoolables[3].foo)
        assertEquals(5, testPoolables[3].bar)
        assertEquals(8, testPoolables[4].foo)
        assertEquals(9, testPoolables[4].bar)
        assertEquals(12, testPoolables[5].foo)
        assertEquals(24, testPoolables[5].bar)
        assertEquals(12, testPoolables[6].foo)
        assertEquals(24, testPoolables[6].bar)
        assertEquals(12, testPoolables[7].foo)
        assertEquals(24, testPoolables[7].bar)
        assertEquals(6, testPoolables[8].foo)
        assertEquals(8, testPoolables[8].bar)
        assertEquals(6, testPoolables[9].foo)
        assertEquals(8, testPoolables[9].bar)

        for (p in testPoolables) {
            TestPoolable.recycleInstance(p)
        }

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(16, poolCapacity)
        assertEquals(16, poolCount)

        testPoolable = TestPoolable.getInstance(9001, 9001)
        assertEquals(9001, testPoolable.foo)
        assertEquals(9001, testPoolable.bar)

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(16, poolCapacity)
        assertEquals(15, poolCount)


        TestPoolable.recycleInstance(testPoolable)

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(16, poolCapacity)
        assertEquals(16, poolCount)

        var e: Exception? = null
        try {
            // expect an exception.
            TestPoolable.recycleInstance(testPoolable)
        } catch (ex: Exception) {
            e = ex
        } finally {
            assertEquals(true, true, e!!.message)
        }

        testPoolables.clear()

        TestPoolable.getPool().replenishPercentage = 0.5f
        var i = 16
        while (i > 0) {
            testPoolables.add(TestPoolable.getInstance(0, 0))
            i--
        }

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(16, poolCapacity)
        assertEquals(0, poolCount)

        testPoolables.add(TestPoolable.getInstance(0, 0))

        poolCapacity = TestPoolable.getPool().poolCapacity
        poolCount = TestPoolable.getPool().poolCount
        assertEquals(16, poolCapacity)
        assertEquals(7, poolCount)
    }
}