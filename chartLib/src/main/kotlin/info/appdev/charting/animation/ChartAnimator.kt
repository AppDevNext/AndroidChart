package info.appdev.charting.animation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import androidx.annotation.RequiresApi
import info.appdev.charting.animation.Easing.EasingFunction

class ChartAnimator {
    /** object that is updated upon animation update  */
    private var animatorUpdateListener: AnimatorUpdateListener? = null

    /** The phase of drawn values on the y-axis. 0 - 1  */
    protected var mPhaseY: Float = 1f

    /** The phase of drawn values on the x-axis. 0 - 1  */
    protected var mPhaseX: Float = 1f

    constructor()

    @RequiresApi(11)
    constructor(listener: AnimatorUpdateListener?) {
        animatorUpdateListener = listener
    }

    @RequiresApi(11)
    private fun xAnimator(duration: Int, easing: EasingFunction?): ObjectAnimator {
        val animatorX = ObjectAnimator.ofFloat(this, "phaseX", 0f, 1f)
        animatorX.interpolator = easing
        animatorX.duration = duration.toLong()

        return animatorX
    }

    @RequiresApi(11)
    private fun yAnimator(duration: Int, easing: EasingFunction?): ObjectAnimator {
        val animatorY = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f)
        animatorY.interpolator = easing
        animatorY.duration = duration.toLong()

        return animatorY
    }

    /**
     * Animates values along the X axis, in a linear fashion.
     *
     * @param durationMillis animation duration
     */
    @RequiresApi(11)
    fun animateX(durationMillis: Int) {
        animateX(durationMillis, Easing.linear)
    }

    /**
     * Animates values along the X axis.
     *
     * @param durationMillis animation duration
     * @param easing EasingFunction
     */
    @RequiresApi(11)
    fun animateX(durationMillis: Int, easing: EasingFunction?) {
        val animatorX = xAnimator(durationMillis, easing)
        animatorX.addUpdateListener(animatorUpdateListener)
        animatorX.start()
    }

    /**
     * Animates values along both the X and Y axes, in a linear fashion.
     *
     * @param durationMillisX animation duration along the X axis
     * @param durationMillisY animation duration along the Y axis
     */
    @RequiresApi(11)
    fun animateXY(durationMillisX: Int, durationMillisY: Int) {
        animateXY(durationMillisX, durationMillisY, Easing.linear, Easing.linear)
    }

    /**
     * Animates values along both the X and Y axes.
     *
     * @param durationMillisX animation duration along the X axis
     * @param durationMillisY animation duration along the Y axis
     * @param easing EasingFunction for both axes
     */
    @RequiresApi(11)
    fun animateXY(durationMillisX: Int, durationMillisY: Int, easing: EasingFunction?) {
        val xAnimator = xAnimator(durationMillisX, easing)
        val yAnimator = yAnimator(durationMillisY, easing)

        if (durationMillisX > durationMillisY) {
            xAnimator.addUpdateListener(animatorUpdateListener)
        } else {
            yAnimator.addUpdateListener(animatorUpdateListener)
        }

        xAnimator.start()
        yAnimator.start()
    }

    /**
     * Animates values along both the X and Y axes.
     *
     * @param durationMillisX animation duration along the X axis
     * @param durationMillisY animation duration along the Y axis
     * @param easingX EasingFunction for the X axis
     * @param easingY EasingFunction for the Y axis
     */
    @RequiresApi(11)
    fun animateXY(
        durationMillisX: Int, durationMillisY: Int, easingX: EasingFunction?,
        easingY: EasingFunction?
    ) {
        val xAnimator = xAnimator(durationMillisX, easingX)
        val yAnimator = yAnimator(durationMillisY, easingY)

        if (durationMillisX > durationMillisY) {
            xAnimator.addUpdateListener(animatorUpdateListener)
        } else {
            yAnimator.addUpdateListener(animatorUpdateListener)
        }

        xAnimator.start()
        yAnimator.start()
    }

    /**
     * Animates values along the Y axis, in a linear fashion.
     *
     * @param durationMillis animation duration
     */
    @RequiresApi(11)
    fun animateY(durationMillis: Int) {
        animateY(durationMillis, Easing.linear)
    }

    /**
     * Animates values along the Y axis.
     *
     * @param durationMillis animation duration
     * @param easing EasingFunction
     */
    @RequiresApi(11)
    fun animateY(durationMillis: Int, easing: EasingFunction?) {
        val animatorY = yAnimator(durationMillis, easing)
        animatorY.addUpdateListener(animatorUpdateListener)
        animatorY.start()
    }

    var phaseY: Float
        /**
         * Gets the Y axis phase of the animation.
         */
        get() = mPhaseY
        /**
         * Sets the Y axis phase of the animation.
         *
         * @param phase float value between 0 - 1
         */
        set(phase) {
            var phase = phase
            if (phase > 1f) {
                phase = 1f
            } else if (phase < 0f) {
                phase = 0f
            }
            mPhaseY = phase
        }

    var phaseX: Float
        /**
         * Gets the X axis phase of the animation.
         */
        get() = mPhaseX
        /**
         * Sets the X axis phase of the animation.
         *
         * @param phase float value between 0 - 1
         */
        set(phase) {
            var phase = phase
            if (phase > 1f) {
                phase = 1f
            } else if (phase < 0f) {
                phase = 0f
            }
            mPhaseX = phase
        }
}
