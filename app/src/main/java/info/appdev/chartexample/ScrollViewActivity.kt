package info.appdev.chartexample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

class ScrollViewActivity : DemoBase() {
    private var chart: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_scrollview)

        title = "ScrollViewActivity"

        chart = findViewById<BarChart>(R.id.chart1)

        chart!!.description.isEnabled = false

        // scaling can now only be done on x- and y-axis separately
        chart!!.setPinchZoom(false)

        chart!!.setDrawBarShadow(false)
        chart!!.setDrawGridBackground(false)

        val xAxis = chart!!.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        chart!!.axisLeft.setDrawGridLines(false)

        chart!!.legend.isEnabled = false

        setData(10)
        chart!!.setFitBars(true)
    }

    private fun setData(count: Int) {
        val sampleValues = getValues(count)
        val values = ArrayList<BarEntry?>()

        for (i in 0..<count) {
            val `val` = (sampleValues[i]!!.toFloat() * count) + 15
            values.add(BarEntry(i.toFloat(), `val`.toInt().toFloat()))
        }

        val set = BarDataSet(values, "Data Set")
        set.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        set.setDrawValues(false)

        val data = BarData(set)

        chart!!.setData(data)
        chart!!.invalidate()
        chart!!.animateY(800)
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
