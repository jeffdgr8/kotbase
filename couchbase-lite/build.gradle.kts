import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.DefFileTask
import org.jetbrains.kotlin.konan.target.Family

plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Community Edition for Kotlin Multiplatform"

kotlin {
    cocoapods {
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            // Workaround for 'CBLQueryMeta' is going to be declared twice
            // https://youtrack.jetbrains.com/issue/KT-41709
            extraOpts = listOf("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")
        }
    }

    linkLibcblite()

    targets.withType<KotlinNativeTarget>().configureEach {
        if (!konanTarget.family.isAppleFamily) {
            val main by compilations.getting
            main.cinterops.create("libcblite") {
                includeDirs(libcbliteIncludePath)
                if (konanTarget.family == Family.MINGW) {
                    extraOpts("-libraryPath", "$projectDir/$libcbliteLibPath")
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.io)
                implementation(libs.kotlinx.atomicfu)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupport)
            }
        }
        jvmCommonMain {
            dependencies {
                compileOnly(libs.couchbase.lite.java)
            }
        }
        jvmMain {
            dependencies {
                api(libs.couchbase.lite.java)
            }
        }
        androidMain {
            dependencies {
                api(libs.couchbase.lite.android)
                implementation(libs.androidx.startup)
            }
        }
    }
}

android.namespace = "dev.kotbase"

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        val cblDefFile = file("src/nativeInterop/cinterop/podCouchbaseLite.def")
        defFile.get().asFile.appendText(cblDefFile.readText())
    }
}
