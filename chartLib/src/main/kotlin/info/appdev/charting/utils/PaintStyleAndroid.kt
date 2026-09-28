package info.appdev.charting.utils

import android.graphics.Paint

/**
 * Converts the platform-independent [PaintStyle] used by the data model (e.g.
 * [info.appdev.charting.interfaces.datasets.ICandleDataSet]) to the `android.graphics.Paint.Style`
 * expected by the Android rendering pipeline.
 */
fun PaintStyle?.toAndroidPaintStyle(default: Paint.Style = Paint.Style.FILL): Paint.Style = when (this) {
    PaintStyle.FILL -> Paint.Style.FILL
    PaintStyle.STROKE -> Paint.Style.STROKE
    PaintStyle.FILL_AND_STROKE -> Paint.Style.FILL_AND_STROKE
    null -> default
}
