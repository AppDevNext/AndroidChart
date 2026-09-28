package info.appdev.charting.utils

/**
 * Converts the platform-independent [Matrix] to an `android.graphics.Matrix`, for the few call
 * sites that must hand a matrix to native Android APIs (e.g. `Path.transform(Matrix)`).
 */
fun Matrix.toAndroidMatrix(): android.graphics.Matrix {
    val values = FloatArray(9)
    getValues(values)
    val androidMatrix = android.graphics.Matrix()
    androidMatrix.setValues(values)
    return androidMatrix
}

/**
 * Converts an `android.graphics.Matrix` to the platform-independent [Matrix].
 */
fun android.graphics.Matrix.toCommonMatrix(): Matrix {
    val values = FloatArray(9)
    getValues(values)
    val commonMatrix = Matrix()
    commonMatrix.setValues(values)
    return commonMatrix
}
