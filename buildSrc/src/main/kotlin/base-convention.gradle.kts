import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.target.Family
import rules.applyCouchbaseLiteRule

plugins {
    `kotlin-multiplatform`
    `android-library`
}

kotlin {
    sourceSets.configureEach {
        languageSettings {
            if (!name.startsWith("common") &&
                !name.startsWith("jvm") &&
                !name.startsWith("android")
            ) {
                optIn("kotlinx.cinterop.BetaInteropApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        if (konanTarget.family.isAppleFamily) {
            // Run Apple tests on background thread with main run loop
            binaries.withType<TestExecutable>().configureEach {
                freeCompilerArgs += listOf("-e", "kotbase.test.mainBackground")
            }
        }
        if (konanTarget.family != Family.MINGW) {
            binaries.configureEach {
                binaryOptions["sourceInfoType"] = "libbacktrace"
            }
        }
        binaries.getTest(DEBUG).linkTaskProvider.configure {
            doLast {
                val outputDir = outputFile.get().parentFile
                projectDir.resolve("src/commonTest/resources").listFiles()?.forEach { file ->
                    file.copyRecursively(outputDir.resolve(file.name), overwrite = true)
                }
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

android {
    compileSdk = 33
    defaultConfig {
        minSdk = 22
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    // required by coroutines 1.7.0+ to avoid errors:
    // 6 files found with path 'META-INF/LICENSE.md'.
    packagingOptions.resources.pickFirsts += "META-INF/**"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    device.set("iPhone 14")
}

dependencies {
    components {
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java", "com.couchbase.lite:couchbase-lite-android")
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java-ee", "com.couchbase.lite:couchbase-lite-android-ee")
    }
}

// work around native compiler and linker warnings in tests
// https://youtrack.jetbrains.com/issue/KT-51110
// (leave off by default, enable to avoid warnings)
/*
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-serialization-core") {
            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()
            useVersion(libs.versions.kotlinx.serialization.get())
        }
    }
}//*/
