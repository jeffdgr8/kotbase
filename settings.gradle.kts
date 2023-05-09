pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

rootProject.name = "couchbase-lite-kmp"
include(":couchbase-lite")
include(":couchbase-lite-ktx")
include(":couchbase-lite-ee")
include(":couchbase-lite-ee-ktx")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
