import info.git.versionHelper.getVersionText
import java.net.URI

plugins {
    id("com.android.library")
    id("maven-publish")
    id("kotlin-android")
    id("com.vanniktech.maven.publish") version "0.35.0"
}

android {
    namespace = "com.github.mikephil.charting"
    defaultConfig {
        minSdk = 21
        compileSdk = 35

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
    }
    testOptions {
        unitTests.isReturnDefaultValues = true // this prevents "not mocked" error
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.core:core:1.16.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
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
