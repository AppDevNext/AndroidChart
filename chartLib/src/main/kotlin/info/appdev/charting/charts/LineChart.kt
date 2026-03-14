package info.appdev.charting.charts

import android.content.Context
import android.util.AttributeSet
import info.appdev.charting.data.BarLineScatterCandleBubbleData
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.renderer.LineChartRenderer

open class LineChart : BarLineChartBase<BarLineScatterCandleBubbleData<*, *>>, LineDataProvider {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()
        dataRenderer = LineChartRenderer(this, mAnimator, viewPortHandler)
    }

    override var lineData: BarLineScatterCandleBubbleData<*, *>?
        get() = mData
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
            val lineData = lineData
            val numberOfPoints = lineData?.entryCount ?: 0

            // Min and max values...
            val yAxisValueFormatter = axisLeft.valueFormatter
            val minVal = yAxisValueFormatter?.getFormattedValue(lineData?.yMin ?: 0f, null)
            val maxVal = yAxisValueFormatter?.getFormattedValue(lineData?.yMax ?: 0f, null)

            // Data range...
            val xAxisValueFormatter = xAxis.valueFormatter
            val minRange = xAxisValueFormatter?.getFormattedValue(lineData?.xMin ?: 0f, null)
            val maxRange = xAxisValueFormatter?.getFormattedValue(lineData?.xMax ?: 0f, null)
            val pluralOrSingular = if (lineData.entryCount == 1) "entry" else "entries"
            return "The line chart has ${lineData.entryCount} $pluralOrSingular. " +
                    "The minimum value is $minVal and maximum value is $maxVal." +
                    "Data ranges from $minRange to $maxRange."
        }

}
