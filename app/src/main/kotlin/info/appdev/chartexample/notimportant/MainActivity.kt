package info.appdev.chartexample.notimportant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.mikephil.charting.utils.Utils
import info.appdev.chartexample.AnotherBarActivity
import info.appdev.chartexample.BarChartActivity
import info.appdev.chartexample.BarChartActivityMultiDataset
import info.appdev.chartexample.BarChartActivitySinus
import info.appdev.chartexample.BarChartPositiveNegative
import info.appdev.chartexample.BubbleChartActivity
import info.appdev.chartexample.CandleStickChartActivity
import info.appdev.chartexample.CombinedChartActivity
import info.appdev.chartexample.CubicLineChartActivity
import info.appdev.chartexample.DynamicalAddingActivity
import info.appdev.chartexample.FilledLineActivity
import info.appdev.chartexample.HalfPieChartActivity
import info.appdev.chartexample.HorizontalBarChartActivity
import info.appdev.chartexample.InvertedLineChartActivity
import info.appdev.chartexample.LineChartActivity
import info.appdev.chartexample.LineChartActivityColored
import info.appdev.chartexample.LineChartDualAxisActivity
import info.appdev.chartexample.LineChartTimeActivity
import info.appdev.chartexample.ListViewBarChartActivity
import info.appdev.chartexample.ListViewMultiChartActivity
import info.appdev.chartexample.MultiLineChartActivity
import info.appdev.chartexample.PerformanceLineChart
import info.appdev.chartexample.PieChartActivity
import info.appdev.chartexample.PieChartRoundedActivity
import info.appdev.chartexample.PiePolylineChartActivity
import info.appdev.chartexample.R
import info.appdev.chartexample.RadarChartActivity
import info.appdev.chartexample.RealtimeLineChartActivity
import info.appdev.chartexample.ScatterChartActivity
import info.appdev.chartexample.ScrollViewActivity
import info.appdev.chartexample.SpecificPositionsLineChartActivity
import info.appdev.chartexample.StackedBarActivity
import info.appdev.chartexample.StackedBarActivityNegative
import info.appdev.chartexample.compose.HorizontalBarComposeActivity
import info.appdev.chartexample.fragments.ViewPagerSimpleChartDemo

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge and hide status bar
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Initialize the utilities
        Utils.init(this)

        setContent {
            ChartExampleTheme {
                MainScreen(
                    menuItems = menuItems,
                    onItemClick = { item ->
                        item.clazz?.let { clazz ->
                            val intent = Intent(this, clazz)
                            startActivity(intent)
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                overrideActivityTransition(
                                    OVERRIDE_TRANSITION_OPEN,
                                    R.anim.move_right_in_activity,
                                    R.anim.move_left_out_activity
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                overridePendingTransition(
                                    R.anim.move_right_in_activity,
                                    R.anim.move_left_out_activity
                                )
                            }
                        }
                    },
                    onMenuAction = { action ->
                        when (action) {
                            MenuAction.VIEW_GITHUB -> {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://github.com/AppDevNext/AndroidChart".toUri()
                                }
                                startActivity(intent)
                            }

                            MenuAction.REPORT -> {
                                val intent = Intent(
                                    Intent.ACTION_SENDTO,
                                    Uri.fromParts("mailto", "philjay.librarysup@gmail.com", null)
                                ).apply {
                                    putExtra(Intent.EXTRA_SUBJECT, "AndroidChart Issue")
                                    putExtra(Intent.EXTRA_TEXT, "Your error report here...")
                                }
                                startActivity(Intent.createChooser(intent, "Report Problem"))
                            }

                            MenuAction.WEBSITE -> {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = "http://at.linkedin.com/in/xxxxx".toUri()
                                }
                                startActivity(intent)
                            }
                        }
                    }
                )
            }
        }
    }

    companion object {
        val menuItems = ArrayList<ContentItem<out DemoBase>>().apply {
            add(ContentItem("Line Charts"))
            add(ContentItem("Basic", "Simple line chart.", LineChartActivity::class.java))
            add(ContentItem("Multiple", "Show multiple data sets.", MultiLineChartActivity::class.java))
            add(ContentItem("Dual Axis", "Line chart with dual y-axes.", LineChartDualAxisActivity::class.java))
            add(ContentItem("Inverted Axis", "Inverted y-axis.", InvertedLineChartActivity::class.java))
            add(ContentItem("Cubic", "Line chart with a cubic line shape.", CubicLineChartActivity::class.java))
            add(ContentItem("Colorful", "Colorful line chart.", LineChartActivityColored::class.java))
            add(ContentItem("Performance", "Render 30.000 data points smoothly.", PerformanceLineChart::class.java))
            add(ContentItem("Filled", "Colored area between two lines.", FilledLineActivity::class.java))

            add(ContentItem("Bar Charts"))
            add(ContentItem("Basic", "Simple bar chart.", BarChartActivity::class.java))
            add(ContentItem("Basic 2", "Variation of the simple bar chart.", AnotherBarActivity::class.java))
            add(ContentItem("Multiple", "Show multiple data sets.", BarChartActivityMultiDataset::class.java))
            add(ContentItem("Horizontal", "Render bar chart horizontally.", HorizontalBarChartActivity::class.java))
            add(ContentItem("Stacked", "Stacked bar chart.", StackedBarActivity::class.java))
            add(ContentItem("Negative", "Positive and negative values with unique colors.", BarChartPositiveNegative::class.java))
            //objects.add(ContentItem("Negative Horizontal", "demonstrates how to create a HorizontalBarChart with positive and negative values."))
            add(ContentItem("Stacked 2", "Stacked bar chart with negative values.", StackedBarActivityNegative::class.java))
            add(ContentItem("Sine", "Sine function in bar chart format.", BarChartActivitySinus::class.java))

            add(ContentItem("Pie Charts"))
            add(ContentItem("Basic", "Simple pie chart.", PieChartActivity::class.java))
            add(ContentItem("Basic", "Rounded pie chart.", PieChartRoundedActivity::class.java))
            add(ContentItem("Value Lines", "Stylish lines drawn outward from slices.", PiePolylineChartActivity::class.java))
            add(ContentItem("Half Pie", "180° (half) pie chart.", HalfPieChartActivity::class.java))
            add(
                ContentItem(
                    "Specific positions", "This demonstrates how to pass a list of specific positions for lines and labels on x and y axis",
                    SpecificPositionsLineChartActivity::class.java
                )
            )

            add(ContentItem("Other Charts"))
            add(ContentItem("Combined Chart", "Bar and line chart together.", CombinedChartActivity::class.java))
            add(ContentItem("Scatter Plot", "Simple scatter plot.", ScatterChartActivity::class.java))
            add(ContentItem("Bubble Chart", "Simple bubble chart.", BubbleChartActivity::class.java))
            add(ContentItem("Candlestick", "Simple financial chart.", CandleStickChartActivity::class.java))
            add(ContentItem("Radar Chart", "Simple web chart.", RadarChartActivity::class.java))

            add(ContentItem("Scrolling Charts"))
            add(ContentItem("Multiple", "Various types of charts as fragments.", ListViewMultiChartActivity::class.java))
            add(ContentItem("View Pager", "Swipe through different charts.", ViewPagerSimpleChartDemo::class.java))
            add(ContentItem("Tall Bar Chart", "Bars bigger than your screen!", ScrollViewActivity::class.java))
            add(ContentItem("Many Bar Charts", "More bars than your screen can handle!", ListViewBarChartActivity::class.java))

            add(ContentItem("Even More Line Charts"))
            add(ContentItem("Dynamic", "Build a line chart by adding points and sets.", DynamicalAddingActivity::class.java))
            add(ContentItem("Realtime", "Add data points in realtime.", RealtimeLineChartActivity::class.java))
            add(ContentItem("Hourly", "Uses the current time to add a data point for each hour.", LineChartTimeActivity::class.java))
            //add(new ContentItem("Realm.io Examples", "See more examples that use Realm.io mobile database."));

            add(ContentItem("Compose Horizontal"))
            add(ComposeItem("Horizontal", "Render bar chart horizontally.", HorizontalBarComposeActivity::class.java).toDemoBase())
        }
    }
}

enum class MenuAction {
    VIEW_GITHUB, REPORT, WEBSITE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    menuItems: List<ContentItem<out DemoBase>>,
    onItemClick: (ContentItem<out DemoBase>) -> Unit,
    onMenuAction: (MenuAction) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chart Examples") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View on GitHub") },
                                onClick = {
                                    showMenu = false
                                    onMenuAction(MenuAction.VIEW_GITHUB)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Report Problem") },
                                onClick = {
                                    showMenu = false
                                    onMenuAction(MenuAction.REPORT)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View Website") },
                                onClick = {
                                    showMenu = false
                                    onMenuAction(MenuAction.WEBSITE)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("menuList")
        ) {
            itemsIndexed(menuItems) { index, item ->
                MenuItem(
                    item = item,
                    onClick = { onItemClick(item) },
                    testTag = "menuItem_$index"
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    item: ContentItem<out DemoBase>,
    onClick: () -> Unit,
    testTag: String = ""
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(enabled = !item.isSection) { onClick() },
        color = if (item.isSection) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = if (item.isSection) 12.dp else 8.dp
                )
        ) {
            Text(
                text = item.name,
                fontSize = if (item.isSection) 18.sp else 16.sp,
                color = if (item.isSection) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (item.isSection) FontWeight.Medium else FontWeight.Light
            )
            if (item.desc.isNotEmpty()) {
                Text(
                    text = item.desc,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ChartExampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
