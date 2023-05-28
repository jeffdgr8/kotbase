plugins {
    id("library-convention")
    kotlin("native.cocoapods")
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

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
        commonMain {
            dependencies {
                api(projects.couchbaseLiteEeKtx)
                api(libs.paging)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupportEe)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.atomicfu)
            }
        }
    }
}

android.namespace = "com.udobny.kmp.couchbase.lite.paging"
