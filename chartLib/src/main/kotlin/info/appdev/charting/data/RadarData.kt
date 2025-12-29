package info.appdev.charting.data

import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IRadarDataSet

/**
 * Data container for the RadarChart.
 */
class RadarData : ChartData<IRadarDataSet> {
    /**
     * Sets the labels that should be drawn around the RadarChart at the end of each web line.
     *
     * @param labels
     */
    var labels: MutableList<String>? = null

    constructor() : super()

    constructor(dataSets: MutableList<IRadarDataSet>) : super(dataSets)

    constructor(vararg dataSets: IRadarDataSet) : super(*dataSets)

    /**
     * Sets the labels that should be drawn around the RadarChart at the end of each web line.
     */
    fun setLabels(vararg labels: String) {
        this.labels = mutableListOf(*labels)
    }

    override fun getEntryForHighlight(highlight: Highlight): Entry? {
        return getDataSetByIndex(highlight.dataSetIndex)!!.getEntryForIndex(highlight.x.toInt())
    }
}
