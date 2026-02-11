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
import info.appdev.chartexample.DataTools.Companion.setData
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.databinding.ActivityLinechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.animation.Easing
import info.appdev.charting.components.Legend.LegendForm
import info.appdev.charting.components.LimitLine
import info.appdev.charting.components.LimitLine.LimitLabelPosition
import info.appdev.charting.components.LimitRange
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import timber.log.Timber

/**
 * Example of a heavily customized [info.appdev.charting.charts.LineChart] with limit lines, custom line shapes, etc.
 */
class LineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityLinechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.max = 180
        binding.seekBarY.setOnSeekBarChangeListener(this)

        // background color
        binding.chart1.setBackgroundColor(Color.WHITE)

        // disable description text
        binding.chart1.description.isEnabled = false

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        // set listeners
        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setDrawGridBackground(false)

        // create marker to display box when values are selected
        val markerView = MyMarkerView(this, R.layout.custom_marker_view)

        // Set the marker to the chart
        markerView.chartView = binding.chart1
        binding.chart1.marker.add(markerView)

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)

        // force pinch zoom along both axis
        binding.chart1.isPinchZoom = true

        // vertical grid lines
        binding.chart1.xAxis.enableGridDashedLine(10f, 10f, 0f)

        // disable dual axis (only use LEFT axis)
        binding.chart1.axisRight.isEnabled = false

        // horizontal grid lines
        binding.chart1.axisLeft.enableGridDashedLine(10f, 10f, 0f)

        // axis range
        binding.chart1.axisLeft.axisMaximum = 200f
        binding.chart1.axisLeft.axisMinimum = -50f

        val limitLineUpper = LimitLine(150f, "Upper Limit").apply {
            lineWidth = 4f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLabelPosition.RIGHT_TOP
            textSize = 10f
            typeface = tfRegular
            lineColor = Color.GREEN
        }

        val limitLineLower = LimitLine(-30f, "Lower Limit").apply {
            lineWidth = 4f
            enableDashedLine(10f, 10f, 0f)
            labelPosition = LimitLabelPosition.RIGHT_BOTTOM
            textSize = 10f
            typeface = tfRegular
            lineColor = Color.GREEN
        }

        val limitRange = LimitRange(45f, 90f, "Middle Range").apply {
            lineWidth = 2f
            labelPosition = LimitLabelPosition.RIGHT_TOP
            textSize = 10f
            typeface = tfRegular
            lineColor = Color.CYAN
            rangeColor = Color.argb(30, 255, 235, 0)
        }

        val limitRangeLower = LimitRange(45f, 52f).apply {
            rangeColor = Color.argb(30, 230, 0, 0)
        }

        // draw limit lines behind data instead of on top
        binding.chart1.axisLeft.isDrawLimitLinesBehindData = true
        binding.chart1.xAxis.isDrawLimitLinesBehindData = true

        // add limit lines
        binding.chart1.axisLeft.addLimitLine(limitLineUpper)
        binding.chart1.axisLeft.addLimitLine(limitLineLower)
        // binding.chart1.axisLeft.addLimitLine(llXAxis10)

        // add limit range
        binding.chart1.axisLeft.addLimitRange(limitRange)
        binding.chart1.axisLeft.addLimitRange(limitRangeLower)

        // add data
        binding.seekBarX.progress = 45
        binding.seekBarY.progress = 180
        setData(this, binding.chart1, 45, 180f)

        // draw points over time
        binding.chart1.animateX(1500)

        // get the legend (only possible after setting data)
        binding.chart1.legend.form = LegendForm.LINE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.line, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/LineChartActivity1.java".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.lineData.dataSets.forEach { set ->
                    set.isDrawValues = !set.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleIcons -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawIcons = !set.isDrawIcons
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.data?.let {
                    it.isHighlight = !it.isHighlight
                    binding.chart1.invalidate()
                }
            }

            R.id.actionToggleFilled -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawFilled = !set.isDrawFilled
                    binding.chart1.invalidate()
                }
            }

            R.id.actionToggleCircles -> {
                binding.chart1.data?.dataSets?.map { it as LineDataSet }?.forEach { set ->
                    set.isDrawCircles = !set.isDrawCircles
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCubic -> {
                binding.chart1.data?.dataSets?.map { it as LineDataSet }?.forEach { set ->
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.CUBIC_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.CUBIC_BEZIER
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleStepped -> {
                binding.chart1.data?.dataSets?.map { it as LineDataSet }?.forEach { set ->
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.STEPPED)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.STEPPED
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHorizontalCubic -> {
                binding.chart1.data?.dataSets?.map { it as LineDataSet }?.forEach { set ->
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.HORIZONTAL_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.HORIZONTAL_BEZIER
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

            R.id.animateX -> binding.chart1.animateX(2000)
            R.id.animateY -> binding.chart1.animateY(2000, Easing.easeInCubic)
            R.id.animateXY -> binding.chart1.animateXY(2000, 2000)
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

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()
        setData(this, binding.chart1, binding.seekBarX.progress, binding.seekBarY.progress.toFloat())

        // redraw
        binding.chart1.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "LineChartActivity1")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i(entry.toString())
        Timber.i("LOW HIGH low:${binding.chart1.lowestVisibleX}, high:${binding.chart1.highestVisibleX}")
        Timber.i("MIN MAX xMin:${binding.chart1.xChartMin}, xMax:${binding.chart1.xChartMax}, yMin:${binding.chart1.yChartMin}, yMax:${binding.chart1.yChartMax}")
    }

    override fun onNothingSelected() = Unit
}
