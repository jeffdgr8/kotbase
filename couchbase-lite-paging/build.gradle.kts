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
        name = "Couchbase-Lite-KMP-Paging"
        homepage = "https://github.com/udobny/couchbase-lite-kmp"
        authors = "Jeff Lockhart"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform AndroidX Paging Extensions"
        ios.deploymentTarget = "9.0"
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
            isStatic = false
        }
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            // use local build
            //source = path("$rootDir/../couchbase-lite-ios")
            linkOnly = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLiteKtx)
                api(libs.paging)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupport)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.atomicfu)
            }
        }
    }
}

android.namespace = "com.udobny.kmp.couchbase.lite.paging"
