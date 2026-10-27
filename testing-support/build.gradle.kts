plugins {
    id("multiplatform-convention")
}

kotlin {
    android.namespace = "dev.kotbase.testingsupport"

    linkLibcblite(projects.couchbaseLite)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLite)
            api(libs.kotlin.test)
            api(libs.kotlinx.serialization.json)
        }
        jvmCommonMain.dependencies {
            api(libs.kotlin.test.junit)
        }
        androidMain.dependencies {
            api(libs.androidx.test.core.ktx)
            api(libs.androidx.test.runner)
        }
        nativeMain.dependencies {
            implementation(libs.korlibs.korio)
        }
    }
}
