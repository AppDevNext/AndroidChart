package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import info.appdev.charting.components.AxisBase
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.formatter.IAxisValueFormatter
import info.appdev.charting.formatter.IValueFormatter
import info.appdev.charting.utils.ViewPortHandler
import info.appdev.chartexample.databinding.ActivityBarchartNoseekbarBinding
import info.appdev.chartexample.notimportant.DemoBase
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

class BarChartPositiveNegative : DemoBase() {

    private lateinit var binding: ActivityBarchartNoseekbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarchartNoseekbarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.setBackgroundColor(Color.WHITE)
        binding.chart1.extraTopOffset = -30f
        binding.chart1.extraBottomOffset = 10f
        binding.chart1.extraLeftOffset = 70f
        binding.chart1.extraRightOffset = 70f

        binding.chart1.setDrawBarShadow(false)
        binding.chart1.setDrawValueAboveBar(true)

        binding.chart1.description.isEnabled = false

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.setDrawGridBackground(false)

        val xAxis = binding.chart1.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.typeface = tfRegular
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textColor = Color.LTGRAY
        xAxis.textSize = 13f
        xAxis.labelCount = 5
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f

        val left = binding.chart1.axisLeft
        left.setDrawLabels(false)
        left.spaceTop = 25f
        left.spaceBottom = 25f
        left.setDrawAxisLine(false)
        left.setDrawGridLines(false)
        left.setDrawZeroLine(true) // draw a zero line
        left.zeroLineColor = Color.GRAY
        left.zeroLineWidth = 0.7f
        binding.chart1.axisRight.isEnabled = false
        binding.chart1.legend.isEnabled = false

        // THIS IS THE ORIGINAL DATA YOU WANT TO PLOT
        val data: MutableList<Data> = ArrayList()
        data.add(Data(0f, -224.1f, "12-29"))
        data.add(Data(1f, 238.5f, "12-30"))
        data.add(Data(2f, 1280.1f, "12-31"))
        data.add(Data(3f, -442.3f, "01-01"))
        data.add(Data(4f, -2280.1f, "01-02"))

        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String? {
                return data[min(max(value.toInt(), 0), data.size - 1)].xAxisValue
            }

            override fun getFormattedValue(value: Long, axis: AxisBase?): String {
                val ma = max(value, 0)
                val mi = min(ma, (data.size - 1).toLong()).toInt()
                return data[mi].xAxisValue!!
            }
        }

        setData(data)
    }

    private fun setData(dataList: MutableList<Data>) {
        val values = ArrayList<BarEntry>()
        val colors: MutableList<Int> = ArrayList()

        val green = Color.rgb(110, 190, 102)
        val red = Color.rgb(211, 74, 88)

        for (i in dataList.indices) {
            val d = dataList[i]
            val entry = BarEntry(d.xValue, d.yValue)
            values.add(entry)

            // specific colors
            if (d.yValue >= 0) colors.add(red)
            else colors.add(green)
        }

        val set: BarDataSet

        if (binding.chart1.barData != null &&
            binding.chart1.barData!!.dataSetCount > 0
        ) {
            set = binding.chart1.barData!!.getDataSetByIndex(0) as BarDataSet
            set.entries  = values
            binding.chart1.barData?.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            set = BarDataSet(values, "Values")
            set.setColors(colors)
            set.valueTextColors = colors

            val data = BarData(set)
            data.setValueTextSize(13f)
            data.setValueTypeface(tfRegular)
            data.setValueFormatter(ValueFormatter())
            data.barWidth = 0.8f

            binding.chart1.setData(data)
            binding.chart1.invalidate()
        }
    }

    /**
     * Demo class representing data.
     */
    private class Data(val xValue: Float, val yValue: Float, val xAxisValue: String?)

    private class ValueFormatter : IValueFormatter {
        private val mFormat: DecimalFormat = DecimalFormat("######.0")

        override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
            return mFormat.format(value.toDouble())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_github, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/BarChartPositiveNegative.java".toUri()
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
