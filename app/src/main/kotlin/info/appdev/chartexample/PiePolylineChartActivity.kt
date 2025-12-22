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
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class PiePolylineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {
    private var chart: PieChart? = null
    private var seekBarX: SeekBar? = null
    private var seekBarY: SeekBar? = null
    private var tvX: TextView? = null
    private var tvY: TextView? = null

    private var tf: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piechart)

        tvX = findViewById(R.id.tvXMax)
        tvY = findViewById(R.id.tvYMax)

        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)

        seekBarX!!.setOnSeekBarChangeListener(this)
        seekBarY!!.setOnSeekBarChangeListener(this)

        chart = findViewById(R.id.chart1)
        chart!!.setUsePercentValues(true)
        chart!!.description.isEnabled = false
        chart!!.setExtraOffsets(5f, 10f, 5f, 5f)

        chart!!.setDragDecelerationFrictionCoef(0.95f)

        tf = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf")

        chart!!.setCenterTextTypeface(Typeface.createFromAsset(assets, "OpenSans-Light.ttf"))
        chart!!.centerText = generateCenterSpannableText()

        chart!!.setExtraOffsets(20f, 0f, 20f, 0f)

        chart!!.isDrawHoleEnabled = true
        chart!!.setHoleColor(Color.WHITE)

        chart!!.setTransparentCircleColor(Color.WHITE)
        chart!!.setTransparentCircleAlpha(110)

        chart!!.holeRadius = 58f
        chart!!.transparentCircleRadius = 61f

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
        seekBarY!!.progress = 100

        chart!!.animateY(1400, Easing.easeInOutQuad)

        // chart.spin(2000, 0, 360);
        val l = chart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = false
    }

    private fun setData(count: Int, range: Float) {
        val sampleValues = getValues(count)
        val entries = ArrayList<PieEntry>()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0..<count) {
            entries.add(PieEntry((sampleValues[i]!!.toFloat() * range) + range / 5, parties[i % parties.size]))
        }

        val dataSet = PieDataSet(entries, "Election Results")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        // add a lot of colors
        val colors = ArrayList<Int>()

        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)

        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)

        colors.add(ColorTemplate.holoBlue)

        dataSet.setColors(colors)


        //dataSet.setSelectionShift(0f);
        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.2f
        dataSet.valueLinePart2Length = 0.4f

        //dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.BLACK)
        data.setValueTypeface(tf)
        chart!!.setData(data)

        // undo all highlights
        chart!!.highlightValues(null)

        chart?.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pie, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/PiePolylineChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                chart!!.data!!.dataSets.forEach {
                    it?.isDrawValues = !it.isDrawValues
                }
                chart?.invalidate()
            }

            R.id.actionToggleHole -> {
                chart!!.isDrawHoleEnabled = !chart!!.isDrawHoleEnabled
                chart?.invalidate()
            }

            R.id.actionToggleMinAngles -> {
                if (chart!!.minAngleForSlices == 0f) chart!!.setMinAngleForSlices(36f)
                else chart!!.setMinAngleForSlices(0f)
                chart?.notifyDataSetChanged()
                chart?.invalidate()
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
                chart?.invalidate()
            }

            R.id.actionDrawCenter -> {
                chart!!.setDrawCenterText(!chart!!.isDrawCenterTextEnabled)
                chart?.invalidate()
            }

            R.id.actionToggleXValues -> {
                chart!!.setDrawEntryLabels(!chart!!.isDrawEntryLabelsEnabled)
                chart?.invalidate()
            }

            R.id.actionTogglePercent -> {
                chart!!.setUsePercentValues(!chart!!.isUsePercentValuesEnabled)
                chart?.invalidate()
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
                chart!!.spin(1000, chart!!.rotationAngle, chart!!.rotationAngle + 360, Easing.easeInOutCubic)
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
        saveToGallery(chart, "PiePolylineChartActivity")
    }

    private fun generateCenterSpannableText(): SpannableString {
        val s = SpannableString("AndroidChart\ndeveloped by AppDevNext")
        s.setSpan(RelativeSizeSpan(1.5f), 0, 12, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 12, s.length - 10, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 12, s.length - 10, 0)
        s.setSpan(RelativeSizeSpan(.65f), 12, s.length - 10, 0)
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 10, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.holoBlue), s.length - 10, s.length, 0)
        return s
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Value: ${entry.y}, xIndex: ${entry.x}, DataSet index: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
