package info.appdev.charting.demokmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Android entry point for the `demoKmp` Compose Multiplatform demo (Step A.9). Lives in its own
 * plain `com.android.application` module (`demoKmpAndroid`), separate from the KMP `demoKmp`
 * library module, because AGP 9's new KMP DSL forbids combining `com.android.application` with
 * `org.jetbrains.kotlin.multiplatform`'s `androidTarget()` in the same Gradle project.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoKmpApp()
        }
    }
}
