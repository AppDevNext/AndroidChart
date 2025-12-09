package info.appdev.chartexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase

class PieChartRoundedActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: PieChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piechart)

        title = "PieChartActivity"

        tvX = findViewById(R.id.tvXMax)
        tvY = this.findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)

        seekBarX!!.setOnSeekBarChangeListener(this)
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setUsePercentValues(true)
        chart!!.description.isEnabled = false
        chart!!.setExtraOffsets(5f, 10f, 5f, 5f)

        chart!!.setDragDecelerationFrictionCoef(0.95f)

        chart!!.setCenterTextTypeface(tfLight)
        chart!!.centerText = generateCenterSpannableText()

        chart!!.isDrawHoleEnabled = true
        chart!!.setHoleColor(Color.TRANSPARENT)

        chart!!.setTransparentCircleColor(Color.TRANSPARENT)
        chart!!.setTransparentCircleAlpha(110)

        chart!!.holeRadius = 50f

        chart!!.transparentCircleRadius = 0f

        chart!!.setDrawCenterText(true)

        chart!!.setRotationAngle(0f)
        // enable rotation of the chart by touch
        chart!!.isRotationEnabled = true
        chart!!.isHighlightPerTapEnabled = true

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        chart!!.setOnChartValueSelectedListener(this)

        seekBarX!!.progress = 4
        seekBarY!!.progress = 10

        chart!!.animateY(1400, Easing.EaseInOutQuad)

        // chart.spin(2000, 0, 360);
        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        // entry label styling
        chart!!.setEntryLabelColor(Color.WHITE)
        chart!!.setEntryLabelTypeface(tfRegular)
        chart!!.setEntryLabelTextSize(12f)
    }

    private fun setData(count: Int, range: Float) {
        val entries = ArrayList<PieEntry?>()
        val sampleValues = getValues(100)

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0..<count) {
            entries.add(
                PieEntry(
                    (sampleValues[i]!!.toFloat() * range) + range / 5,
                    parties[i % parties.size],
                    ResourcesCompat.getDrawable(resources, R.drawable.star, null)
                )
            )
        }

        val dataSet = PieDataSet(entries, "Election Results")

        dataSet.setDrawIcons(false)

        dataSet.setSliceSpace(3f)
        dataSet.setIconsOffset(MPPointF(0f, 40f))
        dataSet.selectionShift = 5f

        // add a lot of colors
        val colors = ArrayList<Int>()

        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)

        colors.add(ColorTemplate.getHoloBlue())

        dataSet.setColors(colors)

        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(tfLight)
        chart!!.setData(data)

        // undo all highlights
        chart!!.highlightValues(null)

        val renderer = chart!!.renderer as PieChartRenderer
        renderer.roundedCornerRadius = 30f
        dataSet.setSliceSpace(renderer.roundedCornerRadius / 2)

        chart!!.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pie, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/PieChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                for (set in chart!!.data!!.getDataSets()) set.setDrawValues(!set.isDrawValuesEnabled())

                chart!!.invalidate()
            }

            R.id.actionToggleIcons -> {
                for (set in chart!!.data!!.getDataSets()) set.setDrawIcons(!set.isDrawIconsEnabled())

                chart!!.invalidate()
            }

            R.id.actionToggleHole -> {
                chart!!.isDrawHoleEnabled = !chart!!.isDrawHoleEnabled
                chart!!.invalidate()
            }

            R.id.actionToggleMinAngles -> {
                if (chart!!.minAngleForSlices == 0f) chart!!.setMinAngleForSlices(36f)
                else chart!!.setMinAngleForSlices(0f)
                chart!!.notifyDataSetChanged()
                chart!!.invalidate()
            }

            R.id.actionToggleCurvedSlices -> {
                val toSet = !chart!!.isDrawRoundedSlicesEnabled || !chart!!.isDrawHoleEnabled
                chart!!.setDrawRoundedSlices(toSet)
                if (toSet && !chart!!.isDrawHoleEnabled) {
                    chart!!.isDrawHoleEnabled = true
                }
                if (toSet && chart!!.isDrawSlicesUnderHoleEnabled) {
                    chart!!.setDrawSlicesUnderHole(false)
                }
                chart!!.invalidate()
            }

            R.id.actionDrawCenter -> {
                chart!!.setDrawCenterText(!chart!!.isDrawCenterTextEnabled)
                chart!!.invalidate()
            }

            R.id.actionToggleXValues -> {
                chart!!.setDrawEntryLabels(!chart!!.isDrawEntryLabelsEnabled)
                chart!!.invalidate()
            }

            R.id.actionTogglePercent -> {
                chart!!.setUsePercentValues(!chart!!.isUsePercentValuesEnabled)
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
                chart!!.spin(1000, chart!!.rotationAngle, chart!!.rotationAngle + 360, Easing.EaseInOutCubic)
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

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvX!!.text = seekBarX!!.progress.toString()
        tvY!!.text = seekBarY!!.progress.toString()

        setData(seekBarX!!.progress, seekBarY!!.progress.toFloat())
    }

    override fun saveToGallery() {
        saveToGallery(chart, "PieChartActivity")
    }

    private fun generateCenterSpannableText(): SpannableString {
        val s = SpannableString("AndroidChart\ndeveloped by AppDevNext")
        s.setSpan(RelativeSizeSpan(1.7f), 0, 14, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 14, s.length - 15, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 14, s.length - 15, 0)
        s.setSpan(RelativeSizeSpan(.8f), 14, s.length - 15, 0)
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 14, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 14, s.length, 0)
        return s
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Log.i("VAL SELECTED", "Value: " + entry.y + ", index: " + highlight.x + ", DataSet index: " + highlight.dataSetIndex)
    }

    override fun onNothingSelected() {
        Log.i("PieChart", "nothing selected")
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
