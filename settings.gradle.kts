pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "couchbase-lite-kmp"
include(":couchbase-lite")
include(":couchbase-lite-ktx")
include(":couchbase-lite-ee")
include(":couchbase-lite-ee-ktx")
