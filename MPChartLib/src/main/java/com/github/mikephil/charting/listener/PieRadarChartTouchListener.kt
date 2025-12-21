package com.github.mikephil.charting.listener

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import com.github.mikephil.charting.charts.PieRadarChartBase
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.convertDpToPixel
import kotlin.math.abs

class PieRadarChartTouchListener(chart: PieRadarChartBase<*>?) : ChartTouchListener<PieRadarChartBase<*>?>(chart) {
    private val touchStartPoint: MPPointF = MPPointF.getInstance(0f, 0f)

    /**
     * the angle where the dragging started
     */
    private var startAngle = 0f

    private val velocitySamples: ArrayList<AngularVelocitySample> = ArrayList<AngularVelocitySample>()

    private var decelerationLastTime: Long = 0
    private var decelerationAngularVelocity = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (gestureDetector!!.onTouchEvent(event)) {
            return true
        }

        // if rotation by touch is enabled
        // TODO: Also check if the pie itself is being touched, rather than the entire chart area
        if (chart!!.isRotationEnabled) {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startAction(event)

                    stopDeceleration()

                    resetVelocity()

                    if (chart!!.isDragDecelerationEnabled) {
                        sampleVelocity(x, y)
                    }

                    setGestureStartAngle(x, y)
                    touchStartPoint.x = x
                    touchStartPoint.y = y
                }

                MotionEvent.ACTION_MOVE -> {
                    if (chart!!.isDragDecelerationEnabled) {
                        sampleVelocity(x, y)
                    }

                    if (touchMode == NONE
                        && (distance(x, touchStartPoint.x, y, touchStartPoint.y)
                                > 8f.convertDpToPixel())
                    ) {
                        lastGesture = ChartGesture.ROTATE
                        touchMode = ROTATE
                        chart!!.disableScroll()
                    } else if (touchMode == ROTATE) {
                        updateGestureRotation(x, y)
                        chart!!.invalidate()
                    }

                    endAction(event)
                }

                MotionEvent.ACTION_UP -> {
                    if (chart!!.isDragDecelerationEnabled) {
                        stopDeceleration()

                        sampleVelocity(x, y)

                        decelerationAngularVelocity = calculateVelocity()

                        if (decelerationAngularVelocity != 0f) {
                            decelerationLastTime = AnimationUtils.currentAnimationTimeMillis()

                            chart?.postInvalidateOnAnimation() // This causes computeScroll to fire, recommended for this by Google
                        }
                    }

                    chart!!.enableScroll()
                    touchMode = NONE

                    endAction(event)
                }
            }
        }

        return true
    }

    override fun onLongPress(me: MotionEvent) {
        lastGesture = ChartGesture.LONG_PRESS

        val onChartGestureListener = chart!!.onChartGestureListener

        onChartGestureListener?.onChartLongPressed(me)
    }

    override fun onSingleTapConfirmed(motionEvent: MotionEvent) = true

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        lastGesture = ChartGesture.SINGLE_TAP

        val onChartGestureListener = chart!!.onChartGestureListener

        onChartGestureListener?.onChartSingleTapped(e)

        if (!chart!!.isHighlightPerTapEnabled) {
            return false
        }

        val high = chart!!.getHighlightByTouchPoint(e.x, e.y)
        performHighlight(high, e)

        return true
    }

    private fun resetVelocity() {
        velocitySamples.clear()
    }

    private fun sampleVelocity(touchLocationX: Float, touchLocationY: Float) {
        val currentTime = AnimationUtils.currentAnimationTimeMillis()

        velocitySamples.add(AngularVelocitySample(currentTime, chart!!.getAngleForPoint(touchLocationX, touchLocationY)))

        // Remove samples older than our sample time - 1 seconds
        var i = 0
        var count = velocitySamples.size
        while (i < count - 2) {
            if (currentTime - velocitySamples[i].time > 1000) {
                velocitySamples.removeAt(0)
                i--
                count--
            } else {
                break
            }
            i++
        }
    }

    private fun calculateVelocity(): Float {
        if (velocitySamples.isEmpty()) {
            return 0f
        }

        val firstSample = velocitySamples[0]
        val lastSample = velocitySamples[velocitySamples.size - 1]

        // Look for a sample that's closest to the latest sample, but not the same, so we can deduce the direction
        var beforeLastSample = firstSample
        for (i in velocitySamples.indices.reversed()) {
            beforeLastSample = velocitySamples[i]
            if (beforeLastSample.angle != lastSample.angle) {
                break
            }
        }

        // Calculate the sampling time
        var timeDelta = (lastSample.time - firstSample.time) / 1000f
        if (timeDelta == 0f) {
            timeDelta = 0.1f
        }

        // Calculate clockwise/ccw by choosing two values that should be closest to each other,
        // so if the angles are two far from each other we know they are inverted "for sure"
        var clockwise = lastSample.angle >= beforeLastSample.angle
        if (abs(lastSample.angle - beforeLastSample.angle) > 270.0) {
            clockwise = !clockwise
        }

        // Now if the "gesture" is over a too big of an angle - then we know the angles are inverted, and we need to move them closer to each other from both sides of the 360.0 wrapping point
        if (lastSample.angle - firstSample.angle > 180.0) {
            firstSample.angle += 360.0.toFloat()
        } else if (firstSample.angle - lastSample.angle > 180.0) {
            lastSample.angle += 360.0.toFloat()
        }

        // The velocity
        var velocity = abs((lastSample.angle - firstSample.angle) / timeDelta)

        // Direction?
        if (!clockwise) {
            velocity = -velocity
        }

        return velocity
    }

    /**
     * sets the starting angle of the rotation, this is only used by the touch
     * listener, x and y is the touch position
     *
     * @param x
     * @param y
     */
    fun setGestureStartAngle(x: Float, y: Float) {
        startAngle = chart!!.getAngleForPoint(x, y) - chart!!.rawRotationAngle
    }

    /**
     * updates the view rotation depending on the given touch position, also
     * takes the starting angle into consideration
     *
     * @param x
     * @param y
     */
    fun updateGestureRotation(x: Float, y: Float) {
        chart!!.setRotationAngle(chart!!.getAngleForPoint(x, y) - startAngle)
    }

    /**
     * Sets the deceleration-angular-velocity to 0f
     */
    fun stopDeceleration() {
        decelerationAngularVelocity = 0f
    }

    fun computeScroll() {
        if (decelerationAngularVelocity == 0f) {
            return  // There's no deceleration in progress
        }

        val currentTime = AnimationUtils.currentAnimationTimeMillis()

        decelerationAngularVelocity *= chart!!.dragDecelerationFrictionCoef

        val timeInterval = (currentTime - decelerationLastTime).toFloat() / 1000f

        chart!!.setRotationAngle(chart!!.rotationAngle + decelerationAngularVelocity * timeInterval)

        decelerationLastTime = currentTime

        if (abs(decelerationAngularVelocity) >= 0.001) {
            chart?.postInvalidateOnAnimation() // This causes computeScroll to fire, recommended for this by Google
        } else {
            stopDeceleration()
        }
    }

    private class AngularVelocitySample(var time: Long, var angle: Float)
}
