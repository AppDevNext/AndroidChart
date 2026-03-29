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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.R
import info.appdev.chartexample.notimportant.DemoBaseCompose
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.compose.HorizontalBarChart
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntryFloat
import timber.log.Timber

class HorizontalBarFullComposeActivity : DemoBaseCompose() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HorizontalBarChartScreen(
                    onSaveToGallery = { /* saveToGallery functionality needs chart bitmap */ },
                    onViewGithub = { viewGithub() }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HorizontalBarChartScreen(
        onSaveToGallery: () -> Unit,
        onViewGithub: () -> Unit
    ) {
        // State management
        var showValues by remember { mutableStateOf(true) }
        var showIcons by remember { mutableStateOf(false) }
        var highlightEnabled by remember { mutableStateOf(true) }
        var pinchZoomEnabled by remember { mutableStateOf(true) }
        var autoScaleMinMaxEnabled by remember { mutableStateOf(false) }
        var barBordersEnabled by remember { mutableStateOf(false) }
        var animationTrigger by remember { mutableStateOf(0) }

        var showMenu by remember { mutableStateOf(false) }
        var seekBarXValue by remember { mutableFloatStateOf(12f) }
        var seekBarYValue by remember { mutableFloatStateOf(50f) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HorizontalBarFullCompose") },
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
                                    text = { Text("Toggle Icons") },
                                    onClick = {
                                        showMenu = false
                                        showIcons = !showIcons
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Icons")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Highlight") },
                                    onClick = {
                                        showMenu = false
                                        highlightEnabled = !highlightEnabled
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Highlight")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Pinch Zoom") },
                                    onClick = {
                                        showMenu = false
                                        pinchZoomEnabled = !pinchZoomEnabled
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Pinch Zoom")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Auto Scale MinMax") },
                                    onClick = {
                                        showMenu = false
                                        autoScaleMinMaxEnabled = !autoScaleMinMaxEnabled
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Auto Scale MinMax")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Bar Borders") },
                                    onClick = {
                                        showMenu = false
                                        barBordersEnabled = !barBordersEnabled
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Bar Borders")
                                )
                                DropdownMenuItem(
                                    text = { Text("Animate X") },
                                    onClick = {
                                        showMenu = false
                                        animationTrigger++
                                    },
                                    modifier = Modifier.testTag("menuItem_Animate X")
                                )
                                DropdownMenuItem(
                                    text = { Text("Animate Y") },
                                    onClick = {
                                        showMenu = false
                                        animationTrigger++
                                    },
                                    modifier = Modifier.testTag("menuItem_Animate Y")
                                )
                                DropdownMenuItem(
                                    text = { Text("Animate XY") },
                                    onClick = {
                                        showMenu = false
                                        animationTrigger++
                                    },
                                    modifier = Modifier.testTag("menuItem_Animate XY")
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
                // Chart - Using Compose HorizontalBarChart
                val barData = remember(
                    seekBarXValue,
                    seekBarYValue,
                    showIcons,
                    barBordersEnabled,
                    showValues
                ) {
                    createBarData(
                        seekBarXValue.toInt(),
                        seekBarYValue,
                        showIcons,
                        barBordersEnabled,
                        showValues
                    )
                }

                key(showIcons) {
                    HorizontalBarChart(
                        data = barData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("horizontalBarChart_$showIcons"),
                        drawValueAboveBar = true,
                        drawBarShadow = false,
                        scaleEnabled = pinchZoomEnabled,
                        touchEnabled = true,
                        dragEnabled = true,
                        highlightFullBarEnabled = highlightEnabled,
                        animationDuration = if (animationTrigger > 0) 2500 else 0,
                        onValueSelected = { entry, _ ->
                            entry?.let {
                                Timber.d("Selected: x=${it.x}, y=${it.y}")
                            }
                        },
                        xAxisConfig = { xAxis ->
                            xAxis.position = XAxisPosition.BOTTOM
                            xAxis.typeface = tfLight
                            xAxis.isDrawAxisLine = true
                            xAxis.isDrawGridLines = false
                            xAxis.granularity = 10f
                        },
                        leftAxisConfig = { leftAxis ->
                            leftAxis.typeface = tfLight
                            leftAxis.isDrawAxisLine = true
                            leftAxis.isDrawGridLines = true
                            if (!autoScaleMinMaxEnabled) {
                                leftAxis.axisMinimum = 0f
                            }
                        },
                        rightAxisConfig = { rightAxis ->
                            rightAxis.typeface = tfLight
                            rightAxis.isDrawAxisLine = true
                            rightAxis.isDrawGridLines = false
                            if (!autoScaleMinMaxEnabled) {
                                rightAxis.axisMinimum = 0f
                            }
                        },
                        legend = { legend ->
                            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                            legend.orientation = Legend.LegendOrientation.HORIZONTAL
                            legend.setDrawInside(false)
                            legend.formSize = 8f
                            legend.xEntrySpace = 4f
                        }
                    )
                }

                // SeekBar X with label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Slider(
                        value = seekBarYValue,
                        onValueChange = { newValue ->
                            seekBarYValue = newValue
                        },
                        valueRange = 0f..100f,
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

    private fun createBarData(count: Int, range: Float, showIcons: Boolean, barBordersEnabled: Boolean, showValues: Boolean): BarData {
        val barWidth = 9f
        val spaceForBar = 10f
        val values = ArrayList<BarEntryFloat>()
        val sampleValues = getValues(100)

        for (i in 0..<count) {
            val value = sampleValues[i]!!.toFloat() * range
            val icon = if (showIcons) {
                ResourcesCompat.getDrawable(resources, R.drawable.star, null)
            } else null

            val barEntry = BarEntryFloat(i * spaceForBar, value, icon)
            Timber.d("x=${barEntry.x} y=${barEntry.y}")
            values.add(barEntry)
        }

        val set1 = BarDataSet(values, "DataSet 1")
        set1.isDrawIcons = showIcons
        set1.isDrawValues = showValues

        if (barBordersEnabled) {
            set1.barBorderWidth = 1f
            set1.barBorderColor = android.graphics.Color.BLACK
        } else {
            set1.barBorderWidth = 0f
        }

        val data = BarData(set1)
        data.setValueTextSize(10f)
        data.setValueTypeface(tfLight)
        data.barWidth = barWidth
        return data
    }


}
