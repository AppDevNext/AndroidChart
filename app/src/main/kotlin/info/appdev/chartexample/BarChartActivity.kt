package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.github.mikephil.charting.utils.MPPointF
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.XYMarkerView
import info.appdev.chartexample.formatter.DayAxisValueFormatter
import info.appdev.chartexample.formatter.MyAxisValueFormatter
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class BarChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: BarChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barchart)

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)

        seekBarY!!.setOnSeekBarChangeListener(this)
        seekBarX!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setOnChartValueSelectedListener(this)
        chart!!.setRoundedBarRadius(50f)

        chart!!.setDrawBarShadow(false)
        chart!!.setDrawValueAboveBar(true)

        chart!!.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart!!.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        chart!!.setDrawGridBackground(false)

        // chart.setDrawYLabels(false);
        val xAxisFormatter: IAxisValueFormatter = DayAxisValueFormatter(chart!!)

        val xAxis = chart!!.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.typeface = tfLight
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // only intervals of 1 day
        xAxis.labelCount = 7
        xAxis.valueFormatter = xAxisFormatter

        val custom: IAxisValueFormatter = MyAxisValueFormatter()

        val leftAxis = chart!!.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.setLabelCount(8, false)
        leftAxis.valueFormatter = custom
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f// this replaces setStartAtZero(true)

        val rightAxis = chart!!.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.typeface = tfLight
        rightAxis.setLabelCount(8, false)
        rightAxis.valueFormatter = custom
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f// this replaces setStartAtZero(true)

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f

        val mv = XYMarkerView(this, xAxisFormatter)
        mv.chartView = chart // For bounds control
        chart?.setMarker(mv) // Set the marker to the chart

        // setting data
        seekBarY!!.progress = 50
        seekBarX!!.progress = 12

        // chart.setDrawLegend(false);
    }

    private fun setData(count: Int, range: Float) {
        val start = 1f

        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(100)

        var i = start.toInt()
        while (i < start + count) {
            val `val` = (sampleValues[i]!!.toFloat() * (range + 1))

            if (`val` * 100 < 25) {
                values.add(BarEntry(i.toFloat(), `val`, ResourcesCompat.getDrawable(resources, R.drawable.star, null)))
            } else {
                values.add(BarEntry(i.toFloat(), `val`))
            }
            i++
        }

        val set1: BarDataSet

        if (chart!!.data != null &&
            chart!!.data!!.getDataSetCount() > 0
        ) {
            set1 = chart!!.data!!.getDataSetByIndex(0) as BarDataSet
            set1.entries  = values
            chart!!.data!!.notifyDataChanged()
            chart?.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "The year 2017")

            set1.isDrawIcons = false

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

            set1.fills = gradientFills

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
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/BarChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                chart!!.data!!.dataSets.forEach {
                    it?.isDrawValues = !it.isDrawValues
                }

                chart?.invalidate()
            }

            R.id.actionToggleIcons -> {
                for (set in chart!!.data!!.dataSets)
                    set?.isDrawIcons = !set.isDrawIcons

                chart?.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled()
                    chart?.invalidate()
                }
            }

            R.id.actionTogglePinch -> {
                if (chart!!.isPinchZoomEnabled) chart!!.setPinchZoom(false)
                else chart!!.setPinchZoom(true)

                chart?.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart?.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                for (set in chart!!.data!!.dataSets)
                    (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f)
                        0f
                    else
                        1f

                chart?.invalidate()
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

            R.id.actionRotateXAxisLabelsBy45Deg -> {
                chart!!.xAxis.labelRotationAngle = 45f
                chart?.notifyDataSetChanged()
                chart?.invalidate()
            }
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX!!.text = seekBarX!!.progress.toString()
        tvY!!.text = seekBarY!!.progress.toString()

        setData(seekBarX!!.progress, seekBarY!!.progress.toFloat())
        chart?.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(chart, "BarChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    private val onValueSelectedRectF = RectF()

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        val bounds = onValueSelectedRectF
        chart!!.getBarBounds(entry as BarEntry, bounds)
        val position = chart!!.getPosition(entry, AxisDependency.LEFT)

        Timber.i("bounds $bounds")
        Timber.i("position = $position")
        Timber.i("x-index low: ${+chart!!.lowestVisibleX}, high: ${+chart!!.highestVisibleX}")

        MPPointF.recycleInstance(position)
    }

    override fun onNothingSelected() = Unit
}
