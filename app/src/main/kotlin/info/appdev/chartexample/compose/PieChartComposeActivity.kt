package info.appdev.chartexample.compose

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import info.appdev.chartexample.notimportant.DemoBaseCompose
import info.appdev.charting.components.Legend
import info.appdev.charting.compose.PieChart
import info.appdev.charting.data.PieData
import info.appdev.charting.data.PieDataSet
import info.appdev.charting.data.PieEntryFloat
import timber.log.Timber

class PieChartComposeActivity : DemoBaseCompose() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                PieChartScreen(onViewGithub = { viewGithub() })
            }
        }
    }

    private val holeColors = listOf(
        Color.White,
        Color.LightGray,
        Color(0xFFB2EBF2),
        Color.Black,
        Color.Transparent
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PieChartScreen(onViewGithub: () -> Unit) {
        var showMenu by remember { mutableStateOf(false) }
        var drawHoleEnabled by remember { mutableStateOf(true) }
        var usePercentValues by remember { mutableStateOf(true) }
        var drawEntryLabels by remember { mutableStateOf(true) }
        var drawRoundedSlices by remember { mutableStateOf(false) }
        var holeColorIndex by remember { mutableIntStateOf(0) }
        var animationTrigger by remember { mutableIntStateOf(0) }
        var holeRadiusValue by remember { mutableFloatStateOf(45f) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("PieChartCompose") },
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
                                    onClick = {
                                        showMenu = false
                                        onViewGithub()
                                    },
                                    modifier = Modifier.testTag("menuItem_View on GitHub")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Hole") },
                                    onClick = {
                                        showMenu = false
                                        drawHoleEnabled = !drawHoleEnabled
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Hole")
                                )
                                DropdownMenuItem(
                                    text = { Text("Cycle Hole Color") },
                                    onClick = {
                                        showMenu = false
                                        holeColorIndex = (holeColorIndex + 1) % holeColors.size
                                    },
                                    modifier = Modifier.testTag("menuItem_Cycle Hole Color")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Percent Values") },
                                    onClick = {
                                        showMenu = false
                                        usePercentValues = !usePercentValues
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Percent Values")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Entry Labels") },
                                    onClick = {
                                        showMenu = false
                                        drawEntryLabels = !drawEntryLabels
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Entry Labels")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Rounded Slices") },
                                    onClick = {
                                        showMenu = false
                                        drawRoundedSlices = !drawRoundedSlices
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Rounded Slices")
                                )
                                DropdownMenuItem(
                                    text = { Text("Animate") },
                                    onClick = {
                                        showMenu = false
                                        animationTrigger++
                                    },
                                    modifier = Modifier.testTag("menuItem_Animate")
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
                    .background(Color.White)
            ) {
                val pieData = remember { createPieData() }

                PieChart(
                    data = pieData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("pieChart"),
                    drawHoleEnabled = drawHoleEnabled,
                    holeRadius = holeRadiusValue,
                    holeColor = holeColors[holeColorIndex],
                    transparentCircleRadius = holeRadiusValue + 5f,
                    centerText = "Market Share",
                    rotationEnabled = true,
                    usePercentValuesEnabled = usePercentValues,
                    drawEntryLabelsEnabled = drawEntryLabels,
                    drawRoundedSlicesEnabled = drawRoundedSlices,
                    animationDuration = if (animationTrigger > 0) 1200 else 0,
                    onValueSelected = { entry, _ ->
                        entry?.let {
                            Timber.d("Selected: ${(it as? PieEntryFloat)?.label} = ${it.y}")
                        }
                    },
                    legend = { legend ->
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.setDrawInside(false)
                    }
                )

                // Hole radius slider with label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hole:",
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = holeRadiusValue,
                        onValueChange = { newValue ->
                            holeRadiusValue = newValue
                        },
                        valueRange = 0f..90f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = holeRadiusValue.toInt().toString(),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    private fun createPieData(): PieData {
        val entries = listOf(
            PieEntryFloat(30f, "Product A"),
            PieEntryFloat(25f, "Product B"),
            PieEntryFloat(20f, "Product C"),
            PieEntryFloat(15f, "Product D"),
            PieEntryFloat(10f, "Product E")
        )

        val dataSet = PieDataSet(entries.toMutableList(), "Sales Distribution").apply {
            setColors(
                mutableListOf(
                    android.graphics.Color.rgb(255, 102, 0),
                    android.graphics.Color.rgb(76, 175, 80),
                    android.graphics.Color.rgb(33, 150, 243),
                    android.graphics.Color.rgb(156, 39, 176),
                    android.graphics.Color.rgb(255, 193, 7)
                )
            )
            valueTextSize = 14f
        }

        return PieData(dataSet)
    }
}
