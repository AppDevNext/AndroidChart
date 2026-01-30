package info.appdev.chartexample

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.captureToBitmap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import info.appdev.chartexample.compose.HorizontalBarComposeActivity
import info.appdev.chartexample.compose.HorizontalBarFullComposeActivity
import info.appdev.chartexample.fragments.ViewPagerSimpleChartDemo
import info.appdev.chartexample.notimportant.ContentItem
import info.appdev.chartexample.notimportant.DemoBase
import info.appdev.chartexample.notimportant.DemoBase.Companion.optionMenus
import info.appdev.chartexample.notimportant.DemoBaseCompose
import info.appdev.chartexample.notimportant.MainActivity
import info.hannes.timber.DebugFormatTree
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import timber.log.Timber


@RunWith(AndroidJUnit4::class)
class StartTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @get:Rule
    var nameRule = TestName()

    @Before
    fun setUp() {
        Intents.init()
        Timber.plant(DebugFormatTree())
    }

    @After
    fun cleanUp() {
        Intents.release()
    }

    @Test
    fun smokeTestStart() {
        // Wait for Compose to be ready
        composeTestRule.waitForIdle()

        onView(ViewMatchers.isRoot())
            .perform(captureToBitmap { bitmap: Bitmap -> bitmap.writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}") })

        var compose: Boolean
        var optionMenu = ""
        // iterate samples - only items with classes (not section headers)
        MainActivity.menuItems.forEachIndexed { index, contentItem ->
            contentItem.clazz?.let { contentClass ->
                compose = false
                Timber.d("Intended ${index}-${contentClass.simpleName}: ${contentItem.name}")

                if (contentItem.clazz == ViewPagerSimpleChartDemo::class.java || contentItem.clazz == ListViewBarChartActivity::class.java)
                    return@forEachIndexed

                try {
                    // Use description to uniquely identify items since names can be duplicated
                    // If description exists, use it; otherwise fall back to name
                    val searchText = contentItem.desc.ifEmpty {
                        contentItem.name
                    }

                    Timber.d("Searching for #$index: '$searchText'")

                    // Scroll to the item in the LazyColumn by index
                    // This ensures the item is composed and visible
                    try {
                        composeTestRule
                            .onNodeWithTag("menuList")
                            .performScrollToIndex(index)
                        composeTestRule.waitForIdle()
                    } catch (e: Exception) {
                        Timber.w("Could not scroll to index $index: ${e.message}")
                    }

                    // Now click the item using its test tag
                    composeTestRule
                        .onNodeWithTag("menuItem_$index")
                        .assertExists("Could not find menu item at index $index")
                        .performClick()

                    // Wait for the new activity to start
                    composeTestRule.waitForIdle()
                    Thread.sleep(300) // Increased delay for activity transition

                    Intents.intended(hasComponent(contentClass.name))
                    onView(ViewMatchers.isRoot())
                        .perform(captureToBitmap { bitmap: Bitmap ->
                            bitmap.writeToTestStorage(
                                "${javaClass.simpleName}_${nameRule.methodName}-${index}-${contentClass.simpleName}-${contentItem.name}-1SampleClick"
                                    .replace(" ", "")
                            )
                        })

                    // Test option menus based on activity type
                    if (DemoBase::class.java.isAssignableFrom(contentClass)) {
                        // Test traditional ActionBar menu for DemoBase activities
                        optionMenu = ""
                        optionMenus.filter { plain -> plain.isNotEmpty() && Character.isDigit(plain.first()) }.forEach { filteredTitle ->
                            optionMenu = "$index->$filteredTitle"
                            Timber.d("optionMenu=$optionMenu")
                            openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
                            Timber.d("screenshot optionMenu=$optionMenu")
                            screenshotOfOptionMenu(
                                "${javaClass.simpleName}_${nameRule.methodName}-${index}-${contentClass.simpleName}-${contentItem.name}",
                                filteredTitle
                            )
                        }
                    } else if (DemoBaseCompose::class.java.isAssignableFrom(contentClass)) {
                        compose = true
                        // Test Compose dropdown menu for DemoBaseCompose activities
                        Timber.d("Testing Compose menu for: ${contentClass.simpleName}")
                        optionMenu = ""

                        try {
                            // Click the menu button to open dropdown
                            composeTestRule
                                .onNodeWithTag("menuButton")
                                .assertIsDisplayed()
                                .performClick()
                            composeTestRule.waitForIdle()
                            Thread.sleep(100) // Wait for dropdown to appear

                            // Define menu items to test (those starting with numbers in traditional menus)
                            val composeMenuItems = listOf(
                                "Toggle Values",
                                "Toggle Icons",
                                "Toggle Highlight",
                                "Toggle Pinch Zoom",
                                "Toggle Auto Scale MinMax",
                                "Toggle Bar Borders",
//                                "Animate X",
//                                "Animate Y",
//                                "Animate XY",
//                                "Save to Gallery"
                            )

                            composeMenuItems.forEach { menuTitle ->
                                try {
                                    optionMenu = "$index->$menuTitle"
                                    Timber.d("Testing Compose menu item: $optionMenu")

                                    // Click the menu item
                                    composeTestRule
                                        .onNodeWithTag("menuItem_$menuTitle")
                                        .performClick()
                                    composeTestRule.waitForIdle()
                                    Thread.sleep(150) // Wait for action to complete
                                    onView(ViewMatchers.isRoot())
                                        .perform(captureToBitmap { bitmap: Bitmap ->
                                            bitmap.writeToTestStorage(
                                                "${javaClass.simpleName}_${nameRule.methodName}-${index}-${contentClass.simpleName}-${contentItem.name}-${menuTitle}"
                                                    .replace(" ", "")
                                            )
                                        })

                                    // Reopen menu for next item
                                    composeTestRule
                                        .onNodeWithTag("menuButton")
                                        .performClick()
                                    composeTestRule.waitForIdle()
                                    Thread.sleep(100)
                                } catch (e: Exception) {
                                    Timber.w("Could not test menu item '$menuTitle': ${e.message}")
                                    // Try to reopen menu if it closed unexpectedly
                                    try {
                                        composeTestRule
                                            .onNodeWithTag("menuButton")
                                            .performClick()
                                        composeTestRule.waitForIdle()
                                    } catch (_: Exception) {
                                        // Menu button might not be available
                                    }
                                }
                            }

                            // Close the menu before going back
                            try {
                                // Click outside to close menu or press back
                                Espresso.pressBack()
                                composeTestRule.waitForIdle()
                            } catch (_: Exception) {
                                // Menu might already be closed
                            }

                        } catch (e: Exception) {
                            Timber.e("Error testing Compose menu: ${e.message}", e)
                        }
                    } else {
                        Timber.d("Unknown activity type: ${contentClass.simpleName}")
                    }

                    if (!compose)
                        doClickTest(index, contentClass, contentItem)

                    //Thread.sleep(100)
                    Espresso.pressBack()

                    // Wait for MainActivity to be visible again
                    composeTestRule.waitForIdle()
                    Thread.sleep(200) // Small delay for back navigation
                } catch (e: Exception) {
                    Timber.e("#$index/'${contentClass.simpleName}'->'$optionMenu' ${e.message}", e)
                    onView(ViewMatchers.isRoot())
                        .perform(captureToBitmap { bitmap: Bitmap ->
                            bitmap.writeToTestStorage(
                                "${javaClass.simpleName}_${nameRule.methodName}-${index}-${contentClass.simpleName}-Error"
                                    .replace(" ", "")
                            )
                        })
                }
            }
        }
    }

    private fun doClickTest(index: Int, contentClass: Class<out DemoBase>, contentItem: ContentItem<out DemoBase>) {
        if (contentItem.clazz == ScrollViewActivity::class.java ||
            contentItem.clazz == DynamicalAddingActivity::class.java ||
            contentItem.clazz == RealtimeLineChartActivity::class.java ||
            contentItem.clazz == LineChartTimeActivity::class.java ||
            contentItem.clazz == HorizontalBarComposeActivity::class.java ||
            contentItem.clazz == HorizontalBarFullComposeActivity::class.java ||
            contentItem.clazz == GradientActivity::class.java ||
            contentItem.clazz == TimeLineActivity::class.java
        ) {
            // These charts have less clickable area, so skip further clicks
            return
        }

        onView(withId(R.id.chart1)).perform(click())
        onView(ViewMatchers.isRoot())
            .perform(captureToBitmap { bitmap: Bitmap ->
                bitmap.writeToTestStorage(
                    "${javaClass.simpleName}_${nameRule.methodName}-${index}-${contentClass.simpleName}-${contentItem.name}-click"
                        .replace(" ", "")
                )
            })

        onView(withId(R.id.chart1)).perform(clickXY(20, 20))
        onView(ViewMatchers.isRoot())
            .perform(captureToBitmap { bitmap: Bitmap ->
                bitmap.writeToTestStorage(
                    "${javaClass.simpleName}_${nameRule.methodName}-${index}-${contentClass.simpleName}-${contentItem.name}-click2020"
                        .replace(" ", "")
                )
            })

        onView(withId(R.id.chart1)).perform(clickXY(70, 70))
        onView(ViewMatchers.isRoot())
            .perform(captureToBitmap { bitmap: Bitmap ->
                bitmap.writeToTestStorage(
                    "${javaClass.simpleName}_${nameRule.methodName}-${index}-${contentClass.simpleName}-${contentItem.name}-click7070"
                        .replace(" ", "")
                )
            })
    }

    private fun screenshotOfOptionMenu(simpleName: String, menuTitle: String) {
        onView(withText(menuTitle)).perform(click())
        Timber.d("screenshotOfOptionMenu ${menuTitle}-${simpleName}")
        onView(ViewMatchers.isRoot())
            .perform(captureToBitmap { bitmap: Bitmap ->
                bitmap.writeToTestStorage("${simpleName}-2menu-click-${menuTitle}".replace(" ", ""))
            }
            )
    }

    fun clickXY(x: Int, y: Int): ViewAction {
        return GeneralClickAction(
            Tap.SINGLE,
            { view ->
                val location = IntArray(2)
                view!!.getLocationOnScreen(location)
                val screenX = (location[0] + x).toFloat()
                val screenY = (location[1] + y).toFloat()
                floatArrayOf(screenX, screenY)
            },
            Press.FINGER,
            0, // inputDevice
            0  // deviceState
        )
    }

}
