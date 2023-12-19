plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Community Edition for Kotlin Multiplatform – Kermit Logger"

kotlin {
    cocoapods {
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    linkLibcblite(projects.couchbaseLite)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLiteKtx)
            api(libs.kermit)
        }
        commonTest.dependencies {
            implementation(projects.testingSupport)
        }
    }
}

android.namespace = "dev.kotbase.kermit"
