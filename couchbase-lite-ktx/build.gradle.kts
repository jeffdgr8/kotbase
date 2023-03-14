@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

kotlin {
    explicitApiWarning()

    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
        publishLibraryVariants("release")
    }
    jvm()
    ios()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    linuxX64()
    // TODO: kotlinx atomicfu, datetime, and coroutines don't support arm64 or armhf
    //  https://github.com/Kotlin/kotlinx.atomicfu/pull/193
    //  https://github.com/Kotlin/kotlinx-datetime/issues/75
    //  https://github.com/Kotlin/kotlinx.coroutines/issues/855
    //  https://github.com/square/okio/issues/1006
    //linuxArm64()
    //linuxArm32Hfp()
    mingwX64()

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
            // Workaround for 'CBLQueryMeta' is going to be declared twice
            // https://youtrack.jetbrains.com/issue/KT-41709
            extraOpts = listOf("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")
        }
    }

    targets.withType<KotlinNativeTarget> {
        if (!konanTarget.family.isAppleFamily) {
            val libraryPath = "${konanTarget.libcblitePath}/${konanTarget.libcbliteLib}"
            if (konanTarget.family == Family.LINUX) {
                binaries.getTest(DEBUG).linkerOpts += listOf("-L$libraryPath", "-lcblite", "-rpath", libraryPath)
            }
        }
        if (konanTarget.family != Family.MINGW) {
            binaries.all {
                binaryOptions["sourceInfoType"] = "libbacktrace"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":couchbase-lite"))
                api(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmIosCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                compileOnly(libs.paging)
            }
        }
        val jvmIosCommonTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.paging)
                implementation(libs.stately.concurrency)
            }
        }

        val jvmCommonTest by creating {
            dependsOn(jvmIosCommonTest)
        }
        val jvmMain by getting {
            dependsOn(jvmIosCommonMain)
        }
        val jvmTest by getting {
            dependsOn(jvmCommonTest)
            dependencies {
                implementation(libs.mockk)
            }
        }
        val androidMain by getting {
            dependsOn(jvmIosCommonMain)
            dependencies {
                compileOnly(libs.androidx.lifecycle.runtime.ktx)
            }
        }
        val androidUnitTest by getting {
            // TODO: hack no longer works in Kotlin 1.8, proposed fix for 1.9
            //(dependsOn as MutableSet).remove(commonTest)
        }
        val androidInstrumentedTest by getting {
            dependsOn(jvmCommonTest)
            dependsOn(jvmIosCommonTest)
            dependencies {
                implementation(libs.androidx.test.core.ktx)
                implementation(libs.androidx.test.runner)
                implementation(libs.mockk.android)
            }
        }

        val nativeCommonTest by creating {
            dependsOn(commonTest)
        }

        val appleTest by creating {
            dependsOn(nativeCommonTest)
        }

        val iosMain by getting {
            dependsOn(jvmIosCommonMain)
            dependencies {
                implementation(libs.paging)
            }
        }
        val iosTest by getting {
            dependsOn(appleTest)
            dependsOn(jvmIosCommonTest)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
        val macosX64Test by getting {
            dependsOn(appleTest)
        }
        val macosArm64Test by getting {
            dependsOn(appleTest)
        }

        val nativeTest by creating {
            dependsOn(nativeCommonTest)
        }

        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }
        // TODO: use linux arm builds from https://github.com/danbrough/kotlinxtras/
        //val linuxArm64Main by getting {
        //    dependsOn(nativeTest)
        //}
        //val linuxArm64Test by getting {
        //    dependsOn(nativeTest)
        //}
        //val linuxArm32HfpTest by getting {
        //    dependsOn(nativeTest)
        //}
        val mingwX64Test by getting {
            dependsOn(nativeTest)
        }
    }
}

android {
    namespace = "com.udobny.kmp.couchbase.lite.ktx"
    compileSdk = 33
    defaultConfig {
        minSdk = 22
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// Documentation Jar

val dokkaOutputDir = buildDir.resolve("dokka")

tasks.dokkaHtml.configure {
    outputDirectory.set(dokkaOutputDir)
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

publishing.publications.withType<MavenPublication> {
    artifact(javadocJar)
}

tasks.withType<KotlinNativeSimulatorTest> {
    device.set("iPhone 14")
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
        tasks.register<Copy>("copy${name.capitalize()}Resources") {
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
    "${project(":couchbase-lite").projectDir}/libs/libcblite/$os/$arch/libcblite-${libs.versions.couchbase.lite.c.get()}"
