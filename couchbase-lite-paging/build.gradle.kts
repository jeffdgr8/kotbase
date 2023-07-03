plugins {
    `library-convention`
}

kotlin {
    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    linuxX64()
    mingwX64()

    cocoapods {
        name = "Kotbase-Paging"
        homepage = "https://github.com/jeffdgr8/kotbase"
        authors = "Couchbase, Jeff Lockhart"
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

    linkLibcblite(projects.couchbaseLite)

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

android.namespace = "kotbase.paging"
