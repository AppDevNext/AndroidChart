package info.appdev.charting.data

import android.graphics.Color
import androidx.annotation.ColorInt
import info.appdev.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet

/**
 * Baseclass of all DataSets for Bar-, Line-, Scatter- and CandleStickChart.
 */
abstract class BarLineScatterCandleBubbleDataSet<T, N_XAxis>(yVals: MutableList<T>, label: String) :
    DataSet<T, N_XAxis>(yVals, label), IBarLineScatterCandleBubbleDataSet<T, N_XAxis> where T : BaseEntry<N_XAxis>, N_XAxis : Number, N_XAxis : Comparable<N_XAxis> {
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
