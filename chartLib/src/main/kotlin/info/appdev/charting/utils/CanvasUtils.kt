package info.appdev.charting.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.drawable.Drawable
import info.appdev.charting.utils.Utils.FDEG2RAD
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val mDrawableBoundsCache = Rect()

/**
 * Utilities class that has some helper methods. Needs to be initialized by
 * calling Utils.init(...) before usage. Inside the Chart.init() method, this is
 * done, if the Utils are used before that, Utils.init(...) needs to be called
 * manually.
 */
fun Canvas.drawImage(
    drawable: Drawable,
    x: Int, y: Int,
) {
    val width: Int = drawable.intrinsicWidth
    val height: Int = drawable.intrinsicHeight
    val drawOffset = PointF.getInstance(width.toFloat(), height.toFloat())
    drawOffset.x = x - (width / 2).toFloat()
    drawOffset.y = y - (height / 2).toFloat()

    drawable.copyBounds(mDrawableBoundsCache)
    drawable.setBounds(
        mDrawableBoundsCache.left,
        mDrawableBoundsCache.top,
        mDrawableBoundsCache.left + width,
        mDrawableBoundsCache.top + width
    )

    val saveId = this.save()
    // translate to the correct position and draw
    this.translate(drawOffset.x, drawOffset.y)
    drawable.draw(this)
    this.restoreToCount(saveId)
}

private val mDrawTextRectBuffer = Rect()
private val mFontMetricsBuffer = Paint.FontMetrics()

fun Canvas.drawXAxisValue(
    text: String?, x: Float, y: Float,
    paint: Paint,
    anchor: PointF, angleDegrees: Float
) {
    var drawOffsetX = 0f
    var drawOffsetY = 0f

    val lineHeight = paint.getFontMetrics(mFontMetricsBuffer)
    text?.let { paint.getTextBounds(text, 0, it.length, mDrawTextRectBuffer) }

    // Android sometimes has pre-padding
    drawOffsetX -= mDrawTextRectBuffer.left.toFloat()

    // Android does not snap the bounds to line boundaries,
    //  and draws from bottom to top.
    // And we want to normalize it.
    drawOffsetY -= mFontMetricsBuffer.ascent

    // To have a consistent point of reference, we always draw left-aligned
    val originalTextAlign = paint.textAlign
    paint.textAlign = Align.LEFT

    if (angleDegrees != 0f) {
        // Move the text drawing rect in a way that it always rotates around its center

        drawOffsetX -= mDrawTextRectBuffer.width() * 0.5f
        drawOffsetY -= lineHeight * 0.5f

        var translateX = x
        var translateY = y

        // Move the "outer" rect relative to the anchor, assuming its centered
        if (anchor.x != 0.5f || anchor.y != 0.5f) {
            val rotatedSize = getSizeOfRotatedRectangleByDegrees(
                mDrawTextRectBuffer.width().toFloat(),
                lineHeight,
                angleDegrees
            )

            translateX -= rotatedSize.width * (anchor.x - 0.5f)
            translateY -= rotatedSize.height * (anchor.y - 0.5f)
            FSize.recycleInstance(rotatedSize)
        }

        this.save()
        this.translate(translateX, translateY)
        this.rotate(angleDegrees)

        text?.let { this.drawText(it, drawOffsetX, drawOffsetY, paint) }

        this.restore()
    } else {
        if (anchor.x != 0f || anchor.y != 0f) {
            drawOffsetX -= mDrawTextRectBuffer.width() * anchor.x
            drawOffsetY -= lineHeight * anchor.y
        }

        drawOffsetX += x
        drawOffsetY += y

        text?.let { this.drawText(it, drawOffsetX, drawOffsetY, paint) }
    }

    paint.textAlign = originalTextAlign
}

/**
 * Returns a recyclable FSize instance.
 * Represents size of a rotated rectangle by degrees.
 *
 * @param rectangleWidth
 * @param rectangleHeight
 * @param degrees
 * @return A Recyclable FSize instance
 */
fun getSizeOfRotatedRectangleByDegrees(rectangleWidth: Float, rectangleHeight: Float, degrees: Float): FSize {
    val radians = degrees * FDEG2RAD
    return getSizeOfRotatedRectangleByRadians(rectangleWidth, rectangleHeight, radians)
}

/**
 * Returns a recyclable FSize instance.
 * Represents size of a rotated rectangle by radians.
 *
 * @param rectangleWidth
 * @param rectangleHeight
 * @param radians
 * @return A Recyclable FSize instance
 */
fun getSizeOfRotatedRectangleByRadians(rectangleWidth: Float, rectangleHeight: Float, radians: Float): FSize {
    return FSize.getInstance(
        abs(rectangleWidth * cos(radians.toDouble()).toFloat()) + abs(rectangleHeight * sin(radians.toDouble()).toFloat()),
        abs(rectangleWidth * sin(radians.toDouble()).toFloat()) + abs(rectangleHeight * cos(radians.toDouble()).toFloat())
    )
}
