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
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.databinding.ActivityLinechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Legend.LegendForm
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.EntryXComparator
import timber.log.Timber
import java.util.Collections

class InvertedLineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityLinechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.seekBarX.setOnSeekBarChangeListener(this)

        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setDrawGridBackground(false)

        // no description text
        binding.chart1.description.isEnabled = false

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.setPinchZoom(true)

        // set an alternative background color
        // chart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv = MyMarkerView(this, R.layout.custom_marker_view)
        mv.chartView = binding.chart1 // For bounds control
        binding.chart1.setMarker(mv) // Set the marker to the chart

        val xl = binding.chart1.xAxis
        xl.setAvoidFirstLastClipping(true)
        xl.axisMinimum = 0f

        val leftAxis = binding.chart1.axisLeft
        leftAxis.isInverted = true
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = binding.chart1.axisRight
        rightAxis.isEnabled = false

        // add data
        binding.seekBarX.progress = 25
        binding.seekBarY.progress = 50

        // // restrain the maximum scale-out factor
        // chart.setScaleMinima(3f, 3f);
        //
        // // center the view to a specific position inside the chart
        // chart.centerViewPort(10, 50);

        // get the legend (only possible after setting data)
        binding.chart1.legend.apply {
            form = LegendForm.LINE
        }

        // don't forget to refresh the drawing
        binding.chart1.invalidate()
    }

    private fun setData(count: Int, range: Float) {
        val entries = ArrayList<Entry>()
        val sampleValues = getValues(count + 2)

        for (i in 0..<count) {
            val xVal = sampleValues[i]!!.toFloat() * range
            val yVal = sampleValues[i + 1]!!.toFloat() * range
            entries.add(Entry(xVal, yVal))
        }

        // sort by x-value
        Collections.sort(entries, EntryXComparator())

        // create a dataset and give it a type
        val set1 = LineDataSet(entries, "DataSet 1")

        set1.lineWidth = 1.5f
        set1.circleRadius = 4f

        // create a data object with the data sets
        val data = LineData(set1)

        // set data
        binding.chart1.data = data
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.line, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/InvertedLineChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.lineData.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.data?.let {
                    it.isHighlightEnabled = !it.isHighlightEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleFilled -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawFilledEnabled = !set.isDrawFilledEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCircles -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawCirclesEnabled = !set.isDrawCirclesEnabled
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

            R.id.actionTogglePinch -> {
                binding.chart1.setPinchZoom(!binding.chart1.isPinchZoomEnabled)

                binding.chart1.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                binding.chart1.isAutoScaleMinMaxEnabled = !binding.chart1.isAutoScaleMinMaxEnabled
                binding.chart1.notifyDataSetChanged()
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

        // redraw
        binding.chart1.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "InvertedLineChartActivity")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Value: ${entry.y}, xIndex: ${entry.x}, DataSet index: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
