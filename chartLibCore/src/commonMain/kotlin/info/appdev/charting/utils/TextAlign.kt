package info.appdev.charting.utils

/**
 * Platform-independent replacement for `android.graphics.Paint.Align`.
 * Used by components (e.g. [info.appdev.charting.components.Description]) to describe
 * how text should be aligned, without depending on `android.graphics.Paint`.
 */
enum class TextAlign {
    LEFT,
    CENTER,
    RIGHT
}
