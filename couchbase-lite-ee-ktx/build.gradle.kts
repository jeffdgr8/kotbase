plugins {
    id("multiplatform-convention")
    id("library-convention")
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform – Kotlin Extensions"

kotlin {
    android.namespace = "dev.kotbase.ktx"

    linkLibcblite(projects.couchbaseLiteEe)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLiteEe)
        }
        commonTest.dependencies {
            implementation(projects.testingSupportEe)
        }
        jvmCommonTest.dependencies {
            implementation(libs.mockk)
        }
        androidMain.dependencies {
            compileOnly(libs.androidx.lifecycle.runtime.ktx)
        }
        androidDeviceTest.dependencies {
            implementation(libs.mockk.android)
        }
    }
}
