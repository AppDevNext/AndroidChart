buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Lint API version tracks AGP: AGP 9.1.1 → Lint 32.1.1
        // in module lint:
        // val lintVersion = "32.1.1"
        classpath("com.android.tools.build:gradle:9.2.0")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
