@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetHierarchy.SourceSetTree
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

kotlin {
    jvmToolchain(8)

    androidTarget {
        publishLibraryVariants("release")
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.android {
        instrumentedTest.sourceSetTree.set(SourceSetTree.test)
        unitTest.sourceSetTree.set(SourceSetTree.unitTest)
    }

    jvm()
    ios()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.couchbaseLite)
                api(libs.kotlin.test)
                api(libs.kotlin.test.junit)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.test.core.ktx)
            }
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val iosMain by getting {
            dependsOn(nativeMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val macosX64Main by getting {
            dependsOn(nativeMain)
        }
        val macosArm64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
    }
}

android {
    namespace = "com.udobny.kmp.couchbase.lite.testingsupport"
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
