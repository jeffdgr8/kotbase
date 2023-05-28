@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("multiplatform-convention")
    id("library-convention")
    kotlin("native.cocoapods")
}

kotlin {
    cocoapods {
        name = "CouchbaseLite-KMP-KTX"
        homepage = "https://github.com/udobny/couchbase-lite-kmp"
        authors = "Couchbase, MOLO17, Jeff Lockhart"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform Kotlin Extensions"
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
            linkOnly = true
        }
    }

    targets.withType<KotlinNativeTarget> {
        if (!konanTarget.family.isAppleFamily) {
            val libraryPath = "${konanTarget.libcblitePath}/${konanTarget.libcbliteLib}"
            if (konanTarget.family == Family.LINUX) {
                binaries.getTest(DEBUG).linkerOpts += listOf(
                    "-L$libraryPath", "-lcblite", "-rpath", libraryPath
                )
            }
        }
        if (konanTarget.family != Family.MINGW) {
            binaries.all {
                binaryOptions["sourceInfoType"] = "libbacktrace"
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.couchbaseLite)
                api(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.testingSupport)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.mockk)
            }
        }
        val androidMain by getting {
            dependencies {
                compileOnly(libs.androidx.lifecycle.runtime.ktx)
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.test.runner)
                implementation(libs.mockk.android)
            }
        }
    }
}

android.namespace = "com.udobny.kmp.couchbase.lite.ktx"

if (System.getProperty("os.name") == "Linux") {
    tasks.withType<Test> {
        environment(
            "LD_LIBRARY_PATH",
            "\$LD_LIBRARY_PATH:$rootDir/libs/libicu-dev/linux/x86_64/libicu-dev-54.1/lib/x86_64-linux-gnu"
        )
    }
}

tasks.withType<KotlinNativeTest> {
    val dir = name.substring(0, name.lastIndex - 3)
    dependsOn(
        tasks.register<Copy>("copy${name.capitalized()}Resources") {
            from("src/commonTest/resources")
            into("build/bin/$dir/debugTest")
        }
    )
    if (dir.isWindows) {
        dependsOn(
            tasks.register<Copy>("copyLibcbliteDll") {
                from("${libcblitePath(dir.os, dir.arch)}/bin/cblite.dll")
                into("build/bin/$dir/debugTest")
            }
        )
    }
}

val KonanTarget.os: String
    get() {
        return when (family) {
            Family.LINUX -> "linux"
            Family.MINGW -> "windows"
            else -> error("Unhandled native OS: $this")
        }
    }

val KonanTarget.arch: String
    get() {
        return when (architecture) {
            Architecture.X64 -> "x86_64"
            Architecture.ARM64 -> "arm64"
            Architecture.ARM32 -> "armhf"
            else -> error("Unhandled native architecture: $this")
        }
    }

val KonanTarget.libcblitePath: String
    get() = libcblitePath(os, arch)

val KonanTarget.libcbliteLib: String
    get() {
        return when (family) {
            Family.LINUX -> when (architecture) {
                Architecture.X64 -> "lib/x86_64-linux-gnu"
                Architecture.ARM64 -> "lib/aarch64-linux-gnu"
                Architecture.ARM32 -> "lib/arm-linux-gnueabihf"
                else -> error("Unhandled native architecture: $arch")
            }
            Family.MINGW -> "lib"
            else -> error("Unhandled native OS: $os")
        }
    }

val String.isWindows: Boolean
    get() = startsWith("mingw")

val String.os: String
    get() {
        return when {
            startsWith("linux") -> "linux"
            startsWith("mingw") -> "windows"
            else -> error("Unhandled native OS: $this")
        }
    }

val String.arch: String
    get() {
        return when {
            endsWith("X64") -> "x86_64"
            endsWith("Arm64") -> "arm64"
            endsWith("Arm32") -> "armhf"
            else -> error("Unhandled native architecture: $this")
        }
    }

fun libcblitePath(os: String, arch: String): String =
    "${projects.couchbaseLite.dependencyProject.projectDir}/libs/libcblite/$os/$arch/libcblite-${libs.versions.couchbase.lite.c.get()}"
