package info.appdev.charting.utils

import android.graphics.Paint

/**
 * Converts the platform-independent [TextAlign] used by the data model (e.g.
 * [info.appdev.charting.components.Description]) to the `android.graphics.Paint.Align`
 * expected by the Android rendering pipeline.
 */
fun TextAlign?.toAndroidAlign(default: Paint.Align = Paint.Align.LEFT): Paint.Align = when (this) {
    TextAlign.LEFT -> Paint.Align.LEFT
    TextAlign.CENTER -> Paint.Align.CENTER
    TextAlign.RIGHT -> Paint.Align.RIGHT
    null -> default
}
