import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose") version "1.12.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "info.appdev.charting.demokmp"
        compileSdk = 37
        minSdk = 23
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    // Same iOS target scoping as chartLibComposeMultiplatform (Step A.7): Compose Multiplatform
    // (as of 1.12.x) no longer publishes iosX64 (Intel simulator) artifacts.
    val xcfName = "demoKmpKit"
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":chartLibComposeMultiplatform"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

// Exposes `:demoKmp:run` to launch the desktop entry point (`desktopMain/Main.kt`) directly,
// without needing the separate `demoKmpAndroid` application module.
compose.desktop {
    application {
        mainClass = "info.appdev.charting.demokmp.MainKt"
    }
}
