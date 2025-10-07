import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.DefFileTask
import org.jetbrains.kotlin.konan.target.Family

plugins {
    `multiplatform-convention`
    `library-convention`
}

description = "Couchbase Lite Enterprise Edition for Kotlin Multiplatform"

kotlin {
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
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.io)
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
        androidInstrumentedTest.dependencies {
            implementation(libs.couchbase.lite.android.vector.search.arm64)
            implementation(libs.couchbase.lite.android.vector.search.x64)
        }

        all {
            kotlin.srcDir("src/$name/ee")
        }
    }
}

android.namespace = "dev.kotbase"

// Internal headers required for tests
tasks.named<DefFileTask>("generateDefCouchbaseLite") {
    doLast {
        val cblDefFile = file("src/nativeInterop/cinterop/podCouchbaseLite.def")
        defFile.get().asFile.appendText(cblDefFile.readText())
    }
}
