plugins {
    id("multiplatform-convention")
    id("library-convention")
}

description = "Couchbase Lite Community Edition for Kotlin Multiplatform – Kermit Logger"

kotlin {
    android.namespace = "dev.kotbase.kermit"

    linkLibcblite(projects.couchbaseLite)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLiteKtx)
            api(libs.kermit)
        }
        commonTest.dependencies {
            implementation(projects.testingSupport)
            implementation(libs.stately)
        }
    }
}
