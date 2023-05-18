@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetHierarchy.SourceSetTree
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

repositories {
    maven("https://mobile.maven.couchbase.com/maven2/dev/")
}

kotlin {
    explicitApiWarning()

    jvmToolchain(8)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        publishLibraryVariants("release")
        instrumentedTestVariant.sourceSetTree.set(SourceSetTree.test)
        unitTestVariant.sourceSetTree.set(SourceSetTree.unitTest)
    }

    jvm()
    ios()
    iosSimulatorArm64()

    cocoapods {
        name = "CouchbaseLite-Enterprise-KMP-Paging"
        homepage = "https://github.com/udobny/couchbase-lite-kmp"
        authors = "Couchbase, Jeff Lockhart"
        license = "Custom, Apache License, Version 2.0"
        summary = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform AndroidX Paging Extensions"
        ios.deploymentTarget = "9.0"
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
            isStatic = false
        }
        pod("CouchbaseLite-Enterprise") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.couchbaseLiteEeKtx)
                api(libs.paging)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(projects.testingSupportEe)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.atomicfu)
            }
        }
        val iosMain by getting
        val iosTest by getting
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
    }
}

android {
    namespace = "com.udobny.kmp.couchbase.lite.paging"
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

// Documentation Jar

val dokkaOutputDir = buildDir.resolve("dokka")

tasks.dokkaHtml.configure {
    outputDirectory.set(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

publishing.publications.withType<MavenPublication> {
    artifact(javadocJar)
}

tasks.withType<KotlinNativeSimulatorTest> {
    device.set("iPhone 14")
}
