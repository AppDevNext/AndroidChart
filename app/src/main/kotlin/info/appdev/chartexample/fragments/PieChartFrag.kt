package info.appdev.chartexample.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import info.appdev.charting.charts.PieChart
import info.appdev.charting.components.Legend
import info.appdev.chartexample.R

class PieChartFrag : SimpleFragment() {
    private var chart: PieChart? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.frag_simple_pie, container, false)

        chart = v.findViewById(R.id.pieChart1)
        chart!!.description.isEnabled = false

        val tf = Typeface.createFromAsset(requireContext().assets, "OpenSans-Light.ttf")

        chart!!.setCenterTextTypeface(tf)
        chart!!.centerText = generateCenterText()
        chart!!.setCenterTextSize(10f)
        chart!!.setCenterTextTypeface(tf)

        // radius of the center hole in percent of maximum radius
        chart!!.holeRadius = 45f
        chart!!.transparentCircleRadius = 50f

        chart!!.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
        }

        chart!!.setData(generatePieData())

        return v
    }

    private fun generateCenterText(): SpannableString {
        val s = SpannableString("Revenues\nQuarters 2015")
        s.setSpan(RelativeSizeSpan(2f), 0, 8, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 8, s.length, 0)
        return s
    }

    companion object {
        fun newInstance(): Fragment {
            return PieChartFrag()
        }
    }
}
