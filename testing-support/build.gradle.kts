plugins {
    `multiplatform-convention`
    `kotlin-native-cocoapods`
}

kotlin {
    cocoapods { noPodspec() }
    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLite)
                api(libs.kotlin.test)
                api(libs.kotlin.test.junit)
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.atomicfu)
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
