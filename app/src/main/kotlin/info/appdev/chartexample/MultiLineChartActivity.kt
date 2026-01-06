package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityLinechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Legend
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.listener.ChartTouchListener.ChartGesture
import info.appdev.charting.listener.OnChartGestureListener
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.ColorTemplate
import timber.log.Timber

class MultiLineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartGestureListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityLinechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setDrawGridBackground(false)
        binding.chart1.description.isEnabled = false
        binding.chart1.setDrawBorders(false)

        binding.chart1.axisLeft.isEnabled = false
        binding.chart1.axisRight.setDrawAxisLine(false)
        binding.chart1.axisRight.setDrawGridLines(false)
        binding.chart1.xAxis.setDrawAxisLine(false)
        binding.chart1.xAxis.setDrawGridLines(false)

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.seekBarX.progress = 20
        binding.seekBarY.progress = 100

        binding.chart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
        }
    }

    private val colors = intArrayOf(
        ColorTemplate.VORDIPLOM_COLORS[0],
        ColorTemplate.VORDIPLOM_COLORS[1],
        ColorTemplate.VORDIPLOM_COLORS[2]
    )

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.chart1.resetTracking()

        val progress: Int = binding.seekBarX.progress

        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        val dataSets = ArrayList<ILineDataSet>()

        for (datasetNumber in 0..2) {
            val values = ArrayList<Entry>()
            val sampleValues = getValues(100)

            for (i in 0..<progress) {
                val valuesY = (sampleValues[i]!!.toFloat() * binding.seekBarY.progress) + 3
                values.add(Entry(i.toFloat(), valuesY))
            }

            val lineDataSet = LineDataSet(values, "DataSet " + (datasetNumber + 1))
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4f

            val color = colors[datasetNumber % colors.size]
            lineDataSet.color = color
            lineDataSet.setCircleColor(color)
            dataSets.add(lineDataSet)
        }

        // make the first DataSet dashed
        (dataSets[0] as LineDataSet).enableDashedLine(10f, 10f, 0f)
        (dataSets[0] as LineDataSet).setColors(*ColorTemplate.VORDIPLOM_COLORS)
        (dataSets[0] as LineDataSet).setCircleColors(*ColorTemplate.VORDIPLOM_COLORS)

        val data = LineData(dataSets)
        binding.chart1.data = data
        binding.chart1.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.line, menu)
        menu.removeItem(R.id.actionToggleIcons)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/MultiLineChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.lineData.dataSets?.forEach { set ->
                    set.isDrawValues = !set.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionTogglePinch -> {
                if (binding.chart1.isPinchZoomEnabled) binding.chart1.setPinchZoom(false)
                else binding.chart1.setPinchZoom(true)

                binding.chart1.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                binding.chart1.isAutoScaleMinMaxEnabled = !binding.chart1.isAutoScaleMinMaxEnabled
                binding.chart1.notifyDataSetChanged()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.data?.let { data ->
                    data.isHighlightEnabled = !data.isHighlightEnabled
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
                    if (set is LineDataSet) {
                        set.isDrawCircles = !set.isDrawCircles
                    }
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCubic -> {
                binding.chart1.data?.dataSets?.forEach { iSet ->
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.CUBIC_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.CUBIC_BEZIER
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleStepped -> {
                binding.chart1.data?.dataSets?.forEach { iSet ->
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.STEPPED)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.STEPPED
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHorizontalCubic -> {
                binding.chart1.data?.dataSets?.forEach { iSet ->
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.HORIZONTAL_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.HORIZONTAL_BEZIER
                }
                binding.chart1.invalidate()
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
        saveToGallery(binding.chart1, "MultiLineChartActivity")
    }

    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("START, x: ${me.x}, y: ${me.y}")
    }

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("END, lastGesture: $lastPerformedGesture")

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartGesture.SINGLE_TAP) binding.chart1.highlightValues(null) // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    override fun onChartLongPressed(me: MotionEvent) {
        Timber.i("Chart long pressed.")
    }

    override fun onChartDoubleTapped(me: MotionEvent) {
        Timber.i("Chart double-tapped.")
    }

    override fun onChartSingleTapped(me: MotionEvent) {
        Timber.i("Chart single-tapped.")
    }

    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent, velocityX: Float, velocityY: Float) {
        Timber.i("Chart fling. VelocityX: $velocityX, VelocityY: $velocityY")
    }

    override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
        Timber.i("ScaleX: $scaleX, ScaleY: $scaleY")
    }

    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
        Timber.i("dX: $dX, dY: $dY")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Value: ${entry.y}, xIndex: ${entry.x}, DataSet index: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
