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
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityBubblechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.data.BubbleData
import info.appdev.charting.data.BubbleDataSet
import info.appdev.charting.data.BubbleEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IBubbleDataSet
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.PointF
import timber.log.Timber

class BubbleChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityBubblechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBubblechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.chart1.description.isEnabled = false
        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setDrawGridBackground(false)
        binding.chart1.setTouchEnabled(true)

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)

        binding.chart1.setMaxVisibleValueCount(200)
        binding.chart1.isPinchZoom = true

        binding.seekBarX.progress = 10
        binding.seekBarY.progress = 50

        binding.chart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            typeface = tfLight
        }

        val yl = binding.chart1.axisLeft
        yl.typeface = tfLight
        yl.spaceTop = 30f
        yl.spaceBottom = 30f
        yl.isDrawZeroLine = false

        binding.chart1.axisRight.isEnabled = false

        val xl = binding.chart1.xAxis
        xl.position = XAxis.XAxisPosition.BOTTOM
        xl.typeface = tfLight
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val count = binding.seekBarX.progress
        val range = binding.seekBarY.progress

        binding.tvXMax.text = count.toString()
        binding.tvYMax.text = range.toString()

        val values1 = ArrayList<BubbleEntry>()
        val values2 = ArrayList<BubbleEntry>()
        val values3 = ArrayList<BubbleEntry>()
        val sampleValues = getValues(100)

        for (i in 0..<count) {
            values1.add(
                BubbleEntry(
                    i.toFloat(),
                    (sampleValues[i + 1]!! * range).toFloat(),
                    (sampleValues[i]!!.toFloat() * range),
                    ResourcesCompat.getDrawable(resources, R.drawable.star, null)
                )
            )
            values2.add(
                BubbleEntry(
                    i.toFloat(),
                    (sampleValues[i + 2]!! * range).toFloat(),
                    (sampleValues[i + 1]!!.toFloat() * range),
                    ResourcesCompat.getDrawable(resources, R.drawable.star, null)
                )
            )
            values3.add(BubbleEntry(i.toFloat(), (sampleValues[i]!! * range).toFloat(), (sampleValues[i + 2]!!.toFloat() * range)))
        }

        // create a dataset and give it a type
        val set1 = BubbleDataSet(values1, "DS 1")
        set1.isDrawIcons = false
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0], 130)
        set1.isDrawValues = true

        val set2 = BubbleDataSet(values2, "DS 2")
        set2.isDrawIcons = false
        set2.iconsOffset = PointF(0f, 15f)
        set2.setColor(ColorTemplate.COLORFUL_COLORS[1], 130)
        set2.isDrawValues = true

        val set3 = BubbleDataSet(values3, "DS 3")
        set3.setColor(ColorTemplate.COLORFUL_COLORS[2], 130)
        set3.isDrawValues = true

        val dataSets = ArrayList<IBubbleDataSet>()
        dataSets.add(set1) // add the data sets
        dataSets.add(set2)
        dataSets.add(set3)

        // create a data object with the data sets
        val data = BubbleData(dataSets)
        data.setDrawValues(false)
        data.setValueTypeface(tfLight)
        data.setValueTextSize(8f)
        data.setValueTextColor(Color.WHITE)
        data.setHighlightCircleWidth(1.5f)

        binding.chart1.data = data
        binding.chart1.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bubble, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/BubbleChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.bubbleData?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleIcons -> {
                binding.chart1.bubbleData?.dataSets?.forEach { set ->
                    set.isDrawIcons = !set.isDrawIcons
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.bubbleData?.let {
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
        saveToGallery(binding.chart1, "BubbleChartActivity")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Value: ${entry.y}, xIndex: ${entry.x}, DataSet index: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
