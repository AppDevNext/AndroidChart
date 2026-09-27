package info.appdev.charting.charts

/**
 * Predefined ScatterShapes that allow the specification of a shape a ScatterDataSet should be drawn with.
 * If a ScatterShape is specified for a ScatterDataSet, the required renderer is set.
 *
 * Extracted out of the (Android-only) `ScatterChart` class so that `ScatterDataSet.setScatterShape(...)`
 * can reference it from portable code; previously accessed as the nested `ScatterChart.ScatterShape`,
 * now a top-level type in the same package, so call sites use `ScatterShape.SQUARE` etc. directly.
 */
enum class ScatterShape(private val shapeIdentifier: String) {
    SQUARE("SQUARE"),
    CIRCLE("CIRCLE"),
    TRIANGLE("TRIANGLE"),
    CROSS("CROSS"),
    X("X"),
    CHEVRON_UP("CHEVRON_UP"),
    CHEVRON_DOWN("CHEVRON_DOWN");

    override fun toString(): String {
        return shapeIdentifier
    }

    companion object {
        val allDefaultShapes: Array<ScatterShape>
            get() = arrayOf(
                SQUARE,
                CIRCLE,
                TRIANGLE,
                CROSS,
                X,
                CHEVRON_UP,
                CHEVRON_DOWN
            )
    }
}
