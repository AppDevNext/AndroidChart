package info.appdev.charting.utils

/**
 * Platform-independent replacement for `android.graphics.RectF`, covering the subset of the API
 * the chart rendering pipeline relies on.
 */
class RectF(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) {
    fun width(): Float = right - left

    fun height(): Float = bottom - top

    fun centerX(): Float = (left + right) / 2f

    fun centerY(): Float = (top + bottom) / 2f

    /**
     * Also enables the `rect[left, top, right] = bottom` indexed-assignment syntax used by
     * `ViewPortHandler`, since `operator fun set` supports both call styles.
     */
    operator fun set(left: Float, top: Float, right: Float, bottom: Float) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    fun set(other: RectF) {
        set(other.left, other.top, other.right, other.bottom)
    }

    fun setEmpty() {
        set(0f, 0f, 0f, 0f)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RectF) return false
        return left == other.left && top == other.top && right == other.right && bottom == other.bottom
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + top.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + bottom.hashCode()
        return result
    }

    override fun toString(): String = "RectF($left, $top, $right, $bottom)"
}
