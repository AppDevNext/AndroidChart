buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Lowering to a stable local AGP version to fix the sync environment
        classpath("com.android.tools.build:gradle:8.13.0")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}