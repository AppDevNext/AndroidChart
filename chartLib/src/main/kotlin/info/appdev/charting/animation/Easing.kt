package info.appdev.charting.animation

import android.animation.TimeInterpolator
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Easing options.
 */
object Easing {
    private const val DOUBLE_PI = 2f * Math.PI.toFloat()

    @Suppress("unused")
    val linear: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return input
        }
    }

    @Suppress("unused")
    val easeInQuad: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return input * input
        }
    }

    @Suppress("unused")
    val easeOutQuad: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return -input * (input - 2f)
        }
    }

    @Suppress("unused")
    val easeInOutQuad: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            input *= 2f

            if (input < 1f) {
                return 0.5f * input * input
            }

            return -0.5f * ((--input) * (input - 2f) - 1f)
        }
    }

    @Suppress("unused")
    val easeInCubic: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return input.toDouble().pow(3.0).toFloat()
        }
    }

    @Suppress("unused")
    val easeOutCubic: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            input--
            return input.toDouble().pow(3.0).toFloat() + 1f
        }
    }

    @Suppress("unused")
    val easeInOutCubic: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            input *= 2f
            if (input < 1f) {
                return 0.5f * input.toDouble().pow(3.0).toFloat()
            }
            input -= 2f
            return 0.5f * (input.toDouble().pow(3.0).toFloat() + 2f)
        }
    }

    @Suppress("unused")
    val easeInQuart: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return input.toDouble().pow(4.0).toFloat()
        }
    }

    @Suppress("unused")
    val easeOutQuart: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            input--
            return -(input.toDouble().pow(4.0).toFloat() - 1f)
        }
    }

    @Suppress("unused")
    val easeInOutQuart: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            input *= 2f
            if (input < 1f) {
                return 0.5f * input.toDouble().pow(4.0).toFloat()
            }
            input -= 2f
            return -0.5f * (input.toDouble().pow(4.0).toFloat() - 2f)
        }
    }

    @Suppress("unused")
    val easeInSine: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return -cos(input * (Math.PI / 2f)).toFloat() + 1f
        }
    }

    @Suppress("unused")
    val easeOutSine: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return sin(input * (Math.PI / 2f)).toFloat()
        }
    }

    @Suppress("unused")
    val easeInOutSine: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return -0.5f * (cos(Math.PI * input).toFloat() - 1f)
        }
    }

    @Suppress("unused")
    val easeInExpo: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return if (input == 0f) 0f else 2.0.pow((10f * (input - 1f)).toDouble()).toFloat()
        }
    }

    @Suppress("unused")
    val easeOutExpo: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return if (input == 1f) 1f else (-2.0.pow((-10f * (input + 1f)).toDouble()).toFloat())
        }
    }

    @Suppress("unused")
    val easeInOutExpo: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            if (input == 0f) {
                return 0f
            } else if (input == 1f) {
                return 1f
            }

            input *= 2f
            if (input < 1f) {
                return 0.5f * 2.0.pow((10f * (input - 1f)).toDouble()).toFloat()
            }
            return 0.5f * (-2.0.pow((-10f * --input).toDouble()).toFloat() + 2f)
        }
    }

    @Suppress("unused")
    val easeInCirc: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return -(sqrt((1f - input * input).toDouble()).toFloat() - 1f)
        }
    }

    @Suppress("unused")
    val easeOutCirc: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            input--
            return sqrt((1f - input * input).toDouble()).toFloat()
        }
    }

    @Suppress("unused")
    val easeInOutCirc: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            input *= 2f
            if (input < 1f) {
                return -0.5f * (sqrt((1f - input * input).toDouble()).toFloat() - 1f)
            }
            return 0.5f * (sqrt((1f - (2f.let { input -= it; input }) * input).toDouble()).toFloat() + 1f)
        }
    }

    @Suppress("unused")
    val easeInElastic: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            if (input == 0f) {
                return 0f
            } else if (input == 1f) {
                return 1f
            }

            val p = 0.3f
            val s = p / DOUBLE_PI * asin(1.0).toFloat()
            return -(2.0.pow((10f * (1f.let { input -= it; input })).toDouble()).toFloat() * sin(((input - s) * DOUBLE_PI / p).toDouble()).toFloat())
        }
    }

    @Suppress("unused")
    val easeOutElastic: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            if (input == 0f) {
                return 0f
            } else if (input == 1f) {
                return 1f
            }

            val p = 0.3f
            val s = p / DOUBLE_PI * asin(1.0).toFloat()
            return (1f + 2.0.pow((-10f * input).toDouble()).toFloat() * sin(((input - s) * DOUBLE_PI / p).toDouble()).toFloat())
        }
    }

    @Suppress("unused")
    val easeInOutElastic: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            if (input == 0f) {
                return 0f
            }

            input *= 2f
            if (input == 2f) {
                return 1f
            }

            val p = 1f / 0.45f
            val s = 0.45f / DOUBLE_PI * asin(1.0).toFloat()
            if (input < 1f) {
                return (-0.5f
                        * (2.0.pow((10f * (1f.let { input -= it; input })).toDouble())
                    .toFloat() * sin(((input * 1f - s) * DOUBLE_PI * p).toDouble()).toFloat()))
            }
            return 1f + (0.5f
                    * 2.0.pow((-10f * (1f.let { input -= it; input })).toDouble()).toFloat() * sin(((input * 1f - s) * DOUBLE_PI * p).toDouble()).toFloat())
        }
    }

    @Suppress("unused")
    val easeInBack: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            val s = 1.70158f
            return input * input * ((s + 1f) * input - s)
        }
    }

    @Suppress("unused")
    val easeOutBack: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            val s = 1.70158f
            input--
            return (input * input * ((s + 1f) * input + s) + 1f)
        }
    }

    @Suppress("unused")
    val easeInOutBack: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            var s = 1.70158f
            input *= 2f
            if (input < 1f) {
                return 0.5f * (input * input * (((1.525f.let { s *= it; s }) + 1f) * input - s))
            }
            return 0.5f * ((2f.let { input -= it; input }) * input * (((1.525f.let { s *= it; s }) + 1f) * input + s) + 2f)
        }
    }

    @Suppress("unused")
    val easeInBounce: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            return 1f - easeOutBounce.getInterpolation(1f - input)
        }
    }

    @Suppress("unused")
    val easeOutBounce: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            var input = input
            val s = 7.5625f
            if (input < (1f / 2.75f)) {
                return s * input * input
            } else if (input < (2f / 2.75f)) {
                return s * (1.5f / 2.75f.let { input -= it; input }) * input + 0.75f
            } else if (input < (2.5f / 2.75f)) {
                return s * (2.25f / 2.75f.let { input -= it; input }) * input + 0.9375f
            }
            return s * (2.625f / 2.75f.let { input -= it; input }) * input + 0.984375f
        }
    }

    @Suppress("unused")
    val easeInOutBounce: EasingFunction = object : EasingFunction {
        override fun getInterpolation(input: Float): Float {
            if (input < 0.5f) {
                return easeInBounce.getInterpolation(input * 2f) * 0.5f
            }
            return easeOutBounce.getInterpolation(input * 2f - 1f) * 0.5f + 0.5f
        }
    }

    interface EasingFunction : TimeInterpolator {
        override fun getInterpolation(input: Float): Float
    }
}
