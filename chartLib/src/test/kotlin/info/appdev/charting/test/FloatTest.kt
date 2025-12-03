package info.appdev.charting.test

import org.junit.Assert
import org.junit.Test

class FloatTest {


    @Test
    fun testFloatConversion() {
        val timestamp: Long = System.currentTimeMillis()

        // 2) Cast to Float and back
        val asFloat: Float = timestamp.toFloat()
        val roundTripFromFloat: Long = asFloat.toLong()

        // 3) Cast to Double and back
        val asDouble: Double = timestamp.toDouble()
        val roundTripFromDouble: Long = asDouble.toLong()

        println("Original timestamp:       $timestamp")
        println("→ as Float:               $asFloat")
        println("→ back to Long (Float):   $roundTripFromFloat")
        println()
        println("→ as Double:              $asDouble")
        println("→ back to Long (Double):  $roundTripFromDouble")
        println()
        Assert.assertNotEquals(timestamp, roundTripFromFloat)
        Assert.assertEquals(timestamp, roundTripFromDouble)
    }
}
