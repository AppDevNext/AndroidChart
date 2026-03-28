package info.appdev.charting.data

import kotlin.math.max
import kotlin.math.min

/**
 * Data container for Gantt chart.
 * Manages a list of tasks and provides convenient access methods.
 */
class GanttChartData {
    /**
     * Get all tasks.
     * 
     * @return List of all tasks
     */
    private val tasks: MutableList<GanttTask> = mutableListOf()

    /**
     * Add a task to the Gantt chart.
     * 
     * @param task The task to add
     */
    fun addTask(task: GanttTask?) {
        tasks.add(task!!)
    }

    /**
     * Add multiple tasks to the Gantt chart.
     * 
     * @param taskList List of tasks to add
     */
    fun addTasks(taskList: MutableList<GanttTask>) {
        tasks.addAll(taskList)
    }

    /**
     * Get a specific task by index.
     * 
     * @param index Task index
     * @return The task at the given index
     */
    fun getTask(index: Int): GanttTask {
        return tasks[index]
    }

    /**
     * Get the number of tasks.
     *
     * @return Number of tasks in the chart
     */
    val taskCount: Int
        get() = tasks.size

    /**
     * Get the earliest start time across all tasks.
     *
     * @return Minimum start time
     */
    val minTime: Float
        get() {
            if (tasks.isEmpty()) return 0f
            var min = Float.MAX_VALUE
            for (task in tasks) {
                min = min(min, task.startTime)
            }
            return min
        }

    /**
     * Get the latest end time across all tasks.
     *
     * @return Maximum end time
     */
    val maxTime: Float
        get() {
            if (tasks.isEmpty()) return 100f
            var max = 0f
            for (task in tasks) {
                max = max(max, task.endTime)
            }
            return max
        }

    /**
     * Clear all tasks.
     */
    fun clearTasks() {
        tasks.clear()
    }
}
