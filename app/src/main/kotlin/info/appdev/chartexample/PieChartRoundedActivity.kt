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
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityPiechartBinding
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.charting.animation.Easing
import info.appdev.charting.components.Legend
import info.appdev.charting.data.Entry
import info.appdev.charting.data.PieData
import info.appdev.charting.data.PieDataSet
import info.appdev.charting.data.PieEntry
import info.appdev.charting.formatter.PercentFormatter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.renderer.PieChartRenderer
import info.appdev.charting.utils.ColorTemplate
import info.appdev.charting.utils.PointF
import timber.log.Timber

class PieChartRoundedActivity : DemoBase(), OnSeekBarChangeListener, OnChartValueSelectedListener {

    private lateinit var binding: ActivityPiechartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPiechartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.seekBarX.setOnSeekBarChangeListener(this)
        binding.seekBarY.setOnSeekBarChangeListener(this)
        binding.chart1.isUsePercentValues = true
        binding.chart1.description.isEnabled = false
        binding.chart1.setExtraOffsets(5f, 10f, 5f, 5f)

        binding.chart1.dragDecelerationFrictionCoef = 0.95f

        binding.chart1.setCenterTextTypeface(tfLight)
        binding.chart1.centerText = generateCenterSpannableText()

        binding.chart1.isDrawHole = true
        binding.chart1.setHoleColor(Color.TRANSPARENT)

        binding.chart1.setTransparentCircleColor(Color.TRANSPARENT)
        binding.chart1.setTransparentCircleAlpha(110)

        binding.chart1.holeRadius = 50f

        binding.chart1.transparentCircleRadius = 0f

        binding.chart1.isDrawCenterText = true

        binding.chart1.rotationAngle = 0f
        // enable rotation of the chart by touch
        binding.chart1.isRotationEnabled = true
        binding.chart1.isHighlightPerTapEnabled = true

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        binding.chart1.setOnChartValueSelectedListener(this)

        binding.seekBarX.progress = 4
        binding.seekBarY.progress = 10

        binding.chart1.animateY(1400, Easing.easeInOutQuad)

        // chart.spin(2000, 0, 360);
        binding.chart1.legend.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            xEntrySpace = 7f
            yEntrySpace = 0f
            yOffset = 0f
        }

        // entry label styling
        binding.chart1.setEntryLabelColor(Color.WHITE)
        binding.chart1.setEntryLabelTypeface(tfRegular)
        binding.chart1.setEntryLabelTextSize(12f)
    }

    private fun setData(count: Int, range: Float) {
        val entries = ArrayList<PieEntry>()
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

        dataSet.isDrawIcons = false

        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = PointF(0f, 40f)
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
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(tfLight)
        binding.chart1.data = data

        // undo all highlights
        binding.chart1.highlightValues(null)

        val renderer = binding.chart1.renderer as PieChartRenderer
        renderer.roundedCornerRadius = 30f
        dataSet.sliceSpace = renderer.roundedCornerRadius / 2

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
                i.data = "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/PieChartActivity.kt".toUri()
                startActivity(i)
            }

            R.id.actionToggleValues -> {
                binding.chart1.data?.dataSets?.forEach {
                    it.isDrawValues = !it.isDrawValues
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleIcons -> {
                binding.chart1.data?.dataSets?.forEach { set ->
                    set.isDrawIcons = !set.isDrawIcons
                }
                binding.chart1.invalidate()
            }

            R.id.actionToggleHole -> {
                binding.chart1.isDrawHole = !binding.chart1.isDrawHole
                binding.chart1.invalidate()
            }

            R.id.actionToggleMinAngles -> {
                if (binding.chart1.minAngleForSlices == 0f)
                    binding.chart1.minAngleForSlices = 36f
                else
                    binding.chart1.minAngleForSlices = 0f
                binding.chart1.notifyDataSetChanged()
                binding.chart1.invalidate()
            }

            R.id.actionToggleCurvedSlices -> {
                val toSet = !binding.chart1.isDrawRoundedSlices || !binding.chart1.isDrawHole
                binding.chart1.isDrawRoundedSlices = toSet
                if (toSet && !binding.chart1.isDrawHole) {
                    binding.chart1.isDrawHole = true
                }
                if (toSet && binding.chart1.isDrawSlicesUnderHole) {
                    binding.chart1.isDrawSlicesUnderHole = false
                }
                binding.chart1.invalidate()
            }

            R.id.actionDrawCenter -> {
                binding.chart1.isDrawCenterText = !binding.chart1.isDrawCenterText
                binding.chart1.invalidate()
            }

            R.id.actionToggleXValues -> {
                binding.chart1.isDrawEntryLabels = !binding.chart1.isDrawEntryLabels
                binding.chart1.invalidate()
            }

            R.id.actionTogglePercent -> {
                binding.chart1.isUsePercentValues = !binding.chart1.isUsePercentValues
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
        saveToGallery(binding.chart1, "PieChartActivity")
    }

    private fun generateCenterSpannableText(): SpannableString {
        val s = SpannableString("AndroidChart\ndeveloped by AppDevNext")
        s.setSpan(RelativeSizeSpan(1.7f), 0, 14, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 14, s.length - 15, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 14, s.length - 15, 0)
        s.setSpan(RelativeSizeSpan(.8f), 14, s.length - 15, 0)
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 14, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.holoBlue), s.length - 14, s.length, 0)
        return s
    }

    override fun onValueSelected(entry: Entry, highlight: Highlight) {
        Timber.i("Value: ${entry.y}, xIndex: ${entry.x}, DataSet index: ${highlight.dataSetIndex}")
    }

    override fun onNothingSelected() = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}
