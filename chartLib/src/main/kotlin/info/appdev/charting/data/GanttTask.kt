package info.appdev.charting.data

/**
 * Represents a single task in a Gantt chart.
 * Each task has a name, start time, duration, and display color.
 *
 * @param name Task name/label
 * @param startTime When the task starts
 * @param duration How long the task lasts
 * @param color Display color (Android color int)
 */
class GanttTask(val name: String?, val startTime: Float, val duration: Float, val color: Int, val hatched: Boolean = false) {
    val endTime: Float
        get() = startTime + duration

    override fun toString(): String =
        "GanttTask(name=$name, startTime=$startTime, duration=$duration, endTime=$endTime, hatched=$hatched, color=$color)"
}
