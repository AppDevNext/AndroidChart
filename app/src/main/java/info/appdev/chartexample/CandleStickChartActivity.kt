package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

class CandleStickChartActivity : DemoBase(), OnSeekBarChangeListener {
    private var chart: CandleStickChart? = null
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
        setContentView(R.layout.activity_candlechart)

        setTitle("CandleStickChartActivity")

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarX!!.setOnSeekBarChangeListener(this)

        seekBarY = findViewById(R.id.seekBarY)
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setBackgroundColor(Color.WHITE)

        chart!!.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart!!.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        chart!!.drawGridBackground = false

        val xAxis = chart!!.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val leftAxis = chart!!.axisLeft
        //        leftAxis.setEnabled(false);
        leftAxis.setLabelCount(7, false)
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)

        val rightAxis = chart!!.axisRight
        rightAxis.isEnabled = false

        //        rightAxis.setStartAtZero(false);

        // setting data
        seekBarX!!.progress = 40
        seekBarY!!.progress = 100

        chart!!.legend.isEnabled = false
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val progress: Int = (seekBarX!!.progress)

        tvX!!.text = progress.toString()
        tvY!!.text = seekBarY!!.progress.toString()

        chart!!.resetTracking()

        val values = ArrayList<CandleEntry>()
        val sampleValues = getValues(100)

        for (i in 0..<progress) {
            val multi = (seekBarY!!.progress + 1).toFloat()
            val `val` = (sampleValues[i].toFloat() * 40) + multi

            val high = (sampleValues[i].toFloat() * 9) + 8f
            val low = (sampleValues[i].toFloat() * 8) + 8f

            val open = (sampleValues[i].toFloat() * 6) + 1f
            val close = (sampleValues[i].toFloat() * 7) + 1f

            val even = i % 2 == 0

            values.add(
                CandleEntry(
                    i.toFloat(), `val` + high,
                    `val` - low,
                    if (even) `val` + open else `val` - open,
                    if (even) `val` - close else `val` + close,
                    ResourcesCompat.getDrawable(resources, R.drawable.star, theme)
                )
            )
        }

        val set1 = CandleDataSet(values, "Data Set")

        set1.isDrawIconsEnabled = false
        set1.axisDependency = AxisDependency.LEFT
        //        set1.setColor(Color.rgb(80, 80, 80));
        set1.setShadowColor(Color.DKGRAY)
        set1.setShadowWidth(0.7f)
        set1.setDecreasingColor(Color.BLUE)
        set1.setDecreasingPaintStyle(Paint.Style.FILL)
        set1.setIncreasingColor(Color.rgb(122, 242, 84))
        set1.setIncreasingPaintStyle(Paint.Style.STROKE)
        set1.setNeutralColor(Color.BLUE)

        //set1.setHighlightLineWidth(1f);
        val data = CandleData(set1)

        chart!!.setData(data)
        chart!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.candle, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/CandleStickChartActivity.java".toUri())
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

            R.id.actionToggleMakeShadowSameColorAsCandle -> {
                for (set in chart!!.data!!.dataSets) {
                    (set as CandleDataSet).setShadowColorSameAsCandle(!set.shadowColorSameAsCandle)
                }

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
        saveToGallery(chart, "CandleStickChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
