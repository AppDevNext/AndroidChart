package info.appdev.chartexample

import android.graphics.Color
import android.os.Bundle
import info.appdev.chartexample.databinding.ActivityTimeIntervalChartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.data.GanttChartData
import info.appdev.charting.data.GanttTask
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * Demo activity showing Gantt-style timeline visualization.
 * Each horizontal bar represents a task with start time and duration.
 */
class TimeIntervalChartActivity : DemoBase(), OnChartValueSelectedListener {

    private lateinit var binding: ActivityTimeIntervalChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeIntervalChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Gantt chart data
        val ganttData = GanttChartData()

        // Add sample project tasks
        ganttData.addTask(GanttTask("Design", 0f, 50f, Color.rgb(255, 107, 107))) // Red: 0-50
        ganttData.addTask(GanttTask("Dev", 40f, 100f, Color.rgb(66, 165, 245))) // Blue: 40-140
        ganttData.addTask(GanttTask("Testing", 120f, 40f, Color.rgb(76, 175, 80), hatched = true)) // Green: 120-160
        ganttData.addTask(GanttTask("Launch", 150f, 20f, Color.rgb(255, 193, 7))) // Yellow: 150-170
        ganttData.minTime = 10f
        ganttData.maxTime = 200f
        // Set data and render
        binding.chart1.setData(ganttData)
    }

    override fun saveToGallery() = Unit

    override fun onValueSelected(entryFloat: EntryFloat, highlight: Highlight) = Unit

    override fun onNothingSelected() = Unit

}
