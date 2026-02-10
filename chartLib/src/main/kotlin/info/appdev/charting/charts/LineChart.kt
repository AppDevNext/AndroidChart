package info.appdev.charting.charts

import android.content.Context
import android.util.AttributeSet
import info.appdev.charting.data.LineData
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.renderer.LineChartRenderer

open class LineChart : BarLineChartBase<LineData>, LineDataProvider {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()
        dataRenderer = LineChartRenderer(this, mAnimator, viewPortHandler)
    }

    override var lineData: LineData
        get() {
            return mData ?: run {
                LineData()
            }
        }
        set(value) {
            mData = value
            notifyDataSetChanged()
        }

    public override fun onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (dataRenderer != null && dataRenderer is LineChartRenderer) {
            (dataRenderer as LineChartRenderer).releaseBitmap()
        }
        super.onDetachedFromWindow()
    }

    override val accessibilityDescription: String
        get() {
            // Min and max values...
            val yAxisValueFormatter = axisLeft.valueFormatter
            val minVal = yAxisValueFormatter?.getFormattedValue(lineData.yMin, null)
            val maxVal = yAxisValueFormatter?.getFormattedValue(lineData.yMax, null)

            // Data range...
            val xAxisValueFormatter = xAxis.valueFormatter
            val minRange = xAxisValueFormatter?.getFormattedValue(lineData.xMin, null)
            val maxRange = xAxisValueFormatter?.getFormattedValue(lineData.xMax, null)
            val pluralOrSingular = if (lineData.entryCount == 1) "entry" else "entries"
            return "The line chart has ${lineData.entryCount} $pluralOrSingular. " +
                    "The minimum value is $minVal and maximum value is $maxVal." +
                    "Data ranges from $minRange to $maxRange."
        }

}
