package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import info.appdev.chartexample.DataTools.Companion.generateSineWaves
import info.appdev.chartexample.DataTools.Companion.getSawtoothValues
import info.appdev.chartexample.custom.TimeMarkerView
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class TimeLineActivity : DemoBase() {
    private var menuItemMove: MenuItem? = null
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
            isDrawGridLines = true
//            granularity = 1f // only intervals of 1 day
            labelCount = 7
            valueFormatter = UnixTimeAxisValueFormatter("HH:mm:ss")
        }

        // if disabled, scaling can be done on x- and y-axis separately
        binding.chart1.isPinchZoom = false
        binding.chart1.setExtraOffsets(0f, 0f, 0f, 24f)
        binding.chart1.legend.apply {
            isEnabled = false
            form = LegendForm.LINE
        }

        binding.chart1.axisLeft.apply {
            axisMaximum = 150f
            axisMinimum = -50f
            isDrawAxisLine = true
            isDrawZeroLine = true
            isDrawGridLines = true
        }

        binding.chart1.axisRight.isEnabled = false

        val timeMarkerView = TimeMarkerView(this, R.layout.custom_marker_view, "HH:mm:ss.sss")
        timeMarkerView.chartView = binding.chart1
        binding.chart1.marker.add(timeMarkerView)

        setData(60f, TIME_OFFSET, true)

        binding.chart1.invalidate()
    }

    @Suppress("SameParameterValue")
    private fun setData(range: Float, timeOffset: Long, sinus: Boolean) {

        val sampleEntries = if (sinus)
            generateSineWaves(3, 30).mapIndexed { index, data ->
                val valueY = (data.toFloat() * range) + 50
                Entry(timeOffset + index.toFloat() * 1000, valueY)
            }
        else {
            var previousEntry: Entry? = null
            getSawtoothValues(14).mapIndexed { index, data ->
                val valueY = data.toFloat() * 20
                val entry = previousEntry?.let {
                    // nay third value is 0, so we add here more then 1 second, otherwise we have a one second entry
                    if (index % 3 == 0) {
                        Entry(it.x + 3000, valueY)
                    } else
                        Entry(it.x + 1000, valueY)
                } ?: run {
                    Entry(timeOffset + index.toFloat() * 1000, valueY)
                }
                previousEntry = entry
                // Now you can use 'prev' which holds the previous Entry
                entry
            }
        }

        val simpleDateFormat = SimpleDateFormat("HH:mm:ss.sss", Locale.getDefault())
        sampleEntries.forEach { entry ->
            val entryText = "Entry: x=${simpleDateFormat.format(entry.x)} x=${entry.x}, y=${entry.y}"
            Timber.d(entryText)
        }

        val set1: LineDataSet

        if (binding.chart1.lineData.dataSetCount > 0) {
            set1 = binding.chart1.lineData.getDataSetByIndex(0) as LineDataSet
            set1.entries = sampleEntries.toMutableList()
            if (sinus)
                set1.lineMode = LineDataSet.Mode.LINEAR
            else
                set1.lineMode = LineDataSet.Mode.STEPPED
            binding.chart1.lineData.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
        } else {
            // create a dataset and give it a type
            set1 = LineDataSet(sampleEntries.toMutableList(), "DataSet 1")
            if (sinus)
                set1.lineMode = LineDataSet.Mode.LINEAR
            else
                set1.lineMode = LineDataSet.Mode.STEPPED
            set1.axisDependency = YAxis.AxisDependency.LEFT
            set1.color = Color.rgb(255, 241, 46)
            set1.isDrawCircles = false
            set1.lineWidth = 2f
            set1.circleRadius = 3f
            set1.fillAlpha = 255
            set1.isDrawFilled = true
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
        menuItemMove = menu?.add("Move chart")?.apply {
            setOnMenuItemClickListener { menuItem ->
                menuItem.isChecked = !menuItem.isChecked
                lifecycleScope.launch {
                    moveChart()
                }
                true
            }.isCheckable = true
        }
        menuItemMove = menu?.add("Move X-Axis")?.apply {
            setOnMenuItemClickListener { menuItem ->
                menuItem.isChecked = !menuItem.isChecked
                lifecycleScope.launch {
                    moveXAxis()
                }
                true
            }.isCheckable = true
        }
        menuItemMove = menu?.add("Show sinus data")?.apply {
            this.isChecked = true
            setOnMenuItemClickListener { menuItem ->
                menuItem.isChecked = !menuItem.isChecked
                lifecycleScope.launch {
                    setData(60f, TIME_OFFSET, menuItem.isChecked)
                    binding.chart1.invalidate()
                }
                true
            }.isCheckable = true
        }
        return true
    }

    fun <T> MutableList<T>.moveFirstToLast() {
        val first = this[0]
        val second = this[1] // needed to get time diff
        val last = this[size - 1]
        val timeDiff = (second as Entry).x - (first as Entry).x
        removeAt(0)
        first.x = (last as Entry).x + timeDiff
        add(first)
    }

    private suspend fun moveXAxis() {
        withContext(Dispatchers.Default) {
            while (menuItemMove!!.isChecked) {
                withContext(Dispatchers.Main) {
                    binding.chart1.xAxis.apply {
                        this.axisMinimum += 1000f
                        this.axisMaximum += 1000f
                        binding.chart1.notifyDataSetChanged()
                        binding.chart1.invalidate()
                    }
                }
                delay(100)
            }
        }
    }

    private suspend fun moveChart() {
        withContext(Dispatchers.Default) {
            while (menuItemMove!!.isChecked) {
                withContext(Dispatchers.Main) {
                    binding.chart1.lineData.dataSets[0].apply {
                        (this as LineDataSet).entries.moveFirstToLast()
                        this.notifyDataChanged()
                        binding.chart1.lineData.notifyDataChanged()
                        binding.chart1.notifyDataSetChanged()
                        binding.chart1.invalidate()
                    }
                }
                delay(100)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/TimeLineActivity.kt".toUri()
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
