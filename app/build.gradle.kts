import info.git.versionHelper.getGitCommitCount
import info.git.versionHelper.getVersionText
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
}

android {
    namespace = "info.appdev.chartexample"
    defaultConfig {
        applicationId = "info.appdev.chartexample"
        minSdk = 24
        compileSdk = 36
        targetSdk = 36
        versionCode = getGitCommitCount()
        versionName = getVersionText()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments.putAll(
            mapOf(
                "useTestStorageService" to "true",
            ),
        )
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
        viewBinding = true
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles.addAll(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), File("proguard-rules.pro")))
        }
    }
    // https://stackoverflow.com/a/67635863/1079990
    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation(project(":chartLib"))
    implementation("androidx.window:window:1.5.1")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2026.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose dependencies
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.github.AppDevNext.Logcat:LogcatCoreLib:3.4")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Compose testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
    androidTestImplementation("com.github.AppDevNext.Logcat:LogcatCoreLib:3.4")
    androidTestUtil("androidx.test.services:test-services:1.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
}
