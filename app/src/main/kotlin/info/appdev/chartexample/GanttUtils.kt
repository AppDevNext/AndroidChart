package info.appdev.chartexample

import info.appdev.charting.data.BarEntry

/**
 * Utility class for creating time interval entries for Gantt-style charts.
 * Provides helper methods to create BarEntry objects with time interval data.
 */
object GanttUtils {
    /**
     * Create a time interval entry for a single task.
     *
     * @param taskIndex Y-axis position (task row)
     * @param startTime Start time value
     * @param duration Duration value
     * @return BarEntry configured for time interval rendering
     */
    fun createTimeIntervalEntry(taskIndex: Float, startTime: Float, duration: Float): BarEntry {
        return BarEntry(taskIndex, floatArrayOf(startTime, duration))
    }

    /**
     * Create a time interval entry with multiple segments.
     * Useful for showing multiple time ranges for a single task.
     *
     * @param taskIndex Y-axis position (task row)
     * @param timeIntervals Array of [start1, duration1, start2, duration2, ...]
     * @return BarEntry configured for time interval rendering
     */
    fun createMultiSegmentEntry(taskIndex: Float, timeIntervals: FloatArray): BarEntry {
        require(timeIntervals.size >= 2) { "timeIntervals must have at least 2 elements" }
        return BarEntry(taskIndex, timeIntervals)
    }

    /**
     * Create a time interval entry with custom data.
     *
     * @param taskIndex Y-axis position (task row)
     * @param startTime Start time value
     * @param duration Duration value
     * @param data Custom data object
     * @return BarEntry configured for time interval rendering with data
     */
    fun createTimeIntervalEntry(
        taskIndex: Float, startTime: Float,
        duration: Float, data: Any?
    ): BarEntry {
        return BarEntry(taskIndex, floatArrayOf(startTime, duration), data)
    }
}