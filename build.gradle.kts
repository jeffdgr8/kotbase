plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dokka)
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
