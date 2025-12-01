package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase
import androidx.core.net.toUri

/**
 * This works by inverting the background and desired "fill" color. First, we draw the fill color
 * that we want between the lines as the actual background of the chart. Then, we fill the area
 * above the highest line and the area under the lowest line with the desired background color.
 * This method makes it look like we filled the area between the lines, but really we are filling
 * the area OUTSIDE the lines!
 */
class FilledLineActivity : DemoBase() {
    private var chart: LineChart? = null
    private val fillColor = Color.argb(150, 51, 181, 229)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_linechart_noseekbar)

        title = "FilledLineActivity"

        chart = findViewById<LineChart>(R.id.chart1)
        chart!!.setBackgroundColor(Color.WHITE)
        chart!!.setGridBackgroundColor(fillColor)
        chart!!.setDrawGridBackground(true)

        chart!!.setDrawBorders(true)

        // no description text
        chart!!.description.isEnabled = false

        // if disabled, scaling can be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        val l = chart!!.legend
        l.isEnabled = false

        val xAxis = chart!!.xAxis
        xAxis.isEnabled = false

        val leftAxis = chart!!.axisLeft
        leftAxis.setAxisMaximum(900f)
        leftAxis.setAxisMinimum(-250f)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawZeroLine(false)
        leftAxis.setDrawGridLines(false)

        chart!!.axisRight.isEnabled = false

        // add data
        setData(60f)

        chart!!.invalidate()
    }

    private fun setData(range: Float) {
        val count = 100
        val values1 = ArrayList<Entry?>()
        val sampleValues = getValues(count + 2)

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * range) + 50
            values1.add(Entry(i.toFloat(), `val`))
        }

        val values2 = ArrayList<Entry?>()

        for (i in 0..<count) {
            val `val` = (sampleValues[i + 1]!!.toFloat() * range) + 450
            values2.add(Entry(i.toFloat(), `val`))
        }

        val set1: LineDataSet
        val set2: LineDataSet

        if (chart!!.data != null &&
            chart!!.data!!.getDataSetCount() > 0
        ) {
            set1 = chart!!.data!!.getDataSetByIndex(0) as LineDataSet
            set2 = chart!!.data!!.getDataSetByIndex(1) as LineDataSet
            set1.setEntries(values1)
            set2.setEntries(values2)
            chart!!.data!!.notifyDataChanged()
            chart!!.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(values1, "DataSet 1")

            set1.axisDependency = YAxis.AxisDependency.LEFT
            set1.setColor(Color.rgb(255, 241, 46))
            set1.setDrawCircles(false)
            set1.setLineWidth(2f)
            set1.circleRadius = 3f
            set1.fillAlpha = 255
            set1.setDrawFilled(true)
            set1.setFillColor(Color.WHITE)
            set1.setHighLightColor(Color.rgb(244, 117, 117))
            set1.setDrawCircleHole(false)
            set1.fillFormatter = object : IFillFormatter {
                override fun getFillLinePosition(dataSet: ILineDataSet?, dataProvider: LineDataProvider?): Float {
                    // change the return value here to better understand the effect
                    // return 0;
                    return chart!!.axisLeft.axisMinimum
                }
            }

            // create a dataset and give it a type
            set2 = LineDataSet(values2, "DataSet 2")
            set2.axisDependency = YAxis.AxisDependency.LEFT
            set2.setColor(Color.rgb(255, 241, 46))
            set2.setDrawCircles(false)
            set2.setLineWidth(2f)
            set2.circleRadius = 3f
            set2.fillAlpha = 255
            set2.setDrawFilled(true)
            set2.setFillColor(Color.WHITE)
            set2.setDrawCircleHole(false)
            set2.setHighLightColor(Color.rgb(244, 117, 117))
            set2.fillFormatter = object : IFillFormatter {
                override fun getFillLinePosition(dataSet: ILineDataSet?, dataProvider: LineDataProvider?): Float {
                    // change the return value here to better understand the effect
                    // return 600;
                    return chart!!.axisLeft.axisMaximum
                }
            }

            val dataSets = ArrayList<ILineDataSet?>()
            dataSets.add(set1) // add the data sets
            dataSets.add(set2)

            // create a data object with the data sets
            val data = LineData(dataSets)
            data.setDrawValues(false)

            // set data
            chart!!.setData(data)
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
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/FilledLineActivity.java".toUri()
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
