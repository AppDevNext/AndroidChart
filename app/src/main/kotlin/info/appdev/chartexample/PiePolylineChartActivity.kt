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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.mikephil.charting.animation.Easing
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
import info.appdev.chartexample.databinding.ActivityPiechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import timber.log.Timber

class PiePolylineChartActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private var tf: Typeface? = null

    private lateinit var binding: ActivityPiechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPiechartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)

        binding.chart1.setUsePercentValues(true)
        binding.chart1.description.isEnabled = false
        binding.chart1.setExtraOffsets(5f, 10f, 5f, 5f)

        binding.chart1.setDragDecelerationFrictionCoef(0.95f)

        tf = Typeface.createFromAsset(assets, "OpenSans-Regular.ttf")

        binding.chart1.setCenterTextTypeface(Typeface.createFromAsset(assets, "OpenSans-Light.ttf"))
        binding.chart1.centerText = generateCenterSpannableText()

        binding.chart1.setExtraOffsets(20f, 0f, 20f, 0f)

        binding.chart1.isDrawHoleEnabled = true
        binding.chart1.setHoleColor(Color.WHITE)

        binding.chart1.setTransparentCircleColor(Color.WHITE)
        binding.chart1.setTransparentCircleAlpha(110)

        binding.chart1.holeRadius = 58f
        binding.chart1.transparentCircleRadius = 61f

        binding.chart1.setDrawCenterText(true)

        binding.chart1.rotationAngle = 0f
        // enable rotation of the chart by touch
        binding.chart1.isRotationEnabled = true
        binding.chart1.isHighlightPerTapEnabled = true

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        binding.chart1.setOnChartValueSelectedListener(this)

        binding.seekBarX.progress = 4
        binding.seekBarY.progress = 100

        binding.chart1.animateY(1400, Easing.easeInOutQuad)

        // chart.spin(2000, 0, 360);
        val l = binding.chart1.legend
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
        binding.chart1.setData(data)

        // undo all highlights
        binding.chart1.highlightValues(null)

        binding.chart1.invalidate()
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
                binding.chart1.data!!.dataSets.forEach {
                    it?.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHole -> {
                binding.chart1.isDrawHoleEnabled = !binding.chart1.isDrawHoleEnabled
                binding.chart1.invalidate()
            }

            R.id.actionToggleMinAngles -> {
                if (binding.chart1.minAngleForSlices == 0f) binding.chart1.setMinAngleForSlices(36f)
                else binding.chart1.setMinAngleForSlices(0f)
                binding.chart1.notifyDataSetChanged()
                binding.chart1.invalidate()
            }

            R.id.actionToggleCurvedSlices -> {
                val toSet = !binding.chart1.isDrawRoundedSlicesEnabled || !binding.chart1.isDrawHoleEnabled
                binding.chart1.setDrawRoundedSlices(toSet)
                if (toSet && !binding.chart1.isDrawHoleEnabled) {
                    binding.chart1.isDrawHoleEnabled = true
                }
                if (toSet && binding.chart1.isDrawSlicesUnderHoleEnabled) {
                    binding.chart1.setDrawSlicesUnderHole(false)
                }
                binding.chart1.invalidate()
            }

            R.id.actionDrawCenter -> {
                binding.chart1.setDrawCenterText(!binding.chart1.isDrawCenterTextEnabled)
                binding.chart1.invalidate()
            }

            R.id.actionToggleXValues -> {
                binding.chart1.setDrawEntryLabels(!binding.chart1.isDrawEntryLabelsEnabled)
                binding.chart1.invalidate()
            }

            R.id.actionTogglePercent -> {
                binding.chart1.setUsePercentValues(!binding.chart1.isUsePercentValuesEnabled)
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
                binding.chart1.spin(1000, binding.chart1.rotationAngle, binding.chart1.rotationAngle + 360, Easing.easeInOutCubic)
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

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        binding.tvXMax.text = binding.seekBarX.progress.toString()
        binding.tvYMax.text = binding.seekBarY.progress.toString()

        setData(binding.seekBarX.progress, binding.seekBarY.progress.toFloat())
    }

    override fun saveToGallery() {
        saveToGallery(binding.chart1, "PiePolylineChartActivity")
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
