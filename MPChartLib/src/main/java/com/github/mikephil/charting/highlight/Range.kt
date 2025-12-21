package com.github.mikephil.charting.highlight

/**
 * Class that represents the range of one value in a stacked bar entry. e.g.
 * stack values are -10, 5, 20 -> then ranges are (-10 - 0, 0 - 5, 5 - 25).
 */
class Range(var from: Float, @JvmField var to: Float) {
    /**
     * Returns true if this range contains (if the value is in between) the given value, false if not.
     */
    fun contains(value: Float): Boolean {
        if (value > from && value <= to) return true
        else return false
    }

    fun isLarger(value: Float): Boolean {
        return value > to
    }

    fun isSmaller(value: Float): Boolean {
        return value < from
    }
}
