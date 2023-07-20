plugins {
    `multiplatform-convention`
    `library-convention`
}

kotlin {
    cocoapods {
        name = "Kotbase-Enterprise-KTX"
        homepage = "https://github.com/jeffdgr8/kotbase"
        authors = "Couchbase, MOLO17, Jeff Lockhart"
        license = "Custom, Apache License, Version 2.0"
        summary = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform Kotlin Extensions"
        ios.deploymentTarget = "9.0"
        osx.deploymentTarget = "10.11"
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
                api(projects.couchbaseLiteEe)
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupportEe)
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

android.namespace = "dev.kotbase.ktx"
