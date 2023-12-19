plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – AndroidX Paging Extensions"

kotlin {
    cocoapods {
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
        }
    }
}

android.namespace = "dev.kotbase.paging"
