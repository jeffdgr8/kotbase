import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetHierarchy.SourceSetTree

plugins {
    id("base-convention")
    `kotlin-native-cocoapods`
    org.jetbrains.dokka
    `maven-publish`
    org.jetbrains.kotlinx.kover
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

val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

publishing.publications.withType<MavenPublication>().configureEach {
    artifact(javadocJar)
}

// Workaround for https://github.com/Kotlin/dokka/issues/2977.
tasks.withType<AbstractDokkaTask>().configureEach {
    val className = "org.jetbrains.kotlin.gradle.targets.native.internal.CInteropMetadataDependencyTransformationTask"
    @Suppress("UNCHECKED_CAST")
    val taskClass = Class.forName(className) as Class<Task>
    parent?.subprojects?.forEach {
        dependsOn(it.tasks.withType(taskClass))
    }
}
