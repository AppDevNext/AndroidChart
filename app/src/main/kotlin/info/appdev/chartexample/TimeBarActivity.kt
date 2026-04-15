package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityHorizontalbarchartBinding
import info.appdev.chartexample.formatter.TimeRangeValueFormatter
import info.appdev.chartexample.formatter.UnixTimeRelative2NowAxisValueFormatter
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Description
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntryDouble
import info.appdev.charting.data.BarEntryFloat
import info.appdev.charting.interfaces.datasets.IBarDataSet
import timber.log.Timber

class TimeBarActivity : DemoBase(), OnSeekBarChangeListener {

    private lateinit var binding: ActivityHorizontalbarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHorizontalbarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)

        binding.chart1.isLogging = true
        binding.chart1.isDrawBarShadow = false
        binding.chart1.isDrawValueAboveBar = true
        binding.chart1.description.isEnabled = true
        binding.chart1.description = Description().apply {
            text = "Time Bar Line"
        }

        // if more than 60 entries are displayed in the chart, no values will be drawn
        binding.chart1.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.isPinchZoom = false

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);
        binding.chart1.setDrawGridBackground(false)

        binding.chart1.xAxis.apply {
            position = XAxisPosition.BOTTOM
            typeface = tfLight
            isDrawAxisLine = true
            isDrawGridLines = true
            granularity = 10f
//            valueFormatter = UnixTimeAxisValueFormatter("HH:mm:ss")
        }

//        binding.chart1.axisLeft.apply {
//            typeface = tfLight
//            isDrawAxisLine = true
//            isDrawGridLines = true
//            axisMinimum = 0f // this replaces setStartAtZero(true)
//        }

        binding.chart1.axisRight.apply {
            typeface = tfLight
            isDrawAxisLine = true
            axisMaxLabels = 4
            isDrawGridLines = false
            axisMinimum = 0f // this replaces setStartAtZero(true)
            valueFormatter = UnixTimeRelative2NowAxisValueFormatter("mm:ss", 1776000000 * 1000L)
        }

        binding.chart1.setFitBars(true)
        binding.chart1.animateY(2500)

        // setting data
        binding.seekBarX.progress = 4
        binding.seekBarY.progress = 12

        binding.chart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            formSize = 8f
            xEntrySpace = 4f
        }
    }

    private fun setData(count: Int) {
        val barWidth = 9f
        val spaceForBar = 10.0f
        val values = ArrayList<BarEntryFloat>()
        val sampleValues = getValues(100).map { (it!! * 100).toInt() }

        var previousTimeOffset = 0f //TIME_OFFSET.toFloat()
        for (i in 0..<count) {
            Timber.d("add ${sampleValues[i]}s to $previousTimeOffset")
            val yValue = sampleValues[i] + previousTimeOffset
            val value = BarEntryFloat(
                x = i * spaceForBar,
                vals = floatArrayOf(previousTimeOffset, yValue),
                icon = ResourcesCompat.getDrawable(resources, R.drawable.star, null)
            )
            values.add(value)
            previousTimeOffset = yValue
        }

        Timber.d(values.joinToString(separator = "\n"))

        val set1: BarDataSet

        if (binding.chart1.barData != null &&
            binding.chart1.barData!!.dataSetCount > 0
        ) {
            set1 = binding.chart1.barData!!.getDataSetByIndex(0) as BarDataSet
            @Suppress("UNCHECKED_CAST")
            set1.entries = values as MutableList<BarEntryFloat>
            binding.chart1.barData?.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            @Suppress("UNCHECKED_CAST")
            set1 = BarDataSet(values as MutableList<BarEntryFloat>, "Bar DataSet")
            set1.setColors(
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW
            )

            set1.isDrawIcons = false

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTypeface(tfLight)
            data.setValueFormatter(TimeRangeValueFormatter("HH:mm:ss"))
            data.barWidth = barWidth
            binding.chart1.data = data
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/HorizontalBarChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.barData?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleIcons -> {
                binding.chart1.barData?.dataSets?.forEach { set ->
                    set.isDrawIcons = !set.isDrawIcons
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.barData?.let { data ->
                    data.isHighlight = !data.isHighlight
                    binding.chart1.invalidate()
                }
            }

            R.id.actionTogglePinch -> {
                binding.chart1.isPinchZoom = !binding.chart1.isPinchZoom
                binding.chart1.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                binding.chart1.isAutoScaleMinMax = !binding.chart1.isAutoScaleMinMax
                binding.chart1.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                binding.chart1.barData?.dataSets?.map { it as BarDataSet }?.forEach { set ->
                    set.barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f
                }
                binding.chart1.invalidate()
            }

            R.id.animateX -> {
                binding.chart1.animateX(2000)
            }

            R.id.animateY -> {
                binding.chart1.animateY(2000)
            }

            R.id.animateXY -> {
                binding.chart1.animateXY(2000, 2000)
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(binding.chart1)
                }
            }
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        setData(binding.seekBarX.progress)
        binding.chart1.setFitBars(true)
        binding.chart1.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "HorizontalBarChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

}
