package info.appdev.chartexample.compose

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
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
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import info.appdev.chartexample.DataTools.Companion.getValues
import info.appdev.chartexample.R
import info.appdev.chartexample.notimportant.DemoBaseCompose

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

                val data = chart.data
                if (data != null) {
                    val position = chart.getPosition(
                        entry, data.getDataSetByIndex(highlight.dataSetIndex)
                            .axisDependency
                    )

                    Log.i("bounds", bounds.toString())
                    Log.i("position", position.toString())

                    MPPointF.recycleInstance(position)
                }
            }

            override fun onNothingSelected() = Unit
        })

        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.description.isEnabled = false
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

        val l = chart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.formSize = 8f
        l.xEntrySpace = 4f
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
            Log.d("values", "x=${barEntry.x} y=${barEntry.y}")
            values.add(barEntry)
        }

        val set1: BarDataSet
        val chartData = localChart.data

        if (chartData != null && chartData.dataSetCount > 0) {
            set1 = chartData.getDataSetByIndex(0) as BarDataSet
            @Suppress("DEPRECATION")
            set1.values = values
            chartData.notifyDataChanged()
            localChart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "DataSet 1")
            set1.setDrawIcons(false)

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)

            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setValueTypeface(tfLight)
            data.barWidth = barWidth
            localChart.data = data
        }

        localChart.setFitBars(true)
        localChart.invalidate()
    }

    private fun toggleValues() {
        chart?.let {
            val sets = it.data?.dataSets ?: return
            for (iSet in sets) {
                iSet.setDrawValues(!iSet.isDrawValuesEnabled)
            }
            it.invalidate()
        }
    }

    private fun toggleIcons() {
        chart?.let {
            val sets = it.data?.dataSets ?: return
            for (iSet in sets) {
                iSet.setDrawIcons(!iSet.isDrawIconsEnabled)
            }
            it.invalidate()
        }
    }

    private fun toggleHighlight() {
        chart?.let {
            val chartData = it.data
            if (chartData != null) {
                chartData.isHighlightEnabled = !chartData.isHighlightEnabled
                it.invalidate()
            }
        }
    }

    private fun togglePinchZoom() {
        chart?.let {
            it.setPinchZoom(!it.isPinchZoomEnabled)
            it.invalidate()
        }
    }

    private fun toggleAutoScaleMinMax() {
        chart?.let {
            it.isAutoScaleMinMaxEnabled = !it.isAutoScaleMinMaxEnabled
            it.notifyDataSetChanged()
        }
    }

    private fun toggleBarBorders() {
        chart?.let {
            for (set in it.data?.dataSets ?: return) {
                (set as BarDataSet).barBorderWidth = if (set.barBorderWidth == 1f) 0f else 1f
            }
            it.invalidate()
        }
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
