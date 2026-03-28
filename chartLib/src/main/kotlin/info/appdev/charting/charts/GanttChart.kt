package info.appdev.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import info.appdev.charting.data.GanttChartData
import java.util.Locale

class GanttChart : View {
    private var data: GanttChartData? = null
    private var taskPaint: Paint? = null
    private var gridPaint: Paint? = null
    private var textPaint: Paint? = null

    private var chartLeft = 0f
    private var chartTop = 0f
    private var chartRight = 0f
    private var chartBottom = 0f
    private val padding = 16f
    private val labelTextSize = 24f
    private val gridLinesMin = 2
    private val gridLinesMax = 10

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        taskPaint = Paint().apply {
            isAntiAlias = true
        }
        gridPaint = Paint().apply {
            color = -0x333334
            strokeWidth = 1f
        }
        textPaint = Paint().apply {
            color = -0x99999a
            textSize = 28f
            isAntiAlias = true
        }
    }

    fun setData(data: GanttChartData?) {
        this.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data == null || data!!.taskCount == 0) {
            return
        }

        calculateDimensions()
        drawGrid(canvas)
        drawTasks(canvas)
    }

    private fun calculateDimensions() {
        val labelMeasurePaint = Paint().apply {
            textSize = labelTextSize
            isAntiAlias = true
        }
        var maxLabelWidth = 0f
        if (data != null) {
            for (i in 0..<data!!.taskCount) {
                val w = labelMeasurePaint.measureText(data!!.getTask(i).name ?: "")
                if (w > maxLabelWidth) maxLabelWidth = w
            }
        }
        chartLeft = maxLabelWidth + padding * 3
        chartTop = padding + 30
        chartRight = width - padding
        chartBottom = height - padding - 30
    }

    private val taskHeight: Float
        // Dynamically calculate task height based on available space
        get() {
            if (data == null || data!!.taskCount == 0) {
                return 40f
            }
            val availableHeight = chartBottom - chartTop
            val taskCount = data!!.taskCount
            // 50% of slot for bar, 50% for gap
            return (availableHeight / taskCount) * 0.5f
        }

    private val taskSpacing: Float
        get() {
            if (data == null || data!!.taskCount == 0) {
                return 12f
            }
            val availableHeight = chartBottom - chartTop
            val taskCount = data!!.taskCount
            return (availableHeight / taskCount) * 0.5f
        }

    private fun drawGrid(canvas: Canvas) {
        val minTime = data!!.minTime
        val maxTime = data!!.maxTime
        var timeRange = maxTime - minTime
        if (timeRange == 0f) {
            timeRange = 100f
        }

        val timeLabelPaint = Paint().apply {
            color = -0x99999a
            textSize = 22f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Calculate how many grid lines fit without overlapping labels
        val sampleLabel = String.format(Locale.getDefault(), "%.0f", maxTime)
        val labelWidth = timeLabelPaint.measureText(sampleLabel) + 8f
        val chartWidth = chartRight - chartLeft
        val maxGridLines = (chartWidth / labelWidth).toInt().coerceIn(gridLinesMin, gridLinesMax)

        for (i in 0..maxGridLines) {
            val x = chartLeft + (i / maxGridLines.toFloat()) * chartWidth
            canvas.drawLine(x, chartTop, x, chartBottom, gridPaint!!)

            val time = minTime + (i / maxGridLines.toFloat()) * timeRange
            canvas.drawText(String.format(Locale.getDefault(), "%.0f", time), x, chartBottom + 30, timeLabelPaint)
        }
    }

    private fun drawTasks(canvas: Canvas) {
        val minTime = data!!.minTime
        val maxTime = data!!.maxTime
        var timeRange = maxTime - minTime
        if (timeRange == 0f) {
            timeRange = 100f
        }

        val taskHeight = this.taskHeight
        val taskSpacing = this.taskSpacing
        val slotHeight = taskHeight + taskSpacing

        val labelPaint = Paint()
        labelPaint.color = -0xcccccd
        labelPaint.textSize = labelTextSize
        labelPaint.isAntiAlias = true
        labelPaint.textAlign = Paint.Align.RIGHT

        val borderPaint = Paint()
        borderPaint.color = -0x666667
        borderPaint.strokeWidth = 2f
        borderPaint.style = Paint.Style.STROKE

        for (i in 0..<data!!.taskCount) {
            val task = data!!.getTask(i)

            val taskY = chartTop + i * slotHeight
            val startX = chartLeft + ((task.startTime - minTime) / timeRange) * (chartRight - chartLeft)
            var endX = chartLeft + ((task.endTime - minTime) / timeRange) * (chartRight - chartLeft)

            if (endX - startX < 10) {
                endX = startX + 10
            }

            // Center label vertically in the slot
            val labelY = taskY + (taskHeight / 2) + 8
            canvas.drawText(task.name!!, chartLeft - padding, labelY, labelPaint)

            val rect = RectF(startX, taskY, endX, taskY + taskHeight)
            taskPaint!!.color = task.color
            canvas.drawRect(rect, taskPaint!!)
            canvas.drawRect(rect, borderPaint)
        }
    }
}
