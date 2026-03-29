package info.appdev.charting.highlight

/**
 * Double-precision equivalent of [Range] for use with [info.appdev.charting.data.BarEntryDouble]
 * stacked entries. e.g. stack values are -10.0, 5.0, 20.0 -> ranges are (-10 to 0, 0 to 5, 5 to 25).
 */
class RangeDouble(var from: Double, var to: Double) {
    fun contains(value: Double): Boolean = value > from && value <= to
    fun isLarger(value: Double): Boolean = value > to
    fun isSmaller(value: Double): Boolean = value < from
}

