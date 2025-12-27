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
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.PointF
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityHorizontalbarchartBinding
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class HorizontalBarNegativeChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityHorizontalbarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHorizontalbarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.seekBarX.setOnSeekBarChangeListener(this)

        binding.chart1.setOnChartValueSelectedListener(this)

        // chart.setHighlightEnabled(false);
        binding.chart1.setDrawBarShadow(false)
        binding.chart1.setDrawValueAboveBar(true)

        binding.chart1.description?.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        binding.chart1.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);
        binding.chart1.setDrawGridBackground(false)

        val xl = binding.chart1.xAxis
        xl.position = XAxisPosition.BOTTOM
        xl.typeface = tfLight
        xl.setDrawAxisLine(true)
        xl.setDrawGridLines(false)
        xl.granularity = 10f

        val yl = binding.chart1.axisLeft
        yl.typeface = tfLight
        yl.setDrawAxisLine(true)
        yl.setDrawGridLines(true)

        //        yl.setInverted(true);
        val yr = binding.chart1.axisRight
        yr.typeface = tfLight
        yr.setDrawAxisLine(true)
        yr.setDrawGridLines(false)

        //        yr.setInverted(true);
        binding.chart1.setFitBars(true)
        binding.chart1.animateY(2500)

        // setting data
        binding.seekBarY.progress = 50
        binding.seekBarX.progress = 12

        binding.chart1.legend?.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            formSize = 8f
            xEntrySpace = 4f
        }
    }

    private fun setData(count: Int, range: Float) {
        val barWidth = 9f
        val spaceForBar = 10f
        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(count + 2)

        for (i in 0..<count) {
            val valueY = sampleValues[i]!!.toFloat() * range - range / 2
            values.add(
                BarEntry(
                    i * spaceForBar, valueY,
                    ResourcesCompat.getDrawable(resources, R.drawable.star, null)
                )
            )
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
            set1 = BarDataSet(values, "DataSet 1")

            set1.isDrawIcons = false

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTypeface(tfLight)
            data.barWidth = barWidth
            binding.chart1.setData(data)
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
                binding.chart1.barData?.let {
                    it.isHighlightEnabled = !it.isHighlightEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionTogglePinch -> {
                binding.chart1.setPinchZoom(!binding.chart1.isPinchZoomEnabled)

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
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        setData(binding.seekBarX.progress, binding.seekBarY.progress.toFloat())
        binding.chart1.setFitBars(true)
        binding.chart1.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "HorizontalBarChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    private val mOnValueSelectedRectF = RectF()

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        val bounds = mOnValueSelectedRectF
        binding.chart1.getBarBounds(entry as BarEntry, bounds)

        val position = binding.chart1.getPosition(
            entry, binding.chart1.barData!!.getDataSetByIndex(highlight.dataSetIndex)?.axisDependency
        )

        Timber.i("bounds $bounds")
        Timber.i("position $position")

        PointF.recycleInstance(position)
    }

    override fun onNothingSelected() {}
}
