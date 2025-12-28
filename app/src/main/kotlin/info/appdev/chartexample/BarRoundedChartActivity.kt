package info.appdev.chartexample

import android.os.Bundle
import info.appdev.chartexample.databinding.ActivityBarchartBinding

class BarRoundedChartActivity : BarChartActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chart!!.isOwnRoundedRendererUsed = false
    }
}
