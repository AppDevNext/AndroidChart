package com.github.mikephil.charting.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

open class YAxisRenderer(viewPortHandler: ViewPortHandler, @JvmField protected var mYAxis: YAxis, trans: Transformer?) :
    AxisRenderer(viewPortHandler, trans, mYAxis) {
    @JvmField
    protected var mZeroLinePaint: Paint? = null

    /**
     * Return the axis label x position based on axis dependency and label position
     * @param dependency
     * @param labelPosition
     * @return
     */
    private fun calculateAxisLabelsXPosition(dependency: AxisDependency, labelPosition: YAxisLabelPosition): Float {
        val viewPortBase = if (dependency == AxisDependency.LEFT) viewPortHandler.offsetLeft() else viewPortHandler.contentRight()
        val xOffset = mYAxis.xOffset * (if (labelPosition == YAxisLabelPosition.OUTSIDE_CHART) -1 else 1)

        return viewPortBase + xOffset
    }

    /**
     * Return the text align based on axis dependency and label position
     * @param dependency
     * @param labelPosition
     * @return
     */
    private fun getAxisLabelTextAlign(dependency: AxisDependency, labelPosition: YAxisLabelPosition): Align {
        if ((dependency == AxisDependency.LEFT) xor (labelPosition == YAxisLabelPosition.OUTSIDE_CHART)) {
            return Align.LEFT
        }

        return Align.RIGHT
    }

    /**
     * draws the y-axis labels to the screen
     */
    override fun renderAxisLabels(c: Canvas) {
        if (!mYAxis.isEnabled || !mYAxis.isDrawLabelsEnabled) return

        val positions = transformedPositions

        paintAxisLabels!!.setTypeface(mYAxis.typeface)
        paintAxisLabels!!.textSize = mYAxis.textSize
        paintAxisLabels!!.color = mYAxis.textColor

        val yOffset = Utils.calcTextHeight(paintAxisLabels, "A") / 2.5f + mYAxis.yOffset

        val dependency = mYAxis.axisDependency
        val labelPosition = mYAxis.labelPosition

        val xPos = calculateAxisLabelsXPosition(dependency, labelPosition)
        paintAxisLabels!!.textAlign = getAxisLabelTextAlign(dependency, labelPosition)

        drawYLabels(c, xPos, positions, yOffset)
    }

    override fun renderAxisLine(c: Canvas) {
        if (!mYAxis.isEnabled || !mYAxis.isDrawAxisLineEnabled) return

        paintAxisLine!!.color = mYAxis.axisLineColor
        paintAxisLine!!.strokeWidth = mYAxis.axisLineWidth

        if (mYAxis.axisDependency == AxisDependency.LEFT) {
            c.drawLine(
                viewPortHandler.contentLeft(), viewPortHandler.contentTop(), viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), paintAxisLine!!
            )
        } else {
            c.drawLine(
                viewPortHandler.contentRight(), viewPortHandler.contentTop(), viewPortHandler.contentRight(),
                viewPortHandler.contentBottom(), paintAxisLine!!
            )
        }
    }

    /**
     * draws the y-labels on the specified x-position
     *
     * @param fixedPosition
     * @param positions
     */
    protected open fun drawYLabels(c: Canvas, fixedPosition: Float, positions: FloatArray, offset: Float) {
        val from: Int
        val to: Int

        if (mYAxis.isShowSpecificPositions) {
            from = 0
            to = if (mYAxis.isDrawTopYLabelEntryEnabled)
                mYAxis.specificPositions.size
            else
                (mYAxis.specificPositions.size - 1)
        } else {
            from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
            to = if (mYAxis.isDrawTopYLabelEntryEnabled)
                mYAxis.mEntryCount
            else
                (mYAxis.mEntryCount - 1)
        }

        val xOffset = mYAxis.labelXOffset

        // draw
        for (i in from..<to) {
            val text = if (mYAxis.isShowSpecificPositions) {
                mYAxis.valueFormatter.getFormattedValue(mYAxis.specificPositions[i], mYAxis)
            } else {
                mYAxis.getFormattedLabel(i)
            }

            c.drawText(
                text!!,
                fixedPosition + xOffset,
                positions[i * 2 + 1] + offset,
                paintAxisLabels!!
            )
        }
    }

    protected var mRenderGridLinesPath: Path = Path()
    override fun renderGridLines(c: Canvas) {
        if (!mYAxis.isEnabled) return

        if (mYAxis.isDrawGridLinesEnabled) {
            val clipRestoreCount = c.save()
            c.clipRect(gridClippingRect!!)

            val positions = transformedPositions

            paintGrid!!.color = mYAxis.gridColor
            paintGrid!!.strokeWidth = mYAxis.gridLineWidth
            paintGrid!!.setPathEffect(mYAxis.gridDashPathEffect)

            val gridLinePath = mRenderGridLinesPath
            gridLinePath.reset()

            // draw the grid
            var i = 0
            while (i < positions.size) {
                // draw a path because lines don't support dashing on lower android versions
                c.drawPath(linePath(gridLinePath, i, positions)!!, paintGrid!!)
                gridLinePath.reset()
                i += 2
            }

            c.restoreToCount(clipRestoreCount)
        }

        if (mYAxis.isDrawZeroLineEnabled) {
            drawZeroLine(c)
        }
    }

    @JvmField
    protected var mGridClippingRect: RectF = RectF()

    open val gridClippingRect: RectF?
        get() {
            mGridClippingRect.set(viewPortHandler.contentRect)
            mGridClippingRect.inset(0f, -mAxis.gridLineWidth)
            return mGridClippingRect
        }

    /**
     * Calculates the path for a grid line.
     *
     * @param p
     * @param i
     * @param positions
     * @return
     */
    protected open fun linePath(p: Path, i: Int, positions: FloatArray): Path? {
        p.moveTo(viewPortHandler.offsetLeft(), positions[i + 1])
        p.lineTo(viewPortHandler.contentRight(), positions[i + 1])

        return p
    }

    @JvmField
    protected var mGetTransformedPositionsBuffer: FloatArray = FloatArray(2)
    protected open val transformedPositions: FloatArray
        /**
         * Transforms the values contained in the axis entries to screen pixels and returns them in form of a float array
         * of x- and y-coordinates.
         *
         * @return
         */
        get() {
            if (mYAxis.isShowSpecificPositions) {
                if (mGetTransformedPositionsBuffer.size != mYAxis.specificPositions.size * 2) {
                    mGetTransformedPositionsBuffer = FloatArray(mYAxis.specificPositions.size * 2)
                }
            } else {
                if (mGetTransformedPositionsBuffer.size != mYAxis.mEntryCount * 2) {
                    mGetTransformedPositionsBuffer = FloatArray(mYAxis.mEntryCount * 2)
                }
            }
            val positions = mGetTransformedPositionsBuffer

            var i = 0
            while (i < positions.size) {
                // only fill y values, x values are not needed for y-labels
                if (mYAxis.isShowSpecificPositions) {
                    positions[i + 1] = mYAxis.specificPositions[i / 2]
                } else {
                    positions[i + 1] = mYAxis.mEntries[i / 2]
                }
                i += 2
            }

            transformer!!.pointValuesToPixel(positions)
            return positions
        }

    protected var mDrawZeroLinePath: Path = Path()

    @JvmField
    protected var mZeroLineClippingRect: RectF = RectF()

    /**
     * Draws the zero line.
     */
    protected open fun drawZeroLine(c: Canvas) {
        val clipRestoreCount = c.save()
        mZeroLineClippingRect.set(viewPortHandler.contentRect)
        mZeroLineClippingRect.inset(0f, -mYAxis.zeroLineWidth)
        c.clipRect(mZeroLineClippingRect)

        // draw zero line
        val pos = transformer!!.getPixelForValues(0f, 0f)

        mZeroLinePaint!!.color = mYAxis.zeroLineColor
        mZeroLinePaint!!.strokeWidth = mYAxis.zeroLineWidth

        val zeroLinePath = mDrawZeroLinePath
        zeroLinePath.reset()

        zeroLinePath.moveTo(viewPortHandler.contentLeft(), pos.y.toFloat())
        zeroLinePath.lineTo(viewPortHandler.contentRight(), pos.y.toFloat())

        // draw a path because lines don't support dashing on lower android versions
        c.drawPath(zeroLinePath, mZeroLinePaint!!)

        c.restoreToCount(clipRestoreCount)
    }

    protected var mRenderLimitLines: Path = Path()
    protected open var mRenderLimitLinesBuffer: FloatArray = FloatArray(2)

    @JvmField
    protected var mLimitLineClippingRect: RectF = RectF()

    init {
        paintAxisLabels!!.color = Color.BLACK
        paintAxisLabels!!.textSize = Utils.convertDpToPixel(10f)

        mZeroLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mZeroLinePaint!!.color = Color.GRAY
        mZeroLinePaint!!.strokeWidth = 1f
        mZeroLinePaint!!.style = Paint.Style.STROKE
    }

    /**
     * Draws the LimitLines associated with this axis to the screen.
     *
     * @param c
     */
    override fun renderLimitLines(c: Canvas) {
        val limitLines = mYAxis.limitLines

        if (limitLines == null || limitLines.size <= 0) return

        val pts = mRenderLimitLinesBuffer
        pts[0] = 0f
        pts[1] = 0f
        val limitLinePath = mRenderLimitLines
        limitLinePath.reset()

        for (i in limitLines.indices) {
            val l = limitLines[i]

            if (!l.isEnabled) continue

            val clipRestoreCount = c.save()
            mLimitLineClippingRect.set(viewPortHandler.contentRect)
            mLimitLineClippingRect.inset(0f, -l.lineWidth)
            c.clipRect(mLimitLineClippingRect)

            mLimitLinePaint!!.style = Paint.Style.STROKE
            mLimitLinePaint!!.color = l.lineColor
            mLimitLinePaint!!.strokeWidth = l.lineWidth
            mLimitLinePaint!!.setPathEffect(l.dashPathEffect)

            pts[1] = l.limit

            transformer!!.pointValuesToPixel(pts)

            limitLinePath.moveTo(viewPortHandler.contentLeft(), pts[1])
            limitLinePath.lineTo(viewPortHandler.contentRight(), pts[1])

            c.drawPath(limitLinePath, mLimitLinePaint!!)
            limitLinePath.reset()

            // c.drawLines(pts, mLimitLinePaint);
            val label = l.label

            // if drawing the limit-value label is enabled
            if (label != null && label != "") {
                mLimitLinePaint!!.style = l.textStyle
                mLimitLinePaint!!.setPathEffect(null)
                mLimitLinePaint!!.color = l.textColor
                mLimitLinePaint!!.setTypeface(l.typeface)
                mLimitLinePaint!!.strokeWidth = 0.5f
                mLimitLinePaint!!.textSize = l.textSize

                val labelLineHeight = Utils.calcTextHeight(mLimitLinePaint, label).toFloat()
                val xOffset = Utils.convertDpToPixel(4f) + l.xOffset
                val yOffset = l.lineWidth + labelLineHeight + l.yOffset

                val position = l.labelPosition

                if (position == LimitLabelPosition.RIGHT_TOP) {
                    mLimitLinePaint!!.textAlign = Align.RIGHT
                    c.drawText(
                        label,
                        viewPortHandler.contentRight() - xOffset,
                        pts[1] - yOffset + labelLineHeight, mLimitLinePaint!!
                    )
                } else if (position == LimitLabelPosition.RIGHT_BOTTOM) {
                    mLimitLinePaint!!.textAlign = Align.RIGHT
                    c.drawText(
                        label,
                        viewPortHandler.contentRight() - xOffset,
                        pts[1] + yOffset, mLimitLinePaint!!
                    )
                } else if (position == LimitLabelPosition.LEFT_TOP) {
                    mLimitLinePaint!!.textAlign = Align.LEFT
                    c.drawText(
                        label,
                        viewPortHandler.contentLeft() + xOffset,
                        pts[1] - yOffset + labelLineHeight, mLimitLinePaint!!
                    )
                } else {
                    mLimitLinePaint!!.textAlign = Align.LEFT
                    c.drawText(
                        label,
                        viewPortHandler.offsetLeft() + xOffset,
                        pts[1] + yOffset, mLimitLinePaint!!
                    )
                }
            }

            c.restoreToCount(clipRestoreCount)
        }
    }
}
