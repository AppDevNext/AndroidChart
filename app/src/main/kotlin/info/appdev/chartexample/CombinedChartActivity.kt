package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import info.appdev.charting.charts.CombinedChart.DrawOrder
import info.appdev.charting.components.AxisBase
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.BubbleData
import info.appdev.charting.data.BubbleDataSet
import info.appdev.charting.data.BubbleEntry
import info.appdev.charting.data.CandleData
import info.appdev.charting.data.CandleDataSet
import info.appdev.charting.data.CandleEntry
import info.appdev.charting.data.CombinedData
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.data.ScatterData
import info.appdev.charting.data.ScatterDataSet
import info.appdev.charting.formatter.IAxisValueFormatter
import info.appdev.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityCombinedBinding
import info.appdev.chartexample.notimportant.DemoBase
import kotlin.math.roundToInt

class CombinedChartActivity : DemoBase() {
    private val sampleCount = 12
    var values: Array<Double?> = getValues(sampleCount * 2)

    private lateinit var binding: ActivityCombinedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCombinedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.description.isEnabled = false
        binding.chart1.setBackgroundColor(Color.WHITE)
        binding.chart1.setDrawGridBackground(false)
        binding.chart1.setDrawBarShadow(false)
        binding.chart1.isHighlightFullBarEnabled = false

        // draw bars behind lines
        binding.chart1.drawOrder = mutableListOf(
            DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.CANDLE, DrawOrder.LINE, DrawOrder.SCATTER
        )

        binding.chart1.legend.apply {
            isWordWrapEnabled = true
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
        }

        val rightAxis = binding.chart1.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val leftAxis = binding.chart1.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val xAxis = binding.chart1.xAxis
        xAxis.position = XAxisPosition.BOTH_SIDED
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : IAxisValueFormatter {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return months[value.toInt() % months.size]
            }
        }

        val data = CombinedData()

        data.setData(generateLineData())
        data.setData(generateBarData())
        data.setData(generateBubbleData())
        data.setData(generateScatterData())
        data.setData(generateCandleData())
        data.setValueTypeface(tfLight)

        xAxis.axisMaximum = data.xMax + 0.25f

        binding.chart1.data = data
        binding.chart1.invalidate()
    }

    private fun generateLineData(): LineData {
        val d = LineData()

        val entries = ArrayList<Entry>()

        for (index in 0..<sampleCount) entries.add(Entry(index + 0.5f, values[index]!!.toFloat() * 15 + 5))

        val set = LineDataSet(entries, "Line DataSet")
        set.color = Color.rgb(240, 238, 70)
        set.lineWidth = 2.5f
        set.setCircleColor(Color.rgb(240, 238, 70))
        set.circleRadius = 5f
        set.fillColor = Color.rgb(240, 238, 70)
        set.lineMode = LineDataSet.Mode.CUBIC_BEZIER
        set.isDrawValues = true
        set.valueTextSize = 10f
        set.setSingleValueTextColor(Color.rgb(240, 238, 70))

        set.axisDependency = YAxis.AxisDependency.LEFT
        d.addDataSet(set)

        return d
    }

    private fun generateBarData(): BarData {
        val entries1 = ArrayList<BarEntry>()
        val entries2 = ArrayList<BarEntry>()

        for (index in 0..<sampleCount) {
            entries1.add(BarEntry(0f, values[index]!!.toFloat() * 25 + 25))

            // stacked
            entries2.add(BarEntry(0f, floatArrayOf(values[index]!!.toFloat() * 13 + 12, values[index]!!.toFloat() * 13 + 12)))
        }

        val set1 = BarDataSet(entries1, "Bar 1")
        set1.color = Color.rgb(60, 220, 78)
        set1.setSingleValueTextColor(Color.rgb(60, 220, 78))
        set1.valueTextSize = 10f
        set1.axisDependency = YAxis.AxisDependency.LEFT

        val set2 = BarDataSet(entries2, "")
        set2.stackLabels = mutableListOf("Stack 1", "Stack 2")
        set2.setColors(Color.rgb(61, 165, 255), Color.rgb(23, 197, 255))
        set2.setSingleValueTextColor(Color.rgb(61, 165, 255))
        set2.valueTextSize = 10f
        set2.axisDependency = YAxis.AxisDependency.LEFT

        val groupSpace = 0.06f
        val barSpace = 0.02f // x2 dataset
        val barWidth = 0.45f // x2 dataset

        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"
        val d = BarData(set1, set2)
        d.barWidth = barWidth

        // make this BarData object grouped
        d.groupBars(0f, groupSpace, barSpace) // start at x = 0

        return d
    }

    private fun generateScatterData(): ScatterData {
        val d = ScatterData()

        val entries = ArrayList<Entry>()

        var index = 0f
        while (index < sampleCount) {
            entries.add(Entry(index + 0.25f, values[(index * 2).roundToInt()]!!.toFloat() * 10 + 55))
            index += 0.5f
        }

        val set = ScatterDataSet(entries, "Scatter DataSet")
        set.setColors(*ColorTemplate.MATERIAL_COLORS)
        set.scatterShapeSize = 7.5f
        set.isDrawValues = false
        set.valueTextSize = 10f
        d.addDataSet(set)

        return d
    }

    private fun generateCandleData(): CandleData {
        val d = CandleData()

        val entries = ArrayList<CandleEntry>()

        var index = 0
        while (index < sampleCount) {
            entries.add(CandleEntry(index + 1f, 90f, 70f, 85f, 75f))
            index += 2
        }

        val set = CandleDataSet(entries, "Candle DataSet")
        set.decreasingColor = Color.rgb(142, 150, 175)
        set.shadowColor = Color.DKGRAY
        set.barSpace = 0.3f
        set.valueTextSize = 10f
        set.isDrawValues = false
        d.addDataSet(set)

        return d
    }

    private fun generateBubbleData(): BubbleData {
        val bd = BubbleData()

        val entries = ArrayList<BubbleEntry>()

        for (index in 0..<sampleCount) {
            val y = values[index]!!.toFloat() * 10 + 105
            val size = values[index]!!.toFloat() * 100 + 105
            entries.add(BubbleEntry(index + 0.5f, y, size))
        }

        val set = BubbleDataSet(entries, "Bubble DataSet")
        set.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        set.valueTextSize = 10f
        set.setSingleValueTextColor(Color.WHITE)
        set.highlightCircleWidth = 1.5f
        set.isDrawValues = true
        bd.addDataSet(set)

        return bd
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.combined, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/CombinedChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleLineValues -> {
                binding.chart1.lineData?.dataSets?.forEach {
                    if (it is LineDataSet)
                        it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleBarValues -> {
                binding.chart1.barData?.dataSets?.forEach {
                    if (it is BarDataSet)
                        it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionRemoveDataSet -> {
                val rnd = values[sampleCount]!!.toFloat().toInt() * binding.chart1.combinedData!!.dataSetCount
                binding.chart1.combinedData!!.removeDataSet(binding.chart1.combinedData!!.getDataSetByIndex(rnd))
                binding.chart1.combinedData!!.notifyDataChanged()
                binding.chart1.notifyDataSetChanged()
                binding.chart1.invalidate()
            }
        }
        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
