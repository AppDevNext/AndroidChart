package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityLinechartNoseekbarBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.data.BaseEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.ColorTemplate

class DynamicalAddingActivity : DemoBase(), OnChartValueSelectedListener {
    var sampleValues: Array<Double?> = getValues(102)

    private lateinit var binding: ActivityLinechartNoseekbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinechartNoseekbarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.setOnChartValueSelectedListener(this)
        binding.chart1.setDrawGridBackground(false)
        binding.chart1.description.isEnabled = false
        binding.chart1.setNoDataText("No chart data available. Use the menu to add entries and data sets!")

        //        chart.getXAxis().setDrawLabels(false);
//        chart.getXAxis().isDrawGridLines = false;
        binding.chart1.invalidate()
    }

    private val colors: IntArray = ColorTemplate.VORDIPLOM_COLORS

    private fun addEntry() {
        var data = binding.chart1.data

        if (data == null) {
            data = LineData()
            binding.chart1.data = data
        }

        var set = data.getDataSetByIndex(0)

        // set.addEntry(...); // can be called as well
        if (set == null) {
            set = createSet()
            data.addDataSet(set)
        }

        val lastDataSetIndex = data.dataSetCount - 1 // add data only to the last
        val lastSet = data.getDataSetByIndex(lastDataSetIndex)
        lastSet?.let {
            val cycleValue = (lastSet.entryCount % 100.0).toInt()

            val value = (sampleValues[cycleValue]!!.toFloat() * 50) + 50f * (lastDataSetIndex + 1)

            data.addEntry(Entry(lastSet.entryCount.toFloat(), value), lastDataSetIndex)
            data.notifyDataChanged()

            // let the chart know it's data has changed
            binding.chart1.notifyDataSetChanged()

            binding.chart1.setVisibleXRangeMaximum(6f)
            //chart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
//
            // this automatically refreshes the chart (calls invalidate())
            binding.chart1.moveViewTo((data.entryCount - 7).toFloat(), 50f, AxisDependency.LEFT)
        }
    }

    private fun removeLastEntry() {
        val data = binding.chart1.data

        if (data != null) {
            val set = data.getDataSetByIndex(0)

            if (set != null) {
                set.getEntryForXValue((set.entryCount - 1).toFloat(), Float.NaN)?.let { entry ->
                    data.removeEntry(entry, 0)
                    // or remove by index
                    // mData.removeEntryByXValue(xIndex, dataSetIndex);
                    data.notifyDataChanged()
                }

                binding.chart1.notifyDataSetChanged()
                binding.chart1.invalidate()
            }
        }
    }

    private fun addDataSet() {
        val data = binding.chart1.data

        if (data == null) {
            binding.chart1.data = LineData()
        } else {
            val count = (data.dataSetCount + 1)
            val amount = data.getDataSetByIndex(0)?.entryCount ?: 0

            val values = ArrayList<Entry>()

            for (i in 0..<amount) {
                val cycleValue = (i % 100.0).toInt()

                values.add(Entry(i.toFloat(), (sampleValues[cycleValue]!!.toFloat() * 50f) + 50f * count))
            }

            val set = LineDataSet(values, "DataSet $count")
            set.lineWidth = 2.5f
            set.circleRadius = 4.5f

            val color = colors[count % colors.size]

            set.color = color
            set.setCircleColor(color)
            set.highLightColor = color
            set.valueTextSize = 10f
            set.valueTextColor = color

            data.addDataSet(set)
            data.notifyDataChanged()
            binding.chart1.notifyDataSetChanged()
            binding.chart1.invalidate()
        }
    }

    private fun removeDataSet() {
        val data = binding.chart1.data

        if (data != null) {
            data.removeDataSet(data.getDataSetByIndex(data.dataSetCount - 1))

            binding.chart1.notifyDataSetChanged()
            binding.chart1.invalidate()
        }
    }

    private fun createSet(): LineDataSet<BaseEntry<Float>, Float> {
        val set = LineDataSet<BaseEntry<Float>, Float>(label = "DataSet 1")
        set.lineWidth = 2.5f
        set.circleRadius = 4.5f
        set.color = Color.rgb(240, 99, 99)
        set.setCircleColor(Color.rgb(240, 99, 99))
        set.highLightColor = Color.rgb(190, 190, 190)
        set.axisDependency = AxisDependency.LEFT
        set.valueTextSize = 10f

        return set
    }

    override fun onValueSelected(entry: BaseEntry<Float>, highlight: Highlight) {
        Toast.makeText(this, entry.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onNothingSelected() {}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dynamical, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/DynamicalAddingActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionAddEntry -> {
                addEntry()
                Toast.makeText(this, "Entry added!", Toast.LENGTH_SHORT).show()
            }

            R.id.actionRemoveEntry -> {
                removeLastEntry()
                Toast.makeText(this, "Entry removed!", Toast.LENGTH_SHORT).show()
            }

            R.id.actionAddDataSet -> {
                addDataSet()
                Toast.makeText(this, "DataSet added!", Toast.LENGTH_SHORT).show()
            }

            R.id.actionRemoveDataSet -> {
                removeDataSet()
                Toast.makeText(this, "DataSet removed!", Toast.LENGTH_SHORT).show()
            }

            R.id.actionClear -> {
                binding.chart1.clear()
                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show()
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
        saveToGallery(binding.chart1, "DynamicalAddingActivity")
    }
}
