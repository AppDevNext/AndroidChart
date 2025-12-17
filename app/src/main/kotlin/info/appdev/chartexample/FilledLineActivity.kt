package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityLinechartNoseekbarBinding
import info.appdev.chartexample.notimportant.DemoBase

/**
 * This works by inverting the background and desired "fill" color. First, we draw the fill color
 * that we want between the lines as the actual background of the chart. Then, we fill the area
 * above the highest line and the area under the lowest line with the desired background color.
 * This method makes it look like we filled the area between the lines, but really we are filling
 * the area OUTSIDE the lines!
 */
class FilledLineActivity : DemoBase() {
    private val fillColor = Color.argb(150, 51, 181, 229)

    private lateinit var binding: ActivityLinechartNoseekbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartNoseekbarBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.chart1.setBackgroundColor(Color.WHITE)
        binding.chart1.setGridBackgroundColor(fillColor)
        binding.chart1.setDrawGridBackground(true)

        binding.chart1.setDrawBorders(true)

        // no description text
        binding.chart1.description.isEnabled = false

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        val l = binding.chart1.legend
        l.isEnabled = false

        val xAxis = binding.chart1.xAxis
        xAxis.isEnabled = false

        val leftAxis = binding.chart1.axisLeft
        leftAxis.setAxisMaximum(900f)
        leftAxis.setAxisMinimum(-250f)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawZeroLine(false)
        leftAxis.setDrawGridLines(false)

        binding.chart1.axisRight.isEnabled = false

        setData(60f)

        binding.chart1.invalidate()
    }

    private fun setData(@Suppress("SameParameterValue") range: Float) {
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

        if (binding.chart1.data != null &&
            binding.chart1.data!!.getDataSetCount() > 0
        ) {
            set1 = binding.chart1.data!!.getDataSetByIndex(0) as LineDataSet
            set2 = binding.chart1.data!!.getDataSetByIndex(1) as LineDataSet
            set1.setEntries(values1)
            set2.setEntries(values2)
            binding.chart1.data!!.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
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
                    return binding.chart1.axisLeft.axisMinimum
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
                    return binding.chart1.axisLeft.axisMaximum
                }
            }

            val dataSets = ArrayList<ILineDataSet?>()
            dataSets.add(set1) // add the data sets
            dataSets.add(set2)

            // create a data object with the data sets
            val data = LineData(dataSets)
            data.setDrawValues(false)

            binding.chart1.setData(data)
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
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/FilledLineActivity.kt".toUri()
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() = Unit // Intentionally left empty
}
