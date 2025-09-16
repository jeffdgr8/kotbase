plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – AndroidX Paging Extensions"

kotlin {
    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase.paging")
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
            api(projects.couchbaseLiteEeKtx)
            api(libs.paging)
        }
        commonTest.dependencies {
            implementation(projects.testingSupportEe)
            implementation(libs.stately)
        }
    }
}

android {
    namespace = "dev.kotbase.paging"
    defaultConfig.minSdk = 23
}
