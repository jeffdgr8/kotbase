@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

plugins {
    id("multiplatform-convention")
    id("library-convention")
}

description = "Couchbase Lite Community Edition for Kotlin Multiplatform"

kotlin {
    android.namespace = "dev.kotbase"

    swiftPMDependencies {
        iosMinimumDeploymentTarget = "15.0"
        macosMinimumDeploymentTarget = "13.0"
        // Only link the official Swift package, Objective-C bindings come from the CouchbaseLite cinterop below
        discoverClangModulesImplicitly = false
        swiftPackage(
            url = url("https://github.com/couchbase/couchbase-lite-swift.git"),
            version = exact(libs.versions.couchbase.lite.objc.get()),
            products = listOf(product("CouchbaseLiteSwift")),
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
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(projects.testingSupport)
            implementation(libs.stately)
        }
        jvmCommonMain.dependencies {
            compileOnly(libs.couchbase.lite.java)
        }
        jvmMain.dependencies {
            api(libs.couchbase.lite.java)
        }
        androidMain.dependencies {
            api(libs.couchbase.lite.android)
            implementation(libs.androidx.startup)
        }

        all {
            kotlin.srcDir("src/$name/ce")
        }
    }
}
