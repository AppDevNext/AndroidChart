package com.github.mikephil.charting.animation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import com.github.mikephil.charting.animation.Easing.EasingFunction

/**
 * Object responsible for all animations in the Chart. Animations require API level 11.
 *
 * @author Philipp Jahoda
 * @author Mick Ashton
 */
open class ChartAnimator {
    /** object that is updated upon animation update  */
    private var mListener: AnimatorUpdateListener? = null

    /** The phase of drawn values on the y-axis. 0 - 1  */
    protected var mPhaseY: Float = 1f

    /** The phase of drawn values on the x-axis. 0 - 1  */
    protected var mPhaseX: Float = 1f

    constructor()

    constructor(listener: AnimatorUpdateListener?) {
        mListener = listener
    }

    private fun xAnimator(duration: Int, easing: EasingFunction?): ObjectAnimator {
        val animatorX = ObjectAnimator.ofFloat(this, "phaseX", 0f, 1f)
        animatorX.interpolator = easing
        animatorX.setDuration(duration.toLong())

        return animatorX
    }

    private fun yAnimator(duration: Int, easing: EasingFunction?): ObjectAnimator {
        val animatorY = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f)
        animatorY.interpolator = easing
        animatorY.setDuration(duration.toLong())

        return animatorY
    }

    /**
     * Animates values along the X axis, in a linear fashion.
     *
     * @param durationMillis animation duration
     */
    fun animateX(durationMillis: Int) {
        animateX(durationMillis, Easing.Linear)
    }

    /**
     * Animates values along the X axis.
     *
     * @param durationMillis animation duration
     * @param easing EasingFunction
     */
    fun animateX(durationMillis: Int, easing: EasingFunction?) {
        val animatorX = xAnimator(durationMillis, easing)
        animatorX.addUpdateListener(mListener)
        animatorX.start()
    }

    /**
     * Animates values along both the X and Y axes, in a linear fashion.
     *
     * @param durationMillisX animation duration along the X axis
     * @param durationMillisY animation duration along the Y axis
     */
    fun animateXY(durationMillisX: Int, durationMillisY: Int) {
        animateXY(durationMillisX, durationMillisY, Easing.Linear, Easing.Linear)
    }

    /**
     * Animates values along both the X and Y axes.
     *
     * @param durationMillisX animation duration along the X axis
     * @param durationMillisY animation duration along the Y axis
     * @param easing EasingFunction for both axes
     */
    fun animateXY(durationMillisX: Int, durationMillisY: Int, easing: EasingFunction?) {
        val xAnimator = xAnimator(durationMillisX, easing)
        val yAnimator = yAnimator(durationMillisY, easing)

        if (durationMillisX > durationMillisY) {
            xAnimator.addUpdateListener(mListener)
        } else {
            yAnimator.addUpdateListener(mListener)
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
    fun animateXY(
        durationMillisX: Int, durationMillisY: Int, easingX: EasingFunction?,
        easingY: EasingFunction?
    ) {
        val xAnimator = xAnimator(durationMillisX, easingX)
        val yAnimator = yAnimator(durationMillisY, easingY)

        if (durationMillisX > durationMillisY) {
            xAnimator.addUpdateListener(mListener)
        } else {
            yAnimator.addUpdateListener(mListener)
        }

        xAnimator.start()
        yAnimator.start()
    }

    /**
     * Animates values along the Y axis, in a linear fashion.
     *
     * @param durationMillis animation duration
     */
    fun animateY(durationMillis: Int) {
        animateY(durationMillis, Easing.Linear)
    }

    /**
     * Animates values along the Y axis.
     *
     * @param durationMillis animation duration
     * @param easing EasingFunction
     */
    fun animateY(durationMillis: Int, easing: EasingFunction?) {
        val animatorY = yAnimator(durationMillis, easing)
        animatorY.addUpdateListener(mListener)
        animatorY.start()
    }

    var phaseY: Float
        /**
         * Gets the Y axis phase of the animation.
         *
         * @return float value of [.mPhaseY]
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
         *
         * @return float value of [.mPhaseX]
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
