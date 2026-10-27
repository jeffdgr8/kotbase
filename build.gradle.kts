plugins {
    id("dokka-convention")
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

        // work around https://github.com/Kotlin/dokka/issues/3965
        fun removeExtraNewlines(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    removeExtraNewlines(file)
                } else if (file.extension == "html") {
                    val contents = file.readText().dropLast(1)
                    file.writeText(contents)
                }
            }
        }
        removeExtraNewlines(olderDir)
    }
}
