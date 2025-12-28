package info.appdev.chartexample

import android.os.Bundle

class BarRoundedChartActivity : BarChartActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chart!!.isOwnRoundedRendererUsed = false
    }
}
