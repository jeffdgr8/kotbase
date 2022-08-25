@file:Suppress("UNUSED_VARIABLE")

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

    jvm()
    android {
        publishLibraryVariants("release")
    }
    ios()

    cocoapods {
        name = "CouchbaseLite-KMP-KTX"
        homepage = "https://github.com/udobny/couchbase-lite-kmp"
        authors = "Couchbase, MOLO17, Jeff Lockhart"
        license = "Apache License, Version 2.0"
        summary = "Couchbase Lite for Kotlin Multiplatform Kotlin Extensions"
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":couchbase-lite"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":couchbase-lite"))
            }
        }
        // TODO: shared jvm/android source set not supported
        //  https://youtrack.jetbrains.com/issue/KT-42466
        //val jvmCommonTest by creating {
        //    dependsOn(commonTest)
        //}
        val jvmTest by getting {
            kotlin.srcDir("src/jvmCommonTest/kotlin")
            //dependsOn(jvmCommonTest)
            dependencies {
                implementation("io.mockk:mockk:1.12.5")
            }
        }
        val androidMain by getting {
            dependencies {
                compileOnly("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
            }
        }
        val androidTest by getting {
            (dependsOn as MutableSet).remove(commonTest)
        }
        val androidAndroidTest by getting {
            kotlin.srcDir("src/jvmCommonTest/kotlin")
            //dependsOn(jvmCommonTest)
            dependencies {
                implementation("androidx.test:core-ktx:1.4.0")
                implementation("androidx.test:runner:1.4.0")
                implementation("io.mockk:mockk-android:1.12.5")
            }
        }
    }
}

android {
    namespace = "com.udobny.kmp.couchbase.lite.ktx"
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
