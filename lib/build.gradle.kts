@file:Suppress("UNUSED_VARIABLE")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
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

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
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
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
                implementation("com.squareup.okio:okio:3.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.couchbase.lite:couchbase-lite-android-ktx:3.0.0")
                //implementation(fileTree("libs"))
            }
        }
        val androidTest by getting {
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

tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        outputFile.appendText("""

                ---

                typedef uint32_t C4DocumentFlags; enum {
                    kDocDeleted         = 0x01,
                    kDocConflicted      = 0x02,
                    kDocHasAttachments  = 0x04,
                    kDocExists          = 0x1000
                };

                struct C4Document {
                    void* _internal1;
                    void* _internal2;
                
                    C4DocumentFlags flags;
                };

                @interface CBLC4Document : NSObject

                @property (readonly, nonatomic) struct C4Document* rawDoc;

                @end
            """.trimIndent())
    }
}

tasks.withType(Test::class) {
    testLogging {
        events(FAILED, PASSED, STANDARD_OUT, STANDARD_ERROR)
        exceptionFormat = FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

val copyIosX64TestResources = tasks.register<Copy>("copyIosX64TestResources") {
    from("src/iosTest/resources")
    into("build/bin/iosX64/debugTest/resources")
}

tasks.findByName("iosX64Test")!!.dependsOn(copyIosX64TestResources)
