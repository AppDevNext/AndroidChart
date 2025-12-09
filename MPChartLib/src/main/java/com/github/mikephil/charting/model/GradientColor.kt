package com.github.mikephil.charting.model

import com.github.mikephil.charting.utils.Fill

@Deprecated("Use `Fill` instead")
class GradientColor : Fill() {
    @get:Deprecated("Use `Fill.getGradientColors()`")
    @set:Deprecated("Use `Fill.setGradientColors(...)`")
    var startColor: Int
        get() = gradientColors[0]
        set(startColor) {
            if (gradientColors == null || gradientColors.size != 2) {
                setGradientColors(
                    intArrayOf(
                        startColor,
                        if (gradientColors != null && gradientColors.size > 1)
                            gradientColors[1]
                        else
                            0
                    )
                )
            } else {
                gradientColors[0] = startColor
            }
        }

    @get:Deprecated("Use `Fill.getGradientColors()`")
    @set:Deprecated("Use `Fill.setGradientColors(...)`")
    var endColor: Int
        get() = gradientColors[1]
        set(endColor) {
            if (gradientColors == null || gradientColors.size != 2) {
                setGradientColors(
                    intArrayOf(
                        if (gradientColors != null && gradientColors.size > 0)
                            gradientColors[0]
                        else
                            0,
                        endColor
                    )
                )
            } else {
                gradientColors[1] = endColor
            }
        }
}
