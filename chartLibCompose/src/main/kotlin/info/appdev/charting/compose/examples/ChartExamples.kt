package info.appdev.charting.compose.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import info.appdev.charting.charts.ScatterChart
import info.appdev.charting.components.Legend
import info.appdev.charting.components.XAxis
import info.appdev.charting.compose.BarChart
import info.appdev.charting.compose.LineChart
import info.appdev.charting.compose.PieChart
import info.appdev.charting.compose.RadarChart
import info.appdev.charting.compose.ScatterChart
import info.appdev.charting.compose.rememberLineChartState
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarDataSet
import info.appdev.charting.data.BarEntry
import info.appdev.charting.data.Entry
import info.appdev.charting.data.LineData
import info.appdev.charting.data.LineDataSet
import info.appdev.charting.data.PieData
import info.appdev.charting.data.PieDataSet
import info.appdev.charting.data.PieEntry
import info.appdev.charting.data.RadarData
import info.appdev.charting.data.RadarDataSet
import info.appdev.charting.data.RadarEntry
import info.appdev.charting.data.ScatterData
import info.appdev.charting.data.ScatterDataSet

/**
 * Example composable demonstrating all chart types in Compose.
 * This file serves as a reference for using the new Compose chart APIs.
 */
@Composable
fun ChartExamplesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Chart Examples", style = MaterialTheme.typography.headlineMedium)

        LineChartExample()
        BarChartExample()
        PieChartExample()
        ScatterChartExample()
        RadarChartExample()
    }
}

@Composable
fun LineChartExample() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Line Chart", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val entries = remember {
                (0..10).map { Entry(it.toFloat(), (10..50).random().toFloat()) }
            }

            val dataSet = remember(entries) {
                LineDataSet(entries.toMutableList(), "Sample Data").apply {
                    color = android.graphics.Color.BLUE
                    lineWidth = 2.5f
                    isDrawCircles = true
                    isDrawValues = false
                    isDrawFilled = true
                    fillColor = android.graphics.Color.BLUE
                    fillAlpha = 50
                }
            }

            val lineData = remember(dataSet) { LineData(dataSet) }

            var selectedValue by remember { mutableStateOf<Float?>(null) }

            LineChart(
                data = lineData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                description = "Monthly Sales",
                animationDuration = 1000,
                dragEnabled = true,
                scaleEnabled = true,
                pinchZoomEnabled = true,
                onValueSelected = { entry, _ ->
                    selectedValue = entry?.y
                },
                xAxisConfig = { xAxis ->
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.isDrawGridLines = true
                },
                leftAxisConfig = { axis ->
                    axis.axisMinimum = 0f
                },
                rightAxisConfig = { axis ->
                    axis.isEnabled = false
                },
                legend = { legend ->
                    legend.isEnabled = true
                    legend.textSize = 12f
                }
            )

            selectedValue?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Selected Value: ${"%.2f".format(it)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun BarChartExample() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bar Chart", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val entries = remember {
                (0..5).map { BarEntry(it.toFloat(), (20..80).random().toFloat()) }
            }

            val dataSet = remember(entries) {
                BarDataSet(entries.toMutableList(), "Revenue").apply {
                    color = android.graphics.Color.rgb(104, 241, 175)
                    isDrawValues = true
                    valueTextSize = 12f
                }
            }

            val barData = remember(dataSet) { BarData(dataSet) }

            BarChart(
                data = barData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                description = "Quarterly Revenue",
                backgroundColor = Color(0xFFF5F5F5),
                drawValueAboveBar = true,
                animationDuration = 1200,
                xAxisConfig = { xAxis ->
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.isDrawGridLines = false
                    xAxis.granularity = 1f
                },
                leftAxisConfig = { axis ->
                    axis.axisMinimum = 0f
                }
            )
        }
    }
}

@Composable
fun PieChartExample() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pie Chart", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val entries = remember {
                listOf(
                    PieEntry(30f, "Product A"),
                    PieEntry(25f, "Product B"),
                    PieEntry(20f, "Product C"),
                    PieEntry(15f, "Product D"),
                    PieEntry(10f, "Product E")
                )
            }

            val dataSet = remember(entries) {
                PieDataSet(entries.toMutableList(), "Sales Distribution").apply {
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
//                    valueTextColor = android.graphics.Color.WHITE
                }
            }

            val pieData = remember(dataSet) { PieData(dataSet) }

            PieChart(
                data = pieData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                drawHoleEnabled = true,
                holeRadius = 40f,
                transparentCircleRadius = 45f,
                centerText = "Market Share",
                rotationEnabled = true,
                usePercentValuesEnabled = true,
                drawEntryLabelsEnabled = true,
                animationDuration = 1500,
                legend = { legend ->
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    legend.orientation = Legend.LegendOrientation.HORIZONTAL
                    legend.setDrawInside(false)
                }
            )
        }
    }
}

@Composable
fun ScatterChartExample() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Scatter Chart", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val entries = remember {
                (0..20).map {
                    Entry(it.toFloat(), (10..50).random().toFloat())
                }
            }

            val dataSet = remember(entries) {
                ScatterDataSet(entries.toMutableList(), "Data Points").apply {
                    color = android.graphics.Color.rgb(255, 87, 34)
                    setScatterShape(ScatterChart.ScatterShape.CIRCLE)
                    scatterShapeSize = 12f
                    isDrawValues = false
                }
            }

            val scatterData = remember(dataSet) { ScatterData(dataSet) }

            ScatterChart(
                data = scatterData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                description = "Distribution Analysis",
                animationDuration = 1000,
                xAxisConfig = { xAxis ->
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                }
            )
        }
    }
}

@Composable
fun RadarChartExample() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Radar Chart", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val entries = remember {
                listOf(
                    RadarEntry(8f, "Speed"),
                    RadarEntry(7f, "Strength"),
                    RadarEntry(6f, "Defense"),
                    RadarEntry(9f, "Agility"),
                    RadarEntry(7f, "Intelligence")
                )
            }

            val dataSet = remember(entries) {
                RadarDataSet(entries.toMutableList(), "Character Stats").apply {
                    color = android.graphics.Color.rgb(103, 110, 129)
                    fillColor = android.graphics.Color.rgb(103, 110, 129)
                    isDrawFilled = true
                    fillAlpha = 100
                    lineWidth = 2f
                    isDrawValues = true
                }
            }

            val radarData = remember(dataSet) { RadarData(mutableListOf(dataSet)) }

            RadarChart(
                data = radarData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                description = "Performance Metrics",
                rotationEnabled = true,
                webLineWidth = 1.5f,
                webLineWidthInner = 0.75f,
                webAlpha = 100,
                animationDuration = 1000,
                xAxisConfig = { xAxis ->
                    xAxis.textSize = 12f
                }
            )
        }
    }
}

/**
 * Example showing state management with dynamic updates
 */
@Composable
fun DynamicChartExample() {
    val state = rememberLineChartState()
    var dataPoints by remember { mutableStateOf(10) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Dynamic Chart", style = MaterialTheme.typography.titleMedium)

        LineChart(
            data = state.data,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            state = state
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                if (dataPoints > 5) dataPoints--
            }) {
                Text("- Points")
            }

            Text("Points: $dataPoints")

            Button(onClick = {
                if (dataPoints < 20) dataPoints++
            }) {
                Text("+ Points")
            }
        }

        Button(
            onClick = {
                val entries = (0 until dataPoints).map {
                    Entry(it.toFloat(), (10..50).random().toFloat())
                }
                val dataSet = LineDataSet(entries.toMutableList(), "Random Data")
                state.data = LineData(dataSet)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh Data")
        }
    }
}

