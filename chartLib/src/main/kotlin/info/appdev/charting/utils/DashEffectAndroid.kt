package info.appdev.charting.utils

import android.graphics.DashPathEffect

/**
 * Converts a platform-independent [DashEffect] into an Android [DashPathEffect],
 * for use at Canvas/Paint drawing boundaries (e.g. `Paint.pathEffect`).
 */
fun DashEffect.toAndroidDashPathEffect(): DashPathEffect = DashPathEffect(intervals, phase)
