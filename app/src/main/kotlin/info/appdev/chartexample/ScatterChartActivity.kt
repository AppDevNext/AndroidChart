package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.ScatterChart
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
import info.appdev.chartexample.databinding.ActivityScatterchartBinding
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class ScatterChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityScatterchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScatterchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.chart1.description?.isEnabled = false
        binding.chart1.setOnChartValueSelectedListener(this)

        binding.chart1.setDrawGridBackground(false)
        binding.chart1.setTouchEnabled(true)
        binding.chart1.maxHighlightDistance = 50f

        // enable scaling and dragging
        binding.chart1.setDragEnabled(true)
        binding.chart1.setScaleEnabled(true)

        binding.chart1.setMaxVisibleValueCount(200)
        binding.chart1.setPinchZoom(true)

        binding.seekBarX.progress = 45
        binding.seekBarY.progress = 100

        binding.chart1.legend?.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            typeface = tfLight
            xOffset = 5f
        }

        val yl = binding.chart1.axisLeft
        yl.typeface = tfLight
        yl.axisMinimum = 0f // this replaces setStartAtZero(true)

        binding.chart1.axisRight.isEnabled = false

        val xl = binding.chart1.xAxis
        xl.typeface = tfLight
        xl.setDrawGridLines(false)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        val values1 = ArrayList<Entry>()
        val values2 = ArrayList<Entry>()
        val values3 = ArrayList<Entry>()
        val sampleValues = getValues(100 + 2)

        for (i in 0..<binding.seekBarX.progress) {
            val `val` = (sampleValues[i]!!.toFloat() * binding.seekBarY.progress) + 3
            values1.add(Entry(i.toFloat(), `val`))
        }

        for (i in 0..<binding.seekBarX.progress) {
            val `val` = (sampleValues[i + 1]!!.toFloat() * binding.seekBarY.progress) + 3
            values2.add(Entry(i + 0.33f, `val`))
        }

        for (i in 0..<binding.seekBarX.progress) {
            val `val` = (sampleValues[i + 2]!!.toFloat() * binding.seekBarY.progress) + 3
            values3.add(Entry(i + 0.66f, `val`))
        }

        // create a dataset and give it a type
        val set1 = ScatterDataSet(values1, "DS 1")
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE)
        set1.color = ColorTemplate.COLORFUL_COLORS[0]
        val set2 = ScatterDataSet(values2, "DS 2")
        set2.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        set2.scatterShapeHoleColor = ColorTemplate.COLORFUL_COLORS[3]
        set2.scatterShapeHoleRadius = 3f
        set2.color = ColorTemplate.COLORFUL_COLORS[1]
        val set3 = ScatterDataSet(values3, "DS 3")
        set3.shapeRenderer = CustomScatterShapeRenderer()
        set3.color = ColorTemplate.COLORFUL_COLORS[2]

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

        binding.chart1.setData(data)
        binding.chart1.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.scatter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/ScatterChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.scatterData?.dataSets?.forEach { set ->
                    set.isDrawValues = !set.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.scatterData?.let {
                    it.isHighlightEnabled = !it.isHighlightEnabled
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
                binding.chart1.animateX(3000)
            }

            R.id.animateY -> {
                binding.chart1.animateY(3000)
            }

            R.id.animateXY -> {
                binding.chart1.animateXY(3000, 3000)
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
        saveToGallery(binding.chart1, "ScatterChartActivity")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Value: ${entry.y}, xIndex: ${entry.x}, DataSet index: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() {}

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
