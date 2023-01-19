@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
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

val cblVersion = project.property("VERSION") as String

group = project.property("GROUP") as String
version = cblVersion

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
            version = "3.0.2"//cblVersion
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
            compilations["test"].kotlinOptions {
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
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                // TODO: https://github.com/square/okio/pull/1123
                //api("com.squareup.okio:okio:3.2.0")
                api("com.squareup.okio:okio:3.3.0-SNAPSHOT")
                //api(fileTree("libs/okio"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:atomicfu:0.18.5")
            }
        }

        val jvmCommonMain by creating {
            dependsOn(commonMain)
            dependencies {
                compileOnly("com.couchbase.lite:couchbase-lite-java:$cblVersion")
            }
        }
        val jvmCommonTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val androidMain by getting {
            dependsOn(jvmCommonMain)
            dependencies {
                api("com.couchbase.lite:couchbase-lite-android:$cblVersion")
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
            dependsOn(jvmCommonMain)
            dependencies {
                api("com.couchbase.lite:couchbase-lite-java:$cblVersion")
            }
        }
        val jvmTest by getting {
            dependsOn(jvmCommonTest)
        }

        val nativeCommonMain by creating {
            dependsOn(commonMain)
        }
        val nativeCommonTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio:3.2.0")
            }
        }

        val appleMain by creating {
            dependsOn(nativeCommonMain)
        }
        val appleTest by creating {
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
        outputFile.appendText(
            """

            ---

            typedef struct FLSlice {
                const void* buf;
                size_t size;
            } FLSlice;

            typedef uint32_t C4DocumentFlags; enum {
                kDocDeleted         = 0x01,
                kDocConflicted      = 0x02,
                kDocHasAttachments  = 0x04,
                kDocExists          = 0x1000
            };

            typedef uint8_t C4RevisionFlags; enum {
                kRevDeleted        = 0x01,
                kRevLeaf           = 0x02,
                kRevNew            = 0x04,
                kRevHasAttachments = 0x08,
                kRevKeepBody       = 0x10,
                kRevIsConflict     = 0x20,
                kRevClosed         = 0x40,
                kRevPurged         = 0x80
            };

            typedef struct C4Revision {
                FLSlice revID;
                C4RevisionFlags flags;
                uint64_t sequence;
            } C4Revision;

            typedef struct C4ExtraInfo {
                void* pointer;
                void (* destructor)(void *ptr);
            } C4ExtraInfo;

            typedef struct C4Document {
                void* _internal1;
                void* _internal2;

                C4DocumentFlags flags;
                FLSlice docID;
                FLSlice revID;
                uint64_t sequence;

                C4Revision selectedRev;

                C4ExtraInfo extraInfo;
            } C4Document;

            @interface CBLC4Document : NSObject
            @property (readonly, nonatomic) C4Document* rawDoc;
            @property (readonly, nonatomic) C4RevisionFlags revFlags;
            @end

            @interface CBLDocument ()
            @property (atomic, nullable) CBLC4Document* c4Doc;
            @property (nonatomic, readonly) NSUInteger generation;
            - (nullable instancetype) initWithDatabase: (CBLDatabase*)database
                                            documentID: (NSString*)documentID
                                        includeDeleted: (BOOL)includeDeleted
                                                 error: (NSError**)outError;
            @end

            @interface CBLDatabase ()
            - (BOOL) isClosed;
            @end

            @interface CBLQueryExpression ()
            - (id) asJSON;
            @end

            @interface CBLQueryCollation ()
            - (id) asJSON;
            @end
        """.trimIndent()
        )
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
