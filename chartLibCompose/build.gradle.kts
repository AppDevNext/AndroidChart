import info.git.versionHelper.getVersionText
import org.gradle.kotlin.dsl.implementation
import java.net.URI

plugins {
    id("com.android.library")
    id("maven-publish")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

android {
    namespace = "info.appdev.charting.compose"
    defaultConfig {
        minSdk = 23
        compileSdk = 36

        // VERSION_NAME no longer available as of 4.1
        // https://issuetracker.google.com/issues/158695880
        buildConfigField("String", "VERSION_NAME", "\"${getVersionText()}\"")

        consumerProguardFiles.add(File("proguard-lib.pro"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true // this prevents "not mocked" error
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.core:core:1.17.0")
    implementation("androidx.activity:activity-ktx:1.12.3")
    implementation("com.github.AppDevNext.Logcat:LogcatCoreLib:3.4")
    api(project(":chartLib"))

    // Compose dependencies
    val composeBom = platform("androidx.compose:compose-bom:2026.01.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.runtime:runtime-saveable")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

    testImplementation("junit:junit:4.13.2")
}

tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

group = "info.mxtracks"
var versionVersion = getVersionText()
println("Build version $versionVersion")

mavenPublishing {
    pom {
        name = "Android Chart"
        description =
            "A powerful Android chart view/graph view library, supporting line- bar- pie- radar- bubble- and candlestick charts as well as scaling, dragging and animations"
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
