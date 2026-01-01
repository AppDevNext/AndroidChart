package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import info.appdev.charting.animation.Easing
import info.appdev.charting.components.AxisBase
import info.appdev.charting.components.Legend
import info.appdev.charting.components.MarkerView
import info.appdev.charting.data.RadarData
import info.appdev.charting.data.RadarDataSet
import info.appdev.charting.data.RadarEntry
import info.appdev.charting.formatter.IAxisValueFormatter
import info.appdev.charting.interfaces.datasets.IRadarDataSet
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.RadarMarkerView
import info.appdev.chartexample.databinding.ActivityRadarchartBinding
import info.appdev.chartexample.notimportant.DemoBase

class RadarChartActivity : DemoBase() {

    private lateinit var binding: ActivityRadarchartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRadarchartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.setBackgroundColor(Color.rgb(60, 65, 82))
        binding.chart1.description.isEnabled = false

        binding.chart1.webLineWidth = 1f
        binding.chart1.webColor = Color.LTGRAY
        binding.chart1.webLineWidthInner = 1f
        binding.chart1.webColorInner = Color.LTGRAY
        binding.chart1.webAlpha = 100

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv: MarkerView = RadarMarkerView(this, R.layout.radar_markerview)
        mv.chartView = binding.chart1 // For bounds control
        binding.chart1.setMarker(mv) // Set the marker to the chart

        setData()

        binding.chart1.animateXY(1400, 1400, Easing.easeInOutQuad)

        val xAxis = binding.chart1.xAxis
        xAxis.typeface = tfLight
        xAxis.textSize = 9f
        xAxis.yOffset = 0f
        xAxis.xOffset = 0f
        xAxis.valueFormatter = object : IAxisValueFormatter {
            private val mActivities: Array<String> = arrayOf("Burger", "Steak", "Salad", "Pasta", "Pizza")

            override fun getFormattedValue(value: Float, axis: AxisBase?): String {
                return mActivities[value.toInt() % mActivities.size]
            }
        }
        xAxis.textColor = Color.WHITE

        val yAxis = binding.chart1.yAxis
        yAxis.typeface = tfLight
        yAxis.setLabelCount(5, false)
        yAxis.textSize = 9f
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 80f
        yAxis.setDrawLabels(false)

        binding.chart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            typeface = tfLight
            xEntrySpace = 7f
            yEntrySpace = 5f
            textColor = Color.WHITE
        }
    }

    private fun setData() {
        val mul = 80f
        val min = 20f
        val cnt = 5
        val sampleValues = getValues(cnt + 1)

        val entries1 = ArrayList<RadarEntry>()
        val entries2 = ArrayList<RadarEntry>()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0..<cnt) {
            val val1 = (sampleValues[i]!!.toFloat() * mul) + min
            entries1.add(RadarEntry(val1))

            val val2 = (sampleValues[i + 1]!!.toFloat() * mul) + min
            entries2.add(RadarEntry(val2))
        }

        val set1 = RadarDataSet(entries1, "Last Week")
        set1.color = Color.rgb(103, 110, 129)
        set1.fillColor = Color.rgb(103, 110, 129)
        set1.isDrawFilledEnabled = true
        set1.fillAlpha = 180
        set1.lineWidth = 2f
        set1.isDrawHighlightCircleEnabled = true
        set1.setDrawHighlightIndicators(false)

        val set2 = RadarDataSet(entries2, "This Week")
        set2.color = Color.rgb(121, 162, 175)
        set2.fillColor = Color.rgb(121, 162, 175)
        set2.isDrawFilledEnabled = true
        set2.fillAlpha = 180
        set2.lineWidth = 2f
        set2.isDrawHighlightCircleEnabled = true
        set2.setDrawHighlightIndicators(false)

        val sets = ArrayList<IRadarDataSet>()
        sets.add(set1)
        sets.add(set2)

        val data = RadarData(sets)
        data.setValueTypeface(tfLight)
        data.setValueTextSize(8f)
        data.setDrawValues(false)
        data.setValueTextColor(Color.WHITE)

        binding.chart1.data = data
        val colorList: MutableList<Int> = ArrayList()
        colorList.add(Color.rgb(222, 166, 111))
        colorList.add(Color.rgb(220, 206, 138))
        colorList.add(Color.rgb(243, 255, 192))
        colorList.add(Color.rgb(240, 255, 240))
        colorList.add(Color.rgb(250, 255, 250))
        binding.chart1.layerColorList = colorList
        binding.chart1.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.radar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/RadarChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.data?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlight -> {
                binding.chart1.data?.let {
                    it.isHighlightEnabled = !it.isHighlightEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleRotate -> {
                binding.chart1.isRotationEnabled = !binding.chart1.isRotationEnabled
                binding.chart1.invalidate()
            }

            R.id.actionToggleFilled -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawFilledEnabled = !set.isDrawFilledEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHighlightCircle -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawHighlightCircleEnabled = !set.isDrawHighlightCircleEnabled
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleXLabels -> {
                binding.chart1.xAxis.isEnabled = !binding.chart1.xAxis.isEnabled
                binding.chart1.notifyDataSetChanged()
                binding.chart1.invalidate()
            }

            R.id.actionToggleYLabels -> {
                binding.chart1.yAxis.isEnabled = !binding.chart1.yAxis.isEnabled
                binding.chart1.invalidate()
            }

            R.id.animateX -> {
                binding.chart1.animateX(1400)
            }

            R.id.animateY -> {
                binding.chart1.animateY(1400)
            }

            R.id.animateXY -> {
                binding.chart1.animateXY(1400, 1400)
            }

            R.id.actionToggleSpin -> {
                binding.chart1.spin(2000, binding.chart1.rotationAngle, binding.chart1.rotationAngle + 360, Easing.easeInOutCubic)
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(binding.chart1)
                }
            }
        }
        return true
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "RadarChartActivity")
    }
}
