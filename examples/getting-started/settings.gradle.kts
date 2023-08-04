rootProject.name = "getting-started"

include(":androidApp")
include(":desktopApp")
include(":cliApp")
include(":shared")

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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
