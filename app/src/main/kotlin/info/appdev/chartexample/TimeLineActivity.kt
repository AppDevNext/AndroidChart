package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.generateSineWaves
import info.appdev.chartexample.databinding.ActivityLinechartNoseekbarBinding
import info.appdev.chartexample.formatter.UnixTimeAxisValueFormatter
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.Description
import info.appdev.charting.components.Legend.LegendForm
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.components.YAxis
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.formatter.IFillFormatter
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.interfaces.datasets.ILineDataSet

class TimeLineActivity : DemoBase() {
    private lateinit var binding: ActivityLinechartNoseekbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartNoseekbarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.setBackgroundColor(Color.WHITE)
        binding.chart1.setDrawGridBackground(true)

        binding.chart1.setDrawBorders(true)

        // no description text
        binding.chart1.description = Description().apply {
            text = "Sinus Time Line"
        }
        binding.chart1.description.isEnabled = true

        binding.chart1.xAxis.apply {
//            enableGridDashedLine(10f, 10f, 0f)
//            this.position = XAxis.XAxisPosition.BOTH_SIDED
            isEnabled = true
            position = XAxisPosition.BOTTOM
            typeface = tfLight
            labelRotationAngle = 45f
            setDrawGridLines(false)
//            granularity = 1f // only intervals of 1 day
            labelCount = 7
            valueFormatter = UnixTimeAxisValueFormatter("yyyy-MM-dd HH:mm:ss")
        }

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.legend.apply {
            isEnabled = false
            form = LegendForm.LINE
        }

        binding.chart1.axisLeft.apply {
            axisMaximum = 150f
            axisMinimum = -50f
            setDrawAxisLine(true)
            setDrawZeroLine(true)
            setDrawGridLines(true)
        }

        binding.chart1.axisRight.isEnabled = false

        setData(60f, TIME_OFFSET)

        binding.chart1.invalidate()
    }

    @Suppress("SameParameterValue")
    private fun setData(range: Float, timeOffset: Long) {

        val sampleEntries = generateSineWaves(3, 30)
            .mapIndexed { index, data ->
                val valueY = (data.toFloat() * range) + 50
                Entry(timeOffset + index.toFloat() * 1000, valueY)
            }.toMutableList()

        val set1: LineDataSet

        if (binding.chart1.lineData.dataSetCount > 0) {
            set1 = binding.chart1.lineData.getDataSetByIndex(0) as LineDataSet
            set1.entries = sampleEntries
            binding.chart1.lineData.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(sampleEntries, "DataSet 1")

            set1.axisDependency = YAxis.AxisDependency.LEFT
            set1.color = Color.rgb(255, 241, 46)
            set1.isDrawCirclesEnabled = false
            set1.lineWidth = 2f
            set1.circleRadius = 3f
            set1.fillAlpha = 255
            set1.isDrawFilledEnabled = true
            set1.fillColor = Color.WHITE
            set1.highLightColor = Color.rgb(244, 117, 117)
            set1.isDrawCircleHoleEnabled = false
            set1.fillFormatter = object : IFillFormatter {
                override fun getFillLinePosition(dataSet: ILineDataSet?, dataProvider: LineDataProvider): Float {
                    // change the return value here to better understand the effect
                    // return 0;
                    return binding.chart1.axisLeft.axisMinimum
                }
            }

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(set1) // add the data sets

            // create a data object with the data sets
            val data = LineData(dataSets)
            data.setDrawValues(false)

            binding.chart1.data = data
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

    companion object {
        const val TIME_OFFSET = 1767105583L
    }
}
