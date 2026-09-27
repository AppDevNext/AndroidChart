package info.appdev.charting.charts

import android.content.Context
import android.util.AttributeSet
import info.appdev.charting.data.ScatterData
import info.appdev.charting.interfaces.dataprovider.ScatterDataProvider
import info.appdev.charting.renderer.ScatterChartRenderer

/**
 * The ScatterChart. Draws dots, triangles, squares and custom shapes into the
 * Chart-View. CIRCLE and SCQUARE offer the best performance, TRIANGLE has the
 * worst performance.
 */
open class ScatterChart : BarLineChartBase<ScatterData>, ScatterDataProvider {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    override fun init() {
        super.init()

        dataRenderer = ScatterChartRenderer(this, mAnimator, viewPortHandler)

        xAxis.spaceMin = 0.5f
        xAxis.spaceMax = 0.5f
    }

    override val scatterData: ScatterData?
        get() = mData

    override val accessibilityDescription: String
        get() = "This is scatter chart"
}
