plugins {
    id(libs.plugins.dokka.get().pluginId)
    alias(libs.plugins.complete.kotlin)
}

allprojects {
    group = property("GROUP") as String
    val cblVersion = rootProject.libs.versions.couchbase.lite.java.get()
    val kmpVersion = property("VERSION") as String
    version = "$cblVersion-$kmpVersion"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
