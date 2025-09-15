import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import java.time.LocalDate

plugins {
    org.jetbrains.dokka
}

val libs = the<LibrariesForLibs>()
dependencies {
    dokkaPlugin(libs.dokka.versioning)
}

dokka {
    dokkaSourceSets.configureEach {
        skipDeprecated.set(true)

        if (name == "jvmCommonMain") {
            analysisPlatform.set(KotlinPlatform.JVM)
        }

        includes.from("README.md")

        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/jeffdgr8/kotbase/tree/main/${project.name}/src")
            remoteLineSuffix.set("#L")
        }

        externalDocumentationLinks {
            register("kotlinx.coroutines") {
                url("https://kotlinlang.org/api/kotlinx.coroutines/")
            }
            register("kotlinx-datetime") {
                url("https://kotlinlang.org/api/kotlinx-datetime/")
                packageListUrl("https://kotlinlang.org/api/kotlinx-datetime/kotlinx-datetime/package-list")
            }
            register("kotlinx-io") {
                url("https://kotlinlang.org/api/kotlinx-io/")
            }
        }
    }

    pluginsConfiguration.html {
        customAssets.from(rootProject.projectDir.resolve("docs/site/assets/images/logo-icon.svg"))
        footerMessage = "© 2022-${LocalDate.now().year} Jeff Lockhart"
    }
}
