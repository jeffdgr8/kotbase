@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("maven-publish")
}

val cblVersion = project.property("CBL_VERSION") as String

group = project.property("GROUP") as String
version = cblVersion

kotlin {
    explicitApiWarning()

    android {
        publishLibraryVariants("release")
    }
    ios()

    cocoapods {
        name = "CouchbaseLite-KMP-KTX"
        homepage = "https://github.com/udobny/couchbase-lite-kmp"
        authors = "Couchbase, MOLO17, Jeff Lockhart"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform Kotlin Extensions"
        ios.deploymentTarget = "9.0"
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
        }
        pod("CouchbaseLite") {
            //version = cblVersion
            // TODO: 3.0.2 required to fix missing classes
            //  https://forums.couchbase.com/t/cblvalueindexconfiguration-and-cblfulltextindexconfiguration-missing-from-objc-framework-for-x86-64/33815
            // 3.0.2-SNAPSHOT
            source = path("$rootDir/../couchbase-lite-ios")
            moduleName = "CouchbaseLite"
            // Workaround for 'CBLQueryMeta' is going to be declared twice
            // https://youtrack.jetbrains.com/issue/KT-41709
            extraOpts = listOf("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(project(":core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":core"))
            }
        }
        val androidMain by getting {
            dependencies {
                compileOnly("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
            }
        }
        val androidTest by getting {
            (dependsOn as MutableSet).remove(commonTest)
        }
        val androidAndroidTest by getting {
            dependencies {
                implementation("androidx.test:core-ktx:1.4.0")
                implementation("androidx.test:runner:1.4.0")
                implementation("io.mockk:mockk-android:1.12.5")
            }
        }
    }
}

android {
    namespace = "com.udobny.kmp.couchbase.lite.ktx"
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 22
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
