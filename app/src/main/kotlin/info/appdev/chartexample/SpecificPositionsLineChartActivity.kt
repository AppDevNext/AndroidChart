package info.appdev.chartexample

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import info.appdev.charting.animation.Easing
import info.appdev.charting.components.Legend.LegendForm
import info.appdev.charting.components.LimitLine
import info.appdev.charting.components.LimitLine.LimitLabelPosition
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.listener.ChartTouchListener.ChartGesture
import info.appdev.charting.listener.OnChartGestureListener
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.getSDKInt
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.databinding.ActivityLinechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class SpecificPositionsLineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartGestureListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityLinechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.progress = 45
        binding.seekBarY.progress = 100
        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.chart1.onChartGestureListener = this
        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setDrawGridBackground(false)

        // no description text
        binding.chart1.description?.isEnabled = false

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        // enable scaling and dragging
        binding.chart1.isDragEnabled = true
        binding.chart1.setScaleEnabled(true)
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.setPinchZoom(true)

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv = MyMarkerView(this, R.layout.custom_marker_view)
        mv.chartView = binding.chart1 // For bounds control
        binding.chart1.marker.add(mv) // Set the marker to the chart

        // x-axis limit line
        val llXAxis = LimitLine(10f, "Index 10")
        llXAxis.lineWidth = 4f
        llXAxis.enableDashedLine(10f, 10f, 0f)
        llXAxis.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        llXAxis.textSize = 10f
        val xAxis = binding.chart1.xAxis
        xAxis.enableGridDashedLine(10f, 10f, 0f)
        xAxis.isShowSpecificPositions = true
        xAxis.specificPositions = floatArrayOf(20f, 30f, 60f)
        val tf = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf")
        val ll1 = LimitLine(150f, "Upper Limit")
        ll1.lineWidth = 4f
        ll1.enableDashedLine(10f, 10f, 0f)
        ll1.labelPosition = LimitLabelPosition.RIGHT_TOP
        ll1.textSize = 10f
        ll1.typeface = tf
        val ll2 = LimitLine(-30f, "Lower Limit")
        ll2.lineWidth = 4f
        ll2.enableDashedLine(10f, 10f, 0f)
        ll2.labelPosition = LimitLabelPosition.RIGHT_BOTTOM
        ll2.textSize = 10f
        ll2.typeface = tf
        val leftAxis = binding.chart1.axisLeft
        leftAxis.removeAllLimitLines() // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1)
        leftAxis.addLimitLine(ll2)
        leftAxis.axisMaximum = 200f
        leftAxis.axisMinimum = -50f
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f)
        leftAxis.setDrawZeroLine(false)
        leftAxis.isShowSpecificPositions = true
        leftAxis.specificPositions = floatArrayOf(0f, 10f, 20f, 50f, 100f, 300f)

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true)
        binding.chart1.axisRight.isEnabled = false
        setData(45, 100f)
        binding.chart1.animateX(2500)
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        binding.chart1.legend?.apply {
            form = LegendForm.LINE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.line, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionToggleValues -> {
                binding.chart1.getData()?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.getData()?.let {
                    it.isHighlightEnabled = !it.isHighlightEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleFilled -> {
                binding.chart1.getData()?.dataSets?.forEach {
                    val set = it as LineDataSet
                    set.isDrawFilledEnabled = !set.isDrawFilledEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCircles -> {
                binding.chart1.getData()?.dataSets?.forEach {
                    val set = it as LineDataSet
                    set.isDrawCirclesEnabled = !set.isDrawCirclesEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCubic -> {
                binding.chart1.getData()?.dataSets?.forEach {
                    val set = it as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.CUBIC_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.CUBIC_BEZIER
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleStepped -> {
                binding.chart1.getData()?.dataSets?.forEach {
                    val set = it as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.STEPPED) LineDataSet.Mode.LINEAR else LineDataSet.Mode.STEPPED
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHorizontalCubic -> {
                binding.chart1.getData()?.dataSets?.forEach {
                    val set = it as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.HORIZONTAL_BEZIER) LineDataSet.Mode.LINEAR else LineDataSet.Mode.HORIZONTAL_BEZIER
                }
                binding.chart1.invalidate()
            }

            R.id.actionTogglePinch -> {
                binding.chart1.setPinchZoom(!binding.chart1.isPinchZoomEnabled)
                binding.chart1.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                binding.chart1.isAutoScaleMinMaxEnabled = !binding.chart1.isAutoScaleMinMaxEnabled
                binding.chart1.notifyDataSetChanged()
            }

            R.id.animateX -> binding.chart1.animateX(3000)
            R.id.animateY -> binding.chart1.animateY(3000, Easing.easeInCubic)
            R.id.animateXY -> binding.chart1.animateXY(3000, 3000)
            R.id.actionSave -> {
                if (binding.chart1.saveToPath("title" + System.currentTimeMillis(), "")) {
                    Toast.makeText(applicationContext, "Saving SUCCESSFUL!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Saving FAILED!", Toast.LENGTH_SHORT).show()
                }

                // mChart.saveToGallery("title"+System.currentTimeMillis())
            }
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = "" + (binding.seekBarX.progress + 1)
        binding.tvXMax.text = "" + binding.seekBarY.progress
        setData(binding.seekBarX.progress + 1, binding.seekBarY.progress.toFloat())

        // redraw
        binding.chart1.invalidate()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // TODO Auto-generated method stub
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // TODO Auto-generated method stub
    }

    private fun setData(count: Int, range: Float) {
        val values = ArrayList<Entry>()
        val sampleValues = getValues(100)
        for (i in 0 until count) {
            val `val` = (sampleValues[i]!!.toFloat() * range) + 3
            values.add(Entry(i.toFloat(), `val`))
        }
        binding.chart1.getData()?.let {
            if (it.dataSetCount > 0) {
                val set1 = it.getDataSetByIndex(0) as LineDataSet
                set1.entries = values
                it.notifyDataChanged()
                binding.chart1.notifyDataSetChanged()
            } else
                createDataset(values)
        } ?: run {
            createDataset(values)
        }
    }

    private fun createDataset(values: ArrayList<Entry>) {
        // create a dataset and give it a type
        val set11 = LineDataSet(values, "DataSet 1")

        // set the line to be drawn like this "- - - - - -"
        set11.enableDashedLine(10f, 5f, 0f)
        set11.enableDashedHighlightLine(10f, 5f, 0f)
        set11.color = Color.BLACK
        set11.setCircleColor(Color.BLACK)
        set11.lineWidth = 1f
        set11.circleRadius = 3f
        set11.isDrawCircleHoleEnabled = false
        set11.valueTextSize = 9f
        set11.isDrawFilledEnabled = true
        set11.formLineWidth = 1f
        set11.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        set11.formSize = 15f
        if (getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue)
            set11.fillDrawable = drawable
        } else {
            set11.fillColor = Color.BLACK
        }
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set11) // add the datasets

        // create a data object with the datasets
        val data = LineData(dataSets)

        // set data
        binding.chart1.setData(data)
    }

    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("START, x: ${me.x}, y: ${me.y}")
    }

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("END, lastGesture: $lastPerformedGesture")

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartGesture.SINGLE_TAP) {
            binding.chart1.highlightValues(null) // or highlightTouch(null) for callback to onNothingSelected(...)
        }
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
        Timber.i("Chart flinged. VeloX: $velocityX, VeloY: $velocityY")
    }

    override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
        Timber.i("ScaleX: $scaleX, ScaleY: $scaleY")
    }

    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
        Timber.i("dX: $dX, dY: $dY")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i(entry.toString())
        Timber.i("LOWHIGH low: ${binding.chart1.lowestVisibleX}, high: ${binding.chart1.highestVisibleX}")
        Timber.i("MIN MAX xmin: ${binding.chart1.xChartMin}, xmax: ${binding.chart1.xChartMax}, ymin: ${binding.chart1.yChartMin}, ymax: ${binding.chart1.yChartMax}")
    }

    override fun onNothingSelected() = Unit

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "SpecificPositionsLineChartActivity")
    }
}
