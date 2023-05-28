import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.konan.target.Family
import rules.applyCouchbaseLiteRule

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain(8)

    sourceSets.all {
        languageSettings {
            optIn("kotlin.ExperimentalStdlibApi")
            optIn("kotlin.ExperimentalUnsignedTypes")
            optIn("kotlinx.cinterop.BetaInteropApi")
            optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    targets.withType<KotlinNativeTarget> {
        if (konanTarget.family != Family.MINGW) {
            binaries.all {
                binaryOptions["sourceInfoType"] = "libbacktrace"
            }
        }
    }
}

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 22
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // required until AGP 8.1.0-alpha09+
    // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<KotlinNativeSimulatorTest> {
    device.set("iPhone 14")
}

dependencies {
    components {
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java", "com.couchbase.lite:couchbase-lite-android")
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java-ee", "com.couchbase.lite:couchbase-lite-android-ee")
    }
}
