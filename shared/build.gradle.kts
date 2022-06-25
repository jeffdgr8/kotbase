plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

version = "3.0.0"

kotlin {
    explicitApiWarning()

    android()
    ios()

    cocoapods {
        summary = "Couchbase Lite Kotlin Multiplatform"
        homepage = "https://udobny.com/couchbase-lite-kotlin"
        ios.deploymentTarget = "10.0"
        framework {
            baseName = "CouchbaseLite-KMM"
        }
        pod("CouchbaseLite", version = "~> 3.0.0", moduleName = "CouchbaseLite")
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        // Workaround for 'CBLQueryMeta' is going to be declared twice https://youtrack.jetbrains.com/issue/KT-41709
        compilations["main"].cinterops["CouchbaseLite"].extraOpts("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")

        // Link CocoaPods frameworks to tests binary
        binaries {
            getTest("DEBUG").apply {
                val frameworkPath = "${buildDir.absolutePath}/cocoapods/synthetic/IOS/shared/Pods/CouchbaseLite/iOS"
                linkerOpts("-F$frameworkPath")
                linkerOpts("-rpath", frameworkPath)
                linkerOpts("-framework", "CouchbaseLite")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
                implementation("com.squareup.okio:okio:3.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.couchbase.lite:couchbase-lite-android-ktx:3.0.0")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 22
        targetSdk = 32
    }
}
