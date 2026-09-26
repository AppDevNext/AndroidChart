package info.appdev.charting.utils

/**
 * Class that holds predefined color integer arrays (e.g. ColorTemplate.VORDIPLOM_COLORS)
 * and convenience methods for loading colors from resources.
 */
object ColorTemplate {
    /**
     * an "invalid" color that indicates that no color is set
     */
    const val COLOR_NONE: Int = 0x00112233

    /**
     * this "color" is used for the Legend creation and indicates that the next
     * form should be skipped
     */
    const val COLOR_SKIP: Int = 0x00112234

    /**
     * THE COLOR THEMES ARE PREDEFINED (predefined color integer arrays), FEEL
     * FREE TO CREATE YOUR OWN WITH AS MANY DIFFERENT COLORS AS YOU WANT
     */
    val LIBERTY_COLORS: IntArray = intArrayOf(
        argb(207, 248, 246),
        argb(148, 212, 212),
        argb(136, 180, 187),
        argb(118, 174, 175),
        argb(42, 109, 130)
    )
    val JOYFUL_COLORS: IntArray = intArrayOf(
        argb(217, 80, 138),
        argb(254, 149, 7),
        argb(254, 247, 120),
        argb(106, 167, 134),
        argb(53, 194, 209)
    )
    val PASTEL_COLORS: IntArray = intArrayOf(
        argb(64, 89, 128),
        argb(149, 165, 124),
        argb(217, 184, 162),
        argb(191, 134, 134),
        argb(179, 48, 80)
    )
    val COLORFUL_COLORS: IntArray = intArrayOf(
        argb(193, 37, 82),
        argb(255, 102, 0),
        argb(245, 199, 0),
        argb(106, 150, 31),
        argb(179, 100, 53)
    )
    val VORDIPLOM_COLORS: IntArray = intArrayOf(
        argb(192, 255, 140),
        argb(255, 247, 140),
        argb(255, 208, 140),
        argb(140, 234, 255),
        argb(255, 140, 157)
    )
    val MATERIAL_COLORS: IntArray = intArrayOf(
        rgb("#2ecc71"),
        rgb("#f1c40f"),
        rgb("#e74c3c"),
        rgb("#3498db")
    )

    /**
     * Converts the given hex-color-string to rgb.
     */
    fun rgb(hex: String): Int {
        val color = hex.replace("#", "").toLong(16).toInt()
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = (color) and 0xFF
        return argb(r, g, b)
    }

    /**
     * Returns the Android ICS holo blue light color.
     */
    val holoBlue: Int
        get() = argb(51, 181, 229)

    /**
     * Sets the alpha component of the given color.
     * @param alpha 0 - 255
     */
    fun colorWithAlpha(color: Int, alpha: Int): Int {
        return (color and 0xffffff) or ((alpha and 0xff) shl 24)
    }

    /**
     * Turns an array of colors (integer color values) into an ArrayList of colors.
     */
    fun createColors(colors: IntArray): MutableList<Int> {
        val result: MutableList<Int> = ArrayList()
        for (i in colors) {
            result.add(i)
        }
        return result
    }

    /**
     * Builds a fully opaque ARGB color int from the given red/green/blue components (0-255),
     * platform-independent replacement for android.graphics.Color.rgb(r, g, b).
     */
    private fun argb(red: Int, green: Int, blue: Int): Int {
        return (0xFF shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF)
    }
}
