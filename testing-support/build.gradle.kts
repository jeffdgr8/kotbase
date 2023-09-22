plugins {
    `multiplatform-convention`
    `kotlin-native-cocoapods`
}

kotlin {
    cocoapods {
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLite)
                api(libs.kotlin.test)
                api(libs.kotlin.test.junit)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.atomicfu)
                api(libs.okio)
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

android.namespace = "dev.kotbase.testingsupport"
