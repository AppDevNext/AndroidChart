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
import info.appdev.chartexample.databinding.ActivityLinechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

/**
 * Example of a dual axis [LineChart] with multiple data sets.
 *
 * @since 1.7.4
 * @version 3.1.0
 */
class LineChartDualAxisActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityLinechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)

        binding.chart1.setOnChartValueSelectedListener(this)

        // no description text
        binding.chart1.description.isEnabled = false

        // enable touch gestures
        binding.chart1.setTouchEnabled(true)

        binding.chart1.setDragDecelerationFrictionCoef(0.9f)

        // enable scaling and dragging
        binding.chart1.setDragEnabled(true)
        binding.chart1.setScaleEnabled(true)
        binding.chart1.setDrawGridBackground(false)
        binding.chart1.isHighlightPerDragEnabled = true

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.setPinchZoom(true)

        // set an alternative background color
        binding.chart1.setBackgroundColor(Color.LTGRAY)

        // add data
        binding.seekBarX.progress = 20
        binding.seekBarY.progress = 30

        binding.chart1.animateX(1500)

        // get the legend (only possible after setting data)
        val l = binding.chart1.legend

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
        val xAxis = binding.chart1.xAxis
        xAxis.typeface = tfLight
        xAxis.textSize = 11f
        xAxis.textColor = Color.WHITE
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        val leftAxis = binding.chart1.axisLeft
        leftAxis.typeface = tfLight
        leftAxis.textColor = ColorTemplate.holoBlue
        leftAxis.axisMaximum = 200f
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true

        val rightAxis = binding.chart1.axisRight
        rightAxis.typeface = tfLight
        rightAxis.textColor = Color.MAGENTA
        rightAxis.axisMaximum = 900f
        rightAxis.axisMinimum = -200f
        rightAxis.setDrawGridLines(false)
        rightAxis.setDrawZeroLine(false)
        rightAxis.isGranularityEnabled = false
    }

    private fun setData(count: Int, range: Float) {
        val values1 = ArrayList<Entry>()
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * (range / 2f)) + 50
            values1.add(Entry(i.toFloat(), `val`))
        }

        val values2 = ArrayList<Entry>()

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * range) + 450
            values2.add(Entry(i.toFloat(), `val`))
        }

        val values3 = ArrayList<Entry>()

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * range) + 500
            values3.add(Entry(i.toFloat(), `val`))
        }

        val set1: LineDataSet
        val set2: LineDataSet
        val set3: LineDataSet

        if (binding.chart1.data != null &&
            binding.chart1.data!!.getDataSetCount() > 0
        ) {
            set1 = binding.chart1.data!!.getDataSetByIndex(0) as LineDataSet
            set2 = binding.chart1.data!!.getDataSetByIndex(1) as LineDataSet
            set3 = binding.chart1.data!!.getDataSetByIndex(2) as LineDataSet
            set1.entries = values1
            set2.entries = values2
            set3.entries = values3
            binding.chart1.data!!.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values1, "DataSet 1")

            set1.axisDependency = AxisDependency.LEFT
            set1.color = ColorTemplate.holoBlue
            set1.setCircleColor(Color.WHITE)
            set1.lineWidth = 2f
            set1.circleRadius = 3f
            set1.fillAlpha = 65
            set1.fillColor = ColorTemplate.holoBlue
            set1.highLightColor = Color.rgb(244, 117, 117)
            set1.isDrawCircleHoleEnabled = false

            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = LineDataSet(values2, "DataSet 2")
            set2.axisDependency = AxisDependency.RIGHT
            set2.color = Color.MAGENTA
            set2.setCircleColor(Color.WHITE)
            set2.lineWidth = 2f
            set2.circleRadius = 3f
            set2.fillAlpha = 65
            set2.fillColor = Color.BLUE
            set2.isDrawCircleHoleEnabled = false
            set2.highLightColor = Color.rgb(244, 117, 117)

            //set2.setFillFormatter(new MyFillFormatter(900f));
            set3 = LineDataSet(values3, "DataSet 3")
            set3.axisDependency = AxisDependency.RIGHT
            set3.color = Color.YELLOW
            set3.setCircleColor(Color.WHITE)
            set3.lineWidth = 2f
            set3.circleRadius = 3f
            set3.fillAlpha = 65
            set3.fillColor = ColorTemplate.colorWithAlpha(Color.YELLOW, 200)
            set3.isDrawCircleHoleEnabled = false
            set3.highLightColor = Color.rgb(244, 117, 117)

            // create a data object with the data sets
            val data = LineData(set1, set2, set3)
            data.setValueTextColor(Color.WHITE)
            data.setValueTextSize(9f)

            // set data
            binding.chart1.setData(data)
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
                binding.chart1.data!!.dataSets.forEach { set ->
                    set?.isDrawValues = !set.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (binding.chart1.data != null) {
                    binding.chart1.data!!.isHighlightEnabled = !binding.chart1.data!!.isHighlightEnabled()
                    binding.chart1.invalidate()
                }
            }

            R.id.actionToggleFilled -> {
                binding.chart1.data!!.dataSets.forEach { set ->
                    set?.setDrawFilled(!set.isDrawFilledEnabled)
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCircles -> {
                val sets = binding.chart1.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.isDrawCirclesEnabled = !set.isDrawCirclesEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleCubic -> {
                val sets = binding.chart1.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.CUBIC_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.CUBIC_BEZIER
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleStepped -> {
                val sets = binding.chart1.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.STEPPED)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.STEPPED
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHorizontalCubic -> {
                val sets = binding.chart1.data!!.dataSets

                for (iSet in sets) {
                    val set = iSet as LineDataSet
                    set.lineMode = if (set.lineMode == LineDataSet.Mode.HORIZONTAL_BEZIER)
                        LineDataSet.Mode.LINEAR
                    else
                        LineDataSet.Mode.HORIZONTAL_BEZIER
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
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        setData(binding.seekBarX.progress, binding.seekBarY.progress.toFloat())

        // redraw
        binding.chart1.invalidate()
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "LineChartActivity2")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i(entry.toString())

        binding.chart1.data!!.getDataSetByIndex(highlight.dataSetIndex)?.let {
            binding.chart1.centerViewToAnimated(entry.x, entry.y, it.axisDependency, 500)
            //chart.zoomAndCenterAnimated(2.5f, 2.5f, e.getX(), e.getY(), chart.getData().getDataSetByIndex(dataSetIndex)
            // .getAxisDependency(), 1000);
            //chart.zoomAndCenterAnimated(1.8f, 1.8f, e.getX(), e.getY(), chart.getData().getDataSetByIndex(dataSetIndex)
            // .getAxisDependency(), 1000);
        }
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
