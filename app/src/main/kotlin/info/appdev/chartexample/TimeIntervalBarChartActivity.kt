package info.appdev.chartexample

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import info.appdev.chartexample.databinding.ActivityHorizontalbarchartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.chartexample.GanttUtils
import timber.log.Timber

/**
 * using HorizontalBarChart for Gantt-style time interval visualization.
 * Shows how to display tasks as horizontal bars with start time and duration.
 */
class TimeIntervalBarChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityHorizontalbarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHorizontalbarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create sample tasks with time intervals
        val entries: MutableList<BarEntry> = ArrayList()

        entries.add(GanttUtils.createMultiSegmentEntry(-1f, floatArrayOf(-50f, -20f, 20f, 50f)))
        // starts at 0, duration 100
        entries.add(GanttUtils.createTimeIntervalEntry(0f, 0f, 100f))
        // starts at 50, duration 150
        entries.add(GanttUtils.createTimeIntervalEntry(1f, 50f, 150f))
        // starts at 150, duration 100
        entries.add(GanttUtils.createTimeIntervalEntry(2f, 150f, 100f))

        Timber.d(entries.joinToString(separator = "\n"))

        // Create dataset with time interval data
        val dataSet = BarDataSet(entries, "Tasks")
        dataSet.setColors(
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW
        )
        dataSet.barBorderWidth = 1f
        dataSet.barBorderColor = Color.BLACK

        // Create and set data
        val barData = BarData(dataSet)
        barData.barWidth = 0.8f
        binding.chart1.data = barData

        // Configure chart
        binding.chart1.setFitBars(true)
        binding.chart1.isDrawValueAboveBar = true
        binding.chart1.xAxis.setDrawLabels(true)

        val yl = binding.chart1.axisLeft
        yl.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)

        // Refresh
        binding.chart1.invalidate()
    }

    override fun saveToGallery() = Unit

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) = Unit

    override fun onStartTrackingTouch(p0: SeekBar?) = Unit

    override fun onStopTrackingTouch(p0: SeekBar?) = Unit

    override fun onValueSelected(entry: Entry, highlight: Highlight) = Unit

    override fun onNothingSelected() = Unit
}
