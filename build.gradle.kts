import kotlinx.validation.ExperimentalBCVApi

plugins {
    `dokka-convention`
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

dependencies {
    dokka(projects.couchbaseLite)
    dokka(projects.couchbaseLiteEe)
    dokka(projects.couchbaseLiteKtx)
    dokka(projects.couchbaseLiteEeKtx)
    dokka(projects.couchbaseLiteKermit)
    dokka(projects.couchbaseLiteEeKermit)
    dokka(projects.couchbaseLitePaging)
    dokka(projects.couchbaseLiteEePaging)
}

allprojects {
    group = property("GROUP") as String
    val cblVersion = rootProject.libs.versions.couchbase.lite.java.get()
    val kotbaseVersion = property("VERSION") as String
    version = "$cblVersion-$kotbaseVersion"
}

tasks.dokkaGeneratePublicationHtml {
    val apiDocsDir = projectDir.resolve("docs/api")
    val olderDir = apiDocsDir.resolve("older")

    val shortVersion = """\d+\.\d+""".toRegex()
        .find(version.toString())!!
        .groupValues.first()

    dokka.pluginsConfiguration.versioning {
        olderVersionsDir = olderDir
        version = shortVersion
    }

    doFirst {
        val versionJson = apiDocsDir.resolve("version.json")
        if (versionJson.exists()) {
            val currentDocsVersion = """"version"\s*:\s*"(\d+\.\d+)"""".toRegex()
                .find(versionJson.readText())!!
                .groupValues[1]

            if (currentDocsVersion != shortVersion) {
                val tempOlderDir = apiDocsDir.parentFile.resolve("older")
                olderDir.renameTo(tempOlderDir)
                val archiveDir = tempOlderDir.resolve(currentDocsVersion)
                apiDocsDir.renameTo(archiveDir)
                apiDocsDir.mkdir()
                tempOlderDir.renameTo(olderDir)
            }
        }
    }

    doLast {
        apiDocsDir.deleteRecursively()
        outputDirectory.get().asFile.renameTo(apiDocsDir)
    }
}

apiValidation {
    ignoredProjects += listOf("testing-support", "testing-support-ee")
    ignoredClasses += listOf(
        "dev.kotbase.BuildConfig",
        "dev.kotbase.ktx.BuildConfig",
        "dev.kotbase.paging.BuildConfig",
        "dev.kotbase.kermit.BuildConfig"
    )
    @OptIn(ExperimentalBCVApi::class)
    klib.enabled = true
}

// Workaround to avoid potential configuration error.
// Execution failed for task ':lib:podspec'.
// > Error while evaluating property 'gradleWrapperPath$kotlin_gradle_plugin_common' of task ':lib:podspec'.
//    > Failed to calculate the value of task ':lib:podspec' property 'gradleWrapperPath$kotlin_gradle_plugin_common'.
//       > java.util.ConcurrentModificationException (no error message)
// https://kotlinlang.slack.com/archives/C3PQML5NU/p1685525274855969?thread_ts=1685426418.942459&cid=C3PQML5NU
tasks.getByName("wrapper")
