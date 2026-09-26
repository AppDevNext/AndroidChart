package info.appdev.charting.utils

/**
 * Opaque, platform-specific icon/image handle that can be attached to a chart entry
 * (e.g. [BaseEntry.icon][info.appdev.charting.data.BaseEntry]).
 *
 * On Android this is a type alias for `android.graphics.drawable.Drawable`, so existing
 * Android renderer code that treats `entry.icon` as a `Drawable` keeps compiling and
 * behaving unchanged. Other platforms currently declare an empty placeholder until a
 * Compose Multiplatform icon representation (e.g. a `Painter`) is designed.
 */
expect abstract class ChartIcon
