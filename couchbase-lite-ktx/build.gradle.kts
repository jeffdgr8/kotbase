plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Community Edition for Kotlin Multiplatform – Kotlin Extensions"

kotlin {
    android.namespace = "dev.kotbase.ktx"

    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase.ktx")
            binaryOption("bundleVersion", version.toString())
        }
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    linkLibcblite(projects.couchbaseLite)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLite)
        }
        commonTest.dependencies {
            implementation(projects.testingSupport)
        }
        jvmCommonTest.dependencies {
            implementation(libs.mockk)
        }
        androidMain.dependencies {
            compileOnly(libs.androidx.lifecycle.runtime.ktx)
        }
        androidDeviceTest.dependencies {
            implementation(libs.mockk.android)
        }
    }
}
