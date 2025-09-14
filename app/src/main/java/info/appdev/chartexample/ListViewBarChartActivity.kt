package info.appdev.chartexample

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.net.toUri
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

/**
 * Demonstrates the use of charts inside a ListView. IMPORTANT: provide a
 * specific height attribute for the chart inside your ListView item
 *
 * @author Philipp Jahoda
 */
class ListViewBarChartActivity : DemoBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_listview_chart)

        setTitle("ListViewBarChartActivity")

        val lv = findViewById<ListView?>(R.id.listViewMain)

        val list = ArrayList<BarData?>()

        // 20 items
        for (i in 0..19) {
            list.add(generateData(i + 1))
        }

        val cda = ChartDataAdapter(applicationContext, list)
        lv!!.setAdapter(cda)
    }

    private inner class ChartDataAdapter(context: Context, objects: MutableList<BarData?>) : ArrayAdapter<BarData?>(context, 0, objects) {
        @SuppressLint("InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val data = getItem(position)

            val holder: ViewHolder

            if (convertView == null) {
                holder = ViewHolder()

                convertView = LayoutInflater.from(context).inflate(
                    R.layout.list_item_barchart, null
                )
                holder.chart = convertView.findViewById(R.id.chart)

                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }

            // apply styling
            if (data != null) {
                data.setValueTypeface(tfLight)
                data.setValueTextColor(Color.BLACK)
            }
            holder.chart!!.description.isEnabled = false
            holder.chart!!.drawGridBackground = false

            val xAxis = holder.chart!!.xAxis
            xAxis.position = XAxisPosition.BOTTOM
            xAxis.typeface = tfLight
            xAxis.setDrawGridLines(false)

            val leftAxis = holder.chart!!.axisLeft
            leftAxis.typeface = tfLight
            leftAxis.setLabelCount(5, false)
            leftAxis.spaceTop = 15f

            val rightAxis = holder.chart!!.axisRight
            rightAxis.typeface = tfLight
            rightAxis.setLabelCount(5, false)
            rightAxis.spaceTop = 15f

            // set data
            holder.chart!!.setData(data)
            holder.chart!!.setFitBars(true)

            // do not forget to refresh the chart
//            holder.chart.invalidate();
            holder.chart!!.animateY(700)

            return convertView
        }

        private inner class ViewHolder {
            var chart: BarChart? = null
        }
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Bar data
     */
    private fun generateData(cnt: Int): BarData {
        val count = 12
        val entries = ArrayList<BarEntry>()
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            entries.add(BarEntry(i.toFloat(), (sampleValues[i].toFloat() * 70) + 30))
        }

        val d = BarDataSet(entries, "New DataSet $cnt")
        d.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        d.setBarShadowColor(Color.rgb(203, 203, 203))

        val sets = ArrayList<IBarDataSet>()
        sets.add(d)

        val cd = BarData(sets)
        cd.barWidth = 0.9f
        return cd
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_github, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.setData("https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/com/xxmassdeveloper/mpchartexample/ListViewBarChartActivity.java".toUri())
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
