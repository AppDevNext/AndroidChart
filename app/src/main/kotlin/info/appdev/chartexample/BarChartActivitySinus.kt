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
import androidx.core.net.toUri
import info.appdev.chartexample.databinding.ActivityBarchartSinusBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Legend
import info.appdev.charting.components.Legend.LegendForm
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.utils.loadBarEntriesFromAssets

class BarChartActivitySinus : DemoBase(), OnSeekBarChangeListener {

    private lateinit var dataSinus: MutableList<BarEntry>

    private lateinit var binding: ActivityBarchartSinusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarchartSinusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataSinus = assets.loadBarEntriesFromAssets("sinus_values.txt")
        binding.chart1.setDrawBarShadow(false)
        binding.chart1.setDrawValueAboveBar(true)

        binding.chart1.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be drawn
        binding.chart1.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        // chart.setDrawXLabels(false);
        binding.chart1.setDrawGridBackground(false)

        // chart.setDrawYLabels(false);
        val xAxis = binding.chart1.xAxis
        xAxis.isEnabled = false

        val leftAxis = binding.chart1.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.setLabelCount(6, false)
        leftAxis.axisMinimum = -2.5f
        leftAxis.axisMaximum = 2.5f
        leftAxis.isGranularityEnabled = true
        leftAxis.granularity = 0.1f

        val rightAxis = binding.chart1.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = tfLight
        rightAxis.setLabelCount(6, false)
        rightAxis.axisMinimum = -2.5f
        rightAxis.axisMaximum = 2.5f
        rightAxis.granularity = 0.1f

        binding.seekbarValues.setOnSeekBarChangeListener(this)
        binding.seekbarValues.progress = 150 // set data

        binding.chart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            form = LegendForm.SQUARE
            formSize = 9f
            textSize = 11f
            xEntrySpace = 4f
        }

        binding.chart1.animateXY(1500, 1500)
    }

    private fun setData(count: Int) {
        val entries = ArrayList<BarEntry>()

        for (i in 0..<count) {
            entries.add(dataSinus[i])
        }

        val set: BarDataSet

        if (binding.chart1.barData != null &&
            binding.chart1.barData!!.dataSetCount > 0
        ) {
            set = binding.chart1.barData!!.getDataSetByIndex(0) as BarDataSet
            set.entries = entries
            binding.chart1.barData?.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            set = BarDataSet(entries, "Sinus Function")
            set.color = Color.BLUE
        }

        val data = BarData(set)
        data.setValueTextSize(10f)
        data.setValueTypeface(tfLight)
        data.setDrawValues(false)
        data.barWidth = 0.8f

        binding.chart1.data = data
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/BarChartActivitySinus.java".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.barData?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.barData?.let {
                    it.isHighlightEnabled = !it.isHighlightEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionTogglePinch -> {
                if (binding.chart1.isPinchZoomEnabled)
                    binding.chart1.setPinchZoom(false)
                else binding.chart1.setPinchZoom(true)

                binding.chart1.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                binding.chart1.isAutoScaleMinMaxEnabled = !binding.chart1.isAutoScaleMinMaxEnabled
                binding.chart1.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                binding.chart1.barData?.dataSets?.forEach { set ->
                    (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f
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
        binding.tvValueCount.text = binding.seekbarValues.progress.toString()

        setData(binding.seekbarValues.progress)
        binding.chart1.invalidate()
    }

    override fun saveToGallery() = saveToGallery(binding.chart1, "BarChartActivitySinus")

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
