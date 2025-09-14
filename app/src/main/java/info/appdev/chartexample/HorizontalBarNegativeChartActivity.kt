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
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF.Companion.recycleInstance
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

class HorizontalBarNegativeChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: HorizontalBarChart? = null
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
        setContentView(R.layout.activity_horizontalbarchart)

        setTitle("HorizontalBarChartActivity")

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)

        seekBarY!!.setOnSeekBarChangeListener(this)
        seekBarX!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setOnChartValueSelectedListener(this)

        // chart.setHighlightEnabled(false);
        chart!!.isDrawBarShadowEnabled = false

        chart!!.isDrawValueAboveBarEnabled = true

        chart!!.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart!!.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);
        chart!!.drawGridBackground = false

        val xl = chart!!.xAxis
        xl.position = XAxisPosition.BOTTOM
        xl.typeface = tfLight
        xl.setDrawAxisLine(true)
        xl.setDrawGridLines(false)
        xl.granularity = 10f

        val yl = chart!!.axisLeft
        yl.typeface = tfLight
        yl.setDrawAxisLine(true)
        yl.setDrawGridLines(true)

        //        yl.setInverted(true);
        val yr = chart!!.axisRight
        yr.typeface = tfLight
        yr.setDrawAxisLine(true)
        yr.setDrawGridLines(false)

        //        yr.setInverted(true);
        chart!!.setFitBars(true)
        chart!!.animateY(2500)

        // setting data
        seekBarY!!.progress = 50
        seekBarX!!.progress = 12

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.formSize = 8f
        l.xEntrySpace = 4f
    }

    private fun setData(count: Int, range: Float) {
        val barWidth = 9f
        val spaceForBar = 10f
        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(count + 2)

        for (i in 0..<count) {
            val `val` = sampleValues[i].toFloat() * range - range / 2
            values.add(
                BarEntry(
                    i * spaceForBar, `val`,
                    ResourcesCompat.getDrawable(resources, R.drawable.star, theme)
                )
            )
        }

        val set1: BarDataSet?

        if (chart!!.data != null &&
            chart!!.data!!.dataSetCount > 0
        ) {
            set1 = chart!!.data!!.getDataSetByIndex(0) as BarDataSet
            set1.entries = values
            chart!!.data!!.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "DataSet 1")

            set1.isDrawIconsEnabled = false

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTypeface(tfLight)
            data.barWidth = barWidth
            chart!!.setData(data)
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
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/HorizontalBarChartActivity.java".toUri())
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    iSet.isDrawValuesEnabled = !iSet.isDrawValuesEnabled
                }

                chart!!.invalidate()
            }

            R.id.actionToggleIcons -> {
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    iSet.isDrawIconsEnabled = !iSet.isDrawIconsEnabled
                }

                chart!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled
                    chart!!.invalidate()
                }
            }

            R.id.actionTogglePinch -> {
                chart!!.setPinchZoom(!chart!!.isPinchZoomEnabled)

                chart!!.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                for (set in chart!!.data!!.dataSets) (set as BarDataSet).setBarBorderWidth(if (set.barBorderWidth == 1f) 0f else 1f)

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
        tvY!!.text = seekBarY!!.progress.toString()

        setData(seekBarX!!.progress, seekBarY!!.progress.toFloat())
        chart!!.setFitBars(true)
        chart!!.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(chart, "HorizontalBarChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    private val mOnValueSelectedRectF = RectF()

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null || h == null) return

        val bounds = mOnValueSelectedRectF
        chart!!.getBarBounds(e as BarEntry, bounds)

        val position = chart!!.getPosition(e, chart!!.data!!.getDataSetByIndex(h.dataSetIndex).axisDependency)

        Log.i("bounds", bounds.toString())
        Log.i("position", position.toString())

        recycleInstance(position)
    }

    override fun onNothingSelected() {}
}
