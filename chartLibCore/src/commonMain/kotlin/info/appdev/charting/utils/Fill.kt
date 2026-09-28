package info.appdev.charting.utils

import kotlin.math.floor

/**
 * Portable, data-only description of a fill (solid color, linear gradient, or drawable-based).
 *
 * The actual Canvas/Paint/Drawable/LinearGradient-based drawing logic (`fillRect`/`fillPath`)
 * has no portable KMP equivalent, so it lives as Android-only extension functions in
 * `chartLib`'s `FillAndroid.kt`. The same applies to the `drawable` payload used by
 * [Type.DRAWABLE], which is exposed there as an extension property.
 */
open class Fill {
    enum class Type {
        EMPTY, COLOR, LINEAR_GRADIENT, DRAWABLE
    }

    enum class Direction {
        DOWN, UP, RIGHT, LEFT
    }

    /**
     * the type of fill
     */
    var type: Type = Type.EMPTY

    /**
     * the color that is used for filling
     */
    private var mColor: Int? = null

    private var mFinalColor: Int? = null

    var gradientColors: IntArray? = null

    var gradientPositions: FloatArray? = null

    /**
     * transparency used for filling
     */
    private var mAlpha = 255

    constructor(startColor: Int, endColor: Int) {
        this.type = Type.LINEAR_GRADIENT
        this.gradientColors = intArrayOf(startColor, endColor)
    }

    var color: Int?
        get() = mColor
        set(color) {
            this.mColor = color
            calculateFinalColor()
        }

    fun setGradientColors(startColor: Int, endColor: Int) {
        this.gradientColors = intArrayOf(startColor, endColor)
    }

    var alpha: Int
        get() = mAlpha
        set(alpha) {
            this.mAlpha = alpha
            calculateFinalColor()
        }

    /**
     * The final ARGB color to use for filling, combining [color] and [alpha].
     * Exposed so that the Android-only drawing extensions (`fillRect`/`fillPath`) can use it.
     */
    val finalColor: Int?
        get() = mFinalColor

    private fun calculateFinalColor() {
        if (mColor == null) {
            mFinalColor = null
        } else {
            val alpha = floor(((mColor!! shr 24) / 255.0) * (mAlpha / 255.0) * 255.0).toInt()
            mFinalColor = (alpha shl 24) or (mColor!! and 0xffffff)
        }
    }
}
