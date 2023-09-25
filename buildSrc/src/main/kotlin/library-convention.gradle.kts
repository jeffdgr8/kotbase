import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetHierarchy.SourceSetTree
import java.net.URL

plugins {
    id("base-convention")
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

tasks.withType<AbstractDokkaLeafTask>().configureEach {
    dokkaSourceSets.configureEach {
        includes.from("README.md")

        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl.set(URL("https://github.com/jeffdgr8/kotbase/tree/main/${project.name}/src"))
            remoteLineSuffix.set("#L")
        }

        externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")
        externalDocumentationLink(
            url = "https://kotlinlang.org/api/kotlinx-datetime/",
            packageListUrl = "https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list"
        )
        // TODO: Update URL when kotlinx-io docs are published
        externalDocumentationLink("https://fzhinkin.github.io/kotlinx-io-dokka-docs-preview/")
    }
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles a Javadoc JAR using Dokka HTML"
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(javadocJar)

        pom {
            name.set(project.name)
            afterEvaluate { this@pom.description.set(this@afterEvaluate.description) }
            url.set("https://kotbase.dev/")

            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }

            developers {
                developer {
                    name.set("Jeff Lockhart")
                    email.set("jeff@kotbase.dev")
                }
            }

            scm {
                url.set("https://github.com/jeffdgr8/kotbase")
                "https://github.com/jeffdgr8/kotbase.git".let {
                    connection.set(it)
                    developerConnection.set(it)
                }
            }

            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/jeffdgr8/kotbase/issues")
            }
        }
    }
}
