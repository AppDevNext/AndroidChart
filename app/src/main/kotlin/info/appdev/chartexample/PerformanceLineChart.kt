package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.net.toUri
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.chartexample.DataTools.Companion.getMuchValues
import info.appdev.chartexample.databinding.ActivityPerformanceLinechartBinding
import info.appdev.chartexample.notimportant.DemoBase

class PerformanceLineChart : DemoBase(), OnSeekBarChangeListener {

    private lateinit var binding: ActivityPerformanceLinechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerformanceLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekbarValues.setOnSeekBarChangeListener(this)

        binding.chart1.setDrawGridBackground(false)

        // no description text
        binding.chart1.description.isEnabled = false

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.axisLeft.setDrawGridLines(false)
        binding.chart1.axisRight.isEnabled = false
        binding.chart1.xAxis.setDrawGridLines(true)
        binding.chart1.xAxis.setDrawAxisLine(false)

        binding.seekbarValues.progress = 9000

        // don't forget to refresh the drawing
        binding.chart1.invalidate()
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
        set1.isDrawCirclesEnabled = false
        set1.lineMode = LineDataSet.Mode.LINEAR
        set1.isDrawFilledEnabled = false

        // create a data object with the data sets
        val data = LineData(set1)

        // set data
        binding.chart1.data = data

        // get the legend (only possible after setting data)
        binding.chart1.legend.apply {
            isEnabled = false
        }
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
        val count = binding.seekbarValues.progress + 1000
        binding.tvValueCount.text = count.toString()

        binding.chart1.resetTracking()

        setData(count, 500f)

        // redraw
        binding.chart1.invalidate()
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
