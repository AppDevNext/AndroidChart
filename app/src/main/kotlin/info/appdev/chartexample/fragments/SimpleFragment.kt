package info.appdev.chartexample.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.ScatterChart.ScatterShape
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.FileUtils
import info.appdev.chartexample.DataTools.Companion.getValues

abstract class SimpleFragment : Fragment() {
    private var tf: Typeface? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tf = Typeface.createFromAsset(requireContext().assets, "OpenSans-Regular.ttf")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @Suppress("SameParameterValue")
    protected fun generateBarData(dataSets: Int, range: Float): BarData {
        val count = 12
        val values = getValues(count)
        val sets = ArrayList<IBarDataSet>()

        for (i in 0..<dataSets) {
            val entries = ArrayList<BarEntry>()

            for (j in 0..<count) {
                entries.add(BarEntry(j.toFloat(), (values[j]!!.toFloat() * range) + range / 4))
            }

            val ds = BarDataSet(entries, getLabel(i))
            ds.setColors(*ColorTemplate.VORDIPLOM_COLORS)
            sets.add(ds)
        }

        val d = BarData(sets)
        d.setValueTypeface(tf)
        return d
    }

    @Suppress("SameParameterValue")
    protected fun generateScatterData(dataSets: Int, range: Float): ScatterData {
        val count = 100
        val values = getValues(count)
        val sets = ArrayList<IScatterDataSet?>()

        val shapes = ScatterShape.getAllDefaultShapes()

        for (i in 0..<dataSets) {
            val entries = ArrayList<Entry>()

            for (j in 0..<count) {
                entries.add(Entry(j.toFloat(), (values[j]!!.toFloat() * range) + range / 4))
            }

            val ds = ScatterDataSet(entries, getLabel(i))
            ds.scatterShapeSize = 12f
            ds.setScatterShape(shapes[i % shapes.size])
            ds.setColors(*ColorTemplate.COLORFUL_COLORS)
            ds.scatterShapeSize = 9f
            sets.add(ds)
        }

        val d = ScatterData(sets)
        d.setValueTypeface(tf)
        return d
    }

    /**
     * generates less data (1 DataSet, 4 values)
     * @return PieData
     */
    protected fun generatePieData(): PieData {
        val count = 4
        val values = getValues(count)
        val entries1 = ArrayList<PieEntry>()

        for (i in 0..<count) {
            entries1.add(PieEntry(((values[i]!!.toFloat() * 60) + 40), "Quarter " + (i + 1)))
        }

        val ds1 = PieDataSet(entries1, "Quarterly Revenues 2015")
        ds1.setColors(*ColorTemplate.VORDIPLOM_COLORS)
        ds1.sliceSpace = 2f
        ds1.setSingleValueTextColor(Color.WHITE)
        ds1.valueTextSize = 12f

        val d = PieData(ds1)
        d.setValueTypeface(tf)

        return d
    }

    protected fun generateLineData(): LineData {
        val sets = ArrayList<ILineDataSet>()
        val ds1 = LineDataSet(FileUtils.loadEntriesFromAssets(requireContext().assets, "sine.txt"), "Sine function")
        val ds2 = LineDataSet(FileUtils.loadEntriesFromAssets(requireContext().assets, "cosine.txt"), "Cosine function")

        ds1.lineWidth = 2f
        ds2.lineWidth = 2f

        ds1.isDrawCirclesEnabled = false
        ds2.isDrawCirclesEnabled = false

        ds1.color = ColorTemplate.VORDIPLOM_COLORS[0]
        ds2.color = ColorTemplate.VORDIPLOM_COLORS[1]

        // load DataSets from files in assets folder
        sets.add(ds1)
        sets.add(ds2)

        val d = LineData(sets)
        d.setValueTypeface(tf)
        return d
    }

    protected val complexity: LineData
        get() {
            val sets = ArrayList<ILineDataSet>()

            val ds1 = LineDataSet(FileUtils.loadEntriesFromAssets(requireContext().assets, "n.txt"), "O(n)")
            val ds2 = LineDataSet(FileUtils.loadEntriesFromAssets(requireContext().assets, "nlogn.txt"), "O(nlogn)")
            val ds3 = LineDataSet(FileUtils.loadEntriesFromAssets(requireContext().assets, "square.txt"), "O(n\u00B2)")
            val ds4 = LineDataSet(FileUtils.loadEntriesFromAssets(requireContext().assets, "three.txt"), "O(n\u00B3)")

            ds1.color = ColorTemplate.VORDIPLOM_COLORS[0]
            ds2.color = ColorTemplate.VORDIPLOM_COLORS[1]
            ds3.color = ColorTemplate.VORDIPLOM_COLORS[2]
            ds4.color = ColorTemplate.VORDIPLOM_COLORS[3]

            ds1.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0])
            ds2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[1])
            ds3.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[2])
            ds4.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[3])

            ds1.lineWidth = 2.5f
            ds1.circleRadius = 3f
            ds2.lineWidth = 2.5f
            ds2.circleRadius = 3f
            ds3.lineWidth = 2.5f
            ds3.circleRadius = 3f
            ds4.lineWidth = 2.5f
            ds4.circleRadius = 3f


            // load DataSets from files in assets folder
            sets.add(ds1)
            sets.add(ds2)
            sets.add(ds3)
            sets.add(ds4)

            val d = LineData(sets)
            d.setValueTypeface(tf)
            return d
        }

    private val labels: Array<String> = arrayOf("Company A", "Company B", "Company C", "Company D", "Company E", "Company F")

    private fun getLabel(i: Int): String {
        return labels[i]
    }
}
