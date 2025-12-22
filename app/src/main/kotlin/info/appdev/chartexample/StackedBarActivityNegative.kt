package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ViewPortHandler
import info.appdev.chartexample.databinding.ActivityAgeDistributionBinding
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber
import java.text.DecimalFormat
import kotlin.math.abs

class StackedBarActivityNegative : DemoBase(), OnChartValueSelectedListener {

    private lateinit var binding: ActivityAgeDistributionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgeDistributionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setDrawGridBackground(false)
        binding.chart1.description.isEnabled = false

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.setDrawBarShadow(false)
        binding.chart1.setDrawValueAboveBar(true)
        binding.chart1.isHighlightFullBarEnabled = false

        binding.chart1.axisLeft.isEnabled = false
        binding.chart1.axisRight.axisMaximum = 25f
        binding.chart1.axisRight.axisMinimum = -25f
        binding.chart1.axisRight.setDrawGridLines(false)
        binding.chart1.axisRight.setDrawZeroLine(true)
        binding.chart1.axisRight.setLabelCount(7, false)
        binding.chart1.axisRight.valueFormatter = CustomFormatter()
        binding.chart1.axisRight.textSize = 9f

        val xAxis = binding.chart1.xAxis
        xAxis.position = XAxisPosition.BOTH_SIDED
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textSize = 9f
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 110f
        xAxis.setCenterAxisLabels(true)
        xAxis.labelCount = 12
        xAxis.granularity = 10f
        xAxis.valueFormatter = object : IAxisValueFormatter {
            private val format = DecimalFormat("###")

            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return format.format(value.toDouble()) + "-" + format.format((value + 10).toDouble())
            }
        }

        val l = binding.chart1.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.formSize = 8f
        l.formToTextSpace = 4f
        l.xEntrySpace = 6f

        // IMPORTANT: When using negative values in stacked bars, always make sure the negative values are in the array first
        val values = ArrayList<BarEntry>()
        values.add(BarEntry(5f, floatArrayOf(-10f, 10f)))
        values.add(BarEntry(15f, floatArrayOf(-12f, 13f)))
        values.add(BarEntry(25f, floatArrayOf(-15f, 15f)))
        values.add(BarEntry(35f, floatArrayOf(-17f, 17f)))
        values.add(BarEntry(45f, floatArrayOf(-19f, 20f)))
        values.add(BarEntry(45f, floatArrayOf(-19f, 20f), ResourcesCompat.getDrawable(resources, R.drawable.star, null)))
        values.add(BarEntry(55f, floatArrayOf(-19f, 19f)))
        values.add(BarEntry(65f, floatArrayOf(-16f, 16f)))
        values.add(BarEntry(75f, floatArrayOf(-13f, 14f)))
        values.add(BarEntry(85f, floatArrayOf(-10f, 11f)))
        values.add(BarEntry(95f, floatArrayOf(-5f, 6f)))
        values.add(BarEntry(105f, floatArrayOf(-1f, 2f)))

        val set = BarDataSet(values, "Age Distribution")
        set.isDrawIcons = false
        set.valueFormatter = CustomFormatter()
        set.valueTextSize = 7f
        set.axisDependency = YAxis.AxisDependency.RIGHT
        set.setColors(Color.rgb(67, 67, 72), Color.rgb(124, 181, 236))
        set.stackLabels = arrayOf("Men", "Women")

        val data = BarData(set)
        data.barWidth = 8.5f
        binding.chart1.setData(data)
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
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/StackedBarActivityNegative.java".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.data!!.dataSets.forEach {
                    it?.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleIcons -> {
                binding.chart1.data!!.dataSets.forEach { set ->
                    set?.isDrawIcons = !set.isDrawIcons
                }

                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (binding.chart1.data != null) {
                    binding.chart1.data!!.isHighlightEnabled = !binding.chart1.data!!.isHighlightEnabled()
                    binding.chart1.invalidate()
                }
            }

            R.id.actionTogglePinch -> {
                if (binding.chart1.isPinchZoomEnabled) {
                    binding.chart1.setPinchZoom(false)
                } else {
                    binding.chart1.setPinchZoom(true)
                }

                binding.chart1.invalidate()
            }

            R.id.actionToggleAutoScaleMinMax -> {
                binding.chart1.isAutoScaleMinMaxEnabled = !binding.chart1.isAutoScaleMinMaxEnabled
                binding.chart1.notifyDataSetChanged()
            }

            R.id.actionToggleBarBorders -> {
                for (set in binding.chart1.data!!.dataSets) {
                    (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f
                }

                binding.chart1.invalidate()
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
        saveToGallery(binding.chart1, "StackedBarActivityNegative")
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        val barEntry = entry as BarEntry
        Timber.i("Value: ${abs(barEntry.yVals!![highlight.stackIndex])}")
    }

    override fun onNothingSelected() = Unit

    private class CustomFormatter : IValueFormatter, IAxisValueFormatter {
        private val decimalFormat: DecimalFormat = DecimalFormat("###")

        // data
        override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
            return decimalFormat.format(abs(value).toDouble()) + "m"
        }

        // YAxis
        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            return decimalFormat.format(abs(value).toDouble()) + "m"
        }
    }
}
