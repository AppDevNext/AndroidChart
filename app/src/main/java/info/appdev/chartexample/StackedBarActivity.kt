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
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.MyAxisValueFormatter
import info.appdev.chartexample.custom.MyValueFormatter
import info.appdev.chartexample.notimportant.DemoBase
import androidx.core.net.toUri

class StackedBarActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: BarChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_barchart)

        setTitle("StackedBarActivity")

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarX!!.setOnSeekBarChangeListener(this)

        seekBarY = findViewById(R.id.seekBarY)
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setOnChartValueSelectedListener(this)

        chart!!.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart!!.setMaxVisibleValueCount(40)

        // scaling can now only be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        chart!!.setDrawGridBackground(false)
        chart!!.setDrawBarShadow(false)

        chart!!.setDrawValueAboveBar(false)
        chart!!.setHighlightFullBarEnabled(false)

        // change the position of the y-labels
        val leftAxis = chart!!.axisLeft
        leftAxis.valueFormatter = MyAxisValueFormatter()
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        chart!!.axisRight.isEnabled = false

        val xLabels = chart!!.xAxis
        xLabels.position = XAxisPosition.TOP

        // chart.setDrawXLabels(false);
        // chart.setDrawYLabels(false);

        // setting data
        seekBarX!!.progress = 12
        seekBarY!!.progress = 100

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.formSize = 8f
        l.formToTextSpace = 4f
        l.xEntrySpace = 6f

        // chart.setDrawLegend(false);
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX!!.text = seekBarX!!.progress.toString()
        tvY!!.text = seekBarY!!.progress.toString()

        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(100 + 2)

        for (i in 0..<seekBarX!!.progress) {
            val mul = (seekBarY!!.progress + 1).toFloat()
            val val1 = (sampleValues[i].toFloat() * mul) + mul / 3
            val val2 = (sampleValues[i + 1].toFloat() * mul) + mul / 3
            val val3 = (sampleValues[i + 2].toFloat() * mul) + mul / 3
            values.add(
                BarEntry(
                    i.toFloat(),
                    floatArrayOf(val1, val2, val3),
                    getResources().getDrawable(R.drawable.star)
                )
            )
        }

        val set1: BarDataSet?

        if (chart!!.data != null &&
            chart!!.data!!.dataSetCount > 0
        ) {
            set1 = chart!!.data!!.getDataSetByIndex(0) as BarDataSet
            set1.entries = values
            chart!!.data!!.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "Statistics Vienna 2014")
            set1.isDrawIconsEnabled = false
            set1.setColors(*this.colors)
            set1.setStackLabels(arrayOf("Births", "Divorces", "Marriages"))

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueFormatter(MyValueFormatter())
            data.setValueTextColor(Color.WHITE)

            chart!!.setData(data)
        }

        chart!!.setFitBars(true)
        chart!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/StackedBarActivity.java".toUri())
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                val sets: MutableList<IBarDataSet> = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as BarDataSet
                    set.isDrawValuesEnabled = !set.isDrawValuesEnabled
                }

                chart!!.invalidate()
            }

            R.id.actionToggleIcons -> {
                val sets: MutableList<IBarDataSet> = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as BarDataSet
                    set.isDrawIconsEnabled = !set.isDrawIconsEnabled
                }

                chart!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled
                    chart!!.invalidate()
                }
            }

            R.id.actionTogglePinch -> {
                chart!!.setPinchZoom(!chart!!.isPinchZoomEnabled)

                chart!!.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                chart!!.isAutoScaleMinMaxEnabled = !chart!!.isAutoScaleMinMaxEnabled
                chart!!.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                for (set in chart!!.data!!.dataSets) (set as BarDataSet).setBarBorderWidth(if (set.barBorderWidth == 1f) 0f else 1f)

                chart!!.invalidate()
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

    override fun saveToGallery() {
        saveToGallery(chart, "StackedBarActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null || h == null) return

        val entry = e as BarEntry

        if (entry.yVals != null) Log.i("VAL SELECTED", "Value: " + entry.yVals!![h.stackIndex])
        else Log.i("VAL SELECTED", "Value: " + entry.y)
    }

    override fun onNothingSelected() {}

    private val colors: IntArray
        get() {
            // have as many colors as stack-values per entry

            val colors = IntArray(3)

            System.arraycopy(ColorTemplate.MATERIAL_COLORS, 0, colors, 0, 3)

            return colors
        }
}
