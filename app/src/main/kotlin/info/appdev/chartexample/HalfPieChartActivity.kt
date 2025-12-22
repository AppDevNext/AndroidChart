package info.appdev.chartexample

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import androidx.core.net.toUri
import androidx.window.layout.WindowMetricsCalculator
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.databinding.ActivityPiechartHalfBinding
import info.appdev.chartexample.notimportant.DemoBase

class HalfPieChartActivity : DemoBase() {

    private lateinit var binding: ActivityPiechartHalfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPiechartHalfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chart1.setBackgroundColor(Color.WHITE)

        moveOffScreen()

        binding.chart1.setUsePercentValues(true)
        binding.chart1.description.isEnabled = false

        binding.chart1.setCenterTextTypeface(tfLight)
        binding.chart1.centerText = generateCenterSpannableText()

        binding.chart1.isDrawHoleEnabled = true
        binding.chart1.setHoleColor(Color.WHITE)

        binding.chart1.setTransparentCircleColor(Color.WHITE)
        binding.chart1.setTransparentCircleAlpha(110)

        binding.chart1.holeRadius = 58f
        binding.chart1.transparentCircleRadius = 61f

        binding.chart1.setDrawCenterText(true)

        binding.chart1.isRotationEnabled = false
        binding.chart1.isHighlightPerTapEnabled = true

        binding.chart1.setMaxAngle(180f) // HALF CHART
        binding.chart1.rotationAngle = 180f
        binding.chart1.setCenterTextOffset(0f, -20f)

        setData(100f)

        binding.chart1.animateY(1400, Easing.easeInOutQuad)

        val l = binding.chart1.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        // entry label styling
        binding.chart1.setEntryLabelColor(Color.WHITE)
        binding.chart1.setEntryLabelTypeface(tfRegular)
        binding.chart1.setEntryLabelTextSize(12f)
    }

    private fun setData(range: Float) {
        val count = 4
        val values = ArrayList<PieEntry>()
        val sampleValues = getValues(count)

        for (i in 0..<count) {
            values.add(PieEntry((sampleValues[i]!!.toFloat() * range) + range / 5, parties[i % parties.size]))
        }

        val dataSet = PieDataSet(values, "Election Results")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)

        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        data.setValueTypeface(tfLight)
        binding.chart1.setData(data)

        binding.chart1.invalidate()
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

    private fun moveOffScreen() {
        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
        val height = windowMetrics.bounds.height()

        val offset = (height * 0.65).toInt() /* percent to move */

        val rlParams = binding.chart1.layoutParams as RelativeLayout.LayoutParams
        rlParams.setMargins(0, 0, 0, -offset)
        binding.chart1.layoutParams = rlParams
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.only_github, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.viewGithub -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data =
                    "https://github.com/AppDevNext/AndroidChart/blob/master/app/src/main/java/info/appdev/chartexample/HalfPieChartActivity.kt".toUri()
                startActivity(i)
            }
        }

        return true
    }

    public override fun saveToGallery() { /* Intentionally left empty */
    }
}
