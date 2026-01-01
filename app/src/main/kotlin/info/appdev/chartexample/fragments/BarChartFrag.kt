package info.appdev.chartexample.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import info.appdev.chartexample.R
import info.appdev.chartexample.custom.MyMarkerView
import info.appdev.chartexample.databinding.FragSimpleBarBinding
import info.appdev.charting.charts.BarChart
import info.appdev.charting.listener.ChartTouchListener.ChartGesture
import info.appdev.charting.listener.OnChartGestureListener
import timber.log.Timber

class BarChartFrag : SimpleFragment(), OnChartGestureListener {
    private lateinit var chart: BarChart

    private var _binding: FragSimpleBarBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragSimpleBarBinding.inflate(inflater, container, false)
        val view = binding.root

        // create a new chart object
        chart = BarChart(requireActivity())
        chart.description.isEnabled = false
        chart.onChartGestureListener = this

        val mv = MyMarkerView(activity, R.layout.custom_marker_view)
        mv.chartView = chart // For bounds control
        chart.setMarker(mv)

        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)

        val tf = Typeface.createFromAsset(requireContext().assets, "OpenSans-Light.ttf")

        chart.data = generateBarData(1, 20000f)

        chart.legend.apply {
            typeface = tf
        }

        val leftAxis = chart.axisLeft
        leftAxis.typeface = tf
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        chart.axisRight.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.isEnabled = false

        // programmatically add the chart
        binding.parentLayout.addView(chart)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("START")
    }

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartGesture?) {
        Timber.i("END")
        chart.highlightValues(null)
    }

    override fun onChartLongPressed(me: MotionEvent) {
        Timber.i("Chart long pressed.")
    }

    override fun onChartDoubleTapped(me: MotionEvent) {
        Timber.i("Chart double-tapped.")
    }

    override fun onChartSingleTapped(me: MotionEvent) {
        Timber.i("Chart single-tapped.")
    }

    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent, velocityX: Float, velocityY: Float) {
        Timber.i("Chart fling. VelocityX: $velocityX, VelocityY: $velocityY")
    }

    override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
        Timber.i("ScaleX: $scaleX, ScaleY: $scaleY")
    }

    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
        Timber.i("dX: $dX, dY: $dY")
    }

    companion object {
        fun newInstance(): Fragment {
            return BarChartFrag()
        }
    }
}
