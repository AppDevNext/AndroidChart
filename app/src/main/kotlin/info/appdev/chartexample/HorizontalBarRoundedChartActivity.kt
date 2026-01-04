package info.appdev.chartexample

import android.os.Bundle

class HorizontalBarRoundedChartActivity : HorizontalBarChartActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.chart1.isOwnRoundedRendererUsed = true
    }
}
