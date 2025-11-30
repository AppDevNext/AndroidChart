import info.git.versionHelper.getGitCommitCount
import info.git.versionHelper.getVersionText
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "info.appdev.chartexample"
    defaultConfig {
        applicationId = "info.appdev.chartexample"
        minSdk = 23
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
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles.addAll(listOf(getDefaultProguardFile("proguard-android.txt"), File("proguard-rules.pro")))
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
    implementation(project(":MPChartLib"))

    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
    androidTestImplementation("com.github.AppDevNext.Logcat:LogcatCoreLib:3.4")
    androidTestUtil("androidx.test.services:test-services:1.6.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")
}
