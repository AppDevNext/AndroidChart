package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate.holoBlue
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

class RealtimeLineChartActivity : DemoBase(), OnChartValueSelectedListener {
    private var chart: LineChart? = null
    var sampleValues: Array<Double> = getValues(102)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_realtime_linechart)

        setTitle("RealtimeLineChartActivity")

        chart = findViewById(R.id.chart1)
        chart!!.setOnChartValueSelectedListener(this)

        // enable description text
        chart!!.description.isEnabled = true

        // enable touch gestures
        chart!!.setTouchEnabled(true)

        // enable scaling and dragging
        chart!!.isDragEnabled = true
        chart!!.setScaleEnabled(true)
        chart!!.drawGridBackground = false

        // if disabled, scaling can be done on x- and y-axis separately
        chart!!.setPinchZoom(true)

        // set an alternative background color
        chart!!.setBackgroundColor(Color.LTGRAY)

        val data = LineData()
        data.setValueTextColor(Color.WHITE)

        // add empty data
        chart!!.setData(data)

        // get the legend (only possible after setting data)
        val l = chart!!.legend

        // modify the legend ...
        l.form = LegendForm.LINE
        l.typeface = tfLight
        l.textColor = Color.WHITE

        val xl = chart!!.xAxis
        xl.typeface = tfLight
        xl.textColor = Color.WHITE
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true

        val leftAxis = chart!!.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.textColor = Color.WHITE
        leftAxis.axisMaximum = 100f
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(true)

        val rightAxis = chart!!.axisRight
        rightAxis.isEnabled = false
    }

    private fun addEntry() {
        val data: LineData? = chart!!.data

        if (data != null) {
            var set: ILineDataSet

            // set.addEntry(...); // can be called as well
            if (data.dataSetCount == 0) {
                set = createSet()
                data.addDataSet(set)
            } else {
                set = data.getDataSetByIndex(0)
            }

            val cycleValue = (set.entryCount % 100.0)
            data.addEntry(Entry(set.entryCount.toFloat(), (sampleValues[cycleValue.toInt()].toFloat() * 40) + 30f), 0)
            data.notifyDataChanged()

            // let the chart know it's data has changed
            chart!!.notifyDataSetChanged()

            // limit the number of visible entries
            chart!!.setVisibleXRangeMaximum(120f)

            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart!!.moveViewToX(data.entryCount.toFloat())

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(mutableListOf(), "Dynamic Data")
        set.axisDependency = AxisDependency.LEFT
        set.setColor(holoBlue)
        set.setCircleColor(Color.WHITE)
        set.lineWidth = 2f
        set.circleRadius = 4f
        set.fillAlpha = 65
        set.fillColor = holoBlue
        set.highLightColor = Color.rgb(244, 117, 117)
        set.valueTextColor = Color.WHITE
        set.valueTextSize = 9f
        set.isDrawValuesEnabled = false
        return set
    }

    private var thread: Thread? = null

    private fun feedMultiple() {
        if (thread != null) thread!!.interrupt()

        val runnable = Runnable { addEntry() }

        thread = Thread {
            for (i in 0..999) {
                // Don't generate garbage runnables inside the loop.

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
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/RealtimeLineChartActivity.java".toUri())
                startActivity(i)
            }

            R.id.actionAdd -> {
                addEntry()
            }

            R.id.actionClear -> {
                chart!!.clearValues()
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show()
            }

            R.id.actionFeedMultiple -> {
                feedMultiple()
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

    override fun saveToGallery() {
        saveToGallery(chart, "RealtimeLineChartActivity")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i("Entry selected", e.toString())
    }

    override fun onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.")
    }

    override fun onPause() {
        super.onPause()

        if (thread != null) {
            thread!!.interrupt()
        }
    }
}
