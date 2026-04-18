package info.appdev.chartexample.compose

import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import info.appdev.chartexample.notimportant.DemoBaseCompose
import info.appdev.charting.compose.GanttChart
import info.appdev.charting.data.GanttChartData
import info.appdev.charting.data.GanttTask

/**
 * Demo activity showing Gantt-style timeline visualization using Compose.
 */
class TimeIntervalComposeActivity : DemoBaseCompose() {

    // Full list of sample tasks
    private val allTasks = listOf(
        GanttTask("Design",  0f,   50f,  Color.rgb(255, 107, 107)),
        GanttTask("Dev",     40f,  100f, Color.rgb(66,  165, 245)),
        GanttTask("Testing", 120f, 40f,  Color.rgb(76,  175, 80), hatched = true),
        GanttTask("Launch",  150f, 20f,  Color.rgb(255, 193, 7)),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GanttChartScreen(onViewGithub = { viewGithub() })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GanttChartScreen(onViewGithub: () -> Unit) {
        var showMenu by remember { mutableStateOf(false) }
        // X: controls visible max time (range 50–300)
        var seekBarXValue by remember { mutableFloatStateOf(190f) }
        // Y: controls how many tasks are shown (1–allTasks.size)
        var seekBarYValue by remember { mutableFloatStateOf(allTasks.size.toFloat()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TimeIntervalChart") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("menuButton")
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.testTag("dropdownMenu")
                            ) {
                                DropdownMenuItem(
                                    text = { Text("View on GitHub") },
                                    onClick = { showMenu = false; onViewGithub() },
                                    modifier = Modifier.testTag("menuItem_View on GitHub")
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(androidx.compose.ui.graphics.Color.White)
            ) {
                val ganttData = remember(seekBarXValue, seekBarYValue) {
                    val maxTime = seekBarXValue
                    val taskCount = seekBarYValue.toInt().coerceAtLeast(1)
                    GanttChartData().apply {
                        allTasks.take(taskCount).forEach { addTask(it) }
                        minTime = 0f
                        this.maxTime = maxTime
                    }
                }

                GanttChart(
                    data = ganttData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // SeekBar X – controls visible time range
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "X:",
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = seekBarXValue,
                        onValueChange = { seekBarXValue = it },
                        valueRange = 1f..200f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = seekBarXValue.toInt().toString(),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // SeekBar Y – controls number of tasks shown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Y:",
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = seekBarYValue,
                        onValueChange = { seekBarYValue = it },
                        valueRange = 1f..allTasks.size.toFloat(),
                        steps = allTasks.size - 2,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = seekBarYValue.toInt().toString(),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
