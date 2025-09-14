package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.Fill
import com.github.mikephil.charting.utils.MPPointF.Companion.recycleInstance
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.DayAxisValueFormatter
import info.appdev.chartexample.custom.MyAxisValueFormatter
import info.appdev.chartexample.custom.XYMarkerView
import info.appdev.chartexample.notimportant.DemoBase

class BarChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
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

        setTitle("BarChartActivity")

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)

        seekBarY?.setOnSeekBarChangeListener(this)
        seekBarX?.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart?.setOnChartValueSelectedListener(this)
        chart?.setRoundedBarRadius(50f)

        chart?.isDrawBarShadowEnabled = false
        chart?.isDrawValueAboveBarEnabled = true

        chart?.description?.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart?.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        chart?.setPinchZoom(false)

        chart?.drawGridBackground = false

        // chart.setDrawYLabels(false);
        val xAxisFormatter: IAxisValueFormatter = DayAxisValueFormatter(chart!!)

        val xAxis = chart?.xAxis
        xAxis?.position = XAxisPosition.BOTTOM
        xAxis?.typeface = tfLight
        xAxis?.setDrawGridLines(false)
        xAxis?.granularity = 1f // only intervals of 1 day
        xAxis?.labelCount = 7
        xAxis?.valueFormatter = xAxisFormatter

        val custom: IAxisValueFormatter = MyAxisValueFormatter()

        val leftAxis = chart?.axisLeft
        leftAxis?.typeface = tfLight
        leftAxis?.setLabelCount(8, false)
        leftAxis?.valueFormatter = custom
        leftAxis?.setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis?.spaceTop = 15f
        leftAxis?.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = chart?.axisRight
        rightAxis?.setDrawGridLines(false)
        rightAxis?.typeface = tfLight
        rightAxis?.setLabelCount(8, false)
        rightAxis?.valueFormatter = custom
        rightAxis?.spaceTop = 15f
        rightAxis?.axisMinimum = 0f // this replaces setStartAtZero(true)

        val l = chart?.legend
        l?.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l?.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l?.orientation = Legend.LegendOrientation.HORIZONTAL
        l?.setDrawInside(false)
        l?.form = LegendForm.SQUARE
        l?.formSize = 9f
        l?.textSize = 11f
        l?.xEntrySpace = 4f

        val mv = XYMarkerView(this, xAxisFormatter)
        mv.chartView = chart // For bounds control
        chart?.setMarker(mv) // Set the marker to the chart

        // setting data
        seekBarY?.progress = 50
        seekBarX?.progress = 12

        // chart.setDrawLegend(false);
    }

    private fun setData(count: Int, range: Float) {
        val start = 1f

        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(100)

        var i = start.toInt()
        while (i < start + count) {
            val `val` = (sampleValues[i].toFloat() * (range + 1))

            if (`val` * 100 < 25) {
                values.add(BarEntry(i.toFloat(), `val`, ResourcesCompat.getDrawable(resources, R.drawable.star, theme)))
            } else {
                values.add(BarEntry(i.toFloat(), `val`))
            }
            i++
        }

        val set1: BarDataSet?

        if (chart?.data != null &&
            chart?.data?.let { it.dataSetCount > 0 } == true
        ) {
            set1 = chart?.data?.getDataSetByIndex(0) as BarDataSet
            set1.entries = values
            chart?.data?.notifyDataChanged()
            chart?.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "The year 2017")

            set1.isDrawIconsEnabled = false

            val startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light)
            val startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light)
            val startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light)
            val startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light)
            val endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            val endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple)
            val endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark)
            val endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark)
            val endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark)

            val gradientFills: MutableList<Fill> = ArrayList()
            gradientFills.add(Fill(startColor1, endColor1))
            gradientFills.add(Fill(startColor2, endColor2))
            gradientFills.add(Fill(startColor3, endColor3))
            gradientFills.add(Fill(startColor4, endColor4))
            gradientFills.add(Fill(startColor5, endColor5))

            set1.setFills(gradientFills)

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTypeface(tfLight)
            data.barWidth = 0.9f

            chart?.setData(data)
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
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/BarChartActivity.java".toUri())
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                chart?.data?.dataSets?.let {
                    for (set in it) set.isDrawValuesEnabled = !set.isDrawValuesEnabled
                }

                chart?.invalidate()
            }

            R.id.actionToggleIcons -> {
                chart?.data?.dataSets?.let {
                    for (set in it) set.isDrawIconsEnabled = !set.isDrawIconsEnabled
                }

                chart?.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart?.data != null) {
                    chart?.data?.isHighlightEnabled = chart?.data?.isHighlightEnabled != true
                    chart?.invalidate()
                }
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

            R.id.animateX -> {
                chart?.animateX(2000)
            }

            R.id.animateY -> {
                chart?.animateY(2000)
            }

            R.id.animateXY -> {
                chart?.animateXY(2000, 2000)
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(chart)
                }
            }

            R.id.actionRotateXAxisLabelsBy45Deg -> {
                chart?.xAxis?.labelRotationAngle = 45f
                chart?.notifyDataSetChanged()
                chart?.invalidate()
            }
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX?.text = seekBarX?.progress.toString()
        tvY?.text = seekBarY?.progress.toString()

        setData(seekBarX?.progress ?: 0, seekBarY?.progress?.toFloat() ?: 0f)
        chart?.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(chart, "BarChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    private val onValueSelectedRectF = RectF()

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null) return

        val bounds = onValueSelectedRectF
        chart?.getBarBounds(e as BarEntry, bounds)
        val position = chart?.getPosition(e, AxisDependency.LEFT)

        Log.i("bounds", bounds.toString())
        Log.i("position", position.toString())

        Log.i(
            "x-index",
            ("low: " + chart?.lowestVisibleX + ", high: "
                    + chart?.highestVisibleX)
        )

        recycleInstance(position)
    }

    override fun onNothingSelected() {}
}
