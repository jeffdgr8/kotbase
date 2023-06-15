pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://mobile.maven.couchbase.com/maven2/dev/")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
        mavenLocal()
    }
}

rootProject.name = "kotbase"

include(":couchbase-lite", ":couchbase-lite-ee")
include(":couchbase-lite-ktx", ":couchbase-lite-ee-ktx")
include(":couchbase-lite-paging", ":couchbase-lite-ee-paging")
include(":testing-support", ":testing-support-ee")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
