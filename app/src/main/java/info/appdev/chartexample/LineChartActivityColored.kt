package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

class LineChartActivityColored : DemoBase() {
    private val charts = arrayOfNulls<LineChart>(4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_colored_lines)

        setTitle("LineChartActivityColored")

        charts[0] = findViewById(R.id.chart1)
        charts[1] = findViewById(R.id.chart2)
        charts[2] = findViewById(R.id.chart3)
        charts[3] = findViewById(R.id.chart4)

        val mTf = Typeface.createFromAsset(assets, "OpenSans-Bold.ttf")

        for (i in charts.indices) {
            val data = getData(100f)
            data.setValueTypeface(mTf)

            // add some transparency to the color with "& 0x90FFFFFF"
            setupChart(charts[i]!!, data, colors[i % colors.size])
        }
    }

    private val colors = intArrayOf(
        Color.rgb(137, 230, 81),
        Color.rgb(240, 240, 30),
        Color.rgb(89, 199, 250),
        Color.rgb(250, 104, 104)
    )

    private fun setupChart(chart: LineChart, data: LineData, color: Int) {
        (data.getDataSetByIndex(0) as LineDataSet).setCircleHoleColor(color)

        // no description text
        chart.description.isEnabled = false

        // chart.setDrawHorizontalGrid(false);
        //
        // enable / disable grid background
        chart.setDrawGridBackground(false)

        //        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);

        // enable touch gestures
        chart.setTouchEnabled(true)

        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)

        chart.setBackgroundColor(color)

        // set custom chart offsets (automatic offset calculation is hereby disabled)
        chart.setViewPortOffsets(10f, 0f, 10f, 0f)

        // add data
        chart.setData(data)

        // get the legend (only possible after setting data)
        val l = chart.legend
        l.isEnabled = false

        chart.axisLeft.isEnabled = false
        chart.axisLeft.spaceTop = 40f
        chart.axisLeft.spaceBottom = 40f
        chart.axisRight.isEnabled = false

        chart.xAxis.isEnabled = false

        // animate calls invalidate()...
        chart.animateX(2500)
    }

    private fun getData(range: Float): LineData {
        val count = 36
        val values = ArrayList<Entry>()
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            val `val` = (sampleValues[i].toFloat() * range) + 3
            values.add(Entry(i.toFloat(), `val`))
        }

        // create a dataset and give it a type
        val set1 = LineDataSet(values, "DataSet 1")

        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);
        set1.setLineWidth(1.75f)
        set1.setCircleRadius(5f)
        set1.setCircleHoleRadius(2.5f)
        set1.setColor(Color.WHITE)
        set1.setCircleColor(Color.WHITE)
        set1.highLightColor = Color.WHITE
        set1.isDrawValuesEnabled = false

        // create a data object with the data sets
        return LineData(set1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_github, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/LineChartActivityColored.java".toUri())
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
