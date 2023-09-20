rootProject.name = "getting-started"

include(":androidApp")
include(":desktopApp")
include(":cliApp")
include(":shared")

if (settings.extra["useLocalLib"]?.toString().toBoolean()) {
    includeBuild("../..")
}

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
