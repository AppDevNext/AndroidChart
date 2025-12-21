package info.appdev.chartexample

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.listviewitems.BarChartItem
import info.appdev.chartexample.listviewitems.ChartItem
import info.appdev.chartexample.listviewitems.LineChartItem
import info.appdev.chartexample.listviewitems.PieChartItem
import info.appdev.chartexample.notimportant.DemoBase
import androidx.core.net.toUri

/**
 * Demonstrates the use of charts inside a ListView. IMPORTANT: provide a
 * specific height attribute for the chart inside your ListView item
 */
class ListViewMultiChartActivity : DemoBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listview_chart)

        val lv = findViewById<ListView>(R.id.listViewMain)

        val list = ArrayList<ChartItem?>()

        // 30 items
        for (i in 0..29) {
            if (i % 3 == 0) {
                list.add(LineChartItem(generateDataLine(i + 1), applicationContext))
            } else if (i % 3 == 1) {
                list.add(BarChartItem(generateDataBar(i + 1), applicationContext))
            } else if (i % 3 == 2) {
                list.add(PieChartItem(generateDataPie(), applicationContext))
            }
        }

        val chartDataAdapter = ChartDataAdapter(applicationContext, list)
        lv.adapter = chartDataAdapter
    }

    /** adapter that supports 3 different item types  */
    private class ChartDataAdapter(context: Context, objects: MutableList<ChartItem?>) : ArrayAdapter<ChartItem>(context, 0, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getItem(position)!!.getView(position, convertView, context)!!
        }

        override fun getItemViewType(position: Int): Int {
            // return the views type
            val ci = getItem(position)
            return if (ci != null) ci.itemType else 0
        }

        override fun getViewTypeCount(): Int {
            return 3 // we have 3 different item-types
        }
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
    private fun generateDataLine(cnt: Int): LineData {
        val values1 = ArrayList<Entry>()
        val count = 12
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            values1.add(Entry(i.toFloat(), ((sampleValues[i]!!.toFloat() * 65).toInt() + 40).toFloat()))
        }

        val d1 = LineDataSet(values1, "New DataSet $cnt, (1)")
        d1.lineWidth = 2.5f
        d1.circleRadius = 4.5f
        d1.highLightColor = Color.rgb(244, 117, 117)
        d1.isDrawValues = false

        val values2 = ArrayList<Entry>()

        for (i in 0..<count) {
            values2.add(Entry(i.toFloat(), values1.get(i).y - 30))
        }

        val d2 = LineDataSet(values2, "New DataSet $cnt, (2)")
        d2.lineWidth = 2.5f
        d2.circleRadius = 4.5f
        d2.highLightColor = Color.rgb(244, 117, 117)
        d2.color = ColorTemplate.VORDIPLOM_COLORS[0]
        d2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
        d2.isDrawValues = false

        val sets = ArrayList<ILineDataSet?>()
        sets.add(d1)
        sets.add(d2)

        return LineData(sets)
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Bar data
     */
    private fun generateDataBar(cnt: Int): BarData {
        val count = 12
        val entries = ArrayList<BarEntry>()
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            entries.add(BarEntry(i.toFloat(), ((sampleValues[i]!!.toFloat() * 70).toInt() + 30).toFloat()))
        }

        val d = BarDataSet(entries, "New DataSet " + cnt)
        d.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        d.highLightAlpha = 255

        val cd = BarData(d)
        cd.barWidth = 0.9f
        return cd
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Pie data
     */
    private fun generateDataPie(): PieData {
        val cnt = 4
        val entries = ArrayList<PieEntry>()
        val sampleValues = getValues(cnt)

        for (i in 0..<cnt) {
            entries.add(PieEntry((sampleValues[i]!!.toFloat() * 70) + 30, "Quarter " + (i + 1)))
        }

        val d = PieDataSet(entries, "")

        // space between slices
        d.sliceSpace = 2f
        d.setColors(*ColorTemplate.VORDIPLOM_COLORS)

        return PieData(d)
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
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/ListViewMultiChartActivity.kt".toUri()
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
