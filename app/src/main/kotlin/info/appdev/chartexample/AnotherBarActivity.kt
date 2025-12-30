package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityBarchartBinding
import info.appdev.chartexample.notimportant.DemoBase

class AnotherBarActivity : DemoBase(), OnSeekBarChangeListener {

    private lateinit var binding: ActivityBarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.chart1.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be drawn
        binding.chart1.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.setDrawBarShadow(false)
        binding.chart1.setDrawGridBackground(false)

        val xAxis = binding.chart1.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        binding.chart1.axisLeft.setDrawGridLines(false)

        // setting data
        binding.seekBarX.progress = DEFAULT_VALUE
        binding.seekBarY.progress = 100

        // add a nice and smooth animation
        binding.chart1.animateY(1500)

        binding.chart1.legend.isEnabled = false
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(100)

        for (i in 0..<binding.seekBarX.progress) {
            val multi = (binding.seekBarY.progress + 1).toFloat()
            val `val` = (sampleValues[i]!!.toFloat() * multi) + multi / 3
            values.add(BarEntry(i.toFloat(), `val`))
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
            set1 = BarDataSet(values, "Data Set")
            set1.setColors(*ColorTemplate.VORDIPLOM_COLORS)
            set1.isDrawValues = false

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            binding.chart1.data = data
            binding.chart1.setFitBars(true)
        }

        binding.chart1.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bar, menu)
        menu.removeItem(R.id.actionToggleIcons)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/AnotherBarActivity.kt".toUri()
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
                else
                    binding.chart1.setPinchZoom(true)

                binding.chart1.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                binding.chart1.isAutoScaleMinMaxEnabled = !binding.chart1.isAutoScaleMinMaxEnabled
                binding.chart1.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                binding.chart1.barData?.dataSets?.forEach { set ->
                    (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f)
                        0f
                    else
                        1f
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

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "AnotherBarActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    companion object {
        private const val DEFAULT_VALUE = 10
    }
}
