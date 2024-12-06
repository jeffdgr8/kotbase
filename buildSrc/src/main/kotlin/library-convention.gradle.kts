import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import java.net.URI

plugins {
    id("base-convention")
    org.jetbrains.dokka
    id("com.vanniktech.maven.publish")
}

val libs = the<LibrariesForLibs>()
dependencies {
    dokkaPlugin(libs.dokka.versioning)
}

kotlin {
    explicitApiWarning()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        publishLibraryVariants("release")
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
        unitTestVariant.sourceSetTree.set(KotlinSourceSetTree.unitTest)
    }
}

tasks.withType<AbstractDokkaLeafTask>().configureEach {
    dokkaSourceSets.configureEach {
        includes.from("README.md")

        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl.set(URI("https://github.com/jeffdgr8/kotbase/tree/main/${project.name}/src").toURL())
            remoteLineSuffix.set("#L")
        }

        externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")
        externalDocumentationLink(
            url = "https://kotlinlang.org/api/kotlinx-datetime/",
            packageListUrl = "https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list"
        )
        externalDocumentationLink("https://kotlinlang.org/api/kotlinx-io/")
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

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
            connection.set("https://github.com/jeffdgr8/kotbase.git")
        }

        issueManagement {
            system.set("GitHub")
            url.set("https://github.com/jeffdgr8/kotbase/issues")
        }
    }
}
