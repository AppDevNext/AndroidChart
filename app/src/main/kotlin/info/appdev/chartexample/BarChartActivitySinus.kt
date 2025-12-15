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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.FileUtils
import info.appdev.chartexample.notimportant.DemoBase

class BarChartActivitySinus : DemoBase(), OnSeekBarChangeListener {
    private var chart1: BarChart? = null
    private var seekbarValues: SeekBar? = null
    private var tvValueCount: TextView? = null

    private var dataSinus: MutableList<BarEntry?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barchart_sinus)

        dataSinus = FileUtils.loadBarEntriesFromAssets(assets, "othersine.txt")

        tvValueCount = findViewById(R.id.tvValueCount)

        seekbarValues = findViewById(R.id.seekbarValues)

        chart1 = findViewById(R.id.chart1)

        chart1!!.setDrawBarShadow(false)
        chart1!!.setDrawValueAboveBar(true)

        chart1!!.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart1!!.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        chart1!!.setPinchZoom(false)

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        // chart.setDrawXLabels(false);
        chart1!!.setDrawGridBackground(false)

        // chart.setDrawYLabels(false);
        val xAxis = chart1!!.xAxis
        xAxis.isEnabled = false

        val leftAxis = chart1!!.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.setLabelCount(6, false)
        leftAxis.setAxisMinimum(-2.5f)
        leftAxis.setAxisMaximum(2.5f)
        leftAxis.isGranularityEnabled = true
        leftAxis.setGranularity(0.1f)

        val rightAxis = chart1!!.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = tfLight
        rightAxis.setLabelCount(6, false)
        rightAxis.setAxisMinimum(-2.5f)
        rightAxis.setAxisMaximum(2.5f)
        rightAxis.setGranularity(0.1f)

        seekbarValues!!.setOnSeekBarChangeListener(this)
        seekbarValues!!.progress = 150 // set data

        chart1!!.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            form = LegendForm.SQUARE
            formSize = 9f
            textSize = 11f
            xEntrySpace = 4f
        }

        chart1!!.animateXY(1500, 1500)
    }

    private fun setData(count: Int) {
        val entries = ArrayList<BarEntry?>()

        for (i in 0..<count) {
            entries.add(dataSinus!![i])
        }

        val set: BarDataSet

        if (chart1!!.data != null &&
            chart1!!.data!!.getDataSetCount() > 0
        ) {
            set = chart1!!.data!!.getDataSetByIndex(0) as BarDataSet
            set.setEntries(entries)
            chart1!!.data!!.notifyDataChanged()
            chart1!!.notifyDataSetChanged()
        } else {
            set = BarDataSet(entries, "Sinus Function")
            set.setColor(Color.BLUE)
        }

        val data = BarData(set)
        data.setValueTextSize(10f)
        data.setValueTypeface(tfLight)
        data.setDrawValues(false)
        data.barWidth = 0.8f

        chart1!!.setData(data)
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
                for (set in chart1!!.data!!.dataSets) set.setDrawValues(!set.isDrawValuesEnabled())

                chart1!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart1!!.data != null) {
                    chart1!!.data!!.isHighlightEnabled = !chart1!!.data!!.isHighlightEnabled()
                    chart1!!.invalidate()
                }
            }

            R.id.actionTogglePinch -> {
                if (chart1!!.isPinchZoomEnabled) chart1!!.setPinchZoom(false)
                else chart1!!.setPinchZoom(true)

                chart1!!.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart1!!.isAutoScaleMinMaxEnabled = !chart1!!.isAutoScaleMinMaxEnabled
                chart1!!.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                for (set in chart1!!.data!!.dataSets) (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f

                chart1!!.invalidate()
            }

            R.id.animateX -> {
                chart1!!.animateX(2000)
            }

            R.id.animateY -> {
                chart1!!.animateY(2000)
            }

            R.id.animateXY -> {
                chart1!!.animateXY(2000, 2000)
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(chart1)
                }
            }
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvValueCount!!.text = seekbarValues!!.progress.toString()

        setData(seekbarValues!!.progress)
        chart1!!.invalidate()
    }

    override fun saveToGallery() = saveToGallery(chart1, "BarChartActivitySinus")

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
