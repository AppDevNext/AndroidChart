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
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityLinechartTimeBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.AxisBase
import info.appdev.charting.components.XAxis
import info.appdev.charting.components.YAxis
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.formatter.IAxisValueFormatter
import info.appdev.charting.utils.ColorTemplate.holoBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class LineChartTimeActivity : DemoBase(), OnSeekBarChangeListener {

    private lateinit var binding: ActivityLinechartTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)

        // no description text
        binding.chart1.description.isEnabled = false

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        binding.chart1.dragDecelerationFrictionCoef = 0.9f

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)
        binding.chart1.setDrawGridBackground(false)
        binding.chart1.isHighlightPerDragEnabled = true

        // set an alternative background color
        binding.chart1.setBackgroundColor(Color.WHITE)
        binding.chart1.setViewPortOffsets(0f, 0f, 0f, 0f)

        // add data
        binding.seekBarX.progress = 100

        // get the legend (only possible after setting data)
        binding.chart1.legend.apply {
            isEnabled = false
        }

        val xAxis = binding.chart1.xAxis
        xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
        xAxis.typeface = tfLight
        xAxis.textSize = 10f
        xAxis.textColor = Color.WHITE
        xAxis.isDrawAxisLine = false
        xAxis.isDrawGridLines = true
        xAxis.textColor = Color.rgb(255, 192, 56)
        xAxis.centerAxisLabels = true
        xAxis.granularity = 1f // one hour
        xAxis.valueFormatter = object : IAxisValueFormatter {
            private val simpleDateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH)

            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                val millis = TimeUnit.HOURS.toMillis(value.toLong())
                return simpleDateFormat.format(Date(millis))
            }
        }

        val leftAxis = binding.chart1.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        leftAxis.typeface = tfLight
        leftAxis.textColor = holoBlue
        leftAxis.isDrawGridLines = true
        leftAxis.isGranularity = true
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 170f
        leftAxis.yOffset = -9f
        leftAxis.textColor = Color.rgb(255, 192, 56)

        val rightAxis = binding.chart1.axisRight
        rightAxis.isEnabled = false
    }

    private fun setData(count: Int) {
        // now in hours

        val now: Long = 0 //470044; //TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

        val values = ArrayList<Entry>()

        // count = hours
        val to = (now + count).toFloat()

        val valuesData = getValues(to.roundToInt())
        // increment by 1 hour
        var x = now.toFloat()
        while (x < to) {
            val y: Float = if (count == 100)  // initial
                (valuesData[x.roundToInt()])!!.toFloat() * 50 + 50
            else (Math.random() * 50 + 50).toFloat() // manually triggered

            values.add(Entry(x, y)) // add one entry per hour
            x++
        }

        // create a dataset and give it a type
        val set1 = LineDataSet(values, "DataSet 1")
        set1.axisDependency = AxisDependency.LEFT
        set1.color = holoBlue
        set1.valueTextColor = holoBlue
        set1.lineWidth = 1.5f
        set1.isDrawCircles = false
        set1.isDrawValues = false
        set1.fillAlpha = 65
        set1.fillColor = holoBlue
        set1.highLightColor = Color.rgb(244, 117, 117)
        set1.isDrawCircleHoleEnabled = false

        // create a data object with the data sets
        val data = LineData(set1)
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(9f)

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
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/LineChartTime.java".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.lineData.dataSets.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.data?.let {
                    it.isHighlight = !it.isHighlight
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleFilled -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawFilled = !set.isDrawFilled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCircles -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawCircles = !set.isDrawCircles
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCubic -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    if (set.lineMode == LineDataSet.Mode.CUBIC_BEZIER)
                        set.lineMode = LineDataSet.Mode.LINEAR
                    else
                        set.lineMode = LineDataSet.Mode.CUBIC_BEZIER
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleStepped -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    if (set.lineMode == LineDataSet.Mode.STEPPED)
                        set.lineMode = LineDataSet.Mode.LINEAR
                    else
                        set.lineMode = LineDataSet.Mode.STEPPED
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

        setData(binding.seekBarX.progress)

        // redraw
        binding.chart1.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "LineChartTime")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
