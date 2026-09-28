import info.git.versionHelper.getVersionText
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose") version "1.12.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10"
    id("maven-publish")
    id("com.vanniktech.maven.publish")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "info.appdev.charting.compose.multiplatform"
        compileSdk = 37
        minSdk = 23
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        withHostTest {}
    }

    jvm("desktop")

    // Compose Multiplatform (as of 1.12.x) no longer publishes iosX64 (Intel simulator)
    // artifacts for compose.ui/compose.foundation/compose.runtime, so unlike chartLibCore
    // this module only targets the two arm64 iOS targets.
    val xcfName = "chartLibComposeMultiplatformKit"
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
            api(project(":chartLibCore"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

group = project.findProperty("group")?.toString() ?: "info.AppDevNext"
var versionVersion = getVersionText()
println("Build version $versionVersion")

mavenPublishing {
    coordinates(
        groupId = project.findProperty("group")?.toString() ?: "info.AppDevNext",
        artifactId = "chartLibComposeMultiplatform",
        version = "$versionVersion"
    )
    pom {
        name = "Android Chart compose multiplatform"
        description =
            "Kotlin/Compose Multiplatform chart renderers/gesture-handling for AndroidChart, built on the portable chartLibCore data model, shared across Android, iOS and Desktop"
        inceptionYear = "2022"
        url = "https://github.com/AppDevNext/AndroidChart/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "AppDevNext"
                name = "AppDevNext"
                url = "https://github.com/AppDevNext/"
            }
        }
        scm {
            url = "https://github.com/AppDevNext/AndroidChart/"
            connection = "scm:git:git://github.com/AppDevNext/AndroidChart.git"
            developerConnection = "scm:git:ssh://git@github.com/AppDevNext/AndroidChart.git"
        }
    }

    // Github packages
    repositories {
        maven {
            version = "$versionVersion-SNAPSHOT"
            name = "GitHubPackages"
            url = URI("https://maven.pkg.github.com/AppDevNext/AndroidChart")
            credentials {
                username = System.getenv("GITHUBACTOR")
                password = System.getenv("GITHUBTOKEN")
            }
        }
    }
}
