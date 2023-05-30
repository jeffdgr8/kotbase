@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("multiplatform-convention")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLite)
                api(libs.kotlin.test)
                api(libs.kotlin.test.junit)
                api(libs.kotlinx.serialization.json)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.test.core.ktx)
                api(libs.androidx.test.runner)
            }
        }
        val nativeCommonMain by getting {
            dependencies {
                implementation(libs.korlibs.korio)
            }
        }
    }
}

android.namespace = "com.udobny.kmp.couchbase.lite.testingsupport"
