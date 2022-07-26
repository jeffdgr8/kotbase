@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

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
        name = project.parent!!.name
        homepage = "https://udobny.com/couchbase-lite-kmm"
        authors = "Couchbase, Udobny"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform"
        ios.deploymentTarget = "9.0"
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
        }
        pod("CouchbaseLite") {
            //version = "~> 3.0.0"
            // TODO: 3.0.2 required to fix missing classes
            //  https://forums.couchbase.com/t/cblvalueindexconfiguration-and-cblfulltextindexconfiguration-missing-from-objc-framework-for-x86-64/33815
            // 3.0.2-SNAPSHOT
            source = path("$rootDir/../couchbase-lite-ios")
            moduleName = "CouchbaseLite"
            // Workaround for 'CBLQueryMeta' is going to be declared twice https://youtrack.jetbrains.com/issue/KT-41709
            extraOpts = listOf("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")
        }
    }

    targets.withType<KotlinNativeTarget> {
        // Run tests on background thread
        // TODO: main thread loop is still not available to dispatch to though
        //  https://youtrack.jetbrains.com/issue/KT-53129
        compilations["test"].kotlinOptions {
            freeCompilerArgs += listOf("-trw")
        }
    }

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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation("org.jetbrains.kotlinx:atomicfu:0.18.2")
            }
        }
        val androidMain by getting {
            dependencies {
                // TODO: 3.0.2 required for public IndexConfiguration
                //  https://forums.couchbase.com/t/can-indexconfiguration-be-made-public/33772
                //implementation("com.couchbase.lite:couchbase-lite-android:3.0.0")
                implementation("com.couchbase.lite:couchbase-lite-android:3.0.2-SNAPSHOT")
                //implementation("com.couchbase.lite:couchbase-lite-android:3.1.0-SNAPSHOT")
                //implementation(fileTree("libs/couchbase-lite"))
            }
        }
        val androidTest by getting {
            (dependsOn as MutableSet).remove(commonTest)
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val androidAndroidTest by getting {
            // doesn't work, so using a symlink
            //resources.srcDir("src/commonTest/resources")
            dependencies {
                implementation("androidx.test:core-ktx:1.4.0")
                implementation("androidx.test:runner:1.4.0")
                implementation("androidx.test:rules:1.4.0")
                implementation("androidx.test.ext:junit-ktx:1.1.3")
            }
        }
        val iosMain by getting
        val iosTest by getting {
            dependencies {
                // TODO: update when PR released
                //  https://github.com/korlibs/korge/pull/842
                implementation("com.soywiz.korlibs.korio:korio:3.0.0-Beta6")
            }
        }
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

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        // TODO: init constructor can't be added to .def with an ObjC category
        //  https://youtrack.jetbrains.com/issue/KT-53285
        //  workaround function:
        //  static inline CBLDocument* getCBLDocument(CBLDatabase* database, NSString* documentID)

        // TODO: remove above --- pending https://github.com/JetBrains/kotlin/pull/4894 in Kotlin 1.8
        //outputFile.appendText("""
        outputFile.writeText("""
            language = Objective-C
            headers = CouchbaseLite/CouchbaseLite.h
            headerFilter = CouchbaseLite/**

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

            static inline CBLDocument* getCBLDocument(CBLDatabase* database, NSString* documentID) {
                return [[CBLDocument alloc] initWithDatabase:database documentID:documentID includeDeleted:YES error:nil];
            }

            @interface CBLDatabase ()
            - (BOOL) isClosed;
            @end

            @interface CBLQueryExpression ()
            - (id) asJSON;
            @end

            @interface CBLQueryCollation ()
            - (id) asJSON;
            @end
        """.trimIndent())
    }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        events(FAILED, PASSED)
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
