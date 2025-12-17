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
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.custom.RadarMarkerView
import info.appdev.chartexample.notimportant.DemoBase

class RadarChartActivity : DemoBase() {
    private var chart: RadarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radarchart)

        chart = findViewById(R.id.chart1)
        chart!!.setBackgroundColor(Color.rgb(60, 65, 82))

        chart!!.description.isEnabled = false

        chart!!.webLineWidth = 1f
        chart!!.webColor = Color.LTGRAY
        chart!!.webLineWidthInner = 1f
        chart!!.webColorInner = Color.LTGRAY
        chart!!.webAlpha = 100

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        val mv: MarkerView = RadarMarkerView(this, R.layout.radar_markerview)
        mv.chartView = chart // For bounds control
        chart!!.setMarker(mv) // Set the marker to the chart

        setData()

        chart!!.animateXY(1400, 1400, Easing.easeInOutQuad)

        val xAxis = chart!!.xAxis
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

        val yAxis = chart!!.yAxis
        yAxis.typeface = tfLight
        yAxis.setLabelCount(5, false)
        yAxis.textSize = 9f
        yAxis.setAxisMinimum(0f)
        yAxis.setAxisMaximum(80f)
        yAxis.setDrawLabels(false)

        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.typeface = tfLight
        l.xEntrySpace = 7f
        l.yEntrySpace = 5f
        l.textColor = Color.WHITE
    }

    private fun setData() {
        val mul = 80f
        val min = 20f
        val cnt = 5
        val sampleValues = getValues(cnt + 1)

        val entries1 = ArrayList<RadarEntry?>()
        val entries2 = ArrayList<RadarEntry?>()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0..<cnt) {
            val val1 = (sampleValues[i]!!.toFloat() * mul) + min
            entries1.add(RadarEntry(val1))

            val val2 = (sampleValues[i + 1]!!.toFloat() * mul) + min
            entries2.add(RadarEntry(val2))
        }

        val set1 = RadarDataSet(entries1, "Last Week")
        set1.setColor(Color.rgb(103, 110, 129))
        set1.setFillColor(Color.rgb(103, 110, 129))
        set1.setDrawFilled(true)
        set1.fillAlpha = 180
        set1.setLineWidth(2f)
        set1.isDrawHighlightCircleEnabled = true
        set1.setDrawHighlightIndicators(false)

        val set2 = RadarDataSet(entries2, "This Week")
        set2.setColor(Color.rgb(121, 162, 175))
        set2.setFillColor(Color.rgb(121, 162, 175))
        set2.setDrawFilled(true)
        set2.fillAlpha = 180
        set2.setLineWidth(2f)
        set2.isDrawHighlightCircleEnabled = true
        set2.setDrawHighlightIndicators(false)

        val sets = ArrayList<IRadarDataSet?>()
        sets.add(set1)
        sets.add(set2)

        val data = RadarData(sets)
        data.setValueTypeface(tfLight)
        data.setValueTextSize(8f)
        data.setDrawValues(false)
        data.setValueTextColor(Color.WHITE)

        chart!!.setData(data)
        val colorList: MutableList<Int?> = ArrayList()
        colorList.add(Color.rgb(222, 166, 111))
        colorList.add(Color.rgb(220, 206, 138))
        colorList.add(Color.rgb(243, 255, 192))
        colorList.add(Color.rgb(240, 255, 240))
        colorList.add(Color.rgb(250, 255, 250))
        chart!!.setLayerColorList(colorList)
        chart!!.invalidate()
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
                for (set in chart!!.data!!.dataSets) set.setDrawValues(!set.isDrawValues())

                chart!!.invalidate()
            }

            R.id.actionToggleHighlight -> {
                if (chart!!.data != null) {
                    chart!!.data!!.isHighlightEnabled = !chart!!.data!!.isHighlightEnabled()
                    chart!!.invalidate()
                }
            }

            R.id.actionToggleRotate -> {
                chart!!.isRotationEnabled = !chart!!.isRotationEnabled
                chart!!.invalidate()
            }

            R.id.actionToggleFilled -> {
                chart!!.data!!.dataSets.forEach { set ->
                    set.setDrawFilled(!set.isDrawFilledEnabled())
                }
                chart!!.invalidate()
            }

            R.id.actionToggleHighlightCircle -> {
                chart!!.data!!.dataSets.forEach { set ->
                    set.setDrawHighlightCircleEnabled(!set.isDrawHighlightCircleEnabled())
                }
                chart!!.invalidate()
            }

            R.id.actionToggleXLabels -> {
                chart!!.xAxis.isEnabled = !chart!!.xAxis.isEnabled
                chart!!.notifyDataSetChanged()
                chart!!.invalidate()
            }

            R.id.actionToggleYLabels -> {
                chart!!.yAxis.isEnabled = !chart!!.yAxis.isEnabled
                chart!!.invalidate()
            }

            R.id.animateX -> {
                chart!!.animateX(1400)
            }

            R.id.animateY -> {
                chart!!.animateY(1400)
            }

            R.id.animateXY -> {
                chart!!.animateXY(1400, 1400)
            }

            R.id.actionToggleSpin -> {
                chart!!.spin(2000, chart!!.rotationAngle, chart!!.rotationAngle + 360, Easing.easeInOutCubic)
            }

            R.id.actionSave -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery()
                } else {
                    requestStoragePermission(chart)
                }
            }
        }
        return true
    }

    override fun saveToGallery() {
        saveToGallery(chart, "RadarChartActivity")
    }
}
