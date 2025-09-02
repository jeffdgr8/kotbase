plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Community Edition for Kotlin Multiplatform – AndroidX Paging Extensions"

kotlin {
    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase.paging")
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
            api(projects.couchbaseLiteKtx)
            api(libs.paging)
        }
        commonTest.dependencies {
            implementation(projects.testingSupport)
            implementation(libs.stately)
        }
    }
}

android {
    namespace = "dev.kotbase.paging"
    defaultConfig.minSdk = 23
}
