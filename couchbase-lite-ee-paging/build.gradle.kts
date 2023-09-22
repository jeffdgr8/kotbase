plugins {
    `multiplatform-convention`
    `library-convention`
}

kotlin {
    cocoapods {
        authors = "Jeff Lockhart, Couchbase"
        license = "Apache License, Version 2.0, Custom"
        summary = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – AndroidX Paging Extensions"
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
            }
        }
    }
}

android.namespace = "dev.kotbase.paging"
