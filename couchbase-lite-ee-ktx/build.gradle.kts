plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – Kotlin Extensions"

kotlin {
    android.namespace = "dev.kotbase.ktx"

    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase.ktx")
            binaryOption("bundleVersion", version.toString())
        }
        pod("CouchbaseLite-Enterprise") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    linkLibcblite(projects.couchbaseLiteEe)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLiteEe)
        }
        commonTest.dependencies {
            implementation(projects.testingSupportEe)
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
