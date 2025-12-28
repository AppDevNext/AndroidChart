package info.appdev.charting.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import info.appdev.charting.animation.Easing
import info.appdev.charting.charts.PieChart
import info.appdev.charting.components.Legend
import info.appdev.charting.data.Entry
import info.appdev.charting.data.PieData
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.listener.OnChartValueSelectedListener

/**
 * A Composable wrapper for [PieChart].
 *
 * @param data The pie chart data to display
 * @param modifier The modifier to be applied to the chart
 * @param state The state holder for the chart
 * @param onValueSelected Callback when a value is selected
 * @param description Description text for the chart
 * @param legend Configuration for the legend
 * @param backgroundColor Background color for the chart
 * @param touchEnabled Whether touch gestures are enabled
 * @param rotationEnabled Whether rotation is enabled
 * @param highlightPerTapEnabled Whether highlighting on tap is enabled
 * @param drawEntryLabelsEnabled Whether to draw entry labels
 * @param drawHoleEnabled Whether to draw a hole in the center
 * @param drawSlicesUnderHoleEnabled Whether to draw slices under the hole
 * @param usePercentValuesEnabled Whether to use percentage values
 * @param drawRoundedSlicesEnabled Whether to draw rounded slices
 * @param holeRadius Radius of the center hole (0-100)
 * @param transparentCircleRadius Radius of the transparent circle (0-100)
 * @param centerText Text to display in the center
 * @param rotationAngle Starting rotation angle in degrees
 * @param minAngleForSlices Minimum angle for slices
 * @param animationDuration Animation duration in milliseconds
 * @param animationEasing Animation easing function
 */
@Composable
fun PieChart(
    data: PieData?,
    modifier: Modifier = Modifier,
    state: PieChartState = rememberPieChartState(),
    onValueSelected: ((Entry?, Highlight?) -> Unit)? = null,
    description: String? = null,
    legend: ((Legend) -> Unit)? = null,
    backgroundColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    touchEnabled: Boolean = true,
    rotationEnabled: Boolean = true,
    highlightPerTapEnabled: Boolean = true,
    drawEntryLabelsEnabled: Boolean = true,
    drawHoleEnabled: Boolean = true,
    drawSlicesUnderHoleEnabled: Boolean = false,
    usePercentValuesEnabled: Boolean = false,
    drawRoundedSlicesEnabled: Boolean = false,
    holeRadius: Float = 50f,
    transparentCircleRadius: Float = 55f,
    centerText: CharSequence = "",
    rotationAngle: Float = 270f,
    minAngleForSlices: Float = 0f,
    animationDuration: Int = 0,
    animationEasing: Easing.EasingFunction? = null,
) {
    val context = LocalContext.current

    val chart = remember {
        PieChart(context).apply {
            this.description.isEnabled = false
        }
    }

    DisposableEffect(chart) {
        onDispose {
            chart.clear()
        }
    }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { pieChart ->
            // Update data
            pieChart.data = data

            // Update configuration
            pieChart.setBackgroundColor(backgroundColor.toArgb())

            // Touch settings
            pieChart.setTouchEnabled(touchEnabled)
            pieChart.isRotationEnabled = rotationEnabled
            pieChart.isHighlightPerTap = highlightPerTapEnabled

            // Pie-specific settings
            pieChart.isDrawEntryLabels = drawEntryLabelsEnabled
            pieChart.isDrawHole = drawHoleEnabled
            pieChart.isDrawSlicesUnderHole = drawSlicesUnderHoleEnabled
            pieChart.isUsePercentValues = usePercentValuesEnabled
            pieChart.isDrawRoundedSlices = drawRoundedSlicesEnabled
            pieChart.holeRadius = holeRadius
            pieChart.transparentCircleRadius = transparentCircleRadius
            pieChart.centerText = centerText
            pieChart.rotationAngle = rotationAngle
            pieChart.minAngleForSlices = minAngleForSlices

            // Description
            pieChart.description.let { desc ->
                if (description != null) {
                    desc.isEnabled = true
                    desc.text = description
                } else {
                    desc.isEnabled = false
                }
            }

            // Legend
            legend?.invoke(pieChart.legend)

            // Selection listener
            if (onValueSelected != null) {
                pieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(entry: Entry, highlight: Highlight) {
                        onValueSelected(entry, highlight)
                    }

                    override fun onNothingSelected() {
                        onValueSelected(null, null)
                    }
                })
            } else {
                pieChart.setOnChartValueSelectedListener(null)
            }

            // Animation
            if (animationDuration > 0) {
                if (animationEasing != null) {
                    pieChart.spin(animationDuration, rotationAngle, rotationAngle + 360f, animationEasing)
                } else {
                    pieChart.animateY(animationDuration)
                }
            }

            // Refresh
            pieChart.invalidate()
        }
    )
}

