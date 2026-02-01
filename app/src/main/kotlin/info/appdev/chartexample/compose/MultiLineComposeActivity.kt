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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import info.appdev.chartexample.DataTools.Companion.generateSineWaves
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.notimportant.DemoBaseCompose
import info.appdev.charting.components.Legend
import info.appdev.charting.compose.LineChart
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.utils.ColorTemplate
import timber.log.Timber

class MultiLineComposeActivity : DemoBaseCompose() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MultiLineChartScreen(
                    onSaveToGallery = { /* saveToGallery functionality needs chart bitmap */ },
                    onViewGithub = { viewGithub() }
                )
            }
        }
    }

    private val colors = intArrayOf(
        ColorTemplate.VORDIPLOM_COLORS[2],
        ColorTemplate.VORDIPLOM_COLORS[3],
        ColorTemplate.VORDIPLOM_COLORS[0]
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MultiLineChartScreen(
        onSaveToGallery: () -> Unit,
        onViewGithub: () -> Unit
    ) {
        var showMenu by remember { mutableStateOf(false) }
        var seekBarXValue by remember { mutableFloatStateOf(20f) }
        var seekBarYValue by remember { mutableFloatStateOf(100f) }

        // State for toggles
        var showValues by remember { mutableStateOf(false) }
        var pinchZoom by remember { mutableStateOf(false) }
        var autoScaleMinMax by remember { mutableStateOf(false) }
        var showFilled by remember { mutableStateOf(false) }
        var showCircles by remember { mutableStateOf(true) }
        var lineMode by remember { mutableStateOf(LineDataSet.Mode.LINEAR) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("MultiLineChartCompose") },
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
                                    text = { Text("Toggle Values") },
                                    onClick = {
                                        showMenu = false
                                        showValues = !showValues
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Values")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Pinch Zoom") },
                                    onClick = {
                                        showMenu = false
                                        pinchZoom = !pinchZoom
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Pinch Zoom")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Auto Scale MinMax") },
                                    onClick = {
                                        showMenu = false
                                        autoScaleMinMax = !autoScaleMinMax
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Auto Scale MinMax")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Filled") },
                                    onClick = {
                                        showMenu = false
                                        showFilled = !showFilled
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Filled")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Circles") },
                                    onClick = {
                                        showMenu = false
                                        showCircles = !showCircles
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Circles")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Cubic") },
                                    onClick = {
                                        showMenu = false
                                        lineMode = when (lineMode) {
                                            LineDataSet.Mode.CUBIC_BEZIER -> LineDataSet.Mode.LINEAR
                                            else -> LineDataSet.Mode.CUBIC_BEZIER
                                        }
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Cubic")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Stepped") },
                                    onClick = {
                                        showMenu = false
                                        lineMode = when (lineMode) {
                                            LineDataSet.Mode.STEPPED -> LineDataSet.Mode.LINEAR
                                            else -> LineDataSet.Mode.STEPPED
                                        }
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Stepped")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Horizontal Cubic") },
                                    onClick = {
                                        showMenu = false
                                        lineMode = when (lineMode) {
                                            LineDataSet.Mode.HORIZONTAL_BEZIER -> LineDataSet.Mode.LINEAR
                                            else -> LineDataSet.Mode.HORIZONTAL_BEZIER
                                        }
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Horizontal Cubic")
                                )
                                DropdownMenuItem(
                                    text = { Text("Save to Gallery") },
                                    onClick = {
                                        showMenu = false
                                        onSaveToGallery()
                                    },
                                    modifier = Modifier.testTag("menuItem_Save to Gallery")
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
                // Chart - Using Compose LineChart
                val lineData = remember(
                    seekBarXValue,
                    seekBarYValue,
                    showValues,
                    showFilled,
                    showCircles,
                    lineMode
                ) {
                    createLineData(
                        seekBarXValue.toInt(),
                        seekBarYValue,
                        showValues,
                        showFilled,
                        showCircles,
                        lineMode
                    )
                }

                LineChart(
                    data = lineData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    drawGridBackground = false,
                    pinchZoomEnabled = pinchZoom,
                    autoScaleMinMax = autoScaleMinMax,
                    animationDuration = 1500,
                    onValueSelected = { entry, highlight ->
                        entry?.let {
                            Timber.i("Value: ${it.y}, xIndex: ${it.x}, DataSet index: ${highlight?.dataSetIndex}")
                        }
                    },
                    xAxisConfig = { xAxis ->
                        xAxis.isDrawAxisLine = false
                        xAxis.isDrawGridLines = false
                    },
                    leftAxisConfig = { leftAxis ->
                        leftAxis.isEnabled = false
                    },
                    rightAxisConfig = { rightAxis ->
                        rightAxis.isDrawAxisLine = false
                        rightAxis.isDrawGridLines = false
                    },
                    legend = { legend ->
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                        legend.orientation = Legend.LegendOrientation.VERTICAL
                        legend.setDrawInside(false)
                    }
                )

                // SeekBar X with label
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
                        onValueChange = { newValue ->
                            seekBarXValue = newValue
                        },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = seekBarXValue.toInt().toString(),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // SeekBar Y with label
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
                        onValueChange = { newValue ->
                            seekBarYValue = newValue
                        },
                        valueRange = 0f..200f,
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

    private fun createLineData(
        progress: Int,
        range: Float,
        showValues: Boolean,
        showFilled: Boolean,
        showCircles: Boolean,
        lineMode: LineDataSet.Mode
    ): LineData {
        val dataSets = ArrayList<LineDataSet>()

        for (datasetNumber in 0..2) {
            val values = ArrayList<Entry>()
            val sampleValues = when (datasetNumber) {
                1 -> getValues(100).reversedArray()
                2 -> generateSineWaves(3, 30).toTypedArray()
                else -> getValues(100)
            }

            for (i in 0..<progress) {
                val valuesY = (sampleValues[i]!!.toFloat() * range) + 3
                values.add(Entry(i.toFloat(), valuesY))
            }

            val lineDataSet = LineDataSet(values, "DataSet $datasetNumber")
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4f
            lineDataSet.isDrawValues = showValues
            lineDataSet.isDrawFilled = showFilled
            lineDataSet.isDrawCircles = showCircles
            lineDataSet.lineMode = lineMode

            val color = colors[datasetNumber]
            lineDataSet.color = color
            lineDataSet.setCircleColor(color)
            Timber.d("DataSet $datasetNumber color=${color.toHexString()}")
            dataSets.add(lineDataSet)
        }

        // Make the first DataSet dashed
        dataSets[0].enableDashedLine(10f, 10f, 0f)

        return LineData(ArrayList(dataSets.map { it as info.appdev.charting.interfaces.datasets.ILineDataSet }))
    }
}
