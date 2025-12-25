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
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
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
import info.appdev.chartexample.databinding.ActivityBarchartBinding
import info.appdev.chartexample.formatter.MyAxisValueFormatter
import info.appdev.chartexample.formatter.MyValueFormatter
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class StackedBarActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityBarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)

        binding.chart1.setOnChartValueSelectedListener(this)

        binding.chart1.description?.isEnabled = false

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        binding.chart1.setMaxVisibleValueCount(40)

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.setDrawGridBackground(false)
        binding.chart1.setDrawBarShadow(false)

        binding.chart1.setDrawValueAboveBar(false)
        binding.chart1.isHighlightFullBarEnabled = false

        // change the position of the y-labels
        val leftAxis = binding.chart1.axisLeft
        leftAxis.valueFormatter = MyAxisValueFormatter()
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        binding.chart1.axisRight.isEnabled = false

        val xLabels = binding.chart1.xAxis
        xLabels.position = XAxisPosition.TOP

        // chart.setDrawXLabels(false);
        // chart.setDrawYLabels(false);

        // setting data
        binding.seekBarX.progress = 12
        binding.seekBarY.progress = 100

        binding.chart1.legend?.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            formSize = 8f
            formToTextSpace = 4f
            xEntrySpace = 6f
        }

        // chart.setDrawLegend(false);
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(100 + 2)

        for (i in 0..<binding.seekBarX.progress) {
            val mul = (binding.seekBarY.progress + 1).toFloat()
            val val1 = (sampleValues[i]!!.toFloat() * mul) + mul / 3
            val val2 = (sampleValues[i + 1]!!.toFloat() * mul) + mul / 3
            val val3 = (sampleValues[i + 2]!!.toFloat() * mul) + mul / 3
            values.add(
                BarEntry(
                    i.toFloat(),
                    floatArrayOf(val1, val2, val3),
                    ResourcesCompat.getDrawable(resources, R.drawable.star, null)
                )
            )
        }

        val set1: BarDataSet

        if (binding.chart1.barData != null &&
            binding.chart1.barData!!.dataSetCount > 0) {
            set1 = binding.chart1.barData!!.getDataSetByIndex(0) as BarDataSet
            set1.entries  = values
            binding.chart1.barData?.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "Statistics Vienna 2014")
            set1.isDrawIcons = false
            set1.setColors(*this.colors)
            set1.stackLabels = mutableListOf("Births", "Divorces", "Marriages")

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueFormatter(MyValueFormatter())
            data.setValueTextColor(Color.WHITE)

            binding.chart1.setData(data)
        }

        binding.chart1.setFitBars(true)
        binding.chart1.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/StackedBarActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.barData?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleIcons -> {
                binding.chart1.barData?.dataSets?.forEach { set ->
                    set.isDrawIcons = !set.isDrawIcons
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.barData?.let {
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

            R.id.actionToggleBarBorders -> {
                binding.chart1.barData?.dataSets?.forEach { set ->
                    (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f
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
        saveToGallery(binding.chart1, "StackedBarActivity")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        val barEntry = entry as BarEntry

        if (barEntry.yVals != null)
            Timber.i("Value: ${barEntry.yVals!![highlight.stackIndex]}")
        else
            Timber.i("Value: ${barEntry.y}")
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
