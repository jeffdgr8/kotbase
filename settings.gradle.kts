pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "couchbase-lite-kmm-parent"
include(":lib")
project(":lib").name = "couchbase-lite-kmm"
