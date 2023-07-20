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
        name = "Kotbase-Enterprise-Paging"
        homepage = "https://github.com/jeffdgr8/kotbase"
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

    linkLibcblite(projects.couchbaseLiteEe)

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
                implementation(libs.kotlinx.atomicfu)
            }
        }
    }
}

android.namespace = "dev.kotbase.paging"
