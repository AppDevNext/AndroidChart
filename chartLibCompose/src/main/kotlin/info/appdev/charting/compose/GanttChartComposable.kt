package info.appdev.charting.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import info.appdev.charting.charts.GanttChart
import info.appdev.charting.data.GanttChartData

/**
 * A Composable wrapper for [GanttChart].
 *
 * @param data The Gantt chart data to display
 * @param modifier The modifier to be applied to the chart
 */
@Composable
fun GanttChart(
    data: GanttChartData?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val chart = remember { GanttChart(context) }

    AndroidView(
        factory = { chart },
        modifier = modifier.fillMaxSize(),
        update = { ganttChart ->
            ganttChart.setData(data)
        }
    )
}

