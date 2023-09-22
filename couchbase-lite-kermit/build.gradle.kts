plugins {
    `multiplatform-convention`
    `library-convention`
}

kotlin {
    cocoapods {
        authors = "Jeff Lockhart, Couchbase"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite Community Edition for Kotlin Multiplatform – Kermit Logger"
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    linkLibcblite(projects.couchbaseLite)

    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLiteKtx)
                api(libs.kermit)
            }
        }
    }
}

android.namespace = "dev.kotbase.kermit"
