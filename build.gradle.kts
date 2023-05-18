@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.kotlin.gradle.targets.native.internal.CInteropMetadataDependencyTransformationTask

plugins {
    kotlin("multiplatform") version "1.9.0-Beta-187" apply false
    id("org.jetbrains.dokka") version "1.8.10" apply false
    id("com.android.library") version "7.4.2" apply false
    id("com.louiscad.complete-kotlin") version "1.1.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
    group = property("GROUP") as String
    val cblVersion = rootProject.libs.versions.couchbase.lite.java.get()
    val kmpVersion = property("VERSION") as String
    version = "$cblVersion-$kmpVersion"

    // Workaround for https://github.com/Kotlin/dokka/issues/2977.
    // We disable the C Interop IDE metadata task when generating documentation using Dokka.
    gradle.taskGraph.whenReady {
        val hasDokkaTasks = allTasks.any {
            it is AbstractDokkaTask
        }
        if (hasDokkaTasks) {
            tasks.withType<CInteropMetadataDependencyTransformationTask>().configureEach {
                enabled = false
            }
        }
    }

    // TODO: Report this if it's still a bug in 1.9.0-RC
    // Note in Kotlin 1.9.0-Beta-152 (1.8.21 works without this)
    // transformCommonMainDependenciesMetadata needs to be run first before publishToMavenLocal
    // and even still, paging modules are missing a file generated in other modules
    // build/kotlinTransformedCInteropMetadataLibraries/iosMain/.couchbase-lite-paging-iosMain.cinteropLibraries
    // copy from ktx modules and fix file path, then publishToMavenLocal will succeed
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
