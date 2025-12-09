package com.github.mikephil.charting.jobs

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.view.View
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

@SuppressLint("NewApi")
abstract class AnimatedViewPortJob(
    viewPortHandler: ViewPortHandler?,
    xValue: Float,
    yValue: Float,
    trans: Transformer?,
    v: View?,
    var xOrigin: Float,
    var yOrigin: Float,
    duration: Long
) : ViewPortJob(viewPortHandler, xValue, yValue, trans, v), AnimatorUpdateListener, Animator.AnimatorListener {
    protected var animator: ObjectAnimator = ObjectAnimator.ofFloat(this, "phase", 0f, 1f)

    var phase: Float = 0f

    init {
        animator.duration = duration
        animator.addUpdateListener(this)
        animator.addListener(this)
    }

    @SuppressLint("NewApi")
    override fun run() {
        animator.start()
    }

    abstract fun recycleSelf()

    protected fun resetAnimator() {
        animator.removeAllListeners()
        animator.removeAllUpdateListeners()
        animator.reverse()
        animator.addUpdateListener(this)
        animator.addListener(this)
    }

    override fun onAnimationStart(animation: Animator) {
    }

    override fun onAnimationEnd(animation: Animator) {
        try {
            recycleSelf()
        } catch (_: IllegalArgumentException) {
        }
    }

    override fun onAnimationCancel(animation: Animator) {
        try {
            recycleSelf()
        } catch (_: IllegalArgumentException) {
        }
    }

    override fun onAnimationRepeat(animation: Animator) = Unit

    override fun onAnimationUpdate(animation: ValueAnimator) = Unit
}
