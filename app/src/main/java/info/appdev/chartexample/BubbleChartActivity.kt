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
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.BubbleChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

class BubbleChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: BubbleChart? = null
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
        setContentView(R.layout.activity_bubblechart)

        setTitle("BubbleChartActivity")

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarX!!.setOnSeekBarChangeListener(this)

        seekBarY = findViewById(R.id.seekBarY)
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.description.isEnabled = false

        chart!!.setOnChartValueSelectedListener(this)

        chart!!.drawGridBackground = false

        chart!!.setTouchEnabled(true)

        // enable scaling and dragging
        chart!!.isDragEnabled = true
        chart!!.setScaleEnabled(true)

        chart!!.setMaxVisibleValueCount(200)
        chart!!.setPinchZoom(true)

        seekBarX!!.progress = 10
        seekBarY!!.progress = 50

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.typeface = tfLight

        val yl = chart!!.axisLeft
        yl.typeface = tfLight
        yl.spaceTop = 30f
        yl.spaceBottom = 30f
        yl.setDrawZeroLine(false)

        chart!!.axisRight.isEnabled = false

        val xl = chart!!.xAxis
        xl.position = XAxisPosition.BOTTOM
        xl.typeface = tfLight
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val count = seekBarX!!.progress
        val range = seekBarY!!.progress

        tvX!!.text = count.toString()
        tvY!!.text = range.toString()

        val values1 = ArrayList<BubbleEntry>()
        val values2 = ArrayList<BubbleEntry>()
        val values3 = ArrayList<BubbleEntry>()
        val sampleValues = getValues(100)

        for (i in 0..<count) {
            values1.add(
                BubbleEntry(
                    i.toFloat(),
                    (sampleValues[i + 1] * range).toFloat(),
                    (sampleValues[i].toFloat() * range),
                    getResources().getDrawable(R.drawable.star)
                )
            )
            values2.add(
                BubbleEntry(
                    i.toFloat(), (sampleValues[i + 2] * range).toFloat(), (sampleValues[i + 1].toFloat() * range), getResources().getDrawable(
                        R.drawable.star
                    )
                )
            )
            values3.add(BubbleEntry(i.toFloat(), (sampleValues[i] * range).toFloat(), (sampleValues[i + 2].toFloat() * range)))
        }

        // create a dataset and give it a type
        val set1 = BubbleDataSet(values1, "DS 1")
        set1.isDrawIconsEnabled = false
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0], 130)
        set1.isDrawValuesEnabled = true

        val set2 = BubbleDataSet(values2, "DS 2")
        set2.isDrawIconsEnabled = false
        set2.iconsOffset = MPPointF(0f, 15f)
        set2.setColor(ColorTemplate.COLORFUL_COLORS[1], 130)
        set2.isDrawValuesEnabled = true

        val set3 = BubbleDataSet(values3, "DS 3")
        set3.setColor(ColorTemplate.COLORFUL_COLORS[2], 130)
        set3.isDrawValuesEnabled = true

        val dataSets = ArrayList<IBubbleDataSet>()
        dataSets.add(set1) // add the data sets
        dataSets.add(set2)
        dataSets.add(set3)

        // create a data object with the data sets
        val data = BubbleData(dataSets)
        data.setDrawValues(false)
        data.setValueTypeface(tfLight)
        data.setValueTextSize(8f)
        data.setValueTextColor(Color.WHITE)
        data.setHighlightCircleWidth(1.5f)

        chart!!.setData(data)
        chart!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bubble, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/BubbleChartActivity.java".toUri())
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                for (set in chart!!.data!!.dataSets) set.isDrawValuesEnabled = !set.isDrawValuesEnabled

                chart!!.invalidate()
            }

            R.id.actionToggleIcons -> {
                for (set in chart!!.data!!.dataSets) set.isDrawIconsEnabled = !set.isDrawIconsEnabled

                chart!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled
                    chart!!.invalidate()
                }
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
        saveToGallery(chart, "BubbleChartActivity")
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
