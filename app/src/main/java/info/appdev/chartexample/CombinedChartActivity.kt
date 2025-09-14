package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase
import kotlin.math.roundToInt
import androidx.core.net.toUri

class CombinedChartActivity : DemoBase() {
    private var chart: CombinedChart? = null
    private val sampleCount = 12
    var values: Array<Double> = getValues(sampleCount * 2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_combined)

        setTitle("CombinedChartActivity")

        chart = findViewById(R.id.chart1)
        chart!!.description.isEnabled = false
        chart!!.setBackgroundColor(Color.WHITE)
        chart!!.setDrawGridBackground(false)
        chart!!.setDrawBarShadow(false)
        chart!!.setHighlightFullBarEnabled(false)

        // draw bars behind lines
        chart!!.drawOrder = arrayOf(
            DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.CANDLE, DrawOrder.LINE, DrawOrder.SCATTER
        )

        val l = chart!!.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val rightAxis = chart!!.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val leftAxis = chart!!.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val xAxis = chart!!.xAxis
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

        chart!!.setData(data)
        chart!!.invalidate()
    }

    private fun generateLineData(): LineData {
        val d = LineData()

        val entries = ArrayList<Entry>()

        for (index in 0..<sampleCount) entries.add(Entry(index + 0.5f, values[index]!!.toFloat() * 15 + 5))

        val set = LineDataSet(entries, "Line DataSet")
        set.setColor(Color.rgb(240, 238, 70))
        set.setLineWidth(2.5f)
        set.setCircleColor(Color.rgb(240, 238, 70))
        set.setCircleRadius(5f)
        set.setFillColor(Color.rgb(240, 238, 70))
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER)
        set.isDrawValuesEnabled = true
        set.valueTextSize = 10f
        set.valueTextColor = Color.rgb(240, 238, 70)

        set.axisDependency = AxisDependency.LEFT
        d.addDataSet(set)

        return d
    }

    private fun generateBarData(): BarData {
        val entries1 = ArrayList<BarEntry>()
        val entries2 = ArrayList<BarEntry>()

        for (index in 0..<sampleCount) {
            entries1.add(BarEntry(0f, values[index].toFloat() * 25 + 25))

            // stacked
            entries2.add(BarEntry(0f, floatArrayOf(values[index].toFloat() * 13 + 12, values[index].toFloat() * 13 + 12)))
        }

        val set1 = BarDataSet(entries1, "Bar 1")
        set1.setColor(Color.rgb(60, 220, 78))
        set1.valueTextColor = Color.rgb(60, 220, 78)
        set1.valueTextSize = 10f
        set1.axisDependency = AxisDependency.LEFT

        val set2 = BarDataSet(entries2, "")
        set2.setStackLabels(arrayOf("Stack 1", "Stack 2"))
        set2.setColors(Color.rgb(61, 165, 255), Color.rgb(23, 197, 255))
        set2.valueTextColor = Color.rgb(61, 165, 255)
        set2.valueTextSize = 10f
        set2.axisDependency = AxisDependency.LEFT

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
            entries.add(Entry(index + 0.25f, values[(index * 2).roundToInt()].toFloat() * 10 + 55))
            index += 0.5f
        }

        val set = ScatterDataSet(entries, "Scatter DataSet")
        set.setColors(*ColorTemplate.MATERIAL_COLORS)
        set.scatterShapeSize = 7.5f
        set.isDrawValuesEnabled = false
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
        set.setDecreasingColor(Color.rgb(142, 150, 175))
        set.setShadowColor(Color.DKGRAY)
        set.setBarSpace(0.3f)
        set.valueTextSize = 10f
        set.isDrawValuesEnabled = false
        d.addDataSet(set)

        return d
    }

    private fun generateBubbleData(): BubbleData {
        val bd = BubbleData()

        val entries = ArrayList<BubbleEntry>()

        for (index in 0..<sampleCount) {
            val y = values[index].toFloat() * 10 + 105
            val size = values[index].toFloat() * 100 + 105
            entries.add(BubbleEntry(index + 0.5f, y, size))
        }

        val set = BubbleDataSet(entries, "Bubble DataSet")
        set.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        set.valueTextSize = 10f
        set.valueTextColor = Color.WHITE
        set.highlightCircleWidth = 1.5f
        set.isDrawValuesEnabled = true
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
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/CombinedChartActivity.java".toUri())
                startActivity(i)
            }

            R.id.actionToggleLineValues -> {
                for (set in chart!!.data!!.dataSets) {
                    if (set is LineDataSet) set.isDrawValuesEnabled = !set.isDrawValuesEnabled
                }

                chart!!.invalidate()
            }

            R.id.actionToggleBarValues -> {
                for (set in chart!!.data!!.dataSets) {
                    if (set is BarDataSet) set.isDrawValuesEnabled = !set.isDrawValuesEnabled
                }

                chart!!.invalidate()
            }

            R.id.actionRemoveDataSet -> {
                val rnd = (values[sampleCount] * chart!!.data!!.dataSetCount).toInt()
                chart!!.data!!.removeDataSet(chart!!.data!!.getDataSetByIndex(rnd))
                chart!!.data!!.notifyDataChanged()
                chart!!.notifyDataSetChanged()
                chart!!.invalidate()
            }
        }
        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
