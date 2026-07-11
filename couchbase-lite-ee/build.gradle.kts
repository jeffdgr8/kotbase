import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.DefFileTask
import org.jetbrains.kotlin.konan.target.Family

plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform"

// Vector Search 2.x is distributed only as an xcframework zip, not via the
// CocoaPods spec repo. Download it and generate a local podspec to consume.
val vectorSearchVersion = libs.versions.couchbase.lite.vector.search.get()
val vectorSearchPodDir = layout.buildDirectory
    .dir("vectorSearchPod/$vectorSearchVersion").get().asFile

val downloadVectorSearchFramework by tasks.registering {
    outputs.dir(vectorSearchPodDir)
    doLast {
        if (!vectorSearchPodDir.resolve("CouchbaseLiteVectorSearch.xcframework").exists()) {
            vectorSearchPodDir.mkdirs()
            val zip = vectorSearchPodDir.resolve("vector-search.zip")
            uri(
                "https://packages.couchbase.com/releases/couchbase-lite-vector-search" +
                    "/$vectorSearchVersion/couchbase-lite-vector-search_xcframework_$vectorSearchVersion.zip"
            ).toURL().openStream().use { input ->
                zip.outputStream().use { input.copyTo(it) }
            }
            // unzip preserves the framework's internal symlinks (zipTree does not)
            ProcessBuilder("unzip", "-q", "-o", zip.absolutePath, "-d", vectorSearchPodDir.absolutePath)
                .inheritIO().start().waitFor().let { check(it == 0) { "unzip failed: $it" } }
            zip.delete()
        }
        vectorSearchPodDir.resolve("CouchbaseLiteVectorSearch.podspec").writeText(
            """
            Pod::Spec.new do |s|
              s.name = 'CouchbaseLiteVectorSearch'
              s.version = '$vectorSearchVersion'
              s.summary = 'Couchbase Lite Vector Search extension'
              s.homepage = 'https://www.couchbase.com/'
              s.license = { :type => 'Couchbase Inc. License Agreement' }
              s.author = 'Couchbase'
              s.source = { :path => '.' }
              s.vendored_frameworks = 'CouchbaseLiteVectorSearch.xcframework'
              s.ios.deployment_target = '15.0'
              s.osx.deployment_target = '13.0'
            end
            """.trimIndent()
        )
    }
}

tasks.matching { it.name.startsWith("podInstall") || it.name.startsWith("podGen") }.configureEach {
    dependsOn(downloadVectorSearchFramework)
}

kotlin {
    android.namespace = "dev.kotbase"

    cocoapods {
        framework {
            binaryOption("bundleId", "dev.kotbase")
            binaryOption("bundleVersion", version.toString())
        }
        pod("CouchbaseLite-Enterprise") {
            version = libs.versions.couchbase.lite.objc.get()
            moduleName = "CouchbaseLite"
            packageName = "cocoapods.CouchbaseLite"
            // Workaround for 'CBLQueryMeta' is going to be declared twice
            // https://youtrack.jetbrains.com/issue/KT-41709
            extraOpts = listOf("-compiler-option", "-DCBLQueryMeta=CBLQueryMetaUnavailable")
        }
        pod("CouchbaseLiteVectorSearch") {
            version = libs.versions.couchbase.lite.vector.search.get()
            // Vector Search 2.x is not published to the CocoaPods spec repo:
            // downloadVectorSearchFramework fetches the xcframework and
            // generates a local podspec
            source = path(vectorSearchPodDir)
            linkOnly = true
        }
    }

    linkLibcblite()

    targets.withType<KotlinNativeTarget>().configureEach {
        if (!konanTarget.family.isAppleFamily) {
            val main by compilations.getting
            main.cinterops.create("libcblite") {
                includeDirs(libcbliteIncludePath)
                if (konanTarget.family == Family.MINGW) {
                    extraOpts("-libraryPath", "$projectDir/$libcbliteLibPath")
                }
            }
        }
    }

    sourceSets {
        // "Two modules in a project cannot share the same content root"
        // symlinking src dirs from ce module as workaround
        // Windows requires developer mode and git core.symlinks = true
        // https://youtrack.jetbrains.com/issue/IDEA-210311
        // https://youtrack.jetbrains.com/issue/IDEABKL-6745
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.io)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(projects.testingSupportEe)
            implementation(libs.stately)
        }
        jvmCommonMain.dependencies {
            compileOnly(libs.couchbase.lite.java.ee)
        }
        jvmMain.dependencies {
            api(libs.couchbase.lite.java.ee)
        }
        jvmTest.dependencies {
            implementation(libs.couchbase.lite.java.vector.search)
        }
        androidMain.dependencies {
            api(libs.couchbase.lite.android.ee)
            implementation(libs.androidx.startup)
        }
        androidDeviceTest.dependencies {
            implementation(libs.couchbase.lite.android.vector.search.arm64)
            implementation(libs.couchbase.lite.android.vector.search.x64)
        }

        all {
            kotlin.srcDir("src/$name/ee")
        }
    }
}

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        val cblDefFile = file("src/nativeInterop/cinterop/podCouchbaseLite.def")
        defFile.get().asFile.appendText(cblDefFile.readText())
    }
}

tasks.withType<KotlinNativeTest>().configureEach {
    with (DefaultNativePlatform.getCurrentOperatingSystem()) {
        if (isLinux || isWindows) {
            environment("CBLITE_VECTOR_SEARCH_LIB_PATH", "$projectDir/$vectorSearchLibPath")
        }
    }
}
