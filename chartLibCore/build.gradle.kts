import info.git.versionHelper.getVersionText
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    id("com.vanniktech.maven.publish")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "info.appdev.charting.core"
        compileSdk = 36
        minSdk = 23
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        withHostTest {}
    }

    jvm("desktop")

    val xcfName = "chartLibCoreKit"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
        }
    }

    sourceSets {
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
        artifactId = "chartLibCore",
        version = "$versionVersion"
    )
    pom {
        name = "Android Chart core"
        description =
            "Kotlin Multiplatform core module of AndroidChart: platform-agnostic chart data model, math and formatters shared across chartLib (Android View), chartLibComposeMultiplatform (Compose Multiplatform), iOS and Desktop"
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
