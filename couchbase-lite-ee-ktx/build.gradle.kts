plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – Kotlin Extensions"

kotlin {
    cocoapods {
        pod("CouchbaseLite-Enterprise") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    linkLibcblite(projects.couchbaseLiteEe)

    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLiteEe)
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupportEe)
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
