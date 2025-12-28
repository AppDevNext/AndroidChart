package info.appdev.charting.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Remember a [LineChartState] across recompositions.
 * The state will be saved and restored across configuration changes.
 */
@Composable
fun rememberLineChartState(): LineChartState {
    return rememberSaveable(saver = LineChartState.Saver) {
        LineChartState()
    }
}

/**
 * Remember a [BarChartState] across recompositions.
 * The state will be saved and restored across configuration changes.
 */
@Composable
fun rememberBarChartState(): BarChartState {
    return rememberSaveable(saver = BarChartState.Saver) {
        BarChartState()
    }
}

/**
 * Remember a [HorizontalBarChartState] across recompositions.
 */
@Composable
fun rememberHorizontalBarChartState(): HorizontalBarChartState {
    return remember {
        HorizontalBarChartState()
    }
}

/**
 * Remember a [PieChartState] across recompositions.
 * The state will be saved and restored across configuration changes.
 */
@Composable
fun rememberPieChartState(): PieChartState {
    return rememberSaveable(saver = PieChartState.Saver) {
        PieChartState()
    }
}

/**
 * Remember a [RadarChartState] across recompositions.
 */
@Composable
fun rememberRadarChartState(): RadarChartState {
    return remember {
        RadarChartState()
    }
}

/**
 * Remember a [ScatterChartState] across recompositions.
 */
@Composable
fun rememberScatterChartState(): ScatterChartState {
    return remember {
        ScatterChartState()
    }
}

/**
 * Remember a [BubbleChartState] across recompositions.
 */
@Composable
fun rememberBubbleChartState(): BubbleChartState {
    return remember {
        BubbleChartState()
    }
}

/**
 * Remember a [CandleStickChartState] across recompositions.
 */
@Composable
fun rememberCandleStickChartState(): CandleStickChartState {
    return remember {
        CandleStickChartState()
    }
}

/**
 * Remember a [CombinedChartState] across recompositions.
 */
@Composable
fun rememberCombinedChartState(): CombinedChartState {
    return remember {
        CombinedChartState()
    }
}

