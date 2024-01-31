import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin

plugins {
    id(libs.plugins.dokka.get().pluginId)
    id(libs.plugins.kotlinx.kover.get().pluginId)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

dependencies {
    dokkaHtmlMultiModulePlugin(libs.dokka.versioning)
}

allprojects {
    group = property("GROUP") as String
    val cblVersion = rootProject.libs.versions.couchbase.lite.java.get()
    val kotbaseVersion = property("VERSION") as String
    version = "$cblVersion-$kotbaseVersion"
}

tasks.dokkaHtmlMultiModule {
    val apiDocsDir = projectDir.resolve("docs/api")
    val olderDir = apiDocsDir.resolve("older")
    val tempOlderDir = apiDocsDir.parentFile.resolve("older")

    val shortVersion = """\d+\.\d+""".toRegex()
        .find(version.toString())!!
        .groupValues.first()

    doFirst {
        olderDir.renameTo(tempOlderDir)

        val versionJson = apiDocsDir.resolve("version.json")
        if (versionJson.exists()) {
            val currentDocsVersion = """"version"\s*:\s*"(\d+\.\d+)"""".toRegex()
                .find(versionJson.readText())!!
                .groupValues[1]

            if (currentDocsVersion != shortVersion) {
                val archiveDir = tempOlderDir.resolve(currentDocsVersion)
                apiDocsDir.renameTo(archiveDir)
            }
        }

        apiDocsDir.deleteRecursively()
    }
    outputDirectory.set(apiDocsDir)

    pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
        olderVersionsDir = tempOlderDir
        version = shortVersion
    }

    doLast {
        tempOlderDir.deleteRecursively()
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
}

tasks.register<Delete>(BasePlugin.CLEAN_TASK_NAME) {
    group = BasePlugin.BUILD_GROUP
    delete(rootProject.layout.buildDirectory)
}

// Workaround to avoid potential configuration error.
// Execution failed for task ':lib:podspec'.
// > Error while evaluating property 'gradleWrapperPath$kotlin_gradle_plugin_common' of task ':lib:podspec'.
//    > Failed to calculate the value of task ':lib:podspec' property 'gradleWrapperPath$kotlin_gradle_plugin_common'.
//       > java.util.ConcurrentModificationException (no error message)
// https://kotlinlang.slack.com/archives/C3PQML5NU/p1685525274855969?thread_ts=1685426418.942459&cid=C3PQML5NU
tasks.getByName("wrapper")
