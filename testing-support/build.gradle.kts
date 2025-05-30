plugins {
    `multiplatform-convention`
    `kotlin-native-cocoapods`
}

kotlin {
    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase.testingsupport")
            binaryOption("bundleVersion", version.toString())
        }
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
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.atomicfu)
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

android.namespace = "dev.kotbase.testingsupport"
