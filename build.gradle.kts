plugins {
    id(libs.plugins.dokka.get().pluginId)
    id(libs.plugins.kotlinx.kover.get().pluginId)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

allprojects {
    group = property("GROUP") as String
    val cblVersion = rootProject.libs.versions.couchbase.lite.java.get()
    val kotbaseVersion = property("VERSION") as String
    version = "$cblVersion-$kotbaseVersion"
}

val apiDocsDir = projectDir.resolve("docs/api")
tasks.register<Delete>("cleanApiDocs") {
    group = "documentation"
    delete(apiDocsDir)
}
tasks.dokkaHtmlMultiModule {
    outputDirectory.set(apiDocsDir)
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

tasks.register<Delete>("clean") {
    group = "build"
    delete(rootProject.layout.buildDirectory)
}
