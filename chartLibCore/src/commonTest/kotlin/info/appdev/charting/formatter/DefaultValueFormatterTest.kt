package info.appdev.charting.formatter

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultValueFormatterTest {

    @Test
    fun formatsWithZeroDigits() {
        val formatter = DefaultValueFormatter(0)
        assertEquals("42", formatter.getFormattedValue(42f, null, 0, null))
        assertEquals("0", formatter.getFormattedValue(0f, null, 0, null))
        assertEquals("-7", formatter.getFormattedValue(-7f, null, 0, null))
    }

    @Test
    fun formatsWithDecimalDigits() {
        val formatter = DefaultValueFormatter(2)
        assertEquals("1.50", formatter.getFormattedValue(1.5f, null, 0, null))
        assertEquals("0.10", formatter.getFormattedValue(0.1f, null, 0, null))
        assertEquals("-3.25", formatter.getFormattedValue(-3.25f, null, 0, null))
    }

    @Test
    fun groupsThousands() {
        val formatter = DefaultValueFormatter(1)
        assertEquals("1,234.5", formatter.getFormattedValue(1234.5f, null, 0, null))
        assertEquals("1,000,000.0", formatter.getFormattedValue(1_000_000f, null, 0, null))
    }

    @Test
    fun negativeZeroDoesNotKeepMinusSign() {
        val formatter = DefaultValueFormatter(0)
        assertEquals("0", formatter.getFormattedValue(-0.1f, null, 0, null))
    }

    @Test
    fun setupUpdatesDecimalDigits() {
        val formatter = DefaultValueFormatter(1)
        formatter.setup(3)
        assertEquals(3, formatter.decimalDigits)
        assertEquals("2.000", formatter.getFormattedValue(2f, null, 0, null))
    }
}
