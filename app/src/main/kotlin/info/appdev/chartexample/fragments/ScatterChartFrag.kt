package info.appdev.chartexample.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.chartexample.R
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.databinding.FragSimpleScatterBinding

class ScatterChartFrag : SimpleFragment() {
    private var _binding: FragSimpleScatterBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragSimpleScatterBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.scatterChart1.description.isEnabled = false

        val tf = Typeface.createFromAsset(requireContext().assets, "OpenSans-Light.ttf")

        val markerView = MyMarkerView(activity, R.layout.custom_marker_view)
        markerView.chartView = binding.scatterChart1 // For bounds control
        binding.scatterChart1.setMarker(markerView)

        binding.scatterChart1.setDrawGridBackground(false)
        binding.scatterChart1.setData(generateScatterData(6, 10000f))

        val xAxis = binding.scatterChart1.xAxis
        xAxis.isEnabled = true
        xAxis.position = XAxisPosition.BOTTOM

        val leftAxis = binding.scatterChart1.axisLeft
        leftAxis.typeface = tf

        val rightAxis = binding.scatterChart1.axisRight
        rightAxis.typeface = tf
        rightAxis.setDrawGridLines(false)

        binding.scatterChart1.legend.apply {
            isWordWrapEnabled = true
            typeface = tf
            formSize = 14f
            textSize = 9f
            // increase the space between legend & bottom and legend & content
            yOffset = 13f
        }

        binding.scatterChart1.extraBottomOffset = 16f

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): Fragment {
            return ScatterChartFrag()
        }
    }
}
