plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – Kermit Logger"

kotlin {
    android.namespace = "dev.kotbase.kermit"

    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase.kermit")
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
            api(libs.kermit)
        }
        commonTest.dependencies {
            implementation(projects.testingSupportEe)
            implementation(libs.stately)
        }
    }
}
