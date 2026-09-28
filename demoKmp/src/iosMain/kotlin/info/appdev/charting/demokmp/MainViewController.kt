package info.appdev.charting.demokmp

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point for the `demoKmp` Compose Multiplatform demo (Step A.9), to be called from a
 * thin Swift/Xcode wrapper app's `UIHostingController`-equivalent (typically
 * `ComposeUIViewController`-based `UIViewController` embedding, e.g.
 * `UIApplication.shared.windows.first?.rootViewController = MainViewController()`).
 *
 * **Not yet done**: the actual Xcode wrapper project (`.xcodeproj`/`.xcworkspace`) that embeds
 * this framework's `MainViewController()` isn't part of this Gradle-buildable slice -- only the
 * `iosArm64`/`iosSimulatorArm64` Kotlin framework artifact is. Building/running it standalone
 * from Gradle is possible via `:demoKmp:linkDebugFrameworkIosSimulatorArm64`.
 */
@Suppress("unused")
fun MainViewController(): UIViewController = ComposeUIViewController {
    DemoKmpApp()
}
