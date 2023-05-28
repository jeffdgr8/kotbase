@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetHierarchy.SourceSetTree
import org.jetbrains.kotlin.gradle.targets.native.internal.CInteropMetadataDependencyTransformationTask

plugins {
    id("base-convention")
    id("org.jetbrains.dokka")
    `maven-publish`
}

kotlin {
    explicitApiWarning()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        publishLibraryVariants("release")
        instrumentedTestVariant.sourceSetTree.set(SourceSetTree.test)
        unitTestVariant.sourceSetTree.set(SourceSetTree.unitTest)
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
