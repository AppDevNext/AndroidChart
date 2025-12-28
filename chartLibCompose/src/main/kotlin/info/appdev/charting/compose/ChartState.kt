package info.appdev.charting.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import info.appdev.charting.charts.CombinedChart
import info.appdev.charting.charts.ScatterChart
import info.appdev.charting.data.*
import info.appdev.charting.highlight.Highlight

/**
 * Base state holder for all chart types in Compose.
 * Manages chart data, highlighting, and configuration in a Compose-friendly way.
 */
@Stable
sealed class ChartState<T : ChartData<*>> {
    var data by mutableStateOf<T?>(null)
    var highlights by mutableStateOf<List<Highlight>>(emptyList())
    var isLogEnabled by mutableStateOf(false)
    var description by mutableStateOf<String?>(null)
    var touchEnabled by mutableStateOf(true)
    var dragDecelerationEnabled by mutableStateOf(true)
    var dragDecelerationFrictionCoef by mutableStateOf(0.9f)
}

/**
 * State holder for LineChart composable.
 */
@Stable
class LineChartState : ChartState<LineData>() {
    companion object {
        val Saver: Saver<LineChartState, *> = listSaver(
            save = { state ->
                listOf(
                    state.isLogEnabled,
                    state.description,
                    state.touchEnabled,
                    state.dragDecelerationEnabled,
                    state.dragDecelerationFrictionCoef
                )
            },
            restore = { list ->
                LineChartState().apply {
                    isLogEnabled = list[0] as Boolean
                    description = list[1] as String?
                    touchEnabled = list[2] as Boolean
                    dragDecelerationEnabled = list[3] as Boolean
                    dragDecelerationFrictionCoef = list[4] as Float
                }
            }
        )
    }
}

/**
 * State holder for BarChart composable.
 */
@Stable
class BarChartState : ChartState<BarData>() {
    var isHighlightFullBar by mutableStateOf(false)
    var isDrawValueAboveBar by mutableStateOf(true)
    var isDrawBarShadow by mutableStateOf(false)

    companion object {
        val Saver: Saver<BarChartState, *> = listSaver(
            save = { state ->
                listOf(
                    state.isLogEnabled,
                    state.description,
                    state.touchEnabled,
                    state.isHighlightFullBar,
                    state.isDrawValueAboveBar,
                    state.isDrawBarShadow
                )
            },
            restore = { list ->
                BarChartState().apply {
                    isLogEnabled = list[0] as Boolean
                    description = list[1] as String?
                    touchEnabled = list[2] as Boolean
                    isHighlightFullBar = list[3] as Boolean
                    isDrawValueAboveBar = list[4] as Boolean
                    isDrawBarShadow = list[5] as Boolean
                }
            }
        )
    }
}

/**
 * State holder for HorizontalBarChart composable.
 */
@Stable
class HorizontalBarChartState : ChartState<BarData>() {
    var isHighlightFullBarEnabled by mutableStateOf(false)
    var isDrawValueAboveBarEnabled by mutableStateOf(true)
    var isDrawBarShadowEnabled by mutableStateOf(false)
}

/**
 * State holder for PieChart composable.
 */
@Stable
class PieChartState : ChartState<PieData>() {
    var isDrawEntryLabelsEnabled by mutableStateOf(true)
    var isDrawHoleEnabled by mutableStateOf(true)
    var isDrawSlicesUnderHoleEnabled by mutableStateOf(false)
    var isUsePercentValuesEnabled by mutableStateOf(false)
    var isDrawRoundedSlicesEnabled by mutableStateOf(false)
    var holeRadius by mutableStateOf(50f)
    var transparentCircleRadius by mutableStateOf(55f)
    var centerText by mutableStateOf<CharSequence>("")
    var rotationAngle by mutableStateOf(270f)
    var isRotationEnabled by mutableStateOf(true)

    companion object {
        val Saver: Saver<PieChartState, *> = listSaver(
            save = { state ->
                listOf(
                    state.isLogEnabled,
                    state.description,
                    state.isDrawEntryLabelsEnabled,
                    state.isDrawHoleEnabled,
                    state.holeRadius,
                    state.transparentCircleRadius,
                    state.centerText.toString(),
                    state.rotationAngle,
                    state.isRotationEnabled
                )
            },
            restore = { list ->
                PieChartState().apply {
                    isLogEnabled = list[0] as Boolean
                    description = list[1] as String?
                    isDrawEntryLabelsEnabled = list[2] as Boolean
                    isDrawHoleEnabled = list[3] as Boolean
                    holeRadius = list[4] as Float
                    transparentCircleRadius = list[5] as Float
                    centerText = list[6] as String
                    rotationAngle = list[7] as Float
                    isRotationEnabled = list[8] as Boolean
                }
            }
        )
    }
}

/**
 * State holder for RadarChart composable.
 */
@Stable
class RadarChartState : ChartState<RadarData>() {
    var webLineWidth by mutableStateOf(2.5f)
    var webLineWidthInner by mutableStateOf(1.5f)
    var webAlpha by mutableStateOf(150)
    var skipWebLineCount by mutableStateOf(0)
}

/**
 * State holder for ScatterChart composable.
 */
@Stable
class ScatterChartState : ChartState<ScatterData>() {
    var scaleType by mutableStateOf(ScatterChart.ScatterShape.CIRCLE)
}

/**
 * State holder for BubbleChart composable.
 */
@Stable
class BubbleChartState : ChartState<BubbleData>()

/**
 * State holder for CandleStickChart composable.
 */
@Stable
class CandleStickChartState : ChartState<CandleData>()

/**
 * State holder for CombinedChart composable.
 */
@Stable
class CombinedChartState : ChartState<CombinedData>() {
    var drawOrder by mutableStateOf(
        arrayOf(
            CombinedChart.DrawOrder.BAR,
            CombinedChart.DrawOrder.BUBBLE,
            CombinedChart.DrawOrder.LINE,
            CombinedChart.DrawOrder.CANDLE,
            CombinedChart.DrawOrder.SCATTER
        )
    )
    var isDrawBarShadowEnabled by mutableStateOf(false)
    var isHighlightFullBarEnabled by mutableStateOf(false)
    var isDrawValueAboveBarEnabled by mutableStateOf(true)
}

