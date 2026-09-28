buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Lint API version tracks AGP: AGP 9.1.1 → Lint 32.1.1
        // in module lint:
        // val lintVersion = "32.1.1"
        classpath("com.android.tools.build:gradle:9.2.1")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    }
}

plugins {
    // Declared once here (without applying) so every module that publishes
    // (chartLib, chartLibCompose, chartLibCore, chartLibComposeMultiplatform) resolves
    // the exact same plugin classloader instance. Applying the same versioned plugin
    // id independently in 3+ sibling subprojects can otherwise make Gradle load it under
    // different classloaders, which breaks the plugin's shared Maven Central build service
    // ("Cannot set the value of task ... property 'buildService' ... loaded with
    // InstrumentingVisitableURLClassLoader...").
    id("com.vanniktech.maven.publish") version "0.37.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
