package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.CustomScatterShapeRenderer
import info.appdev.chartexample.notimportant.DemoBase

class ScatterChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: ScatterChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_scatterchart)

        setTitle("ScatterChartActivity")

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarX!!.setOnSeekBarChangeListener(this)

        seekBarY = findViewById(R.id.seekBarY)
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.description.isEnabled = false
        chart!!.setOnChartValueSelectedListener(this)

        chart!!.setDrawGridBackground(false)
        chart!!.setTouchEnabled(true)
        chart!!.setMaxHighlightDistance(50f)

        // enable scaling and dragging
        chart!!.isDragEnabled = true
        chart!!.setScaleEnabled(true)

        chart!!.setMaxVisibleValueCount(200)
        chart!!.setPinchZoom(true)

        seekBarX!!.progress = 45
        seekBarY!!.progress = 100

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.typeface = tfLight
        l.xOffset = 5f

        val yl = chart!!.axisLeft
        yl.typeface = tfLight
        yl.axisMinimum = 0f // this replaces setStartAtZero(true)

        chart!!.axisRight.isEnabled = false

        val xl = chart!!.xAxis
        xl.typeface = tfLight
        xl.setDrawGridLines(false)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX!!.text = seekBarX!!.progress.toString()
        tvY!!.text = seekBarY!!.progress.toString()

        val values1 = ArrayList<Entry>()
        val values2 = ArrayList<Entry>()
        val values3 = ArrayList<Entry>()
        val sampleValues = getValues(100 + 2)

        for (i in 0..<seekBarX!!.progress) {
            val `val` = (sampleValues[i].toFloat() * seekBarY!!.progress) + 3
            values1.add(Entry(i.toFloat(), `val`))
        }

        for (i in 0..<seekBarX!!.progress) {
            val `val` = (sampleValues[i + 1].toFloat() * seekBarY!!.progress) + 3
            values2.add(Entry(i + 0.33f, `val`))
        }

        for (i in 0..<seekBarX!!.progress) {
            val `val` = (sampleValues[i + 2].toFloat() * seekBarY!!.progress) + 3
            values3.add(Entry(i + 0.66f, `val`))
        }

        // create a dataset and give it a type
        val set1 = ScatterDataSet(values1, "DS 1")
        set1.setScatterShape(ScatterShape.SQUARE)
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0])
        val set2 = ScatterDataSet(values2, "DS 2")
        set2.setScatterShape(ScatterShape.CIRCLE)
        set2.scatterShapeHoleColor = ColorTemplate.COLORFUL_COLORS[3]
        set2.scatterShapeHoleRadius = 3f
        set2.setColor(ColorTemplate.COLORFUL_COLORS[1])
        val set3 = ScatterDataSet(values3, "DS 3")
        set3.shapeRenderer = CustomScatterShapeRenderer()
        set3.setColor(ColorTemplate.COLORFUL_COLORS[2])

        set1.scatterShapeSize = 8f
        set2.scatterShapeSize = 8f
        set3.scatterShapeSize = 8f

        val dataSets = ArrayList<IScatterDataSet>()
        dataSets.add(set1) // add the data sets
        dataSets.add(set2)
        dataSets.add(set3)

        // create a data object with the data sets
        val data = ScatterData(dataSets)
        data.setValueTypeface(tfLight)

        chart!!.setData(data)
        chart!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.scatter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/ScatterChartActivity.java".toUri())
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                val sets: MutableList<IScatterDataSet> = chart!!.data!!
                    .dataSets

                for (iSet in sets) {
                    val set = iSet as ScatterDataSet
                    set.isDrawValuesEnabled = !set.isDrawValuesEnabled
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

            R.id.animateX -> {
                chart!!.animateX(3000)
            }

            R.id.animateY -> {
                chart!!.animateY(3000)
            }

            R.id.animateXY -> {
                chart!!.animateXY(3000, 3000)
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
        saveToGallery(chart, "ScatterChartActivity")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i(
            "VAL SELECTED",
            ("Value: " + e?.y + ", xIndex: " + e?.x
                    + ", DataSet index: " + h?.dataSetIndex)
        )
    }

    override fun onNothingSelected() {}

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
