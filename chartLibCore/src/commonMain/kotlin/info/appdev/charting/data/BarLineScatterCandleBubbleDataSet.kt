package info.appdev.charting.data

import info.appdev.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import info.appdev.charting.utils.ColorTemplate

/**
 * Baseclass of all DataSets for Bar-, Line-, Scatter- and CandleStickChart.
 */
abstract class BarLineScatterCandleBubbleDataSet<T : BaseEntry<Float>>(yVals: MutableList<T>, label: String) :
    DataSet<T>(yVals, label), IBarLineScatterCandleBubbleDataSet<T> {
    /**
     * Sets the color that is used for drawing the highlight indicators.
     */
    override var highLightColor: Int = ColorTemplate.argb(255, 187, 115)

    protected fun copy(barLineScatterCandleBubbleDataSet: BarLineScatterCandleBubbleDataSet<*>) {
        super.copy((barLineScatterCandleBubbleDataSet as BaseDataSet<*>?)!!)
        barLineScatterCandleBubbleDataSet.highLightColor = this.highLightColor
    }
}
