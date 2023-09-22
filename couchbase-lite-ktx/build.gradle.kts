plugins {
    `multiplatform-convention`
    `library-convention`
}

kotlin {
    cocoapods {
        authors = "Jeff Lockhart, MOLO17, Couchbase"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite Community Edition for Kotlin Multiplatform – Kotlin Extensions"
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
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
        jvmCommonTest {
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

android.namespace = "dev.kotbase.ktx"
