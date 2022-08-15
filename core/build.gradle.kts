@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.DefFileTask

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.dokka") version "1.7.10"
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
    ios()

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
        framework {
            baseName = this@cocoapods.name.replace('-', '_')
        }
        pod("CouchbaseLite") {
            version = cblVersion
            // use local build
            //source = path("$rootDir/../couchbase-lite-ios")
            moduleName = "CouchbaseLite"
            // Workaround for 'CBLQueryMeta' is going to be declared twice
            // https://youtrack.jetbrains.com/issue/KT-41709
            extraOpts = listOf("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")
        }
    }

    targets.withType<KotlinNativeTarget> {
        // Run iOS tests on background thread with main run loop
        compilations["test"].kotlinOptions {
            freeCompilerArgs += listOf("-e", "com.udobny.kmp.test.mainBackground")
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
                implementation("org.jetbrains.kotlinx:atomicfu:0.18.2")
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.couchbase.lite:couchbase-lite-android:$cblVersion")
                // use local build
                //api("com.couchbase.lite:couchbase-lite-android:3.1.0-SNAPSHOT")
                //api(fileTree("libs/couchbase-lite"))
            }
        }
        val androidTest by getting {
            (dependsOn as MutableSet).remove(commonTest)
        }
        val androidAndroidTest by getting {
            // TODO: doesn't work, so using a symlink
            //  https://youtrack.jetbrains.com/issue/KT-53383
            //resources.srcDir("src/commonTest/resources")
            dependencies {
                implementation("androidx.test:core-ktx:1.4.0")
                implementation("androidx.test:runner:1.4.0")
            }
        }
        val iosMain by getting
        val iosTest by getting {
            // TODO: doesn't work, so using a copy task
            //  https://youtrack.jetbrains.com/issue/KT-53383
            //resources.srcDir("src/commonTest/resources")
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio:3.0.0-Beta7")
            }
        }
    }
}

android {
    namespace = "com.udobny.kmp.couchbase.lite"
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 22
        targetSdk = 33
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

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        // TODO: remove above --- and append, pending
        //  https://github.com/JetBrains/kotlin/pull/4894 in Kotlin 1.8
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

tasks.findByName("iosX64Test")?.dependsOn(
    tasks.register<Copy>("copyIosX64TestResources") {
        from("src/commonTest/resources")
        into("build/bin/iosX64/debugTest/resources")
    }
)

tasks.findByName("iosSimulatorArm64Test")?.dependsOn(
    tasks.register<Copy>("copyIosSimulatorArm64Resources") {
        from("src/commonTest/resources")
        into("build/bin/iosSimulatorArm64/debugTest/resources")
    }
)
