package info.appdev.charting.interfaces.datasets

import android.graphics.DashPathEffect
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.formatter.IFillFormatter

interface ILineDataSet : ILineRadarDataSet<Entry> {
    /**
     * Returns the drawing mode for this line dataset
     */
    var lineMode: LineDataSet.Mode

    /**
     * Returns the intensity of the cubic lines (the effect intensity).
     * Max = 1f = very cubic, Min = 0.05f = low cubic effect, Default: 0.2f
     */
    var cubicIntensity: Float

    val isDrawCubicEnabled: Boolean

    val isDrawSteppedEnabled: Boolean

    /**
     * Returns the size of the drawn circles.
     */
    var circleRadius: Float

    /**
     * Returns the hole radius of the drawn circles.
     */
    var circleHoleRadius: Float

    /**
     * Returns the color at the given index of the DataSet's circle-color array.
     * Performs a IndexOutOfBounds check by modulus.
     */
    fun getCircleColor(index: Int): Int

    /**
     * Returns the number of colors in this DataSet's circle-color array.
     */
    val circleColorCount: Int

    /**
     * Returns true if drawing circles for this DataSet is enabled, false if not
     */
    var isDrawCircles: Boolean

    /**
     * Returns the color of the inner circle (the circle-hole).
     */
    var circleHoleColor: Int

    /**
     * Returns true if drawing the circle-holes is enabled, false if not.
     */
    var isDrawCircleHoleEnabled: Boolean

    /**
     * Returns the DashPathEffect that is used for drawing the lines.
     */
    var dashPathEffect: DashPathEffect?

    /**
     * Returns true if the dashed-line effect is enabled, false if not.
     * If the DashPathEffect object is null, also return false here.
     */
    var isDashedLineEnabled: Boolean

    /**
     * Returns the IFillFormatter that is set for this DataSet.
     */
    var fillFormatter: IFillFormatter?
}