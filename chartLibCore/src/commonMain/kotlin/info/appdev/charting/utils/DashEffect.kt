package info.appdev.charting.utils

/**
 * Platform-independent replacement for `android.graphics.DashPathEffect`, describing a
 * dashed-line pattern for lines drawn by the chart (legend forms, highlight lines, etc.).
 *
 * @param intervals alternating "on" and "off" lengths, in pixels; must have an even size >= 2
 * @param phase offset into the intervals array at which drawing begins
 */
class DashEffect(val intervals: FloatArray, val phase: Float) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DashEffect) return false
        return intervals.contentEquals(other.intervals) && phase == other.phase
    }

    override fun hashCode(): Int {
        var result = intervals.contentHashCode()
        result = 31 * result + phase.hashCode()
        return result
    }

    override fun toString(): String {
        return "DashEffect(intervals=${intervals.contentToString()}, phase=$phase)"
    }
}
