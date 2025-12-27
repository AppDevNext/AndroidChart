package info.appdev.charting.utils

/**
 * returns an angle between 0.f < 360.f (not less than zero, less than 360)
 */
fun Float.getNormalizedAngle(): Float {
    var angle = this
    while (angle < 0f) {
        angle += 360f
    }

    return angle % 360f
}
