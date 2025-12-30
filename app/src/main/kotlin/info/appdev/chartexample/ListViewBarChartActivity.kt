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
import android.widget.ArrayAdapter
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityListviewChartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.charts.BarChart
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.ColorTemplate

/**
 * Demonstrates the use of charts inside a ListView. IMPORTANT: provide a
 * specific height attribute for the chart inside your ListView item
 */
class ListViewBarChartActivity : DemoBase() {

    private lateinit var binding: ActivityListviewChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListviewChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = ArrayList<BarData>()

        // 20 items
        for (i in 0..19) {
            list.add(generateData(i + 1))
        }

        val chartDataAdapter = ChartDataAdapter(applicationContext, list)
        binding.listViewMain.adapter = chartDataAdapter
    }

    private inner class ChartDataAdapter(context: Context, objects: MutableList<BarData>) : ArrayAdapter<BarData>(context, 0, objects) {
        @SuppressLint("InflateParams")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val data = getItem(position)

            val holder: ViewHolder

            if (convertView == null) {
                holder = ViewHolder()

                convertView = LayoutInflater.from(context).inflate(R.layout.list_item_barchart, null)
                holder.chart = convertView.findViewById(R.id.chart)

                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }

            data?.setValueTypeface(tfLight)
            data?.setValueTextColor(Color.BLACK)
            holder.chart!!.description.isEnabled = false
            holder.chart?.setDrawGridBackground(false)


            holder.chart?.xAxis?.apply {
                this.position = XAxisPosition.BOTTOM
                typeface = tfLight
                setDrawGridLines(false)
            }

            holder.chart?.axisLeft?.apply {
                typeface = tfLight
                setLabelCount(5, false)
                spaceTop = 15f
            }

            holder.chart?.axisRight?.apply {
                typeface = tfLight
                setLabelCount(5, false)
                spaceTop = 15f
            }

            holder.chart?.data = data
            holder.chart?.setFitBars(true)

            // do not forget to refresh the chart
//            holder.chart.invalidate();
            holder.chart?.animateY(700)

            return convertView
        }

        private inner class ViewHolder {
            var chart: BarChart? = null
        }
    }

    /**
     * generates a random ChartData object with just one DataSet
     */
    private fun generateData(cnt: Int): BarData {
        val count = 12
        val entries = ArrayList<BarEntry>()
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            entries.add(BarEntry(i.toFloat(), (sampleValues[i]!!.toFloat() * 70) + 30))
        }

        val d = BarDataSet(entries, "New DataSet $cnt")
        d.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        d.barShadowColor = Color.rgb(203, 203, 203)

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
        if (item.itemId == R.id.viewGithub) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data =
                "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/ListViewBarChartActivity.kt".toUri()
            startActivity(i)
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
