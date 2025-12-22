package info.appdev.chartexample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.net.toUri
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityScrollviewBinding
import info.appdev.chartexample.notimportant.DemoBase

class ScrollViewActivity : DemoBase() {

    private lateinit var binding: ActivityScrollviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScrollviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.description.isEnabled = false

        // scaling can now only be done on x- and y-axis separately
        binding.chart1.setPinchZoom(false)

        binding.chart1.setDrawBarShadow(false)
        binding.chart1.setDrawGridBackground(false)

        val xAxis = binding.chart1.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        binding.chart1.axisLeft.setDrawGridLines(false)

        binding.chart1.legend.isEnabled = false

        setData(10)
        binding.chart1.setFitBars(true)
    }

    private fun setData(count: Int) {
        val sampleValues = getValues(count)
        val values = ArrayList<BarEntry>()

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * count) + 15
            values.add(BarEntry(i.toFloat(), `val`.toInt().toFloat()))
        }

        val set = BarDataSet(values, "Data Set")
        set.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        set.isDrawValues = false

        val data = BarData(set)

        binding.chart1.setData(data)
        binding.chart1.invalidate()
        binding.chart1.animateY(800)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_github, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/ScrollViewActivity.kt".toUri()
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
