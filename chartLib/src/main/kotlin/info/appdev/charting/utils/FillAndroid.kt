package info.appdev.charting.utils

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import java.util.WeakHashMap

/**
 * Android-only drawing logic and [Drawable] payload for the portable [Fill] class.
 *
 * `Canvas`/`Paint`/`Drawable`/`LinearGradient` have no portable KMP equivalent, so this stays
 * Android-only, mirroring the [Fill] side-channel pattern already used for
 * `ILineRadarDataSet.fillDrawable` (see `LineRadarDataSetAndroid.kt`).
 */
private val fillDrawables = WeakHashMap<Fill, Drawable?>()

/**
 * the drawable to be used for filling when [Fill.type] is [Fill.Type.DRAWABLE]
 */
var Fill.drawable: Drawable?
    get() = fillDrawables[this]
    set(value) {
        fillDrawables[this] = value
    }

private val Fill.isClipPathSupported: Boolean
    get() = getSDKInt() >= 18

private fun Fill.ensureClipPathSupported() {
    if (getSDKInt() < 18) {
        throw RuntimeException("Fill-drawables not (yet) supported below API level 18, this code was run on API level \${getSDKInt()}")
    }
}

fun Fill.fillRect(
    canvas: Canvas, paint: Paint,
    left: Float, top: Float, right: Float, bottom: Float,
    gradientDirection: Fill.Direction?, mRoundedBarRadius: Float
) {
    when (this.type) {
        Fill.Type.EMPTY -> return

        Fill.Type.COLOR -> {
            val finalColor = this.finalColor ?: return

            if (this.isClipPathSupported) {
                canvas.withClip(left, top, right, bottom) {

                    canvas.drawColor(finalColor)

                }
            } else {
                // save
                val previous = paint.style
                val previousColor = paint.color

                // set
                paint.style = Paint.Style.FILL
                paint.color = finalColor

                canvas.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)

                // restore
                paint.color = previousColor
                paint.style = previous
            }
        }

        Fill.Type.LINEAR_GRADIENT -> {
            val gradient = LinearGradient(
                (if (gradientDirection == Fill.Direction.RIGHT)
                    right
                else
                    left)
                    .toInt().toFloat(),
                (if (gradientDirection == Fill.Direction.UP)
                    bottom
                else
                    top)
                    .toInt().toFloat(),
                (when (gradientDirection) {
                    Fill.Direction.RIGHT -> left
                    Fill.Direction.LEFT -> right
                    else -> left
                }).toInt().toFloat(),
                (when (gradientDirection) {
                    Fill.Direction.UP -> top
                    Fill.Direction.DOWN -> bottom
                    else -> top
                }).toInt().toFloat(),
                this.gradientColors!!,
                this.gradientPositions,
                Shader.TileMode.MIRROR
            )

            paint.shader = gradient

            canvas.drawRoundRect(RectF(left, top, right, bottom), mRoundedBarRadius, mRoundedBarRadius, paint)
        }

        Fill.Type.DRAWABLE -> {
            val drawable = this.drawable ?: return

            drawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            drawable.draw(canvas)
        }
    }
}

fun Fill.fillPath(
    canvas: Canvas,
    path: Path,
    paint: Paint,
    clipRect: RectF?
) {
    when (this.type) {
        Fill.Type.EMPTY -> return

        Fill.Type.COLOR -> {
            val finalColor = this.finalColor ?: return

            if (clipRect != null && this.isClipPathSupported) {
                canvas.withClip(path) {

                    canvas.drawColor(finalColor)

                }
            } else {
                // save
                val previous = paint.style
                val previousColor = paint.color

                // set
                paint.style = Paint.Style.FILL
                paint.color = finalColor

                canvas.drawPath(path, paint)

                // restore
                paint.color = previousColor
                paint.style = previous
            }
        }

        Fill.Type.LINEAR_GRADIENT -> {
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

        Fill.Type.DRAWABLE -> {
            val drawable = this.drawable ?: return

            ensureClipPathSupported()

            val save = canvas.save()
            canvas.clipPath(path)

            drawable.setBounds(
                if (clipRect == null) 0 else clipRect.left.toInt(),
                if (clipRect == null) 0 else clipRect.top.toInt(),
                if (clipRect == null) canvas.width else clipRect.right.toInt(),
                if (clipRect == null) canvas.height else clipRect.bottom.toInt()
            )
            drawable.draw(canvas)

            canvas.restoreToCount(save)
        }
    }
}
