package info.appdev.charting.utils

import kotlin.math.abs

/**
 * Platform-independent 3x3 affine/perspective transform matrix, API-compatible (in spirit) with
 * `android.graphics.Matrix`'s affine subset that the chart rendering pipeline relies on
 * (`postTranslate`, `postScale`, `setScale`, `setTranslate`, `postConcat`, `invert`, `mapPoints`,
 * `mapRect`, `getValues`/`setValues`).
 *
 * Values are stored row-major, matching Android's layout, so [getValues]/[setValues] round-trip
 * the same way existing `ViewPortHandler` buffer-manipulation code expects:
 * ```
 * [ MSCALE_X  MSKEW_X   MTRANS_X ]
 * [ MSKEW_Y   MSCALE_Y  MTRANS_Y ]
 * [ MPERSP_0  MPERSP_1  MPERSP_2 ]
 * ```
 */
class Matrix {
    private val values = FloatArray(9)

    init {
        reset()
    }

    fun reset() {
        for (i in 0..8) values[i] = if (i == MSCALE_X || i == MSCALE_Y || i == MPERSP_2) 1f else 0f
    }

    fun set(src: Matrix) {
        src.values.copyInto(values)
    }

    fun getValues(dst: FloatArray) {
        values.copyInto(dst)
    }

    fun setValues(src: FloatArray) {
        src.copyInto(values)
    }

    /**
     * Replaces this matrix with a pure translation.
     */
    fun setTranslate(dx: Float, dy: Float) {
        reset()
        values[MTRANS_X] = dx
        values[MTRANS_Y] = dy
    }

    /**
     * Replaces this matrix with a scale about the given pivot point.
     */
    fun setScale(sx: Float, sy: Float, px: Float = 0f, py: Float = 0f) {
        reset()
        values[MSCALE_X] = sx
        values[MSCALE_Y] = sy
        values[MTRANS_X] = px - sx * px
        values[MTRANS_Y] = py - sy * py
    }

    /**
     * Post-concatenates a translation onto this matrix: `this = translate(dx, dy) * this`.
     */
    fun postTranslate(dx: Float, dy: Float) {
        val t = Matrix()
        t.setTranslate(dx, dy)
        postConcat(t)
    }

    /**
     * Post-concatenates a scale about the given pivot onto this matrix.
     */
    fun postScale(sx: Float, sy: Float, px: Float = 0f, py: Float = 0f) {
        val s = Matrix()
        s.setScale(sx, sy, px, py)
        postConcat(s)
    }

    /**
     * Post-concatenates [other] onto this matrix: `this = other * this`, i.e. [other] is applied
     * *after* this matrix's existing transform when mapping a point.
     */
    fun postConcat(other: Matrix) {
        val result = multiply(other.values, this.values)
        result.copyInto(values)
    }

    /**
     * Transforms an array of points in place: `(x0, y0, x1, y1, ...)`.
     */
    fun mapPoints(pts: FloatArray) {
        var i = 0
        while (i + 1 < pts.size) {
            val x = pts[i]
            val y = pts[i + 1]

            val nx = values[MSCALE_X] * x + values[MSKEW_X] * y + values[MTRANS_X]
            val ny = values[MSKEW_Y] * x + values[MSCALE_Y] * y + values[MTRANS_Y]
            val w = values[MPERSP_0] * x + values[MPERSP_1] * y + values[MPERSP_2]

            if (w == 1f || w == 0f) {
                pts[i] = nx
                pts[i + 1] = ny
            } else {
                pts[i] = nx / w
                pts[i + 1] = ny / w
            }
            i += 2
        }
    }

    /**
     * Transforms [r] to the bounding box of its four transformed corners.
     */
    fun mapRect(r: RectF) {
        val pts = floatArrayOf(
            r.left, r.top,
            r.right, r.top,
            r.right, r.bottom,
            r.left, r.bottom
        )
        mapPoints(pts)

        var minX = pts[0]
        var maxX = pts[0]
        var minY = pts[1]
        var maxY = pts[1]
        var i = 2
        while (i < pts.size) {
            if (pts[i] < minX) minX = pts[i]
            if (pts[i] > maxX) maxX = pts[i]
            if (pts[i + 1] < minY) minY = pts[i + 1]
            if (pts[i + 1] > maxY) maxY = pts[i + 1]
            i += 2
        }
        r.left = minX
        r.top = minY
        r.right = maxX
        r.bottom = maxY
    }

    /**
     * Computes the inverse of this matrix into [dst]. Returns false (leaving [dst] unmodified)
     * if this matrix is not invertible.
     */
    fun invert(dst: Matrix): Boolean {
        val m = values
        val a = m[0]; val b = m[1]; val c = m[2]
        val d = m[3]; val e = m[4]; val f = m[5]
        val g = m[6]; val h = m[7]; val i = m[8]

        val det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g)
        if (abs(det) < 1e-12f) return false

        val invDet = 1f / det

        val r = FloatArray(9)
        r[0] = (e * i - f * h) * invDet
        r[1] = (c * h - b * i) * invDet
        r[2] = (b * f - c * e) * invDet
        r[3] = (f * g - d * i) * invDet
        r[4] = (a * i - c * g) * invDet
        r[5] = (c * d - a * f) * invDet
        r[6] = (d * h - e * g) * invDet
        r[7] = (b * g - a * h) * invDet
        r[8] = (a * e - b * d) * invDet

        r.copyInto(dst.values)
        return true
    }

    override fun toString(): String = "Matrix" + values.joinToString(prefix = "[", postfix = "]")

    companion object {
        const val MSCALE_X = 0
        const val MSKEW_X = 1
        const val MTRANS_X = 2
        const val MSKEW_Y = 3
        const val MSCALE_Y = 4
        const val MTRANS_Y = 5
        const val MPERSP_0 = 6
        const val MPERSP_1 = 7
        const val MPERSP_2 = 8

        /**
         * Standard row-major 3x3 matrix multiplication: `result = left * right`.
         */
        private fun multiply(left: FloatArray, right: FloatArray): FloatArray {
            val r = FloatArray(9)
            for (row in 0..2) {
                for (col in 0..2) {
                    var sum = 0f
                    for (k in 0..2) {
                        sum += left[row * 3 + k] * right[k * 3 + col]
                    }
                    r[row * 3 + col] = sum
                }
            }
            return r
        }
    }
}
