package info.appdev.chartexample.compose

import android.graphics.RectF
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import info.appdev.charting.charts.HorizontalBarChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.listener.OnChartValueSelectedListener
import info.appdev.charting.utils.PointF
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.R
import info.appdev.chartexample.notimportant.DemoBaseCompose
import timber.log.Timber

class HorizontalBarComposeActivity : DemoBaseCompose() {
    private var chart: HorizontalBarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                HorizontalBarChartScreen(
                    onSaveToGallery = { saveToGallery(chart!!) },
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
        var showMenu by remember { mutableStateOf(false) }
        var seekBarXValue by remember { mutableFloatStateOf(12f) }
        var seekBarYValue by remember { mutableFloatStateOf(50f) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(this.javaClass.simpleName.replace("Activity", "")) },
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
                                        toggleValues()
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Values")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Icons") },
                                    onClick = {
                                        showMenu = false
                                        toggleIcons()
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Icons")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Highlight") },
                                    onClick = {
                                        showMenu = false
                                        toggleHighlight()
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Highlight")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Pinch Zoom") },
                                    onClick = {
                                        showMenu = false
                                        togglePinchZoom()
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Pinch Zoom")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Auto Scale MinMax") },
                                    onClick = {
                                        showMenu = false
                                        toggleAutoScaleMinMax()
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Auto Scale MinMax")
                                )
                                DropdownMenuItem(
                                    text = { Text("Toggle Bar Borders") },
                                    onClick = {
                                        showMenu = false
                                        toggleBarBorders()
                                    },
                                    modifier = Modifier.testTag("menuItem_Toggle Bar Borders")
                                )
                                DropdownMenuItem(
                                    text = { Text("Animate X") },
                                    onClick = {
                                        showMenu = false
                                        animateX()
                                    },
                                    modifier = Modifier.testTag("menuItem_Animate X")
                                )
                                DropdownMenuItem(
                                    text = { Text("Animate Y") },
                                    onClick = {
                                        showMenu = false
                                        animateY()
                                    },
                                    modifier = Modifier.testTag("menuItem_Animate Y")
                                )
                                DropdownMenuItem(
                                    text = { Text("Animate XY") },
                                    onClick = {
                                        showMenu = false
                                        animateXY()
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
                // Chart
                AndroidView(
                    factory = { context ->
                        HorizontalBarChart(context).apply {
                            chart = this
                            setupChart(this)
                            setData(seekBarXValue.toInt(), seekBarYValue)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

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
                            setData(newValue.toInt(), seekBarYValue)
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
                            setData(seekBarXValue.toInt(), newValue)
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

    private fun setupChart(chart: HorizontalBarChart) {
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(entry: Entry, highlight: Highlight) {
                val bounds = RectF()
                chart.getBarBounds(entry as BarEntry, bounds)

                val data = chart.barData
                if (data != null) {
                    val position = chart.getPosition(
                        entry, data.getDataSetByIndex(highlight.dataSetIndex)?.axisDependency
                    )

                    Timber.tag("bounds $bounds")
                    Timber.tag("position $position")

                    PointF.recycleInstance(position)
                }
            }

            override fun onNothingSelected() = Unit
        })

        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.description?.isEnabled = false
        chart.setMaxVisibleValueCount(60)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)

        val xl = chart.xAxis
        xl.position = XAxisPosition.BOTTOM
        xl.typeface = tfLight
        xl.setDrawAxisLine(true)
        xl.setDrawGridLines(false)
        xl.granularity = 10f

        chart.axisLeft.apply {
            typeface = tfLight
            setDrawAxisLine(true)
            setDrawGridLines(true)
            axisMinimum = 0f

        }

        chart.axisRight.apply {
            typeface = tfLight
            setDrawAxisLine(true)
            setDrawGridLines(false)
            axisMinimum = 0f
        }

        chart.setFitBars(true)
        chart.animateY(2500)

        chart.legend?.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            formSize = 8f
            xEntrySpace = 4f
        }
    }

    private fun setData(count: Int, range: Float) {
        val localChart = chart ?: return

        val barWidth = 9f
        val spaceForBar = 10f
        val values = ArrayList<BarEntry>()
        val sampleValues = getValues(100)

        for (i in 0..<count) {
            val value = sampleValues[i]!!.toFloat() * range
            val barEntry = BarEntry(
                i * spaceForBar, value,
                ResourcesCompat.getDrawable(resources, R.drawable.star, null)
            )
            Timber.d("x=${barEntry.x} y=${barEntry.y}")
            values.add(barEntry)
        }

        val set1: BarDataSet
        val chartData = localChart.barData

        if (chartData != null && chartData.dataSetCount > 0) {
            set1 = chartData.getDataSetByIndex(0) as BarDataSet
            @Suppress("DEPRECATION")
            set1.values = values
            chartData.notifyDataChanged()
            localChart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "DataSet 1")
            set1.isDrawIcons = false

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTypeface(tfLight)
            data.barWidth = barWidth
            localChart.setData(data)
        }

        localChart.setFitBars(true)
        localChart.invalidate()
    }

    private fun toggleValues() {
        chart?.let {
            it.barData?.dataSets?.forEach {
                it.isDrawValues = !it.isDrawValues
            }
            it.invalidate()
        }
    }

    private fun toggleIcons() {
        val sets = chart?.barData?.dataSets ?: return
        for (iSet in sets) {
            iSet.isDrawIcons = !iSet.isDrawIcons
        }
        chart?.invalidate()
    }

    private fun toggleHighlight() {
        val chartData = chart?.barData
        if (chartData != null) {
            chartData.isHighlightEnabled = !chartData.isHighlightEnabled
            chart?.invalidate()
        }
    }

    private fun togglePinchZoom() {
        chart?.isPinchZoomEnabled?.let { chart?.setPinchZoom(!it) }
        chart?.invalidate()
    }

    private fun toggleAutoScaleMinMax() {
        chart?.isAutoScaleMinMaxEnabled?.let { chart?.isAutoScaleMinMaxEnabled = !it }
        chart?.notifyDataSetChanged()
    }

    private fun toggleBarBorders() {
        for (set in chart?.barData?.dataSets ?: return) {
            (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f
        }
        chart?.invalidate()
    }

    private fun animateX() {
        chart?.animateX(2000)
    }

    private fun animateY() {
        chart?.animateY(2000)
    }

    private fun animateXY() {
        chart?.animateXY(2000, 2000)
    }

}
