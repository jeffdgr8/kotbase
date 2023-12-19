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

    linkLibcblite(projects.couchbaseLite)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLite)
            api(libs.kotlin.test)
            api(libs.kotlin.test.junit)
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.atomicfu)
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

android.namespace = "dev.kotbase.testingsupport"
