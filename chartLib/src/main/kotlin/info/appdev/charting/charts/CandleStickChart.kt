package info.appdev.charting.charts

import android.content.Context
import android.util.AttributeSet
import info.appdev.charting.data.CandleData
import info.appdev.charting.interfaces.dataprovider.CandleDataProvider
import info.appdev.charting.renderer.CandleStickChartRenderer

/**
 * Financial chart type that draws candle-sticks (OHCL chart).
 */
class CandleStickChart : BarLineChartBase<CandleData>, CandleDataProvider {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mRenderer = CandleStickChartRenderer(this, mAnimator, viewPortHandler)

        xAxis.spaceMin = 0.5f
        xAxis.spaceMax = 0.5f
    }

    override val candleData: CandleData?
        get() = mData

    override val accessibilityDescription: String
        get() = "This is a candlestick chart"
}
