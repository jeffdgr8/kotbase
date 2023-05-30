@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.DefFileTask
import org.jetbrains.kotlin.konan.target.Family

plugins {
    id("multiplatform-convention")
    id("library-convention")
    kotlin("native.cocoapods")
}

kotlin {
    cocoapods {
        name = "CouchbaseLite-KMP"
        homepage = "https://github.com/udobny/couchbase-lite-kmp"
        // TODO: this isn't working
        //  https://youtrack.jetbrains.com/issue/KT-53362
        //  https://github.com/JetBrains/kotlin/pull/4909
        source = "{ :git => 'https://github.com/udobny/couchbase-lite-kmp.git', :tag => $version }"
        authors = "Couchbase, Jeff Lockhart"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform"
        ios.deploymentTarget = "9.0"
        osx.deploymentTarget = "10.11"
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
            isStatic = false
        }
        pod("CouchbaseLite") {
            version = libs.versions.couchbase.lite.objc.get()
            // use local build
            //source = path("$rootDir/../couchbase-lite-ios")
            // Workaround for 'CBLQueryMeta' is going to be declared twice
            // https://youtrack.jetbrains.com/issue/KT-41709
            extraOpts = listOf("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")
        }
    }

    useCouchbaseLiteNativeCLib()

    targets.withType<KotlinNativeTarget> {
        if (konanTarget.family.isAppleFamily) {
            // Run iOS tests on background thread with main run loop
            binaries.withType<TestExecutable> {
                freeCompilerArgs += listOf("-e", "com.udobny.kmp.test.mainBackground")
            }
        } else {
            val main by compilations.getting
            val libraryPath = "$projectDir/$libcbliteLibPath"
            val libcblite by main.cinterops.creating {
                includeDirs(libcbliteIncludePath)
                if (konanTarget.family == Family.MINGW) {
                    extraOpts("-libraryPath", libraryPath)
                }
            }
        }
    }

    /*
     * On Linux, manually install libicu-dev v54 and v66 from
     * libs/libicu-dev/linux/x86_64/libicu-dev-{v}/lib/x86_64-linux-gnu
     * as -rpath doesn't work to resolve:
     *
     * sudo cp -P libicuuc.so.54* libicui18n.so.54* libicudata.so.54* /usr/lib/x86_64-linux-gnu/
     * sudo cp -P libicuuc.so.66* libicui18n.so.66* libicudata.so.66* /usr/lib/x86_64-linux-gnu/
     */

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.okio)
                implementation(libs.kotlinx.atomicfu)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupport)
            }
        }
        val jvmCommonMain by getting {
            dependencies {
                compileOnly(libs.couchbase.lite.java)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(libs.couchbase.lite.java)
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.couchbase.lite.android)
            }
        }
    }
}

android.namespace = "com.udobny.kmp.couchbase.lite"

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        val defFile = file("src/nativeInterop/cinterop/podCouchbaseLite.def")
        outputFile.appendText(defFile.readText())
    }
}
