package info.appdev.charting.utils

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewConfiguration

var metrics: DisplayMetrics? = null
var minimumFlingVelocity = 0
var maximumFlingVelocity = 0

fun Context.initUtils() {
    val viewConfiguration = ViewConfiguration.get(this)
    minimumFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
    maximumFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity

    metrics = this.resources.displayMetrics
}

fun getSDKInt() = Build.VERSION.SDK_INT

fun Context.convertDpToPixel(dp: Float) = dp * this.resources.displayMetrics.density
