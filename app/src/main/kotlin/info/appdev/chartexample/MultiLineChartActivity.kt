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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class MultiLineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartGestureListener, OnChartValueSelectedListener {
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
        seekBarX!!.setOnSeekBarChangeListener(this)

        seekBarY = findViewById(R.id.seekBarY)
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setOnChartValueSelectedListener(this)

        chart!!.setDrawGridBackground(false)
        chart!!.description.isEnabled = false
        chart!!.setDrawBorders(false)

        chart!!.axisLeft.isEnabled = false
        chart!!.axisRight.setDrawAxisLine(false)
        chart!!.axisRight.setDrawGridLines(false)
        chart!!.xAxis.setDrawAxisLine(false)
        chart!!.xAxis.setDrawGridLines(false)

        // enable touch gestures
        chart!!.setTouchEnabled(true)

        // enable scaling and dragging
        chart!!.setDragEnabled(true)
        chart!!.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        seekBarX!!.progress = 20
        seekBarY!!.progress = 100

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
    }

    private val colors = intArrayOf(
        ColorTemplate.VORDIPLOM_COLORS[0],
        ColorTemplate.VORDIPLOM_COLORS[1],
        ColorTemplate.VORDIPLOM_COLORS[2]
    )

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        chart!!.resetTracking()

        val progress: Int = seekBarX!!.progress

        tvX!!.text = seekBarX!!.progress.toString()
        tvY!!.text = seekBarY!!.progress.toString()

        val dataSets = ArrayList<ILineDataSet?>()

        for (z in 0..2) {
            val values = ArrayList<Entry?>()
            val sampleValues = getValues(100)

            for (i in 0..<progress) {
                val `val` = ((sampleValues[i]!!.toFloat() * seekBarY!!.progress) + 3).toDouble()
                values.add(Entry(i.toFloat(), `val`.toFloat()))
            }

            val d = LineDataSet(values, "DataSet " + (z + 1))
            d.setLineWidth(2.5f)
            d.circleRadius = 4f

            val color = colors[z % colors.size]
            d.setColor(color)
            d.setCircleColor(color)
            dataSets.add(d)
        }

        // make the first DataSet dashed
        (dataSets[0] as LineDataSet).enableDashedLine(10f, 10f, 0f)
        (dataSets[0] as LineDataSet).setColors(*ColorTemplate.VORDIPLOM_COLORS)
        (dataSets[0] as LineDataSet).setCircleColors(*ColorTemplate.VORDIPLOM_COLORS)

        val data = LineData(dataSets)
        chart!!.setData(data)
        chart!!.invalidate()
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
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.setDrawValues(!set.isDrawValuesEnabled)
                }

                chart!!.invalidate()
            }

            R.id.actionTogglePinch -> {
                if (chart!!.isPinchZoomEnabled) chart!!.setPinchZoom(false)
                else chart!!.setPinchZoom(true)

                chart!!.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled()
                    chart!!.invalidate()
                }
            }

            R.id.actionToggleFilled -> {
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawFilledEnabled) set.setDrawFilled(false)
                    else set.setDrawFilled(true)
                }
                chart!!.invalidate()
            }

            R.id.actionToggleCircles -> {
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    if (set.isDrawCirclesEnabled) set.setDrawCircles(false)
                    else set.setDrawCircles(true)
                }
                chart!!.invalidate()
            }

            R.id.actionToggleCubic -> {
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode = if (set.mode == LineDataSet.Mode.CUBIC_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.CUBIC_BEZIER
                }
                chart!!.invalidate()
            }

            R.id.actionToggleStepped -> {
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode = if (set.mode == LineDataSet.Mode.STEPPED)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.STEPPED
                }
                chart!!.invalidate()
            }

            R.id.actionToggleHorizontalCubic -> {
                val sets = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.mode = if (set.mode == LineDataSet.Mode.HORIZONTAL_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.HORIZONTAL_BEZIER
                }
                chart!!.invalidate()
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(chart)
                }
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
        }
        return true
    }

    override fun saveToGallery() {
        saveToGallery(chart, "MultiLineChartActivity")
    }

    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("START, x: " + me.x + ", y: " + me.y)
    }

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("END, lastGesture: $lastPerformedGesture")

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartGesture.SINGLE_TAP) chart!!.highlightValues(null) // or highlightTouch(null) for callback to onNothingSelected(...)
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
        Timber.i("Value: " + entry.y + ", xIndex: " + entry.x + ", DataSet index: " + highlight.dataSetIndex)
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
