plugins {
    id(libs.plugins.dokka.get().pluginId)
    id(libs.plugins.kotlinx.kover.get().pluginId)
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
}

allprojects {
    group = property("GROUP") as String
    val cblVersion = rootProject.libs.versions.couchbase.lite.java.get()
    val kmpVersion = property("VERSION") as String
    version = "$cblVersion-$kmpVersion"
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
}

tasks.register<Delete>("clean") {
    group = "build"
    delete(rootProject.buildDir)
}

// Workaround to avoid potential configuration error. Should be fixed and can be removed in Kotlin 1.9.20.
// > Task :lib:podspec FAILED
// Execution failed for task ':lib:podspec'.
// > Could not create task ':wrapper'.
//    > java.util.ConcurrentModificationException (no error message)
// https://kotlinlang.slack.com/archives/C3PQML5NU/p1685525274855969?thread_ts=1685426418.942459&cid=C3PQML5NU
tasks.getByName("wrapper")
