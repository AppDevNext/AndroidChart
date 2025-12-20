package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import info.appdev.chartexample.DataTools.Companion.getMuchValues
import info.appdev.chartexample.notimportant.DemoBase
import androidx.core.net.toUri

class PerformanceLineChart : DemoBase(), OnSeekBarChangeListener {
    private var chart: LineChart? = null
    private var seekBarValues: SeekBar? = null
    private var tvCount: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performance_linechart)

        tvCount = findViewById(R.id.tvValueCount)
        seekBarValues = findViewById(R.id.seekbarValues)
        seekBarValues!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setDrawGridBackground(false)

        // no description text
        chart!!.description.isEnabled = false

        // enable touch gestures
        chart!!.setTouchEnabled(true)

        // enable scaling and dragging
        chart!!.setDragEnabled(true)
        chart!!.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        chart!!.axisLeft.setDrawGridLines(false)
        chart!!.axisRight.isEnabled = false
        chart!!.xAxis.setDrawGridLines(true)
        chart!!.xAxis.setDrawAxisLine(false)

        seekBarValues!!.progress = 9000

        // don't forget to refresh the drawing
        chart!!.invalidate()
    }

    private fun setData(count: Int, range: Float) {
        val values = ArrayList<Entry>()
        val sampleValues = getMuchValues(count)

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * (range + 1)) + 3
            values.add(Entry(i * 0.001f, `val`))
        }

        // create a dataset and give it a type
        val set1 = LineDataSet(values, "DataSet 1")

        set1.color = Color.BLACK
        set1.lineWidth = 0.5f
        set1.isDrawValues = false
        set1.setDrawCircles(false)
        set1.lineMode = LineDataSet.Mode.LINEAR
        set1.setDrawFilled(false)

        // create a data object with the data sets
        val data = LineData(set1)

        // set data
        chart!!.setData(data)

        // get the legend (only possible after setting data)
        val l = chart!!.legend
        l.isEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_github, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/PerformanceLineChart.java".toUri()
                startActivity(i)
            }
        }

        return true
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val count = seekBarValues!!.progress + 1000
        tvCount!!.text = count.toString()

        chart!!.resetTracking()

        setData(count, 500f)

        // redraw
        chart!!.invalidate()
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
