import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Lint API version tracks AGP: AGP 9.1.1 → Lint 32.1.1
val lintVersion = "32.1.1"

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$lintVersion")
    compileOnly("com.android.tools.lint:lint-checks:$lintVersion")

    testImplementation("com.android.tools.lint:lint:$lintVersion")
    testImplementation("com.android.tools.lint:lint-tests:$lintVersion")
    testImplementation("junit:junit:4.13.2")
}

// The manifest attribute tells the Android Lint tooling which IssueRegistry to load.
tasks.jar {
    manifest {
        attributes["Lint-Registry-v2"] = "info.appdev.charting.lint.LintRegistry"
    }
}

// Expose a single-artifact configuration so library modules can use lintPublish.
// lintPublish requires exactly one JAR — the default project() dependency
// resolves to multiple artifacts and causes "Found more than one jar" errors.
configurations {
    create("lintJar")
}
artifacts {
    add("lintJar", tasks.named("jar"))
}
