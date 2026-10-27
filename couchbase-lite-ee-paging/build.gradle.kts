plugins {
    id("multiplatform-convention")
    id("library-convention")
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – AndroidX Paging Extensions"

kotlin {
    android {
        namespace = "dev.kotbase.paging"
        minSdk = 23
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
