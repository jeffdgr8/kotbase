plugins {
    `multiplatform-convention`
    `library-convention`
}

kotlin {
    cocoapods {
        name = "Kotbase-KTX"
        homepage = "https://github.com/jeffdgr8/kotbase"
        authors = "Couchbase, MOLO17, Jeff Lockhart"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform Kotlin Extensions"
        ios.deploymentTarget = "9.0"
        osx.deploymentTarget = "10.11"
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
                api(projects.couchbaseLite)
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupport)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.mockk)
            }
        }
        androidMain {
            dependencies {
                compileOnly(libs.androidx.lifecycle.runtime.ktx)
            }
        }
        androidInstrumentedTest {
            dependencies {
                implementation(libs.mockk.android)
            }
        }
    }
}

android.namespace = "kotbase.ktx"
