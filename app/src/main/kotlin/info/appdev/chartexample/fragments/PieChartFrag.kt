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
import info.appdev.chartexample.databinding.FragSimplePieBinding
import info.appdev.charting.components.Legend

class PieChartFrag : SimpleFragment() {

    private var _binding: FragSimplePieBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragSimplePieBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.pieChart1.description.isEnabled = false

        val tf = Typeface.createFromAsset(requireContext().assets, "OpenSans-Light.ttf")

        binding.pieChart1.setCenterTextTypeface(tf)
        binding.pieChart1.centerText = generateCenterText()
        binding.pieChart1.setCenterTextSize(10f)
        binding.pieChart1.setCenterTextTypeface(tf)

        // radius of the center hole in percent of maximum radius
        binding.pieChart1.holeRadius = 45f
        binding.pieChart1.transparentCircleRadius = 50f

        binding.pieChart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
        }

        binding.pieChart1.data = generatePieData()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
