package info.appdev.chartexample.formatter

import info.appdev.charting.data.BaseEntry
import info.appdev.charting.formatter.IFillFormatter
import info.appdev.charting.interfaces.dataprovider.LineDataProvider
import info.appdev.charting.interfaces.datasets.ILineDataSet

@Suppress("unused")
class MyFillFormatter(private val fillPos: Float) : IFillFormatter {
    override fun getFillLinePosition(dataSet: ILineDataSet<out BaseEntry<Float>, Float>?, dataProvider: LineDataProvider): Float {
        // your logic could be here
        return fillPos
    }
}