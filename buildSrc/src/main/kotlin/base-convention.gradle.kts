import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.target.Family
import rules.applyCouchbaseLiteRule

plugins {
    `kotlin-multiplatform`
    `kotlin-native-cocoapods`
    `android-library`
}

kotlin {
    cocoapods {
        version = project.version.toString()
        homepage = "https://kotbase.dev/"
        source = "{ :git => 'https://github.com/jeffdgr8/kotbase.git', :tag => $version }"
        authors = "Jeff Lockhart"
        license = "Apache License, Version 2.0"
        afterEvaluate { summary = description }
        ios.deploymentTarget = "11.0"
        osx.deploymentTarget = "10.14"
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

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

android {
    compileSdk = 34
    defaultConfig {
        minSdk = 22
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    @Suppress("UnstableApiUsage")
    testOptions {
        targetSdk = 34
    }
    // required by coroutines 1.7.0+ to avoid errors:
    // 6 files found with path 'META-INF/LICENSE.md'.
    packaging.resources.pickFirsts += "META-INF/**"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
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
