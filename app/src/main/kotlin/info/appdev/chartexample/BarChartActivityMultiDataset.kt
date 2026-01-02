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
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.databinding.ActivityBarchartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.AxisBase
import info.appdev.charting.components.Legend
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.formatter.IAxisValueFormatter
import info.appdev.charting.formatter.LargeValueFormatter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import timber.log.Timber
import java.util.Locale

class BarChartActivityMultiDataset : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityBarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvXMax.textSize = 10f

        binding.seekBarX.max = 50
        binding.seekBarX.setOnSeekBarChangeListener(this)

        binding.seekBarY.setOnSeekBarChangeListener(this)

        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.description.isEnabled = false

        //        chart.setDrawBorders(true);

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.setDrawBarShadow(false)

        binding.chart1.setDrawGridBackground(false)

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv = MyMarkerView(this, R.layout.custom_marker_view)
        mv.chartView = binding.chart1 // For bounds control
        binding.chart1.setMarker(mv) // Set the marker to the chart

        binding.seekBarX.progress = 10
        binding.seekBarY.progress = 100

        binding.chart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(true)
            typeface = tfLight
            yOffset = 0f
            xOffset = 10f
            yEntrySpace = 0f
            textSize = 8f
        }

        val xAxis = binding.chart1.xAxis
        xAxis.typeface = tfLight
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return value.toInt().toString()
            }
        }

        val leftAxis = binding.chart1.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.valueFormatter = LargeValueFormatter()
        leftAxis.setDrawGridLines(false)
        leftAxis.spaceTop = 35f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        binding.chart1.axisRight.isEnabled = false
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val groupSpace = 0.08f
        val barSpace = 0.03f // x4 DataSet
        val barWidth = 0.2f // x4 DataSet

        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"
        val groupCount = binding.seekBarX.progress + 1
        val startYear = 1980
        val endYear = startYear + groupCount

        binding.tvXMax.text = String.format(Locale.ENGLISH, "%d-%d", startYear, endYear)
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        val values1 = ArrayList<BarEntry>()
        val values2 = ArrayList<BarEntry>()
        val values3 = ArrayList<BarEntry>()
        val values4 = ArrayList<BarEntry>()

        val randomMultiplier = binding.seekBarY.progress * 100000f
        val sampleValues = getValues(100 + 2)

        for (i in startYear..<endYear) {
            values1.add(BarEntry(i.toFloat(), (sampleValues[i - startYear]!!.toFloat() * randomMultiplier)))
            values2.add(BarEntry(i.toFloat(), (sampleValues[i - startYear + 1]!!.toFloat() * randomMultiplier)))
            values3.add(BarEntry(i.toFloat(), (sampleValues[i - startYear + 2]!!.toFloat() * randomMultiplier)))
            values4.add(BarEntry(i.toFloat(), (sampleValues[i - startYear]!!.toFloat() * randomMultiplier)))
        }

        val set1: BarDataSet
        val set2: BarDataSet
        val set3: BarDataSet
        val set4: BarDataSet

        if (binding.chart1.barData != null && binding.chart1.barData!!.dataSetCount > 0) {
            set1 = binding.chart1.barData!!.getDataSetByIndex(0) as BarDataSet
            set2 = binding.chart1.barData!!.getDataSetByIndex(1) as BarDataSet
            set3 = binding.chart1.barData!!.getDataSetByIndex(2) as BarDataSet
            set4 = binding.chart1.barData!!.getDataSetByIndex(3) as BarDataSet
            set1.entries = values1
            set2.entries = values2
            set3.entries = values3
            set4.entries = values4
            binding.chart1.barData?.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            // create 4 DataSets
            set1 = BarDataSet(values1, "Company A")
            set1.color = Color.rgb(104, 241, 175)
            set2 = BarDataSet(values2, "Company B")
            set2.color = Color.rgb(164, 228, 251)
            set3 = BarDataSet(values3, "Company C")
            set3.color = Color.rgb(242, 247, 158)
            set4 = BarDataSet(values4, "Company D")
            set4.color = Color.rgb(255, 102, 0)

            val data = BarData(set1, set2, set3, set4)
            data.setValueFormatter(LargeValueFormatter())
            data.setValueTypeface(tfLight)

            binding.chart1.data = data
        }

        // specify the width each bar should have
        binding.chart1.barData?.let { it.barWidth = barWidth }

        // restrict the x-axis range
        binding.chart1.xAxis.axisMinimum = startYear.toFloat()

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        binding.chart1.barData?.let { binding.chart1.xAxis.axisMaximum = startYear + it.getGroupWidth(groupSpace, barSpace) * groupCount }
        binding.chart1.groupBars(startYear.toFloat(), groupSpace, barSpace)
        binding.chart1.invalidate()
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
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/BarChartActivityMultiDataset.java".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.barData?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionTogglePinch -> {
                if (binding.chart1.isPinchZoomEnabled) binding.chart1.setPinchZoom(false)
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

            R.id.actionToggleHighlight -> {
                binding.chart1.barData?.let {
                    it.isHighlightEnabled = !it.isHighlightEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(binding.chart1)
                }
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
        }
        return true
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "BarChartActivityMultiDataset")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Selected: $entry, dataSet: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() = Unit
}
