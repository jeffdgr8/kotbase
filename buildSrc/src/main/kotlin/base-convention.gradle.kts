import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.target.Family
import rules.applyCouchbaseLiteRule

plugins {
    `kotlin-multiplatform`
    `kotlin-native-cocoapods`
    com.android.kotlin.multiplatform.library
    org.jetbrains.kotlinx.atomicfu
}

kotlin {
    android {
        compileSdk = 36
        minSdk = 22
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            targetSdk {
                version = release(36)
            }
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        // required by coroutines 1.7.0+ to avoid errors:
        // 6 files found with path 'META-INF/LICENSE.md'
        packaging.resources.pickFirsts += "META-INF/**"
    }

    cocoapods {
        version = project.version.toString()
        homepage = "https://kotbase.dev/"
        source = "{ :git => 'https://github.com/jeffdgr8/kotbase.git', :tag => $version }"
        authors = "Jeff Lockhart"
        license = "Apache License, Version 2.0"
        afterEvaluate { summary = description }
        ios.deploymentTarget = "12.0"
        osx.deploymentTarget = "12.0"
        noPodspec()
    }

    compilerOptions.freeCompilerArgs.addAll(
        "-Xexpect-actual-classes",
        "-Xdont-warn-on-error-suppression"
    )

    sourceSets.configureEach {
        languageSettings {
            optIn("kotlin.time.ExperimentalTime")
            if (!name.startsWith("common") &&
                !name.startsWith("jvm") &&
                !name.startsWith("android")
            ) {
                optIn("kotlinx.cinterop.BetaInteropApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
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

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

dependencies {
    components {
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java", "com.couchbase.lite:couchbase-lite-android")
        applyCouchbaseLiteRule("com.couchbase.lite:couchbase-lite-java-ee", "com.couchbase.lite:couchbase-lite-android-ee")
    }
}

// work around native compiler and linker warnings in tests
// duplicate library name: org.jetbrains.kotlinx:kotlinx-serialization-core
// https://youtrack.jetbrains.com/issue/KT-51110
// (leave off by default, enable to avoid warnings)
/*
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.name == "kotlinx-serialization-core") {
            val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()
            useVersion(libs.versions.kotlinx.serialization.get())
        }
    }
}//*/
