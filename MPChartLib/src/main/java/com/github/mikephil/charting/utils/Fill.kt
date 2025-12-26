package com.github.mikephil.charting.utils

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.floor

open class Fill {
    enum class Type {
        EMPTY, COLOR, LINEAR_GRADIENT, DRAWABLE
    }

    enum class Direction {
        DOWN, UP, RIGHT, LEFT
    }

    /**
     * the type of fill
     */
    var type: Type = Type.EMPTY

    /**
     * the color that is used for filling
     */
    private var mColor: Int? = null

    private var mFinalColor: Int? = null

    /**
     * the drawable to be used for filling
     */
    protected var drawable: Drawable? = null

    var gradientColors: IntArray? = null

    var gradientPositions: FloatArray? = null

    /**
     * transparency used for filling
     */
    private var mAlpha = 255

    constructor(startColor: Int, endColor: Int) {
        this.type = Type.LINEAR_GRADIENT
        this.gradientColors = intArrayOf(startColor, endColor)
    }

    var color: Int?
        get() = mColor
        set(color) {
            this.mColor = color
            calculateFinalColor()
        }

    fun setGradientColors(startColor: Int, endColor: Int) {
        this.gradientColors = intArrayOf(startColor, endColor)
    }

    var alpha: Int
        get() = mAlpha
        set(alpha) {
            this.mAlpha = alpha
            calculateFinalColor()
        }

    private fun calculateFinalColor() {
        if (mColor == null) {
            mFinalColor = null
        } else {
            val alpha = floor(((mColor!! shr 24) / 255.0) * (mAlpha / 255.0) * 255.0).toInt()
            mFinalColor = (alpha shl 24) or (mColor!! and 0xffffff)
        }
    }

    fun fillRect(
        c: Canvas, paint: Paint,
        left: Float, top: Float, right: Float, bottom: Float,
        gradientDirection: Direction?, mRoundedBarRadius: Float
    ) {
        when (this.type) {
            Type.EMPTY -> return

            Type.COLOR -> {
                if (mFinalColor == null) {
                    return
                }

                if (this.isClipPathSupported) {
                    val save = c.save()

                    c.clipRect(left, top, right, bottom)
                    c.drawColor(mFinalColor!!)

                    c.restoreToCount(save)
                } else {
                    // save
                    val previous = paint.style
                    val previousColor = paint.color

                    // set
                    paint.style = Paint.Style.FILL
                    paint.color = mFinalColor!!

                    c.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)

                    // restore
                    paint.color = previousColor
                    paint.style = previous
                }
            }

            Type.LINEAR_GRADIENT -> {
                val gradient = LinearGradient(
                    (if (gradientDirection == Direction.RIGHT)
                        right
                    else
                        left)
                        .toInt().toFloat(),
                    (if (gradientDirection == Direction.UP)
                        bottom
                    else
                        top)
                        .toInt().toFloat(),
                    (when (gradientDirection) {
                        Direction.RIGHT -> left
                        Direction.LEFT -> right
                        else -> left
                    }).toInt().toFloat(),
                    (when (gradientDirection) {
                        Direction.UP -> top
                        Direction.DOWN -> bottom
                        else -> top
                    }).toInt().toFloat(),
                    this.gradientColors!!,
                    this.gradientPositions,
                    Shader.TileMode.MIRROR
                )

                paint.shader = gradient

                c.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)
            }

            Type.DRAWABLE -> {
                if (drawable == null) {
                    return
                }

                drawable!!.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                drawable!!.draw(c)
            }
        }
    }

    fun fillPath(
        c: Canvas, path: Path, paint: Paint,
        clipRect: RectF?
    ) {
        when (this.type) {
            Type.EMPTY -> return

            Type.COLOR -> {
                if (mFinalColor == null) {
                    return
                }

                if (clipRect != null && this.isClipPathSupported) {
                    val save = c.save()

                    c.clipPath(path)
                    c.drawColor(mFinalColor!!)

                    c.restoreToCount(save)
                } else {
                    // save
                    val previous = paint.style
                    val previousColor = paint.color

                    // set
                    paint.style = Paint.Style.FILL
                    paint.color = mFinalColor!!

                    c.drawPath(path, paint)

                    // restore
                    paint.color = previousColor
                    paint.style = previous
                }
            }

            Type.LINEAR_GRADIENT -> {
                val gradient = LinearGradient(
                    0f,
                    0f,
                    c.width.toFloat(),
                    c.height.toFloat(),
                    this.gradientColors!!,
                    this.gradientPositions,
                    Shader.TileMode.MIRROR
                )

                paint.shader = gradient

                c.drawPath(path, paint)
            }

            Type.DRAWABLE -> {
                if (drawable == null) {
                    return
                }

                ensureClipPathSupported()

                val save = c.save()
                c.clipPath(path)

                drawable!!.setBounds(
                    if (clipRect == null) 0 else clipRect.left.toInt(),
                    if (clipRect == null) 0 else clipRect.top.toInt(),
                    if (clipRect == null) c.width else clipRect.right.toInt(),
                    if (clipRect == null) c.height else clipRect.bottom.toInt()
                )
                drawable!!.draw(c)

                c.restoreToCount(save)
            }
        }
    }

    private val isClipPathSupported: Boolean
        get() = getSDKInt() >= 18

    private fun ensureClipPathSupported() {
        if (getSDKInt() < 18) {
            throw RuntimeException("Fill-drawables not (yet) supported below API level 18, this code was run on API level \${getSDKInt()}")
        }
    }
}
