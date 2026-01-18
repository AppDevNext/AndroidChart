package info.appdev.charting.data

import android.graphics.Color
import androidx.annotation.ColorInt
import info.appdev.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass of all DataSets for Bar-, Line-, Scatter- and CandleStickChart.
 */
abstract class BarLineScatterCandleBubbleDataSet<T, N>(yVals: MutableList<T>, label: String) :
    DataSet<T, N>(yVals, label), IBarLineScatterCandleBubbleDataSet<T, N> where T : BaseEntry<N>, N : Number, N : Comparable<N> {
    /**
     * Sets the color that is used for drawing the highlight indicators.
     */
    @ColorInt
    override var highLightColor: Int = Color.rgb(255, 187, 115)

    protected fun copy(barLineScatterCandleBubbleDataSet: BarLineScatterCandleBubbleDataSet<*, *>) {
        super.copy((barLineScatterCandleBubbleDataSet as BaseDataSet<*, *>?)!!)
        barLineScatterCandleBubbleDataSet.highLightColor = this.highLightColor
    }
}
