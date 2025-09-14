package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.notimportant.DemoBase
import java.util.Locale

class BarChartActivityMultiDataset : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: BarChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_barchart)

        setTitle("BarChartActivityMultiDataset")

        tvX = findViewById(R.id.tvXMax)
        tvX?.textSize = 10f
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarX?.setMax(50)
        seekBarX?.setOnSeekBarChangeListener(this)

        seekBarY = findViewById(R.id.seekBarY)
        seekBarY?.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart?.setOnChartValueSelectedListener(this)
        chart?.description?.isEnabled = false

        //        chart.setDrawBorders(true);

        // scaling can now only be done on x- and y-axis separately
        chart?.setPinchZoom(false)

        chart?.setDrawBarShadow(false)

        chart?.setDrawGridBackground(false)

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv = MyMarkerView(this, R.layout.custom_marker_view)
        mv.chartView = chart // For bounds control
        chart?.setMarker(mv) // Set the marker to the chart

        seekBarX?.progress = 10
        seekBarY?.progress = 100

        val l = chart?.legend
        l?.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l?.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l?.orientation = Legend.LegendOrientation.VERTICAL
        l?.setDrawInside(true)
        l?.typeface = tfLight
        l?.yOffset = 0f
        l?.xOffset = 10f
        l?.yEntrySpace = 0f
        l?.textSize = 8f

        val xAxis = chart?.xAxis
        xAxis?.typeface = tfLight
        xAxis?.granularity = 1f
        xAxis?.setCenterAxisLabels(true)
        xAxis?.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return value.toInt().toString()
            }
        }

        val leftAxis = chart?.axisLeft
        leftAxis?.typeface = tfLight
        leftAxis?.valueFormatter = LargeValueFormatter()
        leftAxis?.setDrawGridLines(false)
        leftAxis?.spaceTop = 35f
        leftAxis?.axisMinimum = 0f // this replaces setStartAtZero(true)

        chart?.axisRight?.isEnabled = false
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val groupSpace = 0.08f
        val barSpace = 0.03f // x4 DataSet
        val barWidth = 0.2f // x4 DataSet

        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"
        val groupCount = (seekBarX?.progress ?: 0) + 1
        val startYear = 1980
        val endYear = startYear + groupCount

        tvX?.text = String.format(Locale.ENGLISH, "%d-%d", startYear, endYear)
        tvY?.text = seekBarY?.progress.toString()

        val values1 = ArrayList<BarEntry>()
        val values2 = ArrayList<BarEntry>()
        val values3 = ArrayList<BarEntry>()
        val values4 = ArrayList<BarEntry>()

        val randomMultiplier = (seekBarY?.progress ?: 0) * 100000f
        val sampleValues = getValues(100 + 2)

        for (i in startYear..<endYear) {
            values1.add(BarEntry(i.toFloat(), (sampleValues[i - startYear].toFloat() * randomMultiplier)))
            values2.add(BarEntry(i.toFloat(), (sampleValues[i - startYear + 1].toFloat() * randomMultiplier)))
            values3.add(BarEntry(i.toFloat(), (sampleValues[i - startYear + 2].toFloat() * randomMultiplier)))
            values4.add(BarEntry(i.toFloat(), (sampleValues[i - startYear].toFloat() * randomMultiplier)))
        }

        val set1: BarDataSet?
        val set2: BarDataSet?
        val set3: BarDataSet?
        val set4: BarDataSet?

        if (chart?.data != null && chart?.data?.let { it.dataSetCount > 0 } == true) {
            set1 = chart?.data?.getDataSetByIndex(0) as BarDataSet
            set2 = chart?.data?.getDataSetByIndex(1) as BarDataSet
            set3 = chart?.data?.getDataSetByIndex(2) as BarDataSet
            set4 = chart?.data?.getDataSetByIndex(3) as BarDataSet
            set1.entries = values1
            set2.entries = values2
            set3.entries = values3
            set4.entries = values4
            chart?.data?.notifyDataChanged()
            chart?.notifyDataSetChanged()
        } else {
            // create 4 DataSets
            set1 = BarDataSet(values1, "Company A")
            set1.setColor(Color.rgb(104, 241, 175))
            set2 = BarDataSet(values2, "Company B")
            set2.setColor(Color.rgb(164, 228, 251))
            set3 = BarDataSet(values3, "Company C")
            set3.setColor(Color.rgb(242, 247, 158))
            set4 = BarDataSet(values4, "Company D")
            set4.setColor(Color.rgb(255, 102, 0))

            val data = BarData(set1, set2, set3, set4)
            data.setValueFormatter(LargeValueFormatter())
            data.setValueTypeface(tfLight)

            chart?.setData(data)
        }

        // specify the width each bar should have
        chart?.barData?.barWidth = barWidth

        // restrict the x-axis range
        chart?.xAxis?.axisMinimum = startYear.toFloat()

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        chart?.xAxis?.axisMaximum = startYear + ((chart?.barData?.getGroupWidth(groupSpace, barSpace) ?: 0f) * groupCount)
        chart?.groupBars(startYear.toFloat(), groupSpace, barSpace)
        chart?.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/BarChartActivityMultiDataset.java".toUri())
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                chart?.data?.dataSets?.let {
                    for (set in it) set.isDrawValuesEnabled = !set.isDrawValuesEnabled
                }

                chart?.invalidate()
            }

            R.id.actionTogglePinch -> {
                if (chart?.isPinchZoomEnabled == true) chart?.setPinchZoom(false)
                else chart?.setPinchZoom(true)

                chart?.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart?.isAutoScaleMinMaxEnabled = chart?.isAutoScaleMinMaxEnabled != true
                chart?.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                chart?.data?.dataSets?.let {
                    for (set in it) (set as BarDataSet).setBarBorderWidth(if (set.barBorderWidth == 1f) 0f else 1f)
                }

                chart?.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart?.data != null) {
                    chart?.data?.isHighlightEnabled = chart?.data?.isHighlightEnabled != true
                    chart?.invalidate()
                }
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(chart)
                }
            }

            R.id.animateX -> {
                chart?.animateX(2000)
            }

            R.id.animateY -> {
                chart?.animateY(2000)
            }

            R.id.animateXY -> {
                chart?.animateXY(2000, 2000)
            }
        }
        return true
    }

    override fun saveToGallery() {
        saveToGallery(chart, "BarChartActivityMultiDataset")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i("Activity", "Selected: " + e.toString() + ", dataSet: " + h?.dataSetIndex)
    }

    override fun onNothingSelected() {
        Log.i("Activity", "Nothing selected.")
    }
}
