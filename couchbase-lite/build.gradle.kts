@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.DefFileTask
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

    targets.withType<KotlinNativeTarget> {
        if (konanTarget.family.isAppleFamily) {
            // Run iOS tests on background thread with main run loop
            binaries.withType<TestExecutable> {
                freeCompilerArgs += listOf("-e", "com.udobny.kmp.test.mainBackground")
            }
        } else {
            val main by compilations.getting
            val libraryPath = "$projectDir/${konanTarget.libcblitePath}/${konanTarget.libcbliteLib}"
            val libcblite by main.cinterops.creating {
                includeDirs("${konanTarget.libcblitePath}/include")
                if (konanTarget.family == Family.MINGW) {
                    extraOpts("-libraryPath", libraryPath)
                }
            }
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
        val jvmCommonTest by getting {
            dependencies {
                implementation(libs.junit)
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
        val androidInstrumentedTest by getting {
            // TODO: doesn't work, so using a symlink
            //  https://youtrack.jetbrains.com/issue/KT-53383
            //resources.srcDir("src/commonTest/resources")
            dependencies {
                implementation(libs.androidx.test.core.ktx)
                implementation(libs.androidx.test.runner)
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
    "libs/libcblite/$os/$arch/libcblite-${libs.versions.couchbase.lite.c.get()}"
