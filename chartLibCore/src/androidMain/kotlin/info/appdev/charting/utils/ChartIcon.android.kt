package info.appdev.charting.utils

/**
 * On Android, [ChartIcon] is literally an `android.graphics.drawable.Drawable`, so all
 * existing renderer/consumer code using `entry.icon` as a `Drawable` continues to compile
 * and behave exactly as before this abstraction was introduced.
 */
actual typealias ChartIcon = android.graphics.drawable.Drawable
