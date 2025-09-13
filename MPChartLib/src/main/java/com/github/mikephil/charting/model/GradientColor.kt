package com.github.mikephil.charting.model

import com.github.mikephil.charting.utils.Fill

/**
 * Deprecated. Use `Fill`
 */
@Deprecated("")
class GradientColor : Fill() {
    @get:Deprecated("")
    @set:Deprecated("")
    var startColor: Int
        /**
         * Deprecated. Use `Fill.getGradientColors()`
         */
        get() = gradientColors!![0]
        /**
         * Deprecated. Use `Fill.setGradientColors(...)`
         */
        set(startColor) {
            if (gradientColors == null || gradientColors!!.size != 2) {
                this.gradientColors = intArrayOf(
                    startColor,
                    if (gradientColors != null && gradientColors!!.size > 1)
                        gradientColors!![1]
                    else
                        0
                )
            } else {
                gradientColors!![0] = startColor
            }
        }

    @get:Deprecated("")
    @set:Deprecated("")
    var endColor: Int
        /**
         * Deprecated. Use `Fill.getGradientColors()`
         */
        get() = gradientColors!![1]
        /**
         * Deprecated. Use `Fill.setGradientColors(...)`
         */
        set(endColor) {
            if (gradientColors == null || gradientColors!!.size != 2) {
                this.gradientColors = intArrayOf(
                    if (gradientColors != null && gradientColors!!.isNotEmpty())
                        gradientColors!![0]
                    else
                        0,
                    endColor
                )
            } else {
                gradientColors!![1] = endColor
            }
        }
}
