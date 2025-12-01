package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.FileUtils
import info.appdev.chartexample.notimportant.DemoBase
import androidx.core.net.toUri

class BarChartActivitySinus : DemoBase(), OnSeekBarChangeListener {
    private var chart: BarChart? = null
    private var seekBarX: SeekBar? = null
    private var tvX: TextView? = null

    private var data: MutableList<BarEntry?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_barchart_sinus)

        title = "BarChartActivitySinus"

        data = FileUtils.loadBarEntriesFromAssets(assets, "othersine.txt")

        tvX = findViewById<TextView>(R.id.tvValueCount)

        seekBarX = findViewById<SeekBar>(R.id.seekbarValues)

        chart = findViewById<BarChart>(R.id.chart1)

        chart!!.setDrawBarShadow(false)
        chart!!.setDrawValueAboveBar(true)

        chart!!.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart!!.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        // chart.setDrawXLabels(false);
        chart!!.setDrawGridBackground(false)

        // chart.setDrawYLabels(false);
        val xAxis = chart!!.xAxis
        xAxis.isEnabled = false

        val leftAxis = chart!!.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.setLabelCount(6, false)
        leftAxis.setAxisMinimum(-2.5f)
        leftAxis.setAxisMaximum(2.5f)
        leftAxis.isGranularityEnabled = true
        leftAxis.setGranularity(0.1f)

        val rightAxis = chart!!.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = tfLight
        rightAxis.setLabelCount(6, false)
        rightAxis.setAxisMinimum(-2.5f)
        rightAxis.setAxisMaximum(2.5f)
        rightAxis.setGranularity(0.1f)

        seekBarX!!.setOnSeekBarChangeListener(this)
        seekBarX!!.progress = 150 // set data

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f

        chart!!.animateXY(1500, 1500)
    }

    private fun setData(count: Int) {
        val entries = ArrayList<BarEntry?>()

        for (i in 0..<count) {
            entries.add(data!!.get(i))
        }

        val set: BarDataSet

        if (chart!!.data != null &&
            chart!!.data!!.getDataSetCount() > 0
        ) {
            set = chart!!.data!!.getDataSetByIndex(0) as BarDataSet
            set.setEntries(entries)
            chart!!.data!!.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else {
            set = BarDataSet(entries, "Sinus Function")
            set.setColor(Color.BLUE)
        }

        val data = BarData(set)
        data.setValueTextSize(10f)
        data.setValueTypeface(tfLight)
        data.setDrawValues(false)
        data.barWidth = 0.8f

        chart!!.setData(data)
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
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/BarChartActivitySinus.java".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                for (set in chart!!.data!!.dataSets) set.setDrawValues(!set.isDrawValuesEnabled())

                chart!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled()
                    chart!!.invalidate()
                }
            }

            R.id.actionTogglePinch -> {
                if (chart!!.isPinchZoomEnabled) chart!!.setPinchZoom(false)
                else chart!!.setPinchZoom(true)

                chart!!.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                for (set in chart!!.data!!.dataSets) (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f

                chart!!.invalidate()
            }

            R.id.animateX -> {
                chart!!.animateX(2000)
            }

            R.id.animateY -> {
                chart!!.animateY(2000)
            }

            R.id.animateXY -> {
                chart!!.animateXY(2000, 2000)
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(chart)
                }
            }
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX!!.text = seekBarX!!.progress.toString()

        setData(seekBarX!!.progress)
        chart!!.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(chart, "BarChartActivitySinus")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
