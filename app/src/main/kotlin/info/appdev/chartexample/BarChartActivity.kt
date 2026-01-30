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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.XYMarkerView
import info.appdev.chartexample.databinding.ActivityBarchartBinding
import info.appdev.chartexample.formatter.DayAxisValueFormatter
import info.appdev.chartexample.formatter.MyAxisValueFormatter
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Legend
import info.appdev.charting.components.Legend.LegendForm
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.components.YAxis.YAxisLabelPosition
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.BaseEntry
import info.appdev.charting.formatter.IAxisValueFormatter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.Fill
import info.appdev.charting.utils.PointF
import timber.log.Timber

class BarChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityBarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.seekBarX.setOnSeekBarChangeListener(this)

        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setRoundedBarRadius(50f)

        binding.chart1.setDrawMarkerViews(true)
        binding.chart1.isDrawBarShadow = false
        binding.chart1.isDrawValueAboveBar = true

        binding.chart1.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        binding.chart1.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.isPinchZoom = false

        binding.chart1.setDrawGridBackground(false)

        // chart.setDrawYLabels(false);
        val xAxisFormatter: IAxisValueFormatter = DayAxisValueFormatter(binding.chart1)

        binding.chart1.xAxis.apply {
            position = XAxisPosition.BOTTOM
            typeface = tfLight
            isDrawGridLines = false
            granularity = 1f // only intervals of 1 day
            labelCount = 7
            valueFormatter = xAxisFormatter
        }

        val custom: IAxisValueFormatter = MyAxisValueFormatter()

        binding.chart1.axisLeft.apply {
            typeface = tfLight
            setLabelCount(8, false)
            valueFormatter = custom
            spaceTop = 15f
            axisMinimum = 0f// this replaces setStartAtZero(true)
            setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        }

        binding.chart1.axisRight.apply {
            isDrawGridLines = false
            typeface = tfLight
            setLabelCount(8, false)
            valueFormatter = custom
            spaceTop = 15f
            axisMinimum = 0f// this replaces setStartAtZero(true)
        }

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

        val mv = XYMarkerView(this, xAxisFormatter)
        mv.chartView = binding.chart1 // For bounds control
        binding.chart1.setMarker(mv) // Set the marker to the chart

        // setting data
        binding.seekBarY.progress = 50
        binding.seekBarX.progress = 12

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

        if (binding.chart1.barData != null &&
            binding.chart1.barData!!.dataSetCount > 0
        ) {
            set1 = binding.chart1.barData!!.getDataSetByIndex(0) as BarDataSet
            set1.entries = values
            binding.chart1.barData?.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
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
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/BarChartActivity.kt".toUri()
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
                binding.chart1.barData?.let {
                    it.isHighlight = !it.isHighlight
                }
                binding.chart1.invalidate()
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

            R.id.actionRotateXAxisLabelsBy45Deg -> {
                binding.chart1.xAxis.labelRotationAngle = 45f
                binding.chart1.notifyDataSetChanged()
                binding.chart1.invalidate()
            }
        }
        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        setData(binding.seekBarX.progress, binding.seekBarY.progress.toFloat())
        binding.chart1.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "BarChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    private val onValueSelectedRectF = RectF()

    override fun onValueSelected(entry: BaseEntry<Float>, highlight: Highlight) {
        val bounds = onValueSelectedRectF
        binding.chart1.getBarBounds(entry as BarEntry, bounds)
        val position = binding.chart1.getPosition(entry, AxisDependency.LEFT)

        Timber.i("bounds $bounds")
        Timber.i("position = $position")
        Timber.i("x-index low: ${+binding.chart1.lowestVisibleX}, high: ${+binding.chart1.highestVisibleX}")

        PointF.recycleInstance(position)
    }

    override fun onNothingSelected() = Unit
}
