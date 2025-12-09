package info.appdev.chartexample

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.captureToBitmap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import info.appdev.chartexample.notimportant.DemoBase.Companion.optionMenus
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

        var optionMenu = ""
        // iterate samples - only items with classes (not section headers)
        MainActivity.menuItems.forEachIndexed { index, contentItem ->
            contentItem.clazz?.let {
                Timber.d("Intended ${index}-${it.simpleName}: ${contentItem.name}")

                try {
                    // Use description to uniquely identify items since names can be duplicated
                    // If description exists, use it; otherwise fall back to name
                    val searchText = if (contentItem.desc.isNotEmpty()) {
                        contentItem.desc
                    } else {
                        contentItem.name
                    }

                    Timber.d("Searching for index $index: $searchText")

                    // Scroll to the item in the LazyColumn by index
                    // This ensures the item is composed and visible
                    try {
                        composeTestRule.onNodeWithTag("menuList")
                            .performScrollToIndex(index)
                        composeTestRule.waitForIdle()
                    } catch (e: Exception) {
                        Timber.w("Could not scroll to index $index: ${e.message}")
                    }

                    // Now click the item using its test tag
                    composeTestRule.onNodeWithTag("menuItem_$index")
                        .assertExists("Could not find menu item at index $index")
                        .performClick()

                    // Wait for the new activity to start
                    composeTestRule.waitForIdle()
                    Thread.sleep(300) // Increased delay for activity transition

                    Intents.intended(hasComponent(it.name))
                    onView(ViewMatchers.isRoot())
                        .perform(captureToBitmap { bitmap: Bitmap -> bitmap.writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-${index}-${it.simpleName}-${contentItem.name}-1SampleClick") })

                    optionMenu = ""
                    optionMenus.filter { plain -> Character.isDigit(plain.first()) }.forEach { filteredTitle ->
                        optionMenu = "$index->$filteredTitle"
                        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
                        screenshotOfOptionMenu("${javaClass.simpleName}_${nameRule.methodName}-${index}-${it.simpleName}-${contentItem.name}", filteredTitle)
                    }

                    //Thread.sleep(100)
                    Espresso.pressBack()

                    // Wait for MainActivity to be visible again
                    composeTestRule.waitForIdle()
                    Thread.sleep(200) // Small delay for back navigation
                } catch (e: Exception) {
                    Timber.e("Error at index $index: $optionMenu - ${e.message}", e)
                    onView(ViewMatchers.isRoot())
                        .perform(captureToBitmap { bitmap: Bitmap -> bitmap.writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-${index}-${it.simpleName}-Error") })
                }
            }
        }
    }

    private fun screenshotOfOptionMenu(simpleName: String, menuTitle: String) {
        onView(withText(menuTitle)).perform(click())
        Timber.d("screenshotOfOptionMenu ${menuTitle}-${simpleName}")
        onView(ViewMatchers.isRoot())
            .perform(captureToBitmap { bitmap: Bitmap -> bitmap.writeToTestStorage("${simpleName}-2menu-click-$menuTitle") })
    }

}
