@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.DefFileTask
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

val cblVersion = property("CBL_VERSION") as String
val kmpVersion = property("KMP_VERSION") as String

group = property("GROUP") as String
version = "$cblVersion-$kmpVersion"

repositories {
    maven("https://mobile.maven.couchbase.com/maven2/dev/")
}

kotlin {
    explicitApiWarning()

    android {
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
            version = "3.0.2"//cblVersion
            moduleName = "CouchbaseLite"
            packageName = "cocoapods.CouchbaseLite"
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
                binaries.getTest(DEBUG).linkerOpts += listOf("-L$libraryPath", "-lcblite", "-rpath", libraryPath)
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

    /*
     * Source set dependency graph:
     *                                    ___________common____________
     *                                   |                             |
     *                        ______nativeCommon_____                  |
     *                       |                       |                 |
     *              _______apple______        _____native_____     jvmCommon
     *             |         |        |      |     |    |     |     |     |
     *         ___ios__  macosX64 macosArm64 |     |    |     |     |     |
     *        |    |   |                  linuxX64 | mingwX64 |  android jvm
     * iosArm64 iosX64 iosSimulatorArm64  linuxArm32Hfp linuxArm64
     */

    sourceSets {
        val commonMain by getting {
            // "Two modules in a project cannot share the same content root"
            // symlinking common source from ce module as workaround
            // Windows requires developer mode and git core.symlinks = true
            // https://youtrack.jetbrains.com/issue/IDEA-210311
            // https://youtrack.jetbrains.com/issue/IDEABKL-6745
            kotlin.srcDir("src/$name/ee")
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                // TODO: https://github.com/square/okio/pull/1123
                //api("com.squareup.okio:okio:3.3.0")
                api("com.squareup.okio:okio:3.3.0-SNAPSHOT")
            }
        }
        val commonTest by getting {
            kotlin.srcDir("src/$name/ee")
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:atomicfu:0.18.5")
            }
        }

        val jvmCommonMain by creating {
            kotlin.srcDir("src/$name/ee")
            dependsOn(commonMain)
            dependencies {
                compileOnly("com.couchbase.lite:couchbase-lite-java-ee:$cblVersion")
            }
        }
        val jvmCommonTest by creating {
            dependsOn(commonTest)
        }
        val androidMain by getting {
            kotlin.srcDir("src/$name/ee")
            dependsOn(jvmCommonMain)
            dependencies {
                api("com.couchbase.lite:couchbase-lite-android-ee:$cblVersion")
            }
        }
        val androidUnitTest by getting {
            (dependsOn as MutableSet).remove(commonTest)
        }
        val androidInstrumentedTest by getting {
            dependsOn(jvmCommonTest)
            // TODO: doesn't work, so using a symlink
            //  https://youtrack.jetbrains.com/issue/KT-53383
            //resources.srcDir("src/commonTest/resources")
            dependencies {
                implementation("androidx.test:core-ktx:1.5.0")
                implementation("androidx.test:runner:1.5.2")
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("src/$name/ee")
            dependsOn(jvmCommonMain)
            dependencies {
                api("com.couchbase.lite:couchbase-lite-java-ee:$cblVersion")
            }
        }
        val jvmTest by getting {
            dependsOn(jvmCommonTest)
        }

        val nativeCommonMain by creating {
            kotlin.srcDir("src/$name/ee")
            dependsOn(commonMain)
        }
        val nativeCommonTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio:3.4.0")
            }
        }

        val appleMain by creating {
            kotlin.srcDir("src/$name/ee")
            dependsOn(nativeCommonMain)
        }
        val appleTest by creating {
            kotlin.srcDir("src/$name/ee")
            dependsOn(nativeCommonTest)
            // TODO: doesn't work, so using a copy task
            //  https://youtrack.jetbrains.com/issue/KT-53383
            //resources.srcDir("src/commonTest/resources")
        }
        val iosMain by getting {
            dependsOn(appleMain)
        }
        val iosTest by getting {
            dependsOn(appleTest)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
        val macosX64Main by getting {
            dependsOn(appleMain)
        }
        val macosX64Test by getting {
            dependsOn(appleTest)
        }
        val macosArm64Main by getting {
            dependsOn(appleMain)
        }
        val macosArm64Test by getting {
            dependsOn(appleTest)
        }

        val nativeMain by creating {
            kotlin.srcDir("src/$name/ee")
            dependsOn(nativeCommonMain)
        }
        val nativeTest by creating {
            dependsOn(nativeCommonTest)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }
        // TODO: use linux arm builds from https://github.com/danbrough/kotlinxtras/
        //val linuxArm64Main by getting {
        //    dependsOn(nativeMain)
        //    dependencies {
        //        api("org.danbrough.kotlinx:kotlinx-datetime:0.4.0")
        //        api("org.danbrough.kotlinx:kotlinx-coroutines-core:1.6.4")
        //    }
        //}
        //val linuxArm64Test by getting {
        //    dependsOn(nativeTest)
        //    dependencies {
        //        implementation("org.danbrough.kotlinx:atomicfu:0.18.3")
        //    }
        //}
        //val linuxArm32HfpMain by getting {
        //    dependsOn(nativeMain)
        //    dependencies {
        //        api("org.danbrough.kotlinx:kotlinx-datetime:0.4.0")
        //        api("org.danbrough.kotlinx:kotlinx-coroutines-core:1.6.4")
        //    }
        //}
        //val linuxArm32HfpTest by getting {
        //    dependsOn(nativeTest)
        //    dependencies {
        //        implementation("org.danbrough.kotlinx:atomicfu:0.18.3")
        //    }
        //}
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX64Test by getting {
            dependsOn(nativeTest)
        }
    }
}

android {
    namespace = "com.udobny.kmp.couchbase.lite"
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
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

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        val defFile = file("src/nativeInterop/cinterop/podCouchbaseLite.def")
        outputFile.appendText(defFile.readText())
    }
}

if (System.getProperty("os.name") == "Linux") {
    tasks.withType<Test> {
        environment("LD_LIBRARY_PATH", "\$LD_LIBRARY_PATH:$projectDir/libs/libicu-dev/linux/x86_64/libicu-dev-54.1/lib/x86_64-linux-gnu")
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
    "libs/libcblite/$os/$arch/libcblite-3.0.2"//$cblVersion"
