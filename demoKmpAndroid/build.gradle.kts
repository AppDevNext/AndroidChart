import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
}

// Plain (non-multiplatform) Android application module: AGP 9's new KMP DSL forbids combining
// `com.android.application` with `org.jetbrains.kotlin.multiplatform`'s `androidTarget()` in the
// same Gradle project, so the actual `applicationId`/launcher-activity/manifest live here, while
// `:demoKmp` (a `com.android.kotlin.multiplatform.library` module) supplies the shared
// `DemoKmpApp()` composable and the Desktop/iOS entry points.
android {
    namespace = "info.appdev.charting.demokmp.androidapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "info.appdev.charting.demokmp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":demoKmp"))
    implementation("androidx.activity:activity-compose:1.10.1")
}
