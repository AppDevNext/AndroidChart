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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

/**
 * Example of a dual axis [LineChart] with multiple data sets.
 *
 * @since 1.7.4
 * @version 3.1.0
 */
class LineChartDualAxisActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
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

        // no description text
        chart!!.description.isEnabled = false

        // enable touch gestures
        chart!!.setTouchEnabled(true)

        chart!!.setDragDecelerationFrictionCoef(0.9f)

        // enable scaling and dragging
        chart!!.setDragEnabled(true)
        chart!!.setScaleEnabled(true)
        chart!!.setDrawGridBackground(false)
        chart!!.isHighlightPerDragEnabled = true

        // if disabled, scaling can be done on x- and y-axis separately
        chart!!.setPinchZoom(true)

        // set an alternative background color
        chart!!.setBackgroundColor(Color.LTGRAY)

        // add data
        seekBarX!!.progress = 20
        seekBarY!!.progress = 30

        chart!!.animateX(1500)

        // get the legend (only possible after setting data)
        val l = chart!!.legend

        // modify the legend ...
        l.form = LegendForm.LINE
        l.typeface = tfLight
        l.textSize = 11f
        l.textColor = Color.WHITE
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        //        l.setYOffset(11f);
        val xAxis = chart!!.xAxis
        xAxis.typeface = tfLight
        xAxis.textSize = 11f
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        val leftAxis = chart!!.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.textColor = ColorTemplate.getHoloBlue()
        leftAxis.axisMaximum = 200f
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true

        val rightAxis = chart!!.axisRight
        rightAxis.typeface = tfLight
        rightAxis.textColor = Color.MAGENTA
        rightAxis.axisMaximum = 900f
        rightAxis.axisMinimum = -200f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.isGranularityEnabled = false
    }

    private fun setData(count: Int, range: Float) {
        val values1 = ArrayList<Entry?>()
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * (range / 2f)) + 50
            values1.add(Entry(i.toFloat(), `val`))
        }

        val values2 = ArrayList<Entry?>()

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * range) + 450
            values2.add(Entry(i.toFloat(), `val`))
        }

        val values3 = ArrayList<Entry?>()

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * range) + 500
            values3.add(Entry(i.toFloat(), `val`))
        }

        val set1: LineDataSet
        val set2: LineDataSet
        val set3: LineDataSet

        if (chart!!.data != null &&
            chart!!.data!!.getDataSetCount() > 0
        ) {
            set1 = chart!!.data!!.getDataSetByIndex(0) as LineDataSet
            set2 = chart!!.data!!.getDataSetByIndex(1) as LineDataSet
            set3 = chart!!.data!!.getDataSetByIndex(2) as LineDataSet
            set1.entries = values1
            set2.entries = values2
            set3.entries = values3
            chart!!.data!!.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values1, "DataSet 1")

            set1.axisDependency = AxisDependency.LEFT
            set1.color = ColorTemplate.getHoloBlue()
            set1.setCircleColor(Color.WHITE)
            set1.setLineWidth(2f)
            set1.circleRadius = 3f
            set1.setFillAlpha(65)
            set1.setFillColor(ColorTemplate.getHoloBlue())
            set1.highLightColor = Color.rgb(244, 117, 117)
            set1.setDrawCircleHole(false)

            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = LineDataSet(values2, "DataSet 2")
            set2.axisDependency = AxisDependency.RIGHT
            set2.color = Color.MAGENTA
            set2.setCircleColor(Color.WHITE)
            set2.setLineWidth(2f)
            set2.circleRadius = 3f
            set2.setFillAlpha(65)
            set2.setFillColor(Color.BLUE)
            set2.setDrawCircleHole(false)
            set2.highLightColor = Color.rgb(244, 117, 117)

            //set2.setFillFormatter(new MyFillFormatter(900f));
            set3 = LineDataSet(values3, "DataSet 3")
            set3.axisDependency = AxisDependency.RIGHT
            set3.color = Color.YELLOW
            set3.setCircleColor(Color.WHITE)
            set3.setLineWidth(2f)
            set3.circleRadius = 3f
            set3.setFillAlpha(65)
            set3.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200))
            set3.setDrawCircleHole(false)
            set3.highLightColor = Color.rgb(244, 117, 117)

            // create a data object with the data sets
            val data = LineData(set1, set2, set3)
            data.setValueTextColor(Color.WHITE)
            data.setValueTextSize(9f)

            // set data
            chart!!.setData(data)
        }
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
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/LineChartActivity2.java".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                chart!!.data!!.dataSets.forEach { set ->
                    set.isDrawValues = !set.isDrawValues
                }
                chart!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled()
                    chart!!.invalidate()
                }
            }

            R.id.actionToggleFilled -> {
                chart!!.data!!.dataSets.forEach { set ->
                    set.setDrawFilled(!set.isDrawFilledEnabled)
                }
                chart!!.invalidate()
            }

            R.id.actionToggleCircles -> {
                val sets = chart!!.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.setDrawCircles(!set.isDrawCirclesEnabled)
                }
                chart!!.invalidate()
            }

            R.id.actionToggleCubic -> {
                val sets = chart!!.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.CUBIC_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.CUBIC_BEZIER
                }
                chart!!.invalidate()
            }

            R.id.actionToggleStepped -> {
                val sets = chart!!.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.STEPPED)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.STEPPED
                }
                chart!!.invalidate()
            }

            R.id.actionToggleHorizontalCubic -> {
                val sets = chart!!.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.HORIZONTAL_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.HORIZONTAL_BEZIER
                }
                chart!!.invalidate()
            }

            R.id.actionTogglePinch -> {
                chart!!.setPinchZoom(!chart!!.isPinchZoomEnabled)

                chart!!.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
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
        chart!!.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(chart, "LineChartActivity2")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i(entry.toString())

        chart!!.centerViewToAnimated(
            entry.x, entry.y, chart!!.data!!.getDataSetByIndex(highlight.dataSetIndex)
                .axisDependency, 500
        )
        //chart.zoomAndCenterAnimated(2.5f, 2.5f, e.getX(), e.getY(), chart.getData().getDataSetByIndex(dataSetIndex)
        // .getAxisDependency(), 1000);
        //chart.zoomAndCenterAnimated(1.8f, 1.8f, e.getX(), e.getY(), chart.getData().getDataSetByIndex(dataSetIndex)
        // .getAxisDependency(), 1000);
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
