@file:Suppress("UNUSED_VARIABLE")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.DefFileTask

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

version = "3.0.0"

kotlin {
    explicitApiWarning()

    android()
    ios()

    cocoapods {
        summary = "Couchbase Lite Kotlin Multiplatform"
        homepage = "https://udobny.com/couchbase-lite-kotlin"
        ios.deploymentTarget = "10.0"
        framework {
            baseName = "CouchbaseLite-KMM"
        }
        pod("CouchbaseLite", version = "~> 3.0.0", moduleName = "CouchbaseLite")
    }

    targets.withType<KotlinNativeTarget> {
        // Workaround for 'CBLQueryMeta' is going to be declared twice https://youtrack.jetbrains.com/issue/KT-41709
        compilations["main"].cinterops["CouchbaseLite"].extraOpts("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")

        // Link CocoaPods frameworks to tests binary
        binaries {
            getTest("DEBUG").apply {
                val frameworkPath = "${buildDir.absolutePath}/cocoapods/synthetic/IOS/lib/Pods/CouchbaseLite/iOS"
                linkerOpts("-F$frameworkPath")
                linkerOpts("-rpath", frameworkPath)
                linkerOpts("-framework", "CouchbaseLite")
            }
        }

        compilations["test"].kotlinOptions {
            freeCompilerArgs += listOf("-trw")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
                api("com.squareup.okio:okio:3.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.3")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.couchbase.lite:couchbase-lite-android-ktx:3.0.0")
                //implementation(fileTree("libs"))
            }
        }
        val androidTest by getting {
            (dependsOn as MutableSet).remove(commonTest)
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val androidAndroidTest by getting {
            //resources.srcDir("src/commonTest/resources")
            dependencies {
                implementation("androidx.test:core-ktx:1.4.0")
                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")
                implementation("androidx.test.ext:junit-ktx:1.1.3")
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}

android {
    namespace = "com.udobny.couchbase.lite.kmm"
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 22
        targetSdk = 32
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

/*tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        outputFile.appendText("""

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

                struct C4Document {
                    void* _internal1;
                    void* _internal2;

                    C4DocumentFlags flags;
                    FLSlice docID;
                    FLSlice revID;
                    uint64_t sequence;

                    C4Revision selectedRev;

                    C4ExtraInfo extraInfo;
                };

                @interface CBLC4Document : NSObject

                @property (readonly, nonatomic) struct C4Document* rawDoc;

                @end
            """.trimIndent())
    }
}*/

tasks.withType<Test> {
    testLogging {
        events(FAILED, PASSED, STANDARD_OUT, STANDARD_ERROR)
        exceptionFormat = FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

val copyIosX64TestResources = tasks.register<Copy>("copyIosX64TestResources") {
    from("src/commonTest/resources")
    into("build/bin/iosX64/debugTest/resources")
}

tasks.findByName("iosX64Test")!!.dependsOn(copyIosX64TestResources)
