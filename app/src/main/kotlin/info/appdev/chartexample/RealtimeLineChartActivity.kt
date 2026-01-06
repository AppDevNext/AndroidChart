package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityRealtimeLinechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Legend.LegendForm
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.ColorTemplate
import timber.log.Timber

class RealtimeLineChartActivity : DemoBase(), OnChartValueSelectedListener {
    var sampleValues: Array<Double?> = getValues(102)

    private lateinit var binding: ActivityRealtimeLinechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealtimeLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.description.isEnabled = true

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)
        binding.chart1.setDrawGridBackground(false)

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.isPinchZoom = true

        // set an alternative background color
        binding.chart1.setBackgroundColor(Color.LTGRAY)

        val data = LineData()
        data.setValueTextColor(Color.WHITE)

        // add empty data
        binding.chart1.data = data

        // get the legend (only possible after setting data)
        binding.chart1.legend.apply {
            form = LegendForm.LINE
            typeface = tfLight
            textColor = Color.WHITE
        }

        val xl = binding.chart1.xAxis
        xl.typeface = tfLight
        xl.textColor = Color.WHITE
        xl.isDrawGridLines = false
        xl.isAvoidFirstLastClipping = true
        xl.isEnabled = true

        val leftAxis = binding.chart1.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.textColor = Color.WHITE
        leftAxis.axisMaximum = 100f
        leftAxis.axisMinimum = 0f
        leftAxis.isDrawGridLines = true

        val rightAxis = binding.chart1.axisRight
        rightAxis.isEnabled = false
    }

    private fun addEntry() {
        val data = binding.chart1.lineData

        var set = data.getDataSetByIndex(0)

        // set.addEntry(...); // can be called as well
        if (set == null) {
            set = createSet()
            data.addDataSet(set)
        }

        val cycleValue = (set.entryCount % 100.0).toInt()
        data.addEntry(Entry(set.entryCount.toFloat(), (sampleValues[cycleValue]!!.toFloat() * 40) + 30f), 0)
        data.notifyDataChanged()

        // let the chart know it's data has changed
        binding.chart1.notifyDataSetChanged()

        // limit the number of visible entries
        binding.chart1.setVisibleXRangeMaximum(120f)

        // chart.setVisibleYRange(30, AxisDependency.LEFT);

        // move to the latest entry
        binding.chart1.moveViewToX(data.dataSetCount.toFloat())

        // this automatically refreshes the chart (calls invalidate())
        // chart.moveViewTo(data.getXValCount()-7, 55f,
        // AxisDependency.LEFT);
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(label = "Dynamic Data")
        set.axisDependency = AxisDependency.LEFT
        set.color = ColorTemplate.holoBlue
        set.setCircleColor(Color.WHITE)
        set.lineWidth = 2f
        set.circleRadius = 4f
        set.fillAlpha = 65
        set.fillColor = ColorTemplate.holoBlue
        set.highLightColor = Color.rgb(244, 117, 117)
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 9f
        set.isDrawValues = false
        return set
    }

    private var thread: Thread? = null

    private fun feedMultiple() {
        if (thread != null) thread!!.interrupt()

        val runnable = Runnable { addEntry() }

        thread = Thread {
            repeat((0..999).count()) {
                // Don't generate garbage runnable inside the loop.

                runOnUiThread(runnable)

                try {
                    Thread.sleep(25)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        thread!!.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.realtime, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/RealtimeLineChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionAdd -> {
                addEntry()
            }

            R.id.actionClear -> {
                binding.chart1.clearValues()
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show()
            }

            R.id.actionFeedMultiple -> {
                feedMultiple()
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
        saveToGallery(binding.chart1, "RealtimeLineChartActivity")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i(entry.toString())
    }

    override fun onNothingSelected() = Unit

    override fun onPause() {
        super.onPause()

        if (thread != null) {
            thread!!.interrupt()
        }
    }
}
