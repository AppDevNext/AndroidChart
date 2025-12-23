package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityCandlechartBinding
import info.appdev.chartexample.notimportant.DemoBase

class CandleStickChartActivity : DemoBase(), OnSeekBarChangeListener {

    private lateinit var binding: ActivityCandlechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCandlechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.chart1.setBackgroundColor(Color.WHITE)
        binding.chart1.description.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be drawn
        binding.chart1.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.setDrawGridBackground(false)

        val xAxis = binding.chart1.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        val leftAxis = binding.chart1.axisLeft
        //        leftAxis.setEnabled(false);
        leftAxis.setLabelCount(7, false)
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawAxisLine(false)

        val rightAxis = binding.chart1.axisRight
        rightAxis.isEnabled = false

        //        rightAxis.setStartAtZero(false);

        // setting data
        binding.seekBarX.progress = 40
        binding.seekBarY.progress = 100

        binding.chart1.legend.isEnabled = false
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val progress: Int = (binding.seekBarX.progress)

        binding.tvXMax.text = progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        binding.chart1.resetTracking()

        val values = ArrayList<CandleEntry>()
        val sampleValues = getValues(100)

        for (i in 0..<progress) {
            val multi = (binding.seekBarY.progress + 1).toFloat()
            val `val` = (sampleValues[i]!!.toFloat() * 40) + multi

            val high = (sampleValues[i]!!.toFloat() * 9) + 8f
            val low = (sampleValues[i]!!.toFloat() * 8) + 8f

            val open = (sampleValues[i]!!.toFloat() * 6) + 1f
            val close = (sampleValues[i]!!.toFloat() * 7) + 1f

            val even = i % 2 == 0

            values.add(
                CandleEntry(
                    i.toFloat(), `val` + high,
                    `val` - low,
                    if (even) `val` + open else `val` - open,
                    if (even) `val` - close else `val` + close,
                    ResourcesCompat.getDrawable(resources, R.drawable.star, null)
                )
            )
        }

        val set1 = CandleDataSet(values, "Data Set")

        set1.isDrawIcons = false
        set1.axisDependency = AxisDependency.LEFT
        //        set1.setColor(Color.rgb(80, 80, 80));
        set1.shadowColor = Color.DKGRAY
        set1.shadowWidth = 0.7f
        set1.decreasingColor = Color.BLUE
        set1.decreasingPaintStyle = Paint.Style.FILL
        set1.increasingColor = Color.rgb(122, 242, 84)
        set1.increasingPaintStyle = Paint.Style.STROKE
        set1.neutralColor = Color.BLUE

        //set1.setHighlightLineWidth(1f);
        val data = CandleData(set1)

        binding.chart1.setData(data)
        binding.chart1.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.candle, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/CandleStickChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.data?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
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
                    it.isHighlightEnabled = !it.isHighlightEnabled
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

            R.id.actionToggleMakeShadowSameColorAsCandle -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    (set as CandleDataSet).shadowColorSameAsCandle = !set.shadowColorSameAsCandle
                }

                binding.chart1.invalidate()
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

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "CandleStickChartActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
