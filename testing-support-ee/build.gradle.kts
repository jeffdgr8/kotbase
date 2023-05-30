plugins {
    id("multiplatform-convention")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLiteEe)
                api(libs.kotlin.test)
                api(libs.kotlin.test.junit)
                api(libs.kotlinx.serialization.json)
            }
        }
        androidMain {
            dependencies {
                api(libs.androidx.test.core.ktx)
                api(libs.androidx.test.runner)
            }
        }
        nativeCommonMain {
            dependencies {
                implementation(libs.korlibs.korio)
            }
        }
    }
}

android.namespace = "com.udobny.kmp.couchbase.lite.testingsupport"
