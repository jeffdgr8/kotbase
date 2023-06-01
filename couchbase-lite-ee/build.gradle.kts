import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.tasks.DefFileTask
import org.jetbrains.kotlin.konan.target.Family

plugins {
    `multiplatform-convention`
    `library-convention`
}

kotlin {
    cocoapods {
        name = "CouchbaseLite-Enterprise-KMP"
        homepage = "https://github.com/udobny/couchbase-lite-kmp"
        authors = "Couchbase, Jeff Lockhart"
        license = "Custom, Apache License, Version 2.0"
        summary = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform"
        ios.deploymentTarget = "9.0"
        osx.deploymentTarget = "10.11"
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
            isStatic = false
        }
        pod("CouchbaseLite-Enterprise") {
            version = libs.versions.couchbase.lite.objc.get()
            moduleName = "CouchbaseLite"
            packageName = "cocoapods.CouchbaseLite"
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
            main.cinterops.create("libcblite") {
                includeDirs(libcbliteIncludePath)
                if (konanTarget.family == Family.MINGW) {
                    extraOpts("-libraryPath", "$projectDir/$libcbliteLibPath")
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
        // "Two modules in a project cannot share the same content root"
        // symlinking src dirs from ce module as workaround
        // Windows requires developer mode and git core.symlinks = true
        // https://youtrack.jetbrains.com/issue/IDEA-210311
        // https://youtrack.jetbrains.com/issue/IDEABKL-6745
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
                implementation(projects.testingSupportEe)
            }
        }
        jvmCommonMain {
            dependencies {
                compileOnly(libs.couchbase.lite.java.ee)
            }
        }
        jvmMain {
            dependencies {
                api(libs.couchbase.lite.java.ee)
            }
        }
        androidMain {
            dependencies {
                api(libs.couchbase.lite.android.ee)
                implementation(libs.androidx.startup)
            }
        }

        all {
            kotlin.srcDir("src/$name/ee")
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
