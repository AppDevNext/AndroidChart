package info.appdev.chartexample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.charts.LineChart
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.formatter.IFillFormatter
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.utils.Utils

class GradientActivity : DemoBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gradient)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Utils.init(this)

        val chart: LineChart = findViewById(R.id.chart)

        // Minimal chart setup
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.setDrawGridBackground(false)

        // -----------------------------
        // Linear function
        // y = k * x
        // -----------------------------
        val entries = kotlin.collections.ArrayList<Entry>(200)
        val k = 2.5f

        for (i in 0 until 200) {
            entries.add(Entry(i.toFloat(), i * k))
        }

        val dataSet = LineDataSet(entries, "Linear").apply {
            isDrawValues = false
            isDrawCirclesEnabled = false
            lineWidth = 2f

            isDrawFilledEnabled = true
            fillAlpha = 255
            fillDrawable = ContextCompat.getDrawable(
                this@GradientActivity,
                R.drawable.gradient_drawable_precipitation
            )

            fillFormatter = object : IFillFormatter {
                override fun getFillLinePosition(
                    dataSet: ILineDataSet?,
                    dataProvider: LineDataProvider
                ): Float = chart.axisLeft.axisMinimum
            }
        }

        chart.axisLeft.axisMinimum = 0f
        chart.setData(LineData(dataSet))
        chart.invalidate()
    }

    override fun saveToGallery() = Unit
}
