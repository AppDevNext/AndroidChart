package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.EntryXComparator
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber
import java.util.Collections

class InvertedLineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: LineChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linechart)

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)

        seekBarY!!.setOnSeekBarChangeListener(this)
        seekBarX!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setOnChartValueSelectedListener(this)
        chart!!.setDrawGridBackground(false)

        // no description text
        chart!!.description.isEnabled = false

        // enable touch gestures
        chart!!.setTouchEnabled(true)

        // enable scaling and dragging
        chart!!.setDragEnabled(true)
        chart!!.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart!!.setPinchZoom(true)

        // set an alternative background color
        // chart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv = MyMarkerView(this, R.layout.custom_marker_view)
        mv.chartView = chart // For bounds control
        chart!!.setMarker(mv) // Set the marker to the chart

        val xl = chart!!.xAxis
        xl.setAvoidFirstLastClipping(true)
        xl.axisMinimum = 0f

        val leftAxis = chart!!.axisLeft
        leftAxis.isInverted = true
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = chart!!.axisRight
        rightAxis.isEnabled = false

        // add data
        seekBarX!!.progress = 25
        seekBarY!!.progress = 50

        // // restrain the maximum scale-out factor
        // chart.setScaleMinima(3f, 3f);
        //
        // // center the view to a specific position inside the chart
        // chart.centerViewPort(10, 50);

        // get the legend (only possible after setting data)
        val l = chart!!.legend

        // modify the legend ...
        l.form = LegendForm.LINE

        // don't forget to refresh the drawing
        chart?.invalidate()
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
        chart!!.setData(data)
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
                chart!!.data!!.dataSets.forEach {
                    it?.isDrawValues = !it.isDrawValues
                }
                chart?.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled()
                    chart?.invalidate()
                }
            }

            R.id.actionToggleFilled -> {
                chart!!.data!!.dataSets.forEach { set ->
                    set?.setDrawFilled(!set.isDrawFilledEnabled)
                }
                chart?.invalidate()
            }

            R.id.actionToggleCircles -> {
                val sets = chart!!.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.isDrawCirclesEnabled = !set.isDrawCirclesEnabled
                }
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

            R.id.actionTogglePinch -> {
                chart!!.setPinchZoom(!chart!!.isPinchZoomEnabled)

                chart?.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart?.notifyDataSetChanged()
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

        // redraw
        chart?.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(chart, "InvertedLineChartActivity")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Value: ${entry.y}, xIndex: ${entry.x}, DataSet index: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
