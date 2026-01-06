package info.appdev.chartexample

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import info.appdev.chartexample.databinding.ActivityGradientBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.formatter.IFillFormatter
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.interfaces.datasets.ILineDataSet
import info.appdev.charting.utils.Utils

class GradientActivity : DemoBase() {

    private lateinit var binding: ActivityGradientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Utils.init(this)

        // Minimal chart setup
        binding.chart.description.isEnabled = false
        binding.chart.legend.isEnabled = false
        binding.chart.axisRight.isEnabled = false
        binding.chart.setDrawGridBackground(false)

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
            isDrawCircles = false
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
                ): Float = binding.chart.axisLeft.axisMinimum
            }
        }

        binding.chart.axisLeft.axisMinimum = 0f
        binding.chart.data = LineData(dataSet)
        binding.chart.invalidate()
    }

    override fun saveToGallery() = Unit
}
