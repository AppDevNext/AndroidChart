package info.appdev.charting.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatrixTest {

    private fun assertClose(expected: Float, actual: Float, tolerance: Float = 1e-4f) {
        assertTrue(kotlin.math.abs(expected - actual) < tolerance, "expected $expected but was $actual")
    }

    @Test
    fun reset_isIdentity() {
        val m = Matrix()
        val pts = floatArrayOf(3f, 4f)
        m.mapPoints(pts)
        assertClose(3f, pts[0])
        assertClose(4f, pts[1])
    }

    @Test
    fun postTranslate_shiftsPoints() {
        val m = Matrix()
        m.postTranslate(10f, -5f)
        val pts = floatArrayOf(1f, 1f)
        m.mapPoints(pts)
        assertClose(11f, pts[0])
        assertClose(-4f, pts[1])
    }

    @Test
    fun postScale_scalesAboutOrigin() {
        val m = Matrix()
        m.postScale(2f, 3f)
        val pts = floatArrayOf(2f, 2f)
        m.mapPoints(pts)
        assertClose(4f, pts[0])
        assertClose(6f, pts[1])
    }

    @Test
    fun postScale_scalesAboutPivot() {
        val m = Matrix()
        m.postScale(2f, 2f, 10f, 10f)
        val pts = floatArrayOf(10f, 10f)
        m.mapPoints(pts)
        // pivot point should be invariant under a scale about itself
        assertClose(10f, pts[0])
        assertClose(10f, pts[1])
    }

    @Test
    fun postTranslateThenPostScale_appliesInOrder() {
        // translate(+10,0) then scale(2,2): scaling is "post", so it applies to the
        // already-translated result -> point (0,0) -> (10,0) -> (20,0)
        val m = Matrix()
        m.postTranslate(10f, 0f)
        m.postScale(2f, 2f)
        val pts = floatArrayOf(0f, 0f)
        m.mapPoints(pts)
        assertClose(20f, pts[0])
        assertClose(0f, pts[1])
    }

    @Test
    fun setValuesAndGetValues_roundTrip() {
        val m = Matrix()
        m.postTranslate(3f, 4f)
        val buffer = FloatArray(9)
        m.getValues(buffer)

        val m2 = Matrix()
        m2.setValues(buffer)

        val pts = floatArrayOf(1f, 1f)
        m2.mapPoints(pts)
        assertClose(4f, pts[0])
        assertClose(5f, pts[1])
    }

    @Test
    fun invert_undoesTransform() {
        val m = Matrix()
        m.postScale(2f, 4f)
        m.postTranslate(3f, -1f)

        val inverse = Matrix()
        val success = m.invert(inverse)
        assertTrue(success)

        val original = floatArrayOf(5f, 7f)
        val pts = original.copyOf()
        m.mapPoints(pts)
        inverse.mapPoints(pts)

        assertClose(original[0], pts[0])
        assertClose(original[1], pts[1])
    }

    @Test
    fun mapRect_computesBoundingBoxAfterTransform() {
        val m = Matrix()
        m.postScale(2f, 3f)
        m.postTranslate(1f, 1f)

        val r = RectF(0f, 0f, 10f, 20f)
        m.mapRect(r)

        assertClose(1f, r.left)
        assertClose(1f, r.top)
        assertClose(21f, r.right)
        assertClose(61f, r.bottom)
    }

    @Test
    fun postConcat_composesInExpectedOrder() {
        val a = Matrix()
        a.postTranslate(5f, 0f)

        val b = Matrix()
        b.postScale(2f, 2f)

        // a.postConcat(b) => new = b * a, i.e. b applied after a
        a.postConcat(b)

        val pts = floatArrayOf(0f, 0f)
        a.mapPoints(pts)
        assertEquals(10f, pts[0])
        assertEquals(0f, pts[1])
    }

    @Test
    fun setScale_replacesExistingTransform() {
        val m = Matrix()
        m.postTranslate(100f, 100f)
        m.setScale(2f, 2f)

        val pts = floatArrayOf(3f, 3f)
        m.mapPoints(pts)
        assertClose(6f, pts[0])
        assertClose(6f, pts[1])
    }

    @Test
    fun setTranslate_replacesExistingTransform() {
        val m = Matrix()
        m.postScale(5f, 5f)
        m.setTranslate(2f, 3f)

        val pts = floatArrayOf(1f, 1f)
        m.mapPoints(pts)
        assertClose(3f, pts[0])
        assertClose(4f, pts[1])
    }
}
