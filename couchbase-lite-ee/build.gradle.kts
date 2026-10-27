@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Family

plugins {
    id("multiplatform-convention")
    id("library-convention")
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform"

kotlin {
    android.namespace = "dev.kotbase"

    swiftPMDependencies {
        iosMinimumDeploymentTarget = "15.0"
        macosMinimumDeploymentTarget = "13.0"
        // Only link the official Swift package, Objective-C bindings come from the CouchbaseLite cinterop below
        discoverClangModulesImplicitly = false
        swiftPackage(
            url = url("https://github.com/couchbase/couchbase-lite-swift-ee.git"),
            version = exact(libs.versions.couchbase.lite.objc.get()),
            products = listOf(product("CouchbaseLiteSwift")),
            importedClangModules = emptyList(),
        )
        swiftPackage(
            url = url("https://github.com/couchbase/couchbase-lite-vector-search-spm.git"),
            version = exact(libs.versions.couchbase.lite.vector.search.get()),
            products = listOf(product("CouchbaseLiteVectorSearch")),
            importedClangModules = emptyList(),
        )
    }

    linkLibcblite()

    targets.withType<KotlinNativeTarget>().configureEach {
        val main = compilations.getByName("main")
        if (konanTarget.family.isAppleFamily) {
            main.cinterops.create("CouchbaseLite") {
                compilerOpts("-F$couchbaseLiteObjcFrameworkSearchPath")
            }
        } else {
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
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(projects.testingSupportEe)
            implementation(libs.stately)
        }
        jvmCommonMain.dependencies {
            compileOnly(libs.couchbase.lite.java.ee)
        }
        jvmMain.dependencies {
            api(libs.couchbase.lite.java.ee)
        }
        jvmTest.dependencies {
            implementation(libs.couchbase.lite.java.vector.search)
        }
        androidMain.dependencies {
            api(libs.couchbase.lite.android.ee)
            implementation(libs.androidx.startup)
        }
        androidDeviceTest.dependencies {
            implementation(libs.couchbase.lite.android.vector.search.arm64)
            implementation(libs.couchbase.lite.android.vector.search.x64)
        }

        all {
            kotlin.srcDir("src/$name/ee")
        }
    }
}

tasks.withType<KotlinNativeTest>().configureEach {
    with (DefaultNativePlatform.getCurrentOperatingSystem()) {
        if (isLinux || isWindows) {
            environment("CBLITE_VECTOR_SEARCH_LIB_PATH", "$projectDir/$vectorSearchLibPath")
        }
    }
}
