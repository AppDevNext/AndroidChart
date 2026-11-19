package info.appdev.chartexample

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w411dp-h891dp-420dpi")
class RoborazziTest {

    // Cubic bezier path rendering produces minor sub-pixel differences between
    // macOS and Linux JVMs, so allow a small tolerance for all comparisons.
    private val options = RoborazziOptions(
        compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f)
    )

    @Test
    fun barChart() {
        ActivityScenario.launch(BarChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun lineChart() {
        ActivityScenario.launch(LineChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun pieChart() {
        ActivityScenario.launch(PieChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun horizontalBarChart() {
        ActivityScenario.launch(HorizontalBarChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun bubbleChart() {
        ActivityScenario.launch(BubbleChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun scatterChart() {
        ActivityScenario.launch(ScatterChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun candleStickChart() {
        ActivityScenario.launch(CandleStickChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun combinedChart() {
        ActivityScenario.launch(CombinedChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }

    @Test
    fun radarChart() {
        ActivityScenario.launch(RadarChartActivity::class.java).use {
            onView(isRoot()).captureRoboImage(roborazziOptions = options)
        }
    }
}
