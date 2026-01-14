package info.appdev.charting.highlight

import info.appdev.charting.charts.PieChart
import info.appdev.charting.data.Entry

open class PieHighlighter(chart: PieChart) : PieRadarHighlighter<PieChart>(chart) {
    override fun getClosestHighlight(index: Int, x: Float, y: Float): Highlight? {
        val pieDataSet = chartPieRadar.data?.dataSets[0]

        val entry: Entry? = pieDataSet?.getEntryForIndex(index)

        entry?.let {
            return Highlight(index.toFloat(), entry.y, x, y, 0, pieDataSet.axisDependency)
        }
        return null
    }
}
