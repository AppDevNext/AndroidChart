package info.appdev.chartexample.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import info.appdev.chartexample.databinding.FragSimpleLineBinding

class ComplexityFragment : SimpleFragment() {

    private var _binding: FragSimpleLineBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragSimpleLineBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.lineChart1.description.isEnabled = false

        binding.lineChart1.setDrawGridBackground(false)

        binding.lineChart1.data = complexity
        binding.lineChart1.animateX(3000)

        val tf = Typeface.createFromAsset(requireContext().assets, "OpenSans-Light.ttf")

        binding.lineChart1.legend.apply {
            typeface = tf
        }

        val leftAxis = binding.lineChart1.axisLeft
        leftAxis.typeface = tf

        binding.lineChart1.axisRight.isEnabled = false

        val xAxis = binding.lineChart1.xAxis
        xAxis.isEnabled = false

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): Fragment {
            return ComplexityFragment()
        }
    }
}
