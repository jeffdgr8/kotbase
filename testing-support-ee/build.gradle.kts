plugins {
    `multiplatform-convention`
    `kotlin-native-cocoapods`
}

kotlin {
    android.namespace = "dev.kotbase.testingsupport"

    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase.testingsupport")
            binaryOption("bundleVersion", version.toString())
        }
        pod("CouchbaseLite-Enterprise") {
            version = libs.versions.couchbase.lite.objc.get()
            linkOnly = true
        }
    }

    linkLibcblite(projects.couchbaseLiteEe)

    sourceSets {
        commonMain.dependencies {
            api(projects.couchbaseLiteEe)
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
