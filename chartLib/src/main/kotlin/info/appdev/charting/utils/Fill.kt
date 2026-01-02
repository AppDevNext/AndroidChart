package info.appdev.charting.utils

import android.R.attr.endColor
import android.R.attr.startColor
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import kotlin.math.floor
import androidx.core.graphics.withClip

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
    @ColorInt
    private var mColor: Int? = null

    @ColorInt
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

    constructor(@ColorInt startColor: Int, @ColorInt endColor: Int) {
        this.type = Type.LINEAR_GRADIENT
        this.gradientColors = intArrayOf(startColor, endColor)
    }

    var color: Int?
        get() = mColor
        set(color) {
            this.mColor = color
            calculateFinalColor()
        }

    fun setGradientColors(@ColorInt startColor: Int, @ColorInt endColor: Int) {
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
        canvas: Canvas, paint: Paint,
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
                    canvas.withClip(left, top, right, bottom) {

                        canvas.drawColor(mFinalColor!!)

                    }
                } else {
                    // save
                    val previous = paint.style
                    val previousColor = paint.color

                    // set
                    paint.style = Paint.Style.FILL
                    paint.color = mFinalColor!!

                    canvas.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)

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

                canvas.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)
            }

            Type.DRAWABLE -> {
                if (drawable == null) {
                    return
                }

                drawable!!.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                drawable!!.draw(canvas)
            }
        }
    }

    fun fillPath(
        canvas: Canvas,
        path: Path,
        paint: Paint,
        clipRect: RectF?
    ) {
        when (this.type) {
            Type.EMPTY -> return

            Type.COLOR -> {
                if (mFinalColor == null) {
                    return
                }

                if (clipRect != null && this.isClipPathSupported) {
                    canvas.withClip(path) {

                        canvas.drawColor(mFinalColor!!)

                    }
                } else {
                    // save
                    val previous = paint.style
                    val previousColor = paint.color

                    // set
                    paint.style = Paint.Style.FILL
                    paint.color = mFinalColor!!

                    canvas.drawPath(path, paint)

                    // restore
                    paint.color = previousColor
                    paint.style = previous
                }
            }

            Type.LINEAR_GRADIENT -> {
                val gradient = LinearGradient(
                    0f,
                    0f,
                    canvas.width.toFloat(),
                    canvas.height.toFloat(),
                    this.gradientColors!!,
                    this.gradientPositions,
                    Shader.TileMode.MIRROR
                )

                paint.shader = gradient

                canvas.drawPath(path, paint)
            }

            Type.DRAWABLE -> {
                if (drawable == null) {
                    return
                }

                ensureClipPathSupported()

                val save = canvas.save()
                canvas.clipPath(path)

                drawable!!.setBounds(
                    if (clipRect == null) 0 else clipRect.left.toInt(),
                    if (clipRect == null) 0 else clipRect.top.toInt(),
                    if (clipRect == null) canvas.width else clipRect.right.toInt(),
                    if (clipRect == null) canvas.height else clipRect.bottom.toInt()
                )
                drawable!!.draw(canvas)

                canvas.restoreToCount(save)
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
