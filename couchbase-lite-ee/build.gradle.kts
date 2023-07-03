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
        name = "Kotbase-Enterprise"
        homepage = "https://github.com/jeffdgr8/kotbase"
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

    linkLibcblite()

    targets.withType<KotlinNativeTarget>().configureEach {
        if (konanTarget.family.isAppleFamily) {
            // Run iOS tests on background thread with main run loop
            binaries.withType<TestExecutable>().configureEach {
                freeCompilerArgs += listOf("-e", "kotbase.test.mainBackground")
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

android.namespace = "kotbase"

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        val defFile = file("src/nativeInterop/cinterop/podCouchbaseLite.def")
        outputFile.appendText(defFile.readText())
    }
}
