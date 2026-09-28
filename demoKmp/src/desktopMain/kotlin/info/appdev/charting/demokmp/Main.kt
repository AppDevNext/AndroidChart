package info.appdev.charting.demokmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Desktop (JVM) entry point for the `demoKmp` Compose Multiplatform demo (Step A.9).
 */
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "demoKmp") {
        DemoKmpApp()
    }
}
