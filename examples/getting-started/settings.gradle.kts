pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "getting-started"

include(":androidApp")
include(":jvmApp")
include(":nativeApp")
include(":shared")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
