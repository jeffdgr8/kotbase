plugins {
    id("multiplatform-convention")
    id("library-convention")
}

description = "Couchbase Lite Community Edition for Kotlin Multiplatform – AndroidX Paging Extensions"

kotlin {
    android {
        namespace = "dev.kotbase.paging"
        minSdk = 23
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
