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
    protected var mDrawable: Drawable? = null

    var gradientColors: IntArray?

    val gradientPositions: FloatArray?

    /**
     * transparency used for filling
     */
    private var mAlpha = 255

    constructor() {
        gradientColors = null
        gradientPositions = null
    }

    constructor(color: Int) {
        this.type = Type.COLOR
        this.mColor = color
        this.gradientColors = null
        this.gradientPositions = null
        calculateFinalColor()
    }

    constructor(startColor: Int, endColor: Int) {
        this.type = Type.LINEAR_GRADIENT
        this.gradientColors = intArrayOf(startColor, endColor)
        this.gradientPositions = null
    }

    constructor(gradientColors: IntArray) {
        this.type = Type.LINEAR_GRADIENT
        this.gradientColors = gradientColors
        this.gradientPositions = null
    }

    constructor(gradientColors: IntArray, gradientPositions: FloatArray) {
        this.type = Type.LINEAR_GRADIENT
        this.gradientColors = gradientColors
        this.gradientPositions = gradientPositions
    }

    constructor(drawable: Drawable) {
        this.type = Type.DRAWABLE
        this.mDrawable = drawable
        this.gradientColors = null
        this.gradientPositions = null
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
                    val previous = paint.getStyle()
                    val previousColor = paint.getColor()

                    // set
                    paint.setStyle(Paint.Style.FILL)
                    paint.setColor(mFinalColor!!)

                    c.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)

                    // restore
                    paint.setColor(previousColor)
                    paint.setStyle(previous)
                }
            }

            Type.LINEAR_GRADIENT -> {
                if (this.gradientColors == null) {
                    return
                }

                val gradient = LinearGradient(
                    (if (gradientDirection == Direction.RIGHT)
                        right
                    else
                        if (gradientDirection == Direction.LEFT)
                            left
                        else
                            left).toInt().toFloat(),
                    (if (gradientDirection == Direction.UP)
                        bottom
                    else
                        if (gradientDirection == Direction.DOWN)
                            top
                        else
                            top).toInt().toFloat(),
                    (if (gradientDirection == Direction.RIGHT)
                        left
                    else
                        if (gradientDirection == Direction.LEFT)
                            right
                        else
                            left).toInt().toFloat(),
                    (if (gradientDirection == Direction.UP)
                        top
                    else
                        if (gradientDirection == Direction.DOWN)
                            bottom
                        else
                            top).toInt().toFloat(),
                    this.gradientColors!!,
                    this.gradientPositions,
                    Shader.TileMode.MIRROR
                )

                paint.setShader(gradient)

                c.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)
            }

            Type.DRAWABLE -> {
                if (mDrawable == null) {
                    return
                }

                mDrawable!!.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                mDrawable!!.draw(c)
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
                    val previous = paint.getStyle()
                    val previousColor = paint.getColor()

                    // set
                    paint.setStyle(Paint.Style.FILL)
                    paint.setColor(mFinalColor!!)

                    c.drawPath(path, paint)

                    // restore
                    paint.setColor(previousColor)
                    paint.setStyle(previous)
                }
            }

            Type.LINEAR_GRADIENT -> {
                if (this.gradientColors == null) {
                    return
                }

                val gradient = LinearGradient(
                    0f,
                    0f,
                    c.getWidth().toFloat(),
                    c.getHeight().toFloat(),
                    this.gradientColors!!,
                    this.gradientPositions,
                    Shader.TileMode.MIRROR
                )

                paint.setShader(gradient)

                c.drawPath(path, paint)
            }

            Type.DRAWABLE -> {
                if (mDrawable == null) {
                    return
                }

                ensureClipPathSupported()

                val save = c.save()
                c.clipPath(path)

                mDrawable!!.setBounds(
                    if (clipRect == null) 0 else clipRect.left.toInt(),
                    if (clipRect == null) 0 else clipRect.top.toInt(),
                    if (clipRect == null) c.getWidth() else clipRect.right.toInt(),
                    if (clipRect == null) c.getHeight() else clipRect.bottom.toInt()
                )
                mDrawable!!.draw(c)

                c.restoreToCount(save)
            }
        }
    }

    private val isClipPathSupported: Boolean
        get() = Utils.sDKInt >= 18

    private fun ensureClipPathSupported() {
        if (Utils.sDKInt < 18) {
            throw RuntimeException(
                "Fill-drawables not (yet) supported below API level 18, " +
                        "this code was run on API level " + Utils.sDKInt + "."
            )
        }
    }
}
