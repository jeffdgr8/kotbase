plugins {
    id("multiplatform-convention")
    id("library-convention")
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – Kermit Logger"

kotlin {
    android.namespace = "dev.kotbase.kermit"

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
